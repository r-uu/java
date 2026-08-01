package de.ruu.app.pragma.fx.admin;

import de.ruu.app.pragma.bean.ChangeLogBean;
import de.ruu.app.pragma.bean.GroupBean;
import de.ruu.app.pragma.bean.MembershipBean;
import de.ruu.app.pragma.bean.TaskAssignmentBean;
import de.ruu.app.pragma.bean.TaskBean;
import de.ruu.app.pragma.bean.TaskOverrunBean;
import de.ruu.app.pragma.bean.UserAvailabilityBean;
import de.ruu.app.pragma.bean.UserBean;
import de.ruu.app.pragma.bean.UserWorkloadBean;
import de.ruu.app.pragma.client.AdminClient;
import de.ruu.app.pragma.client.TaskClient;
import de.ruu.app.pragma.core.AssignmentTargetType;
import de.ruu.app.pragma.core.AssignmentType;
import de.ruu.app.pragma.core.AvailabilityType;
import de.ruu.app.pragma.core.MembershipRole;
import de.ruu.app.pragma.fx.PragmaExceptionDialogSupport;
import de.ruu.lib.fx.comp.FXCController.DefaultFXCController;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Dependent
class AdminController extends DefaultFXCController<Admin, AdminService> implements AdminService
{
    @Inject private AdminClient adminClient;
    @Inject private TaskClient taskClient;

    @FXML private Button btnUsersRefresh;
    @FXML private Button btnUsersAdd;
    @FXML private Button btnUsersEdit;
    @FXML private Button btnUsersDelete;
    @FXML private Label lblUsersStatus;
    @FXML private TableView<UserBean> tblUsers;
    @FXML private TableColumn<UserBean, Long> colUserId;
    @FXML private TableColumn<UserBean, String> colUserUsername;
    @FXML private TableColumn<UserBean, String> colUserKeycloakId;
    @FXML private TableColumn<UserBean, String> colUserDisplayName;
    @FXML private TableColumn<UserBean, String> colUserEmail;
    @FXML private TableColumn<UserBean, Boolean> colUserActive;

    @FXML private Button btnGroupsRefresh;
    @FXML private Button btnGroupsAdd;
    @FXML private Button btnGroupsEdit;
    @FXML private Button btnGroupsDelete;
    @FXML private Label lblGroupsStatus;
    @FXML private TableView<GroupBean> tblGroups;
    @FXML private TableColumn<GroupBean, Long> colGroupId;
    @FXML private TableColumn<GroupBean, String> colGroupName;
    @FXML private TableColumn<GroupBean, String> colGroupDescription;
    @FXML private TableColumn<GroupBean, Boolean> colGroupActive;

    @FXML private Button btnMembershipsRefresh;
    @FXML private Button btnMembershipsAdd;
    @FXML private Button btnMembershipsEdit;
    @FXML private Button btnMembershipsDelete;
    @FXML private Label lblMembershipsStatus;
    @FXML private TableView<MembershipBean> tblMemberships;
    @FXML private TableColumn<MembershipBean, Long> colMembershipId;
    @FXML private TableColumn<MembershipBean, Long> colMembershipUser;
    @FXML private TableColumn<MembershipBean, Long> colMembershipGroup;
    @FXML private TableColumn<MembershipBean, String> colMembershipRole;
    @FXML private TableColumn<MembershipBean, String> colMembershipValidFrom;
    @FXML private TableColumn<MembershipBean, String> colMembershipValidTo;
    @FXML private TableColumn<MembershipBean, Boolean> colMembershipActive;

    @FXML private Button btnAssignmentsRefresh;
    @FXML private Button btnAssignmentsAdd;
    @FXML private Button btnAssignmentsEdit;
    @FXML private Button btnAssignmentsDelete;
    @FXML private Label lblAssignmentsStatus;
    @FXML private TableView<TaskAssignmentBean> tblAssignments;
    @FXML private TableColumn<TaskAssignmentBean, Long> colAssignmentId;
    @FXML private TableColumn<TaskAssignmentBean, Long> colAssignmentTask;
    @FXML private TableColumn<TaskAssignmentBean, String> colAssignmentType;
    @FXML private TableColumn<TaskAssignmentBean, String> colAssignmentTargetType;
    @FXML private TableColumn<TaskAssignmentBean, Long> colAssignmentTargetId;
    @FXML private TableColumn<TaskAssignmentBean, String> colAssignmentShare;
    @FXML private TableColumn<TaskAssignmentBean, String> colAssignmentPriority;
    @FXML private TableColumn<TaskAssignmentBean, Boolean> colAssignmentActive;

