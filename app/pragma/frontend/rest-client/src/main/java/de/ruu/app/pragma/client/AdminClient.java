package de.ruu.app.pragma.client;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import de.ruu.app.pragma.bean.AuthAccountBean;
import de.ruu.app.pragma.bean.ChangeLogBean;
import de.ruu.app.pragma.bean.GroupBean;
import de.ruu.app.pragma.bean.Mappings;
import de.ruu.app.pragma.bean.MembershipBean;
import de.ruu.app.pragma.bean.TaskAssignmentBean;
import de.ruu.app.pragma.bean.TaskOverrunBean;
import de.ruu.app.pragma.bean.UserAvailabilityBean;
import de.ruu.app.pragma.bean.UserBean;
import de.ruu.app.pragma.bean.UserWorkloadBean;
import de.ruu.app.pragma.dto.AuthAccountDto;
import de.ruu.app.pragma.dto.ChangeLogDto;
import de.ruu.app.pragma.dto.GroupDto;
import de.ruu.app.pragma.dto.MembershipDto;
import de.ruu.app.pragma.dto.TaskAssignmentDto;
import de.ruu.app.pragma.dto.TaskOverrunDto;
import de.ruu.app.pragma.dto.UserAvailabilityDto;
import de.ruu.app.pragma.dto.UserDto;
import de.ruu.app.pragma.dto.UserWorkloadDto;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.ConfigProvider;
import org.glassfish.jersey.client.ClientProperties;

import java.util.List;

@Singleton
public class AdminClient
{
    private final String scheme = ConfigProvider.getConfig().getValue("pragma.rest-api.scheme", String.class);
    private final String host = ConfigProvider.getConfig().getValue("pragma.rest-api.host", String.class);
    private final int port = ConfigProvider.getConfig().getValue("pragma.rest-api.port", Integer.class);
    private final RestClientAuthConfig authConfig = RestClientAuthConfig.read();

    private Client client;

    @PostConstruct
    public void postConstruct()
    {
        ObjectMapper mapper = createObjectMapper();
        KeycloakTokenProvider tokenProvider = new KeycloakTokenProvider(
            authConfig.serverUrl(),
            authConfig.realm(),
            authConfig.clientId(),
            authConfig.username(),
            authConfig.password());
        client = ClientBuilder.newBuilder()
            .register(new JacksonJsonProvider(mapper))
            .register(new BearerTokenFilter(tokenProvider))
            .property(ClientProperties.CONNECT_TIMEOUT, 5000)
            .property(ClientProperties.READ_TIMEOUT, 30000)
            .build();
    }

    @PreDestroy
    public void preDestroy()
    {
        if (client != null) client.close();
    }

    public List<UserBean> users()
    {
        try (Response response = target("/admin/users")
            .queryParam("syncFromKeycloak", true)
            .queryParam("syncBestEffort", true)
            .request(MediaType.APPLICATION_JSON)
            .get()) {
            requireSuccess(response);
            return response.readEntity(new GenericType<List<UserDto>>() {}).stream().map(Mappings::toBean).toList();
        } catch (ProcessingException e) { throw new RuntimeException("communication error", e); }
    }

    public UserBean createUser(UserBean bean)
    {
        try (Response response = target("/admin/users").request(MediaType.APPLICATION_JSON).post(Entity.json(Mappings.toDto(bean)))) {
            requireSuccess(response);
            return Mappings.toBean(response.readEntity(UserDto.class));
        } catch (ProcessingException e) { throw new RuntimeException("communication error", e); }
    }

    public UserBean updateUser(UserBean bean)
    {
        try (Response response = target("/admin/users/" + id(bean.id(), "User")).request(MediaType.APPLICATION_JSON).put(Entity.json(Mappings.toDto(bean)))) {
            requireSuccess(response);
            return Mappings.toBean(response.readEntity(UserDto.class));
        } catch (ProcessingException e) { throw new RuntimeException("communication error", e); }
    }

    public void deleteUser(UserBean bean)
    {
        try (Response response = target("/admin/users/" + id(bean.id(), "User")).request().delete()) {
            requireSuccess(response);
        } catch (ProcessingException e) { throw new RuntimeException("communication error", e); }
    }

    public List<GroupBean> groups()
    {
        try (Response response = target("/admin/groups").request(MediaType.APPLICATION_JSON).get()) {
            requireSuccess(response);
            return response.readEntity(new GenericType<List<GroupDto>>() {}).stream().map(Mappings::toBean).toList();
        } catch (ProcessingException e) { throw new RuntimeException("communication error", e); }
    }

