package de.ruu.app.pragma.client.dbcommand;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.ruu.app.pragma.bean.*;
import de.ruu.app.pragma.client.AdminClient;
import de.ruu.app.pragma.client.TaskClient;
import de.ruu.app.pragma.client.TaskGroupClient;
import de.ruu.app.pragma.core.*;
import de.ruu.lib.postgres.PostgresToolBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SetupPragmaTestScenarioBaseline
{
  private static final Logger log = LogManager.getLogger(SetupPragmaTestScenarioBaseline.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
  private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
  private static final LocalDate BASELINE_START = LocalDate.of(2026, 1, 1);
  private static final LocalDate BASELINE_END = LocalDate.of(2026, 3, 31);
  private static final double CAPACITY_HOURS_PER_DAY = 5.6d; // 70% of 8h/day

  public static void main(String[] args)
  {
    SetupConfig config = SetupConfig.read();

    AdminClient adminClient = new AdminClient();
    TaskGroupClient groupClient = new TaskGroupClient();
    TaskClient taskClient = new TaskClient();

    adminClient.postConstruct();
    groupClient.postConstruct();
    taskClient .postConstruct();
    try
    {
      new SetupPragmaTestScenarioBaseline().execute(config, adminClient, groupClient, taskClient);
    }
    catch (Exception e)
    {
      log.error("[FAIL] SetupPragmaTestScenarioBaseline failed: {}", e.getMessage(), e);
      System.exit(1);
    }
    finally
    {
      taskClient.preDestroy();
      groupClient.preDestroy();
      adminClient.preDestroy();
    }
  }

  private void execute(SetupConfig config, AdminClient adminClient, TaskGroupClient groupClient, TaskClient taskClient)
      throws Exception
  {
    Instant runStartedAt = Instant.now();
    Path backupDir = prepareBackupDirectory(config.backupDir());
    Path lockFile = acquireLock(backupDir);
    Snapshot snapshot = null;
    boolean dataMutated = false;
    int createdTeams = 0;
    int createdMembers = 0;
    int createdFeatures = 0;
    int createdTasks = 0;
    int overloadCount = 0;

    try
    {
      Preconditions preconditions = checkPreconditions(config, adminClient, groupClient, taskClient, backupDir);
      if (preconditions.hasBusinessData() && !config.force())
        throw new IllegalStateException(
            "Setup aborted: existing business data found and pragma.setup.force=false");

      snapshot = createSnapshot(config, backupDir);
      dataMutated = true;
      clearExistingData(config, adminClient, groupClient, taskClient);

      BaselineResult baseline = createBaseline(config, adminClient, groupClient, taskClient);
      createdTeams = baseline.createdTeams();
      createdMembers = baseline.createdMembers();
      createdFeatures = baseline.createdFeatures();
      createdTasks = baseline.createdTasks();

      VerificationResult verification = verifyAcceptance(
          config, adminClient, groupClient, taskClient, snapshot, baseline.scenarioGroup());
      overloadCount = verification.overloadCount();

      Instant finished = Instant.now();
      log.info("snapshot_id={}", snapshot.snapshotId());
      log.info("duration_seconds={}", java.time.Duration.between(runStartedAt, finished).toSeconds());
      log.info("created_teams={}, created_members={}, created_features={}, created_tasks={}",
          createdTeams, createdMembers, createdFeatures, createdTasks);
      log.info("detected_overloads={}", overloadCount);
      log.info("[OK] SetupPragmaTestScenarioBaseline completed successfully");
    }
    catch (Exception e)
    {
      if (snapshot != null && dataMutated)
      {
        log.error("Setup failed after data mutation. Starting rollback with snapshot_id={} ...", snapshot.snapshotId());
        try
        {
          rollback(config, snapshot);
          log.info("[OK] Rollback completed.");
        }
        catch (Exception rollbackError)
        {
          rollbackError.addSuppressed(e);
          throw new IllegalStateException(
              "Setup failed and rollback also failed: " + rollbackError.getMessage(), rollbackError);
        }
      }
      throw e;
    }
    finally
    {
      releaseLock(lockFile);
    }
  }

  private Preconditions checkPreconditions(
      SetupConfig config,
      AdminClient adminClient,
      TaskGroupClient groupClient,
      TaskClient taskClient,
      Path backupDir)
  {
    // Backend + Keycloak connectivity through authenticated REST calls.
    adminClient.users();
    List<TaskGroupBean> taskGroups = groupClient.findAll();
    List<TaskBean> tasks = taskClient.findAll();
    List<GroupBean> groups = adminClient.groups();
    List<MembershipBean> memberships = adminClient.memberships();
    List<TaskAssignmentBean> assignments = adminClient.taskAssignments();
    List<UserAvailabilityBean> availabilities = adminClient.userAvailabilities();

    if (!Files.isDirectory(backupDir) || !Files.isWritable(backupDir))
      throw new IllegalStateException("Backup directory is not writable: " + backupDir);

    boolean hasBusinessData = !taskGroups.isEmpty()
        || !tasks.isEmpty()
        || !groups.isEmpty()
        || !memberships.isEmpty()
        || !assignments.isEmpty()
        || !availabilities.isEmpty();

    if (config.keycloakAdminPassword().isBlank())
      throw new IllegalStateException("Missing Keycloak admin password.");

    return new Preconditions(hasBusinessData);
  }

  private Snapshot createSnapshot(SetupConfig config, Path backupDir) throws Exception
  {
    String timestamp = LocalDateTime.now().format(FILE_TS);
    String snapshotId = UUID.randomUUID().toString();
    Instant startedAt = Instant.now();

    Path postgresBackup = backupDir.resolve("backup_pragma_postgres_" + timestamp + ".dump");
    Path keycloakBackup = backupDir.resolve("backup_pragma_keycloak_" + timestamp + ".dump");
    Path manifestFile = backupDir.resolve("backup_pragma_manifest_" + timestamp + ".json");

    PostgresToolBox.backup(
        config.postgresBackupExecutable(),
        config.postgresHost(),
        config.postgresPort(),
        config.postgresDatabase(),
        config.postgresUsername(),
        config.postgresPassword(),
        postgresBackup);

    String keycloakExport = exportKeycloakRealm(config);
    Files.writeString(
        keycloakBackup,
        keycloakExport,
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING);

    Instant endedAt = Instant.now();
    writeManifest(
        manifestFile,
        snapshotId,
        startedAt,
        endedAt,
        postgresBackup,
        keycloakBackup);

    return new Snapshot(snapshotId, postgresBackup, keycloakBackup, manifestFile);
  }

  private void clearExistingData(
      SetupConfig config,
      AdminClient adminClient,
      TaskGroupClient groupClient,
      TaskClient taskClient)
  {
    List<TaskBean> tasks = taskClient.findAll();
    for (TaskBean task : tasks)
    {
      List<TaskBean> predecessors = taskClient.findPredecessors(task);
      for (TaskBean predecessor : predecessors)
      {
        taskClient.removePredecessor(task, predecessor);
      }
    }

    for (TaskGroupBean group : groupClient.findAll())
    {
      groupClient.delete(group);
    }

    for (TaskAssignmentBean assignment : adminClient.taskAssignments())
    {
      if (assignment.id() != null) adminClient.deleteTaskAssignment(assignment);
    }

    for (MembershipBean membership : adminClient.memberships())
    {
      if (membership.id() != null) adminClient.deleteMembership(membership);
    }

    for (UserAvailabilityBean availability : adminClient.userAvailabilities())
    {
      if (availability.id() != null) adminClient.deleteUserAvailability(availability);
    }

    for (GroupBean group : adminClient.groups())
    {
      if (group.id() != null) adminClient.deleteGroup(group);
    }

    Set<String> preservedUsers = Set.of(config.authUsername(), config.keycloakAdminUsername());
    for (UserBean user : adminClient.users())
    {
      if (user.id() == null) continue;
      if (preservedUsers.contains(user.username())) continue;
      adminClient.deleteUser(user);
    }

    clearKeycloakRealmUsers(config, preservedUsers);
  }

  private BaselineResult createBaseline(
      SetupConfig config,
      AdminClient adminClient,
      TaskGroupClient groupClient,
      TaskClient taskClient)
  {
    Random random = new Random(config.seed());
    TaskGroupBean scenarioGroup = groupClient.create(new TaskGroupBean(config.scenarioName()));

    List<TeamSpec> teamSpecs = List.of(
        new TeamSpec("Team Analyse", "Requirements Engineering und fachliche Spezifikation"),
        new TeamSpec("Team Architecture and Design", "Technische Architektur und Lösungsdesign"),
        new TeamSpec("Team Development and Deployment", "Implementierung, Integration und Deployment"),
        new TeamSpec("Team Quality Assurance", "Testplanung, Testdurchführung und Qualitätsnachweise"));

    List<GroupBean> createdGroups = new ArrayList<>();
    Map<String, List<UserBean>> usersByTeam = new LinkedHashMap<>();
    int totalMembers = 0;
    for (TeamSpec team : teamSpecs)
    {
      GroupBean createdGroup = adminClient.createGroup(new GroupBean(team.name()).description(team.description()));
      createdGroups.add(createdGroup);

      List<UserBean> teamUsers = createTeamUsers(config, adminClient, random, team, createdGroup);
      usersByTeam.put(team.name(), teamUsers);
      totalMembers += teamUsers.size();
    }

    int createdFeatures = 0;
    int createdTasks = 0;
    List<TaskBean> rootFeatures = new ArrayList<>();
    List<TaskBean> allCreatedTasks = new ArrayList<>();
    int teamIndex = 0;
    for (TeamSpec team : teamSpecs)
    {
      LocalDate sprintStart = BASELINE_START.plusDays(teamIndex * 14L);
      TaskBean feature = taskClient.create(
          new TaskBean(scenarioGroup, "Feature " + (teamIndex + 1) + " - " + team.name())
              .description(config.scenarioDescription() + " / Sprint " + (teamIndex + 1))
              .scheduledStart(sprintStart)
              .scheduledFinish(sprintStart.plusDays(26))
              .workEstimateInitial(40d + teamIndex * 6d)
              .workEstimateCurrent(44d + teamIndex * 6d)
              .workActual(46d + teamIndex * 6d)
              .status(TaskStatus.IN_PROGRESS)
              .priority(TaskPriority.HIGH));
      rootFeatures.add(feature);
      allCreatedTasks.add(feature);
      createdFeatures++;
      createdTasks++;

      List<UserBean> members = usersByTeam.get(team.name());
      TaskBean previous = null;
      for (int i = 0; i < members.size(); i++)
      {
        LocalDate start = sprintStart.plusDays(i * 4L);
        LocalDate finish = start.plusDays(8);
        TaskBean task = taskClient.create(
            new TaskBean(scenarioGroup, team.name() + " Task " + (i + 1))
                .parentTask(feature)
                .scheduledStart(start)
                .scheduledFinish(finish)
                .workEstimateInitial(22d + i * 2d)
                .workEstimateCurrent(26d + i * 2d)
                .workActual(i < 2 ? 34d + i * 2d : 24d + i * 2d)
                .status(TaskStatus.IN_PROGRESS)
                .priority(i == 0 ? TaskPriority.IMMEDIATE : TaskPriority.NORMAL));
        if (previous != null)
          taskClient.addPredecessor(task, previous);
        previous = task;

        UserBean member = members.get(i);
        TaskAssignmentBean assignment = new TaskAssignmentBean(task.id())
            .assignmentType(AssignmentType.RESPONSIBLE)
            .targetType(AssignmentTargetType.USER)
            .userId(member.id())
            .share(1.0)
            .priority(1)
            .active(true)
            .validFrom(start)
            .validTo(finish);
        adminClient.createTaskAssignment(assignment);
        allCreatedTasks.add(task);
        createdTasks++;
      }
      teamIndex++;
    }

    if (rootFeatures.size() > 1)
    {
      for (int i = 1; i < rootFeatures.size(); i++)
      {
        taskClient.addPredecessor(rootFeatures.get(i), rootFeatures.get(i - 1));
      }
    }

    // Two explicit overload tasks.
    UserBean overloadA = usersByTeam.get(teamSpecs.get(2).name()).get(0);
    UserBean overloadB = usersByTeam.get(teamSpecs.get(3).name()).get(0);
    TaskBean overloadTaskA = taskClient.create(
        new TaskBean(scenarioGroup, "Critical overlap A")
            .scheduledStart(LocalDate.of(2026, 2, 1))
            .scheduledFinish(LocalDate.of(2026, 2, 20))
            .workEstimateInitial(80d)
            .workEstimateCurrent(88d)
            .workActual(92d)
            .status(TaskStatus.IN_PROGRESS)
            .priority(TaskPriority.IMMEDIATE));
    TaskBean overloadTaskB = taskClient.create(
        new TaskBean(scenarioGroup, "Critical overlap B")
            .scheduledStart(LocalDate.of(2026, 2, 10))
            .scheduledFinish(LocalDate.of(2026, 3, 5))
            .workEstimateInitial(76d)
            .workEstimateCurrent(84d)
            .workActual(86d)
            .status(TaskStatus.IN_PROGRESS)
            .priority(TaskPriority.IMMEDIATE));
    adminClient.createTaskAssignment(new TaskAssignmentBean(overloadTaskA.id())
        .assignmentType(AssignmentType.RESPONSIBLE)
        .targetType(AssignmentTargetType.USER)
        .userId(overloadA.id())
        .share(1.0)
        .priority(1)
        .active(true)
        .validFrom(LocalDate.of(2026, 2, 1))
        .validTo(LocalDate.of(2026, 2, 20)));
    adminClient.createTaskAssignment(new TaskAssignmentBean(overloadTaskB.id())
        .assignmentType(AssignmentType.RESPONSIBLE)
        .targetType(AssignmentTargetType.USER)
        .userId(overloadB.id())
        .share(1.0)
        .priority(1)
        .active(true)
        .validFrom(LocalDate.of(2026, 2, 10))
        .validTo(LocalDate.of(2026, 3, 5)));
    allCreatedTasks.add(overloadTaskA);
    allCreatedTasks.add(overloadTaskB);
    createdTasks += 2;

    return new BaselineResult(scenarioGroup, createdGroups.size(), totalMembers, createdFeatures, createdTasks);
  }

  private List<UserBean> createTeamUsers(
      SetupConfig config,
      AdminClient adminClient,
      Random random,
      TeamSpec team,
      GroupBean group)
  {
    String[] firstNames = {"Alex", "Kim", "Robin", "Sam", "Jamie", "Dana", "Taylor", "Casey"};
    String teamSlug = slug(team.name());
    List<UserBean> teamUsers = new ArrayList<>();
    for (int i = 0; i < 3; i++)
    {
      String first = firstNames[random.nextInt(firstNames.length)];
      String displayName = first + " " + teamSlug.toUpperCase(Locale.ROOT) + " " + (i + 1);
      String username = "baseline-" + teamSlug + "-" + (i + 1);
      String email = username + "@example.com";

      UserBean existing = adminClient.users().stream()
          .filter(u -> u.username().equals(username))
          .findFirst()
          .orElse(null);
      UserBean user;
      if (existing == null)
      {
        user = adminClient.createUser(
            new UserBean(username, displayName, email)
                .password(config.defaultUserPassword())
                .active(true));
      }
      else
      {
        existing.displayName(displayName).email(email).password(config.defaultUserPassword()).active(true);
        user = adminClient.updateUser(existing);
      }
      teamUsers.add(user);

      MembershipRole role = i == 0 ? MembershipRole.OWNER : (i == 1 ? MembershipRole.COORDINATOR : MembershipRole.MEMBER);
      adminClient.createMembership(
          new MembershipBean(user.id(), group.id())
              .roleInGroup(role)
              .validFrom(BASELINE_START)
              .validTo(BASELINE_END)
              .active(true));

      adminClient.createUserAvailability(
          new UserAvailabilityBean(user.id(), BASELINE_START, BASELINE_END, CAPACITY_HOURS_PER_DAY)
              .availabilityType(de.ruu.app.pragma.core.AvailabilityType.AVAILABLE)
              .note("scrum 2-week sprint cadence")
      );
    }
    return teamUsers;
  }

  private VerificationResult verifyAcceptance(
      SetupConfig config,
      AdminClient adminClient,
      TaskGroupClient groupClient,
      TaskClient taskClient,
      Snapshot snapshot,
      TaskGroupBean scenarioGroup)
  {
    assertExists(snapshot.postgresBackup(), "Postgres backup missing");
    assertExists(snapshot.keycloakBackup(), "Keycloak backup missing");
    assertExists(snapshot.manifestFile(), "Manifest missing");
    assertManifestHasSnapshotId(snapshot.manifestFile(), snapshot.snapshotId());

    long scenarioCount = groupClient.findAll().stream()
        .filter(g -> g.name().equals(config.scenarioName()))
        .count();
    if (scenarioCount != 1L)
      throw new IllegalStateException("Scenario task group must exist exactly once, found: " + scenarioCount);

    Map<String, GroupBean> groupsByName = new HashMap<>();
    for (GroupBean group : adminClient.groups())
      groupsByName.put(group.name(), group);

    List<String> requiredTeams = List.of(
        "Team Analyse",
        "Team Architecture and Design",
        "Team Development and Deployment",
        "Team Quality Assurance");
    for (String requiredTeam : requiredTeams)
    {
      GroupBean group = groupsByName.get(requiredTeam);
      if (group == null || group.id() == null)
        throw new IllegalStateException("Missing team: " + requiredTeam);
      long members = adminClient.memberships().stream()
          .filter(m -> m.groupId().equals(group.id()) && m.active())
          .count();
      if (members != 3L)
        throw new IllegalStateException("Team " + requiredTeam + " must have exactly 3 members, found: " + members);
    }

    List<TaskBean> tasks = taskClient.findAll(scenarioGroup);
    if (tasks.isEmpty())
      throw new IllegalStateException("Scenario contains no tasks.");
    for (TaskBean task : tasks)
    {
      LocalDate start = task.scheduledStart().orElseThrow(() ->
          new IllegalStateException("Task without scheduledStart: " + task.name()));
      LocalDate finish = task.scheduledFinish().orElseThrow(() ->
          new IllegalStateException("Task without scheduledFinish: " + task.name()));
      if (start.isBefore(BASELINE_START) || finish.isAfter(BASELINE_END))
        throw new IllegalStateException("Task outside baseline period: " + task.name());
    }

    int overloads = (int) adminClient.workload().stream()
        .filter(item -> item.assignedHours() > item.capacityHoursPerDay())
        .count();
    if (overloads < 2)
      throw new IllegalStateException("Expected at least 2 overload situations, found: " + overloads);

    Map<String, Map<String, Object>> keycloakUsersById = keycloakUsersById(config);
    for (UserBean user : adminClient.users())
    {
      String keycloakId = user.keycloakUserId()
          .orElseThrow(() -> new IllegalStateException("User without keycloakUserId: " + user.username()));
      if (!keycloakUsersById.containsKey(keycloakId))
        throw new IllegalStateException("Dangling keycloak reference for user: " + user.username());
    }

    return new VerificationResult(overloads);
  }

  private void rollback(SetupConfig config, Snapshot snapshot) throws Exception
  {
    PostgresToolBox.restore(
        config.postgresRestoreExecutable(),
        config.postgresHost(),
        config.postgresPort(),
        config.postgresDatabase(),
        config.postgresUsername(),
        config.postgresPassword(),
        snapshot.postgresBackup());
    restoreKeycloakRealm(config, snapshot.keycloakBackup());
  }

  private String exportKeycloakRealm(SetupConfig config) throws Exception
  {
    String token = keycloakAdminToken(config);
    String url = config.keycloakServerUrl() + "/admin/realms/" + config.keycloakRealm()
        + "/partial-export?exportClients=true&exportGroupsAndRoles=true";
    Map<String, Object> body = Map.of("users", "same_file");
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .header("Authorization", "Bearer " + token)
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(OBJECT_MAPPER.writeValueAsString(body)))
        .build();
    HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() / 100 != 2)
      throw new IllegalStateException("Keycloak export failed: HTTP " + response.statusCode() + " - " + response.body());
    return response.body();
  }

  private void restoreKeycloakRealm(SetupConfig config, Path keycloakBackup) throws Exception
  {
    String token = keycloakAdminToken(config);
    HttpClient httpClient = HttpClient.newHttpClient();

    HttpRequest deleteRealm = HttpRequest.newBuilder()
        .uri(URI.create(config.keycloakServerUrl() + "/admin/realms/" + config.keycloakRealm()))
        .header("Authorization", "Bearer " + token)
        .DELETE()
        .build();
    HttpResponse<String> deleteResponse = httpClient.send(deleteRealm, HttpResponse.BodyHandlers.ofString());
    if (deleteResponse.statusCode() != 204 && deleteResponse.statusCode() != 404)
      throw new IllegalStateException("Keycloak realm delete failed: HTTP " + deleteResponse.statusCode());

    String realmJson = Files.readString(keycloakBackup, StandardCharsets.UTF_8);
    HttpRequest recreateRealm = HttpRequest.newBuilder()
        .uri(URI.create(config.keycloakServerUrl() + "/admin/realms"))
        .header("Authorization", "Bearer " + token)
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(realmJson))
        .build();
    HttpResponse<String> createResponse = httpClient.send(recreateRealm, HttpResponse.BodyHandlers.ofString());
    if (createResponse.statusCode() != 201)
      throw new IllegalStateException("Keycloak realm restore failed: HTTP " + createResponse.statusCode() + " - " + createResponse.body());
  }

  private void clearKeycloakRealmUsers(SetupConfig config, Set<String> preservedUsers)
  {
    try
    {
      String token = keycloakAdminToken(config);
      HttpClient httpClient = HttpClient.newHttpClient();
      HttpRequest listRequest = HttpRequest.newBuilder()
          .uri(URI.create(config.keycloakServerUrl() + "/admin/realms/" + config.keycloakRealm()
              + "/users?briefRepresentation=false&max=1000"))
          .header("Authorization", "Bearer " + token)
          .GET()
          .build();
      HttpResponse<String> listResponse = httpClient.send(listRequest, HttpResponse.BodyHandlers.ofString());
      if (listResponse.statusCode() / 100 != 2)
        throw new IllegalStateException("Failed to read Keycloak users: HTTP " + listResponse.statusCode());

      @SuppressWarnings("unchecked")
      List<Map<String, Object>> users = OBJECT_MAPPER.readValue(listResponse.body(), List.class);
      for (Map<String, Object> user : users)
      {
        String username = String.valueOf(user.getOrDefault("username", ""));
        if (username.isBlank()) continue;
        if (preservedUsers.contains(username)) continue;
        if (username.startsWith("service-account-")) continue;
        String id = String.valueOf(user.getOrDefault("id", ""));
        if (id.isBlank()) continue;
        HttpRequest deleteRequest = HttpRequest.newBuilder()
            .uri(URI.create(config.keycloakServerUrl() + "/admin/realms/" + config.keycloakRealm() + "/users/" + id))
            .header("Authorization", "Bearer " + token)
            .DELETE()
            .build();
        HttpResponse<String> deleteResponse = httpClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        if (deleteResponse.statusCode() != 204 && deleteResponse.statusCode() != 404)
          throw new IllegalStateException("Failed to delete Keycloak user " + username + ": HTTP " + deleteResponse.statusCode());
      }
    }
    catch (Exception e)
    {
      throw new IllegalStateException("Failed to clear Keycloak realm users", e);
    }
  }

  private Map<String, Map<String, Object>> keycloakUsersById(SetupConfig config)
  {
    try
    {
      String token = keycloakAdminToken(config);
      HttpRequest listRequest = HttpRequest.newBuilder()
          .uri(URI.create(config.keycloakServerUrl() + "/admin/realms/" + config.keycloakRealm()
              + "/users?briefRepresentation=false&max=1000"))
          .header("Authorization", "Bearer " + token)
          .GET()
          .build();
      HttpResponse<String> response = HttpClient.newHttpClient().send(listRequest, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() / 100 != 2)
        throw new IllegalStateException("Failed to list Keycloak users: HTTP " + response.statusCode());
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> users = OBJECT_MAPPER.readValue(response.body(), List.class);
      Map<String, Map<String, Object>> byId = new HashMap<>();
      for (Map<String, Object> user : users)
      {
        String id = String.valueOf(user.getOrDefault("id", ""));
        if (!id.isBlank()) byId.put(id, user);
      }
      return byId;
    }
    catch (Exception e)
    {
      throw new IllegalStateException("Failed to verify Keycloak user references", e);
    }
  }

  private String keycloakAdminToken(SetupConfig config) throws Exception
  {
    String form = "grant_type=password"
        + "&client_id=" + urlEncode(config.keycloakAdminClientId())
        + "&username=" + urlEncode(config.keycloakAdminUsername())
        + "&password=" + urlEncode(config.keycloakAdminPassword());
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(config.keycloakServerUrl() + "/realms/master/protocol/openid-connect/token"))
        .header("Content-Type", "application/x-www-form-urlencoded")
        .POST(HttpRequest.BodyPublishers.ofString(form))
        .build();
    HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() / 100 != 2)
      throw new IllegalStateException("Keycloak token request failed: HTTP " + response.statusCode() + " - " + response.body());
    @SuppressWarnings("unchecked")
    Map<String, Object> payload = OBJECT_MAPPER.readValue(response.body(), Map.class);
    Object token = payload.get("access_token");
    if (!(token instanceof String tokenValue) || tokenValue.isBlank())
      throw new IllegalStateException("Keycloak token response missing access_token");
    return tokenValue;
  }

  private void writeManifest(
      Path manifestFile,
      String snapshotId,
      Instant startedAt,
      Instant endedAt,
      Path postgresBackup,
      Path keycloakBackup) throws Exception
  {
    Map<String, Object> manifest = new LinkedHashMap<>();
    manifest.put("snapshot_id", snapshotId);
    manifest.put("started_at", startedAt.toString());
    manifest.put("ended_at", endedAt.toString());
    manifest.put("postgres_backup_file", postgresBackup.getFileName().toString());
    manifest.put("keycloak_backup_file", keycloakBackup.getFileName().toString());
    manifest.put("postgres_sha256", sha256(postgresBackup));
    manifest.put("keycloak_sha256", sha256(keycloakBackup));
    Files.writeString(
        manifestFile,
        OBJECT_MAPPER.writeValueAsString(manifest),
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING);
  }

  private static String sha256(Path file) throws Exception
  {
    byte[] bytes = Files.readAllBytes(file);
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    return Base64.getEncoder().encodeToString(digest.digest(bytes));
  }

  private static Path prepareBackupDirectory(String configuredPath) throws IOException
  {
    Path backupDir = Path.of(configuredPath);
    if (!backupDir.isAbsolute()) backupDir = Path.of(System.getProperty("user.dir")).resolve(backupDir).normalize();
    Files.createDirectories(backupDir);
    return backupDir;
  }

  private static Path acquireLock(Path backupDir) throws IOException
  {
    Path lockFile = backupDir.resolve(".setup-baseline.lock");
    try
    {
      Files.writeString(lockFile, Long.toString(System.currentTimeMillis()), StandardOpenOption.CREATE_NEW);
      return lockFile;
    }
    catch (IOException e)
    {
      throw new IllegalStateException("Another setup process seems active (lock exists): " + lockFile, e);
    }
  }

  private static void releaseLock(Path lockFile)
  {
    try
    {
      Files.deleteIfExists(lockFile);
    }
    catch (IOException e)
    {
      log.warn("Could not remove lock file {}", lockFile, e);
    }
  }

  private static void assertExists(Path file, String message)
  {
    if (!Files.exists(file)) throw new IllegalStateException(message + ": " + file);
  }

  private static void assertManifestHasSnapshotId(Path manifestFile, String snapshotId)
  {
    try
    {
      @SuppressWarnings("unchecked")
      Map<String, Object> manifest = OBJECT_MAPPER.readValue(Files.readString(manifestFile), Map.class);
      String fromManifest = String.valueOf(manifest.get("snapshot_id"));
      if (!snapshotId.equals(fromManifest))
        throw new IllegalStateException("Manifest snapshot_id mismatch.");
    }
    catch (Exception e)
    {
      throw new IllegalStateException("Invalid manifest file: " + manifestFile, e);
    }
  }

  private static String slug(String value)
  {
    return value.toLowerCase(Locale.ROOT)
        .replace("team ", "")
        .replace(" and ", "-")
        .replaceAll("[^a-z0-9]+", "-")
        .replaceAll("(^-|-$)", "");
  }

  private static String urlEncode(String value)
  {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  private record TeamSpec(String name, String description) { }
  private record Snapshot(String snapshotId, Path postgresBackup, Path keycloakBackup, Path manifestFile) { }
  private record Preconditions(boolean hasBusinessData) { }
  private record BaselineResult(TaskGroupBean scenarioGroup, int createdTeams, int createdMembers, int createdFeatures, int createdTasks) { }
  private record VerificationResult(int overloadCount) { }

  private record SetupConfig(
      String backupDir,
      String scenarioName,
      String scenarioDescription,
      long seed,
      boolean force,
      Path postgresBackupExecutable,
      Path postgresRestoreExecutable,
      String postgresHost,
      int postgresPort,
      String postgresDatabase,
      String postgresUsername,
      String postgresPassword,
      String keycloakServerUrl,
      String keycloakRealm,
      String keycloakAdminUsername,
      String keycloakAdminPassword,
      String keycloakAdminClientId,
      String authUsername,
      String defaultUserPassword)
  {
    private static SetupConfig read()
    {
      Config config = ConfigProvider.getConfig();
      String backupDir = value(config, "pragma.backup.dir").orElse("app/pragma/dbbackup");
      String scenarioName = value(config, "pragma.scenario.name").orElse("pragma test scenario baseline complete");
      String scenarioDescription = value(config, "pragma.scenario.description").orElse("test scenario for pragma");
      long seed = value(config, "pragma.scenario.seed").map(Long::parseLong).orElse(20260805L);
      boolean force = value(config, "pragma.setup.force").map(Boolean::parseBoolean).orElse(false);

      Path pgDump = Path.of(value(config, "pragma.postgres.backup.executable").orElse("/usr/bin/pg_dump"));
      Path pgRestore = Path.of(value(config, "pragma.postgres.restore.executable").orElse("/usr/bin/pg_restore"));
      String pgHost = value(config, "pragma.postgres.host").orElse("localhost");
      int pgPort = value(config, "pragma.postgres.port").map(Integer::parseInt).orElse(5432);
      String pgDatabase = value(config, "pragma.postgres.database").orElse("pragma");
      String pgUsername = value(config, "pragma.postgres.username").orElse("admin-postgres");
      String pgPassword = value(config, "pragma.postgres.password").orElse("r-uu");

      String keycloakServerUrl = value(config, "pragma.keycloak.admin.server-url")
          .or(() -> value(config, "pragma.keycloak.server-url"))
          .orElse("http://localhost:8080");
      String keycloakRealm = value(config, "pragma.keycloak.admin.realm")
          .or(() -> value(config, "pragma.keycloak.realm"))
          .orElse("pragma-realm");
      String keycloakAdminUsername = value(config, "pragma.keycloak.admin.username").orElse("admin");
      String keycloakAdminPassword = value(config, "pragma.keycloak.admin.password").orElse("admin");
      String keycloakAdminClientId = value(config, "pragma.keycloak.admin.client-id").orElse("admin-cli");

      String authUsername = value(config, "pragma.keycloak.username").orElse("r-uu");
      String defaultUserPassword = value(config, "pragma.scenario.default-user-password").orElse("r-uu");

      return new SetupConfig(
          backupDir,
          scenarioName,
          scenarioDescription,
          seed,
          force,
          pgDump,
          pgRestore,
          pgHost,
          pgPort,
          pgDatabase,
          pgUsername,
          pgPassword,
          keycloakServerUrl,
          keycloakRealm,
          keycloakAdminUsername,
          keycloakAdminPassword,
          keycloakAdminClientId,
          authUsername,
          defaultUserPassword);
    }

    private static Optional<String> value(Config config, String key)
    {
      return config.getOptionalValue(key, String.class)
          .map(String::trim)
          .filter(it -> !it.isBlank());
    }
  }
}