    @FXML private Button btnAvailabilityRefresh;
    @FXML private Button btnAvailabilityAdd;
    @FXML private Button btnAvailabilityEdit;
    @FXML private Button btnAvailabilityDelete;
    @FXML private Label lblAvailabilityStatus;
    @FXML private TableView<UserAvailabilityBean> tblAvailabilities;
    @FXML private TableColumn<UserAvailabilityBean, Long> colAvailabilityId;
    @FXML private TableColumn<UserAvailabilityBean, Long> colAvailabilityUser;
    @FXML private TableColumn<UserAvailabilityBean, String> colAvailabilityFrom;
    @FXML private TableColumn<UserAvailabilityBean, String> colAvailabilityTo;
    @FXML private TableColumn<UserAvailabilityBean, String> colAvailabilityCapacity;
    @FXML private TableColumn<UserAvailabilityBean, String> colAvailabilityType;
    @FXML private TableColumn<UserAvailabilityBean, String> colAvailabilityNote;

    @FXML private Button btnHistoryRefresh;
    @FXML private Label lblHistoryStatus;
    @FXML private TableView<ChangeLogBean> tblHistory;
    @FXML private TableColumn<ChangeLogBean, String> colHistoryChangedAt;
    @FXML private TableColumn<ChangeLogBean, String> colHistoryEntityType;
    @FXML private TableColumn<ChangeLogBean, Long> colHistoryEntityId;
    @FXML private TableColumn<ChangeLogBean, String> colHistoryField;
    @FXML private TableColumn<ChangeLogBean, String> colHistoryOld;
    @FXML private TableColumn<ChangeLogBean, String> colHistoryNew;
    @FXML private TableColumn<ChangeLogBean, String> colHistoryCategory;

    @FXML private Button btnAnalyticsRefresh;
    @FXML private Label lblAnalyticsStatus;
    @FXML private TableView<UserWorkloadBean> tblWorkload;
    @FXML private TableColumn<UserWorkloadBean, String> colWorkloadUser;
    @FXML private TableColumn<UserWorkloadBean, String> colWorkloadCapacity;
    @FXML private TableColumn<UserWorkloadBean, String> colWorkloadAssigned;
    @FXML private TableColumn<UserWorkloadBean, String> colWorkloadOverbooked;
    @FXML private TableView<TaskOverrunBean> tblOverruns;
    @FXML private TableColumn<TaskOverrunBean, String> colOverrunTask;
    @FXML private TableColumn<TaskOverrunBean, String> colOverrunEstimate;
    @FXML private TableColumn<TaskOverrunBean, String> colOverrunActual;
    @FXML private TableColumn<TaskOverrunBean, String> colOverrunDelta;

    @FXML
    protected void initialize()
    {
        initColumns();
        bindActions();
        reloadAll();
    }