    public GroupBean createGroup(GroupBean bean)
    {
        try (Response response = target("/admin/groups").request(MediaType.APPLICATION_JSON).post(Entity.json(Mappings.toDto(bean)))) {
            requireSuccess(response);
            return Mappings.toBean(response.readEntity(GroupDto.class));
        } catch (ProcessingException e) { throw new RuntimeException("communication error", e); }
    }

    public GroupBean updateGroup(GroupBean bean)
    {
        try (Response response = target("/admin/groups/" + id(bean.id(), "Group")).request(MediaType.APPLICATION_JSON).put(Entity.json(Mappings.toDto(bean)))) {
            requireSuccess(response);
            return Mappings.toBean(response.readEntity(GroupDto.class));
        } catch (ProcessingException e) { throw new RuntimeException("communication error", e); }
    }

    public void deleteGroup(GroupBean bean)
    {
        try (Response response = target("/admin/groups/" + id(bean.id(), "Group")).request().delete()) {
            requireSuccess(response);
        } catch (ProcessingException e) { throw new RuntimeException("communication error", e); }
    }

    public List<MembershipBean> memberships()
    {
        try (Response response = target("/admin/memberships").request(MediaType.APPLICATION_JSON).get()) {
            requireSuccess(response);
            return response.readEntity(new GenericType<List<MembershipDto>>() {}).stream().map(Mappings::toBean).toList();
        } catch (ProcessingException e) { throw new RuntimeException("communication error", e); }
    }

    public MembershipBean createMembership(MembershipBean bean)
    {
        try (Response response = target("/admin/memberships").request(MediaType.APPLICATION_JSON).post(Entity.json(Mappings.toDto(bean)))) {
            requireSuccess(response);
            return Mappings.toBean(response.readEntity(MembershipDto.class));
        } catch (ProcessingException e) { throw new RuntimeException("communication error", e); }
    }

    public MembershipBean updateMembership(MembershipBean bean)
    {
        try (Response response = target("/admin/memberships/" + id(bean.id(), "Membership")).request(MediaType.APPLICATION_JSON).put(Entity.json(Mappings.toDto(bean)))) {
            requireSuccess(response);
            return Mappings.toBean(response.readEntity(MembershipDto.class));
        } catch (ProcessingException e) { throw new RuntimeException("communication error", e); }
    }

    public void deleteMembership(MembershipBean bean)
    {
        try (Response response = target("/admin/memberships/" + id(bean.id(), "Membership")).request().delete()) {
            requireSuccess(response);
        } catch (ProcessingException e) { throw new RuntimeException("communication error", e); }
    }

    public List<TaskAssignmentBean> taskAssignments()
    {
        try (Response response = target("/admin/task-assignments").request(MediaType.APPLICATION_JSON).get()) {
            requireSuccess(response);
            return response.readEntity(new GenericType<List<TaskAssignmentDto>>() {}).stream().map(Mappings::toBean).toList();
        } catch (ProcessingException e) { throw new RuntimeException("communication error", e); }
    }

    public TaskAssignmentBean createTaskAssignment(TaskAssignmentBean bean)
    {
        try (Response response = target("/admin/task-assignments").request(MediaType.APPLICATION_JSON).post(Entity.json(Mappings.toDto(bean)))) {
            requireSuccess(response);
            return Mappings.toBean(response.readEntity(TaskAssignmentDto.class));
        } catch (ProcessingException e) { throw new RuntimeException("communication error", e); }
    }

    public TaskAssignmentBean updateTaskAssignment(TaskAssignmentBean bean)
    {
        try (Response response = target("/admin/task-assignments/" + id(bean.id(), "TaskAssignment")).request(MediaType.APPLICATION_JSON).put(Entity.json(Mappings.toDto(bean)))) {
            requireSuccess(response);
            return Mappings.toBean(response.readEntity(TaskAssignmentDto.class));
        } catch (ProcessingException e) { throw new RuntimeException("communication error", e); }
    }

    public void deleteTaskAssignment(TaskAssignmentBean bean)
    {
        try (Response response = target("/admin/task-assignments/" + id(bean.id(), "TaskAssignment")).request().delete()) {
            requireSuccess(response);
        } catch (ProcessingException e) { throw new RuntimeException("communication error", e); }
    }

    public List<UserAvailabilityBean> userAvailabilities()
    {
        try (Response response = target("/admin/user-availabilities").request(MediaType.APPLICATION_JSON).get()) {
            requireSuccess(response);
            return response.readEntity(new GenericType<List<UserAvailabilityDto>>() {}).stream().map(Mappings::toBean).toList();
        } catch (ProcessingException e) { throw new RuntimeException("communication error", e); }
    }

