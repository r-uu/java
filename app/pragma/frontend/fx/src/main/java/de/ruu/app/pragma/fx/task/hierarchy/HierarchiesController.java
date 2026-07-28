package de.ruu.app.pragma.fx.task.hierarchy;

import de.ruu.app.pragma.bean.TaskBean;
import de.ruu.app.pragma.bean.TaskGroupBean;
import de.ruu.app.pragma.client.TaskClient;
import de.ruu.app.pragma.client.TaskGroupClient;
import de.ruu.app.pragma.fx.task.TaskUiSupport;
import de.ruu.app.pragma.fx.task.edit.TaskEditor;
import de.ruu.app.pragma.fx.task.inspector.TaskInspectorSupport;
import de.ruu.app.pragma.fx.taskgroup.edit.TaskGroupEditor;
import de.ruu.lib.fx.comp.FXCController.DefaultFXCController;
import de.ruu.lib.fx.control.autocomplete.textfield.TextFieldAutoCompleteClearableWithArrowButton;
import de.ruu.lib.fx.control.autocomplete.textfield.TextFieldAutoCompleteClearableWithArrowButtonBuilder;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@Dependent
class HierarchiesController extends DefaultFXCController<Hierarchies, HierarchiesService>
        implements HierarchiesService
{
    private static final Logger log = LogManager.getLogger(HierarchiesController.class);

    // ── top bar ──────────────────────────────────────────────────────────────

    @FXML private HBox   cbGroupsContainer;
    @FXML private Button btnManageGroups;
    @FXML private Button btnReload;
    @FXML private Label  lblStatus;
    private TextFieldAutoCompleteClearableWithArrowButton<TaskGroupBean> cbGroups;

    // ── panel containers (populated by FXML, panels inserted in initialize) ──

    @FXML private AnchorPane panePred;
    @FXML private AnchorPane paneCenter;
    @FXML private AnchorPane paneSucc;
    @FXML private BorderPane brdPaneMain;
    @FXML private VBox vBxInspectorContainer;
    @FXML private Button btnInspectorToggle;
    @FXML private Button btnInspectorSave;

    // ── three hierarchy panels ────────────────────────────────────────────────

    private TaskHierarchyPanel predPanel;
    private TaskHierarchyPanel centerPanel;
    private TaskHierarchyPanel succPanel;

    // ── injections ───────────────────────────────────────────────────────────

    @Inject private TaskGroupClient taskGroupClient;
    @Inject private TaskClient      taskClient;
    @Inject private TaskEditor taskEditor;
    @Inject private TaskGroupEditor taskGroupEditor;

    private de.ruu.app.pragma.fx.TaskGroupManagementDialog groupManagementDialog;

    /** All tasks of the current group, keyed by ID; populated by loadGroup(). */
    private Map<Long, TaskBean> taskByIdCache = new HashMap<>();
    private TaskInspectorSupport inspector;

    private boolean handlingNav = false;

    // ── initialization ───────────────────────────────────────────────────────

    @Override
    @FXML
    protected void initialize()
    {
        // group dropdown
        cbGroups = TextFieldAutoCompleteClearableWithArrowButtonBuilder.<TaskGroupBean>create()
                .items(List.of())
                .suggestionFilter((g, text) -> g.name().toLowerCase().contains(text.toLowerCase()))
                .comparator(Comparator.comparing(TaskGroupBean::name))
                .textProvider(TaskGroupBean::name)
                .prompt("group …")
                .build();
        HBox.setHgrow(cbGroups, Priority.ALWAYS);
        cbGroupsContainer.getChildren().add(cbGroups);
        cbGroups.valueProperty()
                .addListener((obs, old, sel) -> {
                    if (handlingNav || sel == null) return;
                    if (!confirmDiscardChanges()) {
                        handlingNav = true;
                        cbGroups.value(old);
                        handlingNav = false;
                        return;
                    }
                    clearDirty();
                    loadGroup(sel);
                });

        btnManageGroups.setOnAction(e -> onManageGroups());
        btnReload.setOnAction(e -> loadGroups());

        // build panels
        predPanel   = new TaskHierarchyPanel("predecessor tasks",  true,
                "add predecessor", "edit predecessor", "remove predecessor link");
        centerPanel = new TaskHierarchyPanel("super/sub tasks",    false,
                "add task", "rename task", "delete task");
        succPanel   = new TaskHierarchyPanel("successor tasks",    false,
                "add successor", "edit successor", "remove successor link");

        embedPanel(panePred,   predPanel);
        embedPanel(paneCenter, centerPanel);
        embedPanel(paneSucc,   succPanel);
        inspector = new TaskInspectorSupport(
            brdPaneMain,
            vBxInspectorContainer,
            btnInspectorToggle,
            btnInspectorSave,
            taskEditor.localRoot(),
            taskEditor.service(),
            taskClient::update,
            updated -> {
                TreeItem<TaskBean> sel = centerPanel.selectedItem();
                if (sel != null && sel.getValue() != null && updated.id() != null
                    && updated.id().equals(sel.getValue().id()))
                    sel.setValue(updated);
                if (updated.id() != null) taskByIdCache.put(updated.id(), updated);
                reloadSidePanels(updated);
                updateButtonStates();
            },
            e -> {
                log.error("failed to save task data", e);
                showError("Save", e);
            });
        inspector.initialize();

        // center selection drives the other two panels
        centerPanel.treeView.getSelectionModel().selectedItemProperty()
                   .addListener((obs, old, sel) -> {
                       if (handlingNav) return;
                       if (!confirmDiscardChanges()) {
                           handlingNav = true;
                           centerPanel.treeView.getSelectionModel().select(old);
                           handlingNav = false;
                           return;
                       }
                       clearDirty();
                       onCenterTaskSelected(sel);
                   });

        // save buttons
        // center CRUD
        centerPanel.btnAdd .setOnAction(e -> onAddTask());
        centerPanel.btnEdit.setOnAction(e -> onEditTask());
        centerPanel.btnDel .setOnAction(e -> onDelTask());

        // predecessor CRUD
        predPanel.btnAdd .setOnAction(e -> onAddPredecessor());
        predPanel.btnEdit.setOnAction(e -> onEditPredecessor());
        predPanel.btnDel .setOnAction(e -> onDelPredecessor());

        // successor CRUD
        succPanel.btnAdd .setOnAction(e -> onAddSuccessor());
        succPanel.btnEdit.setOnAction(e -> onEditSuccessor());
        succPanel.btnDel .setOnAction(e -> onDelSuccessor());

        disableAll(true);
        loadGroups();
    }

    // ── data loading ─────────────────────────────────────────────────────────

    private void loadGroups()
    {
        TaskUiSupport.showConnecting(lblStatus);
        Thread.ofVirtual().start(() ->
        {
            try
            {
                List<TaskGroupBean> groups = taskGroupClient.findAll();
                Platform.runLater(() ->
                {
                    cbGroups.items(groups);
                    if (!groups.isEmpty())
                    {
                        cbGroups.value(groups.get(0));
                        TaskUiSupport.clearStatus(lblStatus);
                    }
                    else
                    {
                        handlingNav = true;
                        centerPanel.treeView.setRoot(new TreeItem<>());
                        predPanel.treeView.setRoot(new TreeItem<>());
                        succPanel.treeView.setRoot(new TreeItem<>());
                        handlingNav = false;
                        taskByIdCache.clear();
                        clearSidePanels();
                        clearInspector();
                        disableAll(true);
                        if (lblStatus != null)
                            lblStatus.setText("[INFO] No task groups available. Create one via folder button.");
                    }
                });
            }
            catch (Exception e)
            {
                log.error("failed to load groups", e);
                Platform.runLater(() -> TaskUiSupport.showConnectionError(lblStatus));
            }
        });
    }

    private void loadGroup(TaskGroupBean group)
    {
        try
        {
            List<TaskBean> tasks = taskClient.findGroupTasksWithRelated(group);
            taskByIdCache = tasks.stream()
                .filter(t -> t.id() != null)
                .collect(java.util.stream.Collectors.toMap(TaskBean::id, t -> t));
            TreeItem<TaskBean> root = buildSuperSubTree(tasks);
            Platform.runLater(() -> {
                handlingNav = true;
                centerPanel.treeView.setRoot(root);
                handlingNav = false;
                clearSidePanels();
                clearInspector();
                disableAll(false);
                updateButtonStates();
                if (lblStatus != null)
                {
                    if (tasks.isEmpty()) lblStatus.setText("[INFO] Group has no tasks.");
                    else lblStatus.setText(tasks.size() + " tasks");
                }
            });
        }
        catch (Exception e) { log.error("failed to load group {}", group.name(), e); }
    }

    private void onCenterTaskSelected(TreeItem<TaskBean> item)
    {
        clearSidePanels();

        if (item == null || item.getValue() == null)
        {
            clearInspector();
            updateButtonStates();
            return;
        }

        TaskBean task = item.getValue();
        inspector.showTask(task);

        if (task.id() == null) { updateButtonStates(); return; }

        try
        {
            TaskBean cached = taskByIdCache.getOrDefault(task.id(), task);

            List<TaskBean> preds = cached.predecessors()
                .<List<TaskBean>>map(ArrayList::new)
                .orElseGet(() -> taskClient.findPredecessors(task));
            TreeItem<TaskBean> predRoot = new TreeItem<>();
            Set<Long> visitedPred = new HashSet<>();
            visitedPred.add(task.id());
            preds.forEach(p -> predRoot.getChildren().add(buildPredecessorNode(p, visitedPred)));
            sortTreeItems(predRoot);
            predPanel.treeView.setRoot(predRoot);

            List<TaskBean> succs = cached.successors()
                .<List<TaskBean>>map(ArrayList::new)
                .orElseGet(() -> taskClient.findSuccessors(task));
            TreeItem<TaskBean> succRoot = new TreeItem<>();
            Set<Long> visitedSucc = new HashSet<>();
            visitedSucc.add(task.id());
            succs.forEach(s -> succRoot.getChildren().add(buildSuccessorNode(s, visitedSucc)));
            sortTreeItems(succRoot);
            succPanel.treeView.setRoot(succRoot);
        }
        catch (Exception e) { log.error("failed to load neighbours for {}", task.name(), e); }

        updateButtonStates();
    }

    // ── center panel: task CRUD ───────────────────────────────────────────────

    private void onAddTask()
    {
        TaskGroupBean group = cbGroups.valueProperty().get();
        if (group == null || group.id() == null) return;

        TaskBean selectedTask = centerPanel.selectedTask();

        boolean createAsSub = false;
        if (selectedTask != null && selectedTask.id() != null)
        {
            ButtonType btnRoot = new ButtonType("Root task");
            ButtonType btnSub  = new ButtonType("Subtask of \"" + selectedTask.name() + "\"");
            Alert choice = new Alert(Alert.AlertType.CONFIRMATION);
            choice.setTitle("Add task");
            choice.setHeaderText("Where should the new task be created?");
            choice.getButtonTypes().setAll(btnRoot, btnSub, ButtonType.CANCEL);
            Optional<ButtonType> result = choice.showAndWait();
            if (result.isEmpty() || result.get() == ButtonType.CANCEL) return;
            createAsSub = result.get() == btnSub;
        }

        final TaskBean parent = createAsSub ? selectedTask : null;
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Add task");
        dlg.setHeaderText(parent != null
                ? "New subtask of \"" + parent.name() + "\""
                : "New root task in group \"" + group.name() + "\"");
        dlg.setContentText("Name:");
        dlg.showAndWait().map(String::trim).filter(s -> !s.isEmpty()).ifPresent(name ->
        {
            try
            {
                TaskBean newTask = new TaskBean(group, name);
                if (parent != null) newTask.parentTask(parent);
                taskClient.create(newTask);
                reloadCurrentGroup();
            }
            catch (Exception e) { log.error("failed to create task", e); showError("Create task", e); }
        });
    }

    private void onEditTask()
    {
        TreeItem<TaskBean> item = centerPanel.selectedItem();
        if (item == null) return;
        editTaskName(item, () -> {
            inspector.showTask(item.getValue());
            updateButtonStates();
        });
    }

    private void onDelTask()
    {
        TaskBean task = centerPanel.selectedTask();
        if (task == null || task.id() == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete task \"" + task.name() + "\"?", ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Delete task");
        confirm.setHeaderText(null);
        if (confirm.showAndWait().filter(bt -> bt == ButtonType.OK).isPresent())
        {
            try { taskClient.delete(task); reloadCurrentGroup(); }
            catch (Exception e) { log.error("failed to delete task", e); showError("Delete task", e); }
        }
    }

    // ── predecessor panel ────────────────────────────────────────────────────

    private void onAddPredecessor()
    {
        TaskBean centerTask = centerPanel.selectedTask();
        if (centerTask == null || centerTask.id() == null) return;

        List<TaskBean> current = collectFromTree(predPanel.treeView.getRoot());
        Set<Long> excluded = toIds(current);
        excluded.add(centerTask.id());

        pickTask("Add predecessor", excluded).ifPresent(pred ->
        {
            try { taskClient.addPredecessor(centerTask, pred); reloadSidePanels(centerTask); }
            catch (Exception e) { log.error("failed to add predecessor", e); showError("Add predecessor", e); }
        });
    }

    private void onEditPredecessor()
    {
        TreeItem<TaskBean> item = predPanel.selectedItem();
        if (item != null) editTaskName(item, () -> {
            TaskBean centerTask = centerPanel.selectedTask();
            if (centerTask != null) reloadSidePanels(centerTask);
        });
    }

    private void onDelPredecessor()
    {
        TaskBean centerTask = centerPanel.selectedTask();
        TreeItem<TaskBean> predItem = predPanel.selectedItem();
        if (centerTask == null || centerTask.id() == null || predItem == null) return;
        TaskBean pred = predItem.getValue();
        if (pred == null || pred.id() == null) return;

        boolean isDirect = predPanel.treeView.getRoot() != null
                && predPanel.treeView.getRoot().getChildren().contains(predItem);
        if (!isDirect) { showInfo("Remove predecessor", "Only direct predecessors can be removed here."); return; }

        try { taskClient.removePredecessor(centerTask, pred); reloadSidePanels(centerTask); }
        catch (Exception e) { log.error("failed to remove predecessor", e); showError("Remove predecessor", e); }
    }

    // ── successor panel ──────────────────────────────────────────────────────

    private void onAddSuccessor()
    {
        TaskBean centerTask = centerPanel.selectedTask();
        if (centerTask == null || centerTask.id() == null) return;

        List<TaskBean> current = collectFromTree(succPanel.treeView.getRoot());
        Set<Long> excluded = toIds(current);
        excluded.add(centerTask.id());

        pickTask("Add successor", excluded).ifPresent(succ ->
        {
            try { taskClient.addPredecessor(succ, centerTask); reloadSidePanels(centerTask); }
            catch (Exception e) { log.error("failed to add successor", e); showError("Add successor", e); }
        });
    }

    private void onEditSuccessor()
    {
        TreeItem<TaskBean> item = succPanel.selectedItem();
        if (item != null) editTaskName(item, () -> {
            TaskBean centerTask = centerPanel.selectedTask();
            if (centerTask != null) reloadSidePanels(centerTask);
        });
    }

    private void onDelSuccessor()
    {
        TaskBean centerTask = centerPanel.selectedTask();
        TreeItem<TaskBean> succItem = succPanel.selectedItem();
        if (centerTask == null || centerTask.id() == null || succItem == null) return;
        TaskBean succ = succItem.getValue();
        if (succ == null || succ.id() == null) return;

        boolean isDirect = succPanel.treeView.getRoot() != null
                && succPanel.treeView.getRoot().getChildren().contains(succItem);
        if (!isDirect) { showInfo("Remove successor", "Only direct successors can be removed here."); return; }

        try { taskClient.removePredecessor(succ, centerTask); reloadSidePanels(centerTask); }
        catch (Exception e) { log.error("failed to remove successor", e); showError("Remove successor", e); }
    }

    // ── manage groups ────────────────────────────────────────────────────────

    @FXML
    private void onManageGroups()
    {
        if (groupManagementDialog == null)
            groupManagementDialog = new de.ruu.app.pragma.fx.TaskGroupManagementDialog(taskGroupClient, taskGroupEditor, this::loadGroups);
        groupManagementDialog.showAndWait();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void reloadCurrentGroup()
    {
        TaskGroupBean group = cbGroups.valueProperty().get();
        if (group != null) loadGroup(group);
    }

    private void reloadSidePanels(TaskBean centerTask)
    {
        TreeItem<TaskBean> centerItem = centerPanel.selectedItem();
        if (centerItem != null && centerItem.getValue() != null
                && centerTask.id() != null
                && centerTask.id().equals(centerItem.getValue().id()))
            onCenterTaskSelected(centerItem);
    }

    private void clearSidePanels()
    {
        predPanel.treeView.setRoot(new TreeItem<>());
        succPanel.treeView.setRoot(new TreeItem<>());
    }

    private void clearInspector()
    {
        inspector.clearTask();
    }

    private void disableAll(boolean disabled)
    {
        predPanel  .setDisabled(disabled);
        centerPanel.setDisabled(disabled);
        succPanel  .setDisabled(disabled);
    }

    private void updateButtonStates()
    {
        TaskBean center    = centerPanel.selectedTask();
        boolean noCenterTask   = center == null;
        boolean noCenterSel    = centerPanel.selectedItem() == null;
        boolean noPredSelected = predPanel.selectedItem() == null;
        boolean noSuccSelected = succPanel.selectedItem() == null;

        centerPanel.btnAdd .setDisable(false);
        centerPanel.btnEdit.setDisable(noCenterSel);
        centerPanel.btnDel .setDisable(noCenterSel);

        predPanel.btnAdd .setDisable(noCenterTask);
        predPanel.btnEdit.setDisable(noPredSelected);
        predPanel.btnDel .setDisable(noCenterTask || noPredSelected);

        succPanel.btnAdd .setDisable(noCenterTask);
        succPanel.btnEdit.setDisable(noSuccSelected);
        succPanel.btnDel .setDisable(noCenterTask || noSuccSelected);
    }

    private void clearDirty()
    {
        inspector.clearDirty();
    }

    private boolean dirty()
    {
        return inspector.dirty();
    }

    private boolean confirmDiscardChanges()
    {
        return TaskUiSupport.confirmDiscardChanges(dirty());
    }

    private void editTaskName(TreeItem<TaskBean> item, Runnable onSuccess)
    {
        TaskBean task = item.getValue();
        if (task == null || task.id() == null) return;

        TextInputDialog dlg = new TextInputDialog(task.name());
        dlg.setTitle("Rename task");
        dlg.setHeaderText(null);
        dlg.setContentText("Name:");

        dlg.showAndWait().map(String::trim).filter(s -> !s.isEmpty() && !s.equals(task.name())).ifPresent(name ->
        {
            try
            {
                task.name(name);
                TaskBean updated = taskClient.update(task);
                item.setValue(updated);
                if (updated.id() != null) taskByIdCache.put(updated.id(), updated);
                onSuccess.run();
            }
            catch (Exception e) { log.error("failed to rename task", e); showError("Rename task", e); }
        });
    }

    private Optional<TaskBean> pickTask(String title, Set<Long> excludedIds)
    {
        List<TaskBean> choices = taskClient.findAll().stream()
                .filter(t -> t.id() != null && !excludedIds.contains(t.id()))
                .sorted(Comparator.<TaskBean, String>comparing(t -> t.taskGroup().name())
                                  .thenComparing(t -> t.name()))
                .toList();
        if (choices.isEmpty()) return Optional.empty();

        Function<TaskBean, String> label = t -> t.taskGroup().name() + " / " + t.name();

        TextFieldAutoCompleteClearableWithArrowButton<TaskBean> field =
                TextFieldAutoCompleteClearableWithArrowButtonBuilder.<TaskBean>create()
                        .items(choices)
                        .suggestionFilter((t, text) -> label.apply(t).toLowerCase().contains(text.toLowerCase()))
                        .comparator(Comparator.comparing(label))
                        .textProvider(label)
                        .prompt("Choose or type a task …")
                        .build();
        field.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(field, Priority.ALWAYS);

        Dialog<TaskBean> dlg = new Dialog<>();
        dlg.setTitle(title);
        dlg.setHeaderText(null);
        dlg.getDialogPane().setContent(field);
        dlg.getDialogPane().setPrefWidth(420);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.setResultConverter(bt -> bt == ButtonType.OK ? field.valueProperty().get() : null);

        return dlg.showAndWait().filter(t -> t != null);
    }

    // ── tree builders ─────────────────────────────────────────────────────────

    private TreeItem<TaskBean> buildSuperSubTree(List<TaskBean> tasks)
    {
        TreeItem<TaskBean>            root = new TreeItem<>();
        Map<Long, TreeItem<TaskBean>> byId = new HashMap<>();

        for (TaskBean task : tasks)
        {
            if (task.id() == null) continue;
            TreeItem<TaskBean> item = new TreeItem<>(task);
            item.setExpanded(true);
            byId.put(task.id(), item);
        }
        for (TaskBean task : tasks)
        {
            if (task.id() == null) continue;
            TreeItem<TaskBean> item     = byId.get(task.id());
            Long               parentId = task.parentTask().map(TaskBean::id).orElse(null);
            if (parentId != null && byId.containsKey(parentId))
                byId.get(parentId).getChildren().add(item);
            else
                root.getChildren().add(item);
        }
        sortTreeItems(root);
        return root;
    }

    private void sortTreeItems(TreeItem<TaskBean> parent)
    {
        Comparator<TreeItem<TaskBean>> byScheduledStart = Comparator.comparing(
                item -> item.getValue() == null ? null : item.getValue().scheduledStart().orElse(null),
                Comparator.nullsLast(Comparator.naturalOrder()));
        Comparator<TreeItem<TaskBean>> byName = Comparator.comparing(
                item -> item.getValue() == null ? "" : item.getValue().name().toLowerCase());
        Comparator<TreeItem<TaskBean>> byId = Comparator.comparing(
                item -> item.getValue() == null ? null : item.getValue().id(),
                Comparator.nullsLast(Comparator.naturalOrder()));
        parent.getChildren().sort(byScheduledStart.thenComparing(byName).thenComparing(byId));
        parent.getChildren().forEach(this::sortTreeItems);
    }

    private TreeItem<TaskBean> buildPredecessorNode(TaskBean task, Set<Long> visited)
    {
        TaskBean resolved = task.id() != null ? taskByIdCache.getOrDefault(task.id(), task) : task;
        TreeItem<TaskBean> item = new TreeItem<>(resolved);
        item.setExpanded(true);
        if (resolved.id() != null && !visited.contains(resolved.id()))
        {
            visited.add(resolved.id());
            try { resolved.predecessors().ifPresent(preds ->
                    preds.forEach(p -> item.getChildren().add(buildPredecessorNode(p, visited)))); }
            finally { visited.remove(resolved.id()); }
        }
        return item;
    }

    private TreeItem<TaskBean> buildSuccessorNode(TaskBean task, Set<Long> visited)
    {
        TaskBean resolved = task.id() != null ? taskByIdCache.getOrDefault(task.id(), task) : task;
        TreeItem<TaskBean> item = new TreeItem<>(resolved);
        item.setExpanded(true);
        if (resolved.id() != null && !visited.contains(resolved.id()))
        {
            visited.add(resolved.id());
            try { resolved.successors().ifPresent(succs ->
                    succs.forEach(s -> item.getChildren().add(buildSuccessorNode(s, visited)))); }
            finally { visited.remove(resolved.id()); }
        }
        return item;
    }

    private List<TaskBean> collectFromTree(TreeItem<TaskBean> root)
    {
        if (root == null) return List.of();
        List<TaskBean> result = new ArrayList<>();
        collectRecursive(root, result);
        return result;
    }

    private void collectRecursive(TreeItem<TaskBean> item, List<TaskBean> acc)
    {
        if (item.getValue() != null) acc.add(item.getValue());
        item.getChildren().forEach(child -> collectRecursive(child, acc));
    }

    private Set<Long> toIds(List<TaskBean> tasks)
    {
        Set<Long> ids = new HashSet<>();
        tasks.stream().filter(t -> t.id() != null).map(TaskBean::id).forEach(ids::add);
        return ids;
    }

    private static void embedPanel(AnchorPane pane, TaskHierarchyPanel panel)
    {
        javafx.scene.Node node = panel.root();
        AnchorPane.setTopAnchor   (node, 0.0);
        AnchorPane.setBottomAnchor(node, 0.0);
        AnchorPane.setLeftAnchor  (node, 0.0);
        AnchorPane.setRightAnchor (node, 0.0);
        pane.getChildren().add(node);
    }

    private void showError(String title, Exception e)
    {
        TaskUiSupport.showError(title, e);
    }

    private void showInfo(String title, String message)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