    private void initColumns()
    {
        colUserId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().id()));
        colUserUsername.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().username()));
        colUserKeycloakId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().keycloakUserId().orElse("")));
        colUserDisplayName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().displayName()));
        colUserEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().email()));
        colUserActive.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().active()));

        colGroupId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().id()));
        colGroupName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().name()));
        colGroupDescription.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().description().orElse("")));
        colGroupActive.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().active()));

        colMembershipId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().id()));
        colMembershipUser.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().userId()));
        colMembershipGroup.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().groupId()));
        colMembershipRole.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().roleInGroup().name()));
        colMembershipValidFrom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().validFrom().map(LocalDate::toString).orElse("")));
        colMembershipValidTo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().validTo().map(LocalDate::toString).orElse("")));
        colMembershipActive.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().active()));

        colAssignmentId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().id()));
        colAssignmentTask.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().taskId()));
        colAssignmentType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().assignmentType().name()));
        colAssignmentTargetType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().targetType().name()));
        colAssignmentTargetId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(
            c.getValue().targetType() == AssignmentTargetType.USER
                ? c.getValue().userId().orElse(null)
                : c.getValue().groupId().orElse(null)));
        colAssignmentShare.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().share().map(Object::toString).orElse("")));
        colAssignmentPriority.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().priority().map(Object::toString).orElse("")));
        colAssignmentActive.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().active()));

        colAvailabilityId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().id()));
        colAvailabilityUser.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().userId()));
        colAvailabilityFrom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().fromDate().toString()));
        colAvailabilityTo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().toDate().toString()));
        colAvailabilityCapacity.setCellValueFactory(c -> new SimpleStringProperty(Double.toString(c.getValue().capacityHoursPerDay())));
        colAvailabilityType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().availabilityType().name()));
        colAvailabilityNote.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().note().orElse("")));

        colHistoryChangedAt.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().changedAt().toString()));
        colHistoryEntityType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().entityType()));
        colHistoryEntityId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().entityId()));
        colHistoryField.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().fieldName()));
        colHistoryOld.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().oldValue().orElse("")));
        colHistoryNew.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().newValue().orElse("")));
        colHistoryCategory.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().category().name()));

        colWorkloadUser.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().displayName() + " (" + c.getValue().username() + ")"));
        colWorkloadCapacity.setCellValueFactory(c -> new SimpleStringProperty(Double.toString(c.getValue().capacityHoursPerDay())));
        colWorkloadAssigned.setCellValueFactory(c -> new SimpleStringProperty(Double.toString(c.getValue().assignedHours())));
        colWorkloadOverbooked.setCellValueFactory(c -> new SimpleStringProperty(Double.toString(c.getValue().overbookedHours())));

        colOverrunTask.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().taskName()));
        colOverrunEstimate.setCellValueFactory(c -> new SimpleStringProperty(nullableToString(c.getValue().estimateHours())));
        colOverrunActual.setCellValueFactory(c -> new SimpleStringProperty(nullableToString(c.getValue().actualHours())));
        colOverrunDelta.setCellValueFactory(c -> new SimpleStringProperty(nullableToString(c.getValue().overrunHours())));
    }

    private void bindActions()
    {
        btnUsersRefresh.setOnAction(e -> reloadUsers());
        btnUsersAdd.setOnAction(e -> addUser());
        btnUsersEdit.setOnAction(e -> editUser());
        btnUsersDelete.setOnAction(e -> deleteUser());

        btnGroupsRefresh.setOnAction(e -> reloadGroups());
        btnGroupsAdd.setOnAction(e -> addGroup());
        btnGroupsEdit.setOnAction(e -> editGroup());
        btnGroupsDelete.setOnAction(e -> deleteGroup());

        btnMembershipsRefresh.setOnAction(e -> reloadMemberships());
        btnMembershipsAdd.setOnAction(e -> addMembership());
        btnMembershipsEdit.setOnAction(e -> editMembership());
        btnMembershipsDelete.setOnAction(e -> deleteMembership());

        btnAssignmentsRefresh.setOnAction(e -> reloadAssignments());
        btnAssignmentsAdd.setOnAction(e -> addAssignment());
        btnAssignmentsEdit.setOnAction(e -> editAssignment());
        btnAssignmentsDelete.setOnAction(e -> deleteAssignment());

        btnAvailabilityRefresh.setOnAction(e -> reloadAvailabilities());
        btnAvailabilityAdd.setOnAction(e -> addAvailability());
        btnAvailabilityEdit.setOnAction(e -> editAvailability());
        btnAvailabilityDelete.setOnAction(e -> deleteAvailability());

        btnHistoryRefresh.setOnAction(e -> reloadHistory());
        btnAnalyticsRefresh.setOnAction(e -> reloadAnalytics());
    }

    private void reloadAll()
    {
        reloadUsers();
        reloadGroups();
        reloadMemberships();
        reloadAssignments();
        reloadAvailabilities();
        reloadHistory();
        reloadAnalytics();
    }

    private void reloadUsers()
    {
        try {
            tblUsers.getItems().setAll(adminClient.users());
            lblUsersStatus.setText("Loaded " + tblUsers.getItems().size() + " (Keycloak sync best effort)");
        } catch (Exception e) { showError("Users", e); }
    }

    private void reloadGroups()
    {
        try {
            tblGroups.getItems().setAll(adminClient.groups());
            lblGroupsStatus.setText("Loaded " + tblGroups.getItems().size());
        } catch (Exception e) { showError("Groups", e); }
    }

    private void reloadMemberships()
    {
        try {
            tblMemberships.getItems().setAll(adminClient.memberships());
            lblMembershipsStatus.setText("Loaded " + tblMemberships.getItems().size());
        } catch (Exception e) { showError("Memberships", e); }
    }

    private void reloadAssignments()
    {
        try {
            tblAssignments.getItems().setAll(adminClient.taskAssignments());
            lblAssignmentsStatus.setText("Loaded " + tblAssignments.getItems().size());
        } catch (Exception e) { showError("Assignments", e); }
    }

    private void reloadAvailabilities()
    {
        try {
            tblAvailabilities.getItems().setAll(adminClient.userAvailabilities());
            lblAvailabilityStatus.setText("Loaded " + tblAvailabilities.getItems().size());
        } catch (Exception e) { showError("Availability", e); }
    }

    private void reloadHistory()
    {
        try {
            tblHistory.getItems().setAll(adminClient.changeLog());
            lblHistoryStatus.setText("Loaded " + tblHistory.getItems().size());
        } catch (Exception e) { showError("History", e); }
    }

    private void reloadAnalytics()
    {
        try {
            List<UserWorkloadBean> workload = adminClient.workload().stream().filter(w -> w.overbookedHours() > 0d).toList();
            tblWorkload.getItems().setAll(workload);
            tblOverruns.getItems().setAll(adminClient.timeOverruns());
            lblAnalyticsStatus.setText("Overload: " + workload.size() + ", Overruns: " + tblOverruns.getItems().size());
        } catch (Exception e) { showError("Analytics", e); }
    }

    private void addUser()
    {
        editUserDialog(new UserBean("user", "Display Name", "user@example.org")).ifPresent(it -> {
            try { adminClient.createUser(it); reloadUsers(); }
            catch (Exception e) { showError("Add user", e); }
        });
    }

    private void editUser()
    {
        UserBean selected = tblUsers.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        editUserDialog(new UserBean(selected)).ifPresent(it -> {
            try { adminClient.updateUser(it); reloadUsers(); }
            catch (Exception e) { showError("Edit user", e); }
        });
    }

    private void deleteUser()
    {
        UserBean selected = tblUsers.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (!confirmDelete("Delete user \"" + selected.username() + "\"?")) return;
        try { adminClient.deleteUser(selected); reloadUsers(); }
        catch (Exception e) { showError("Delete user", e); }
    }

    private void addGroup()
    {
        editGroupDialog(new GroupBean("Group")).ifPresent(it -> {
            try { adminClient.createGroup(it); reloadGroups(); }
            catch (Exception e) { showError("Add group", e); }
        });
    }

    private void editGroup()
    {
        GroupBean selected = tblGroups.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        editGroupDialog(new GroupBean(selected)).ifPresent(it -> {
            try { adminClient.updateGroup(it); reloadGroups(); }
            catch (Exception e) { showError("Edit group", e); }
        });
    }

    private void deleteGroup()
    {
        GroupBean selected = tblGroups.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (!confirmDelete("Delete group \"" + selected.name() + "\"?")) return;
        try { adminClient.deleteGroup(selected); reloadGroups(); }
        catch (Exception e) { showError("Delete group", e); }
    }

    private void addMembership()
    {
        try {
            editMembershipDialog(null).ifPresent(it -> {
                try { adminClient.createMembership(it); reloadMemberships(); }
                catch (Exception e) { showError("Add membership", e); }
            });
        } catch (Exception e) { showError("Add membership", e); }
    }

    private void editMembership()
    {
        MembershipBean selected = tblMemberships.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            editMembershipDialog(selected).ifPresent(it -> {
                try { adminClient.updateMembership(it); reloadMemberships(); }
                catch (Exception e) { showError("Edit membership", e); }
            });
        } catch (Exception e) { showError("Edit membership", e); }
    }

    private void deleteMembership()
    {
        MembershipBean selected = tblMemberships.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (!confirmDelete("Delete selected membership?")) return;
        try { adminClient.deleteMembership(selected); reloadMemberships(); }
        catch (Exception e) { showError("Delete membership", e); }
    }

    private void addAssignment()
    {
        try {
            editAssignmentDialog(null).ifPresent(it -> {
                try { adminClient.createTaskAssignment(it); reloadAssignments(); reloadAnalytics(); }
                catch (Exception e) { showError("Add assignment", e); }
            });
        } catch (Exception e) { showError("Add assignment", e); }
    }

    private void editAssignment()
    {
        TaskAssignmentBean selected = tblAssignments.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            editAssignmentDialog(selected).ifPresent(it -> {
                try { adminClient.updateTaskAssignment(it); reloadAssignments(); reloadAnalytics(); }
                catch (Exception e) { showError("Edit assignment", e); }
            });
        } catch (Exception e) { showError("Edit assignment", e); }
    }

    private void deleteAssignment()
    {
        TaskAssignmentBean selected = tblAssignments.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (!confirmDelete("Delete selected assignment?")) return;
        try { adminClient.deleteTaskAssignment(selected); reloadAssignments(); reloadAnalytics(); }
        catch (Exception e) { showError("Delete assignment", e); }
    }

    private void addAvailability()
    {
        try {
            editAvailabilityDialog(null).ifPresent(it -> {
                try { adminClient.createUserAvailability(it); reloadAvailabilities(); reloadAnalytics(); }
                catch (Exception e) { showError("Add availability", e); }
            });
        } catch (Exception e) { showError("Add availability", e); }
    }

    private void editAvailability()
    {
        UserAvailabilityBean selected = tblAvailabilities.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            editAvailabilityDialog(selected).ifPresent(it -> {
                try { adminClient.updateUserAvailability(it); reloadAvailabilities(); reloadAnalytics(); }
                catch (Exception e) { showError("Edit availability", e); }
            });
        } catch (Exception e) { showError("Edit availability", e); }
    }

    private void deleteAvailability()
    {
        UserAvailabilityBean selected = tblAvailabilities.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (!confirmDelete("Delete selected availability?")) return;
        try { adminClient.deleteUserAvailability(selected); reloadAvailabilities(); reloadAnalytics(); }
        catch (Exception e) { showError("Delete availability", e); }
    }

    private Optional<UserBean> editUserDialog(UserBean seed)
    {
        TextField tfUsername = new TextField(seed.username());
        TextField tfKeycloakUserId = new TextField(seed.keycloakUserId().orElse(""));
        tfKeycloakUserId.setEditable(false);
        tfKeycloakUserId.setStyle("-fx-background-color: transparent;");
        TextField tfPassword = new TextField("");
        TextField tfDisplayName = new TextField(seed.displayName());
        TextField tfEmail = new TextField(seed.email());
        CheckBox cbActive = new CheckBox("active");
        cbActive.setSelected(seed.active());
        Dialog<ButtonType> dialog = dialog("User", form(
            row("username", tfUsername),
            row("keycloakUserId", tfKeycloakUserId),
            row("password (optional)", tfPassword),
            row("displayName", tfDisplayName),
            row("email", tfEmail),
            row("", cbActive)));
        return dialog.showAndWait().filter(ButtonType.OK::equals).map(it -> seed
            .username(tfUsername.getText().trim())
            .keycloakUserId(blankAsNull(tfKeycloakUserId.getText()))
            .password(blankAsNull(tfPassword.getText()))
            .displayName(tfDisplayName.getText().trim())
            .email(tfEmail.getText().trim())
            .active(cbActive.isSelected()));
    }

    private Optional<GroupBean> editGroupDialog(GroupBean seed)
    {
        TextField tfName = new TextField(seed.name());
        TextField tfDescription = new TextField(seed.description().orElse(""));
        CheckBox cbActive = new CheckBox("active");
        cbActive.setSelected(seed.active());
        Dialog<ButtonType> dialog = dialog("Group", form(
            row("name", tfName),
            row("description", tfDescription),
            row("", cbActive)));
        return dialog.showAndWait().filter(ButtonType.OK::equals).map(it -> seed
            .name(tfName.getText().trim())
            .description(blankAsNull(tfDescription.getText()))
            .active(cbActive.isSelected()));
    }

    private Optional<MembershipBean> editMembershipDialog(@Nullable MembershipBean current)
    {
        List<UserBean> users = adminClient.users();
        List<GroupBean> groups = adminClient.groups();
        if (users.isEmpty() || groups.isEmpty()) throw new IllegalStateException("Users and groups are required before creating memberships");
        ComboBox<UserBean> cbUser = new ComboBox<>(FXCollections.observableArrayList(users));
        cbUser.setCellFactory(list -> new SimpleNameCell<>(UserBean::username));
        cbUser.setButtonCell(new SimpleNameCell<>(UserBean::username));
        ComboBox<GroupBean> cbGroup = new ComboBox<>(FXCollections.observableArrayList(groups));
        cbGroup.setCellFactory(list -> new SimpleNameCell<>(GroupBean::name));
        cbGroup.setButtonCell(new SimpleNameCell<>(GroupBean::name));
        ComboBox<MembershipRole> cbRole = new ComboBox<>(FXCollections.observableArrayList(MembershipRole.values()));
        DatePicker dpFrom = new DatePicker();
        DatePicker dpTo = new DatePicker();
        CheckBox cbActive = new CheckBox("active");
        cbActive.setSelected(true);
        MembershipBean seed = current == null
            ? new MembershipBean(users.getFirst().id(), groups.getFirst().id())
            : new MembershipBean(current);
        if (current != null) {
            cbUser.getSelectionModel().select(users.stream().filter(u -> u.id() != null && u.id().equals(current.userId())).findFirst().orElse(null));
            cbGroup.getSelectionModel().select(groups.stream().filter(g -> g.id() != null && g.id().equals(current.groupId())).findFirst().orElse(null));
            cbRole.getSelectionModel().select(current.roleInGroup());
            dpFrom.setValue(current.validFrom().orElse(null));
            dpTo.setValue(current.validTo().orElse(null));
            cbActive.setSelected(current.active());
        } else {
            cbUser.getSelectionModel().selectFirst();
            cbGroup.getSelectionModel().selectFirst();
            cbRole.getSelectionModel().select(MembershipRole.MEMBER);
        }
        Dialog<ButtonType> dialog = dialog("Membership", form(
            row("user", cbUser),
            row("group", cbGroup),
            row("role", cbRole),
            row("validFrom", dpFrom),
            row("validTo", dpTo),
            row("", cbActive)));
        return dialog.showAndWait().filter(ButtonType.OK::equals).map(it -> seed
            .userId(requireId(cbUser.getValue().id(), "user"))
            .groupId(requireId(cbGroup.getValue().id(), "group"))
            .roleInGroup(cbRole.getValue())
            .validFrom(dpFrom.getValue())
            .validTo(dpTo.getValue())
            .active(cbActive.isSelected()));
    }

    private Optional<TaskAssignmentBean> editAssignmentDialog(@Nullable TaskAssignmentBean current)
    {
        List<TaskBean> tasks = taskClient.findAll();
        List<UserBean> users = adminClient.users();
        List<GroupBean> groups = adminClient.groups();
        ComboBox<TaskBean> cbTask = new ComboBox<>(FXCollections.observableArrayList(tasks));
        cbTask.setCellFactory(list -> new SimpleNameCell<>(TaskBean::name));
        cbTask.setButtonCell(new SimpleNameCell<>(TaskBean::name));
        ComboBox<AssignmentType> cbType = new ComboBox<>(FXCollections.observableArrayList(AssignmentType.values()));
        ComboBox<AssignmentTargetType> cbTargetType = new ComboBox<>(FXCollections.observableArrayList(AssignmentTargetType.values()));
        ComboBox<UserBean> cbUser = new ComboBox<>(FXCollections.observableArrayList(users));
        cbUser.setCellFactory(list -> new SimpleNameCell<>(UserBean::username));
        cbUser.setButtonCell(new SimpleNameCell<>(UserBean::username));
        ComboBox<GroupBean> cbGroup = new ComboBox<>(FXCollections.observableArrayList(groups));
        cbGroup.setCellFactory(list -> new SimpleNameCell<>(GroupBean::name));
        cbGroup.setButtonCell(new SimpleNameCell<>(GroupBean::name));
        TextField tfShare = new TextField();
        TextField tfPriority = new TextField();
        DatePicker dpFrom = new DatePicker();
        DatePicker dpTo = new DatePicker();
        TextField tfNote = new TextField();
        CheckBox cbActive = new CheckBox("active");
        cbActive.setSelected(true);
        TaskAssignmentBean seed = current == null
            ? new TaskAssignmentBean(tasks.getFirst().id())
            : new TaskAssignmentBean(current);
        if (current != null) {
            cbTask.getSelectionModel().select(tasks.stream().filter(t -> t.id() != null && t.id().equals(current.taskId())).findFirst().orElse(null));
            cbType.getSelectionModel().select(current.assignmentType());
            cbTargetType.getSelectionModel().select(current.targetType());
            cbUser.getSelectionModel().select(users.stream().filter(u -> current.userId().isPresent() && u.id() != null && u.id().equals(current.userId().get())).findFirst().orElse(null));
            cbGroup.getSelectionModel().select(groups.stream().filter(g -> current.groupId().isPresent() && g.id() != null && g.id().equals(current.groupId().get())).findFirst().orElse(null));
            tfShare.setText(current.share().map(Object::toString).orElse(""));
            tfPriority.setText(current.priority().map(Object::toString).orElse(""));
            dpFrom.setValue(current.validFrom().orElse(null));
            dpTo.setValue(current.validTo().orElse(null));
            tfNote.setText(current.note().orElse(""));
            cbActive.setSelected(current.active());
        } else {
            cbTask.getSelectionModel().selectFirst();
            cbType.getSelectionModel().select(AssignmentType.ASSIGNEE);
            cbTargetType.getSelectionModel().select(AssignmentTargetType.USER);
            cbUser.getSelectionModel().selectFirst();
            cbGroup.getSelectionModel().selectFirst();
        }
        cbTargetType.valueProperty().addListener((obs, old, val) -> {
            boolean userTarget = val == AssignmentTargetType.USER;
            cbUser.setDisable(!userTarget);
            cbGroup.setDisable(userTarget);
        });
        boolean userTarget = cbTargetType.getValue() == AssignmentTargetType.USER;
        cbUser.setDisable(!userTarget);
        cbGroup.setDisable(userTarget);
        Dialog<ButtonType> dialog = dialog("Task assignment", form(
            row("task", cbTask),
            row("type", cbType),
            row("targetType", cbTargetType),
            row("user", cbUser),
            row("group", cbGroup),
            row("share", tfShare),
            row("priority", tfPriority),
            row("validFrom", dpFrom),
            row("validTo", dpTo),
            row("note", tfNote),
            row("", cbActive)));
        return dialog.showAndWait().filter(ButtonType.OK::equals).map(it -> {
            TaskAssignmentBean out = seed
                .taskId(requireId(cbTask.getValue().id(), "task"))
                .assignmentType(cbType.getValue())
                .targetType(cbTargetType.getValue())
                .share(parseDouble(tfShare.getText()))
                .priority(parseInteger(tfPriority.getText()))
                .validFrom(dpFrom.getValue())
                .validTo(dpTo.getValue())
                .note(blankAsNull(tfNote.getText()))
                .active(cbActive.isSelected());
            if (out.targetType() == AssignmentTargetType.USER) {
                out.userId(requireId(cbUser.getValue().id(), "user")).groupId(null);
            } else {
                out.groupId(requireId(cbGroup.getValue().id(), "group")).userId(null);
            }
            return out;
        });
    }

    private Optional<UserAvailabilityBean> editAvailabilityDialog(@Nullable UserAvailabilityBean current)
    {
        List<UserBean> users = adminClient.users();
        if (users.isEmpty()) throw new IllegalStateException("Users are required before creating availability");
        ComboBox<UserBean> cbUser = new ComboBox<>(FXCollections.observableArrayList(users));
        cbUser.setCellFactory(list -> new SimpleNameCell<>(UserBean::username));
        cbUser.setButtonCell(new SimpleNameCell<>(UserBean::username));
        DatePicker dpFrom = new DatePicker(LocalDate.now());
        DatePicker dpTo = new DatePicker(LocalDate.now());
        TextField tfCapacity = new TextField("8");
        ComboBox<AvailabilityType> cbType = new ComboBox<>(FXCollections.observableArrayList(AvailabilityType.values()));
        TextField tfNote = new TextField();
        UserAvailabilityBean seed = current == null
            ? new UserAvailabilityBean(users.getFirst().id(), LocalDate.now(), LocalDate.now(), 8d)
            : new UserAvailabilityBean(current);
        if (current != null) {
            cbUser.getSelectionModel().select(users.stream().filter(u -> u.id() != null && u.id().equals(current.userId())).findFirst().orElse(null));
            dpFrom.setValue(current.fromDate());
            dpTo.setValue(current.toDate());
            tfCapacity.setText(Double.toString(current.capacityHoursPerDay()));
            cbType.getSelectionModel().select(current.availabilityType());
            tfNote.setText(current.note().orElse(""));
        } else {
            cbUser.getSelectionModel().selectFirst();
            cbType.getSelectionModel().select(AvailabilityType.AVAILABLE);
        }
        Dialog<ButtonType> dialog = dialog("User availability", form(
            row("user", cbUser),
            row("from", dpFrom),
            row("to", dpTo),
            row("capacityHoursPerDay", tfCapacity),
            row("type", cbType),
            row("note", tfNote)));
        return dialog.showAndWait().filter(ButtonType.OK::equals).map(it -> seed
            .userId(requireId(cbUser.getValue().id(), "user"))
            .fromDate(dpFrom.getValue())
            .toDate(dpTo.getValue())
            .capacityHoursPerDay(parseDoubleOrDefault(tfCapacity.getText(), 8d))
            .availabilityType(cbType.getValue())
            .note(blankAsNull(tfNote.getText())));
    }

    private static Dialog<ButtonType> dialog(String title, GridPane content)
    {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(520);
        return dialog;
    }

    private static GridPane form(Row... rows)
    {
        GridPane grid = new GridPane();
        grid.setHgap(6);
        grid.setVgap(6);
        int idx = 0;
        for (Row row : rows) {
            if (!row.label().isBlank()) grid.add(new Label(row.label()), 0, idx);
            grid.add(row.node(), 1, idx);
            idx++;
        }
        return grid;
    }

    private static Row row(String label, javafx.scene.Node node)
    {
        return new Row(label, node);
    }

    private static boolean confirmDelete(String message)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Confirm delete");
        alert.setHeaderText(null);
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    private static void showError(String title, Exception e)
    {
        PragmaExceptionDialogSupport.showError(title, e);
    }

    private static long requireId(@Nullable Long id, String label)
    {
        if (id == null) throw new IllegalArgumentException(label + " id is missing");
        return id;
    }

    private static @Nullable String blankAsNull(@Nullable String value)
    {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static @Nullable Double parseDouble(String value)
    {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isEmpty() ? null : Double.parseDouble(trimmed);
    }

    private static @Nullable Integer parseInteger(String value)
    {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isEmpty() ? null : Integer.parseInt(trimmed);
    }

    private static double parseDoubleOrDefault(String value, double fallback)
    {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isEmpty() ? fallback : Double.parseDouble(trimmed);
    }

    private static String nullableToString(@Nullable Object value)
    {
        return value == null ? "" : value.toString();
    }

    private record Row(String label, javafx.scene.Node node)
    {
    }

    private static final class SimpleNameCell<T> extends javafx.scene.control.ListCell<T>
    {
        private final java.util.function.Function<T, String> labelFn;

        private SimpleNameCell(java.util.function.Function<T, String> labelFn)
        {
            this.labelFn = labelFn;
        }

        @Override
        protected void updateItem(T item, boolean empty)
        {
            super.updateItem(item, empty);
            setText(empty || item == null ? null : labelFn.apply(item));
        }
    }
}