    public UserAvailabilityBean createUserAvailability(UserAvailabilityBean bean)
    {
        try (Response response = target("/admin/user-availabilities").request(MediaType.APPLICATION_JSON).post(Entity.json(Mappings.toDto(bean)))) {
            requireSuccess(response);
            return Mappings.toBean(response.readEntity(UserAvailabilityDto.class));
        } catch (ProcessingException e) { throw new RuntimeException("communication error", e); }
    }

    public UserAvailabilityBean updateUserAvailability(UserAvailabilityBean bean)
    {
        try (Response response = target("/admin/user-availabilities/" + id(bean.id(), "UserAvailability")).request(MediaType.APPLICATION_JSON).put(Entity.json(Mappings.toDto(bean)))) {
            requireSuccess(response);
            return Mappings.toBean(response.readEntity(UserAvailabilityDto.class));
        } catch (ProcessingException e) { throw new RuntimeException("communication error", e); }
    }

    public void deleteUserAvailability(UserAvailabilityBean bean)
    {
        try (Response response = target("/admin/user-availabilities/" + id(bean.id(), "UserAvailability")).request().delete()) {
            requireSuccess(response);
        } catch (ProcessingException e) { throw new RuntimeException("communication error", e); }
    }

    public List<AuthAccountBean> authAccounts()
    {
        try (Response response = target("/admin/auth-accounts").request(MediaType.APPLICATION_JSON).get()) {
            requireSuccess(response);
            return response.readEntity(new GenericType<List<AuthAccountDto>>() {}).stream().map(Mappings::toBean).toList();
        } catch (ProcessingException e) { throw new RuntimeException("communication error", e); }
    }

    public AuthAccountBean createAuthAccount(AuthAccountBean bean)
    {
        try (Response response = target("/admin/auth-accounts").request(MediaType.APPLICATION_JSON).post(Entity.json(Mappings.toDto(bean)))) {
            requireSuccess(response);
            return Mappings.toBean(response.readEntity(AuthAccountDto.class));
        } catch (ProcessingException e) { throw new RuntimeException("communication error", e); }
    }

    public AuthAccountBean updateAuthAccount(AuthAccountBean bean)
    {
        try (Response response = target("/admin/auth-accounts/" + id(bean.id(), "AuthAccount")).request(MediaType.APPLICATION_JSON).put(Entity.json(Mappings.toDto(bean)))) {
            requireSuccess(response);
            return Mappings.toBean(response.readEntity(AuthAccountDto.class));
        } catch (ProcessingException e) { throw new RuntimeException("communication error", e); }
    }

    public List<ChangeLogBean> changeLog()
    {
        try (Response response = target("/admin/change-log").request(MediaType.APPLICATION_JSON).get()) {
            requireSuccess(response);
            return response.readEntity(new GenericType<List<ChangeLogDto>>() {}).stream().map(Mappings::toBean).toList();
        } catch (ProcessingException e) { throw new RuntimeException("communication error", e); }
    }

    public List<UserWorkloadBean> workload()
    {
        try (Response response = target("/admin/analytics/workload").request(MediaType.APPLICATION_JSON).get()) {
            requireSuccess(response);
            return response.readEntity(new GenericType<List<UserWorkloadDto>>() {}).stream().map(Mappings::toBean).toList();
        } catch (ProcessingException e) { throw new RuntimeException("communication error", e); }
    }

    public List<TaskOverrunBean> timeOverruns()
    {
        try (Response response = target("/admin/analytics/time-overruns").request(MediaType.APPLICATION_JSON).get()) {
            requireSuccess(response);
            return response.readEntity(new GenericType<List<TaskOverrunDto>>() {}).stream().map(Mappings::toBean).toList();
        } catch (ProcessingException e) { throw new RuntimeException("communication error", e); }
    }

    private long id(Long id, String type)
    {
        if (id == null) throw new IllegalArgumentException(type + " has no id — persist it first");
        return id;
    }

    private jakarta.ws.rs.client.WebTarget target(String path)
    {
        return client.target(scheme + "://" + host + ":" + port + "/pragma/api" + path);
    }

    private void requireSuccess(Response response)
    {
        if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) return;
        String body = readBodySafely(response);
        String detail = body.isBlank()
            ? response.getStatusInfo().getReasonPhrase()
            : body;
        throw new RuntimeException("HTTP " + response.getStatus() + " " + detail);
    }

    private static String readBodySafely(Response response)
    {
        try
        {
            if (response.hasEntity())
            {
                String body = response.readEntity(String.class);
                return body == null ? "" : body.trim();
            }
        }
        catch (Exception ignored) { }
        return "";
    }

    private ObjectMapper createObjectMapper()
    {
        return new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule())
            .setVisibility(VisibilityChecker.Std.defaultInstance()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE))
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
