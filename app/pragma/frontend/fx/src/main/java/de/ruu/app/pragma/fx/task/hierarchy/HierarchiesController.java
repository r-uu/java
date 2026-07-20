package de.ruu.app.pragma.fx.task.hierarchy;

import de.ruu.app.pragma.bean.TaskBean;
import de.ruu.app.pragma.bean.TaskGroupBean;
import de.ruu.app.pragma.client.TaskClient;
import de.ruu.app.pragma.client.TaskGroupClient;
import de.ruu.app.pragma.fx.task.edit.TaskEditor;
import de.ruu.app.pragma.fx.taskgroup.edit.TaskGroupEditor;
import de.ruu.lib.fx.comp.FXCController.DefaultFXCController;
import de.ruu.lib.fx.control.autocomplete.textfield.TextFieldAutoCompleteClearableWithArrowButton;
import de.ruu.lib.fx.control.autocomplete.textfield.TextFieldAutoCompleteClearableWithArrowButtonBuilder;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger log = LoggerFactory.getLogger(HierarchiesController.class);

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

    // ── three hierarchy panels ────────────────────────────────────────────────

    private TaskHierarchyPanel predPanel;
    private TaskHierarchyPanel centerPanel;
    private TaskHierarchyPanel succPanel;

    // ── injections ───────────────────────────────────────────────────────────

    @Inject private TaskGroupClient taskGroupClient;
    @Inject private TaskClient      taskClient;
    @Inject private Instance<TaskEditor> taskEditors;
    @Inject private TaskGroupEditor taskGroupEditor;

    private de.ruu.app.pragma.fx.TaskGroupManagementDialog groupManagementDialog;

    /** All tasks of the current group, keyed by ID; populated by loadGroup(). */
    private Map<Long, TaskBean> taskByIdCache = new HashMap<>();

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
        cbGroups.setMaxWidth(Double.MAX_VALUE);
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
                "Vorgänger hinzufügen", "Vorgänger bearbeiten", "Vorgänger-Verknüpfung entfernen",
                taskEditors.get());
        centerPanel = new TaskHierarchyPanel("super/sub tasks",    false,
                "Aufgabe hinzufügen",   "Aufgabe umbenennen",   "Aufgabe löschen",
                taskEditors.get());
        succPanel   = new TaskHierarchyPanel("successor tasks",    false,
                "Nachfolger hinzufügen","Nachfolger bearbeiten","Nachfolger-Verknüpfung entfernen",
                taskEditors.get());

        embedPanel(panePred,   predPanel);
        embedPanel(paneCenter, centerPanel);
        embedPanel(paneSucc,   succPanel);

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

        // side panel selections only update their own detail form
        predPanel.treeView.getSelectionModel().selectedItemProperty()
                 .addListener((obs, old, sel) -> predPanel.fillDetail(sel));
        succPanel.treeView.getSelectionModel().selectedItemProperty()
                 .addListener((obs, old, sel) -> succPanel.fillDetail(sel));

        // save buttons
        predPanel  .btnSave.setOnAction(e -> saveTask(predPanel));
        centerPanel.btnSave.setOnAction(e -> saveTask(centerPanel));
        succPanel  .btnSave.setOnAction(e -> saveTask(succPanel));

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
        lblStatus.setText("Verbinde …");
        Thread.ofVirtual().start(() ->
        {
            try
            {
                List<TaskGroupBean> groups = taskGroupClient.findAll();
                Platform.runLater(() ->
                {
                    cbGroups.items(groups);
                    if (!groups.isEmpty()) cbGroups.value(groups.get(0));
                    lblStatus.setText("");
                });
            }
            catch (Exception e)
            {
                log.error("failed to load groups", e);
                Platform.runLater(() -> lblStatus.setText("⚠ Verbindungsfehler — Server erreichbar?"));
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
                disableAll(false);
                updateButtonStates();
            });
        }
        catch (Exception e) { log.error("failed to load group {}", group.name(), e); }
    }

    private void onCenterTaskSelected(TreeItem<TaskBean> item)
    {
        clearSidePanels();

        if (item == null || item.getValue() == null)
        {
            centerPanel.fillDetail(null);
            updateButtonStates();
            return;
        }

        TaskBean task = item.getValue();
        centerPanel.fillDetail(item);

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
            predPanel.treeView.setRoot(predRoot);

            List<TaskBean> succs = cached.successors()
                .<List<TaskBean>>map(ArrayList::new)
                .orElseGet(() -> taskClient.findSuccessors(task));
            TreeItem<TaskBean> succRoot = new TreeItem<>();
            Set<Long> visitedSucc = new HashSet<>();
            visitedSucc.add(task.id());
            succs.forEach(s -> succRoot.getChildren().add(buildSuccessorNode(s, visitedSucc)));
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
            ButtonType btnRoot = new ButtonType("Root-Aufgabe");
            ButtonType btnSub  = new ButtonType("Unteraufgabe von \"" + selectedTask.name() + "\"");
            Alert choice = new Alert(Alert.AlertType.CONFIRMATION);
            choice.setTitle("Aufgabe hinzufügen");
            choice.setHeaderText("Wo soll die neue Aufgabe angelegt werden?");
            choice.getButtonTypes().setAll(btnRoot, btnSub, ButtonType.CANCEL);
            Optional<ButtonType> result = choice.showAndWait();
            if (result.isEmpty() || result.get() == ButtonType.CANCEL) return;
            createAsSub = result.get() == btnSub;
        }

        final TaskBean parent = createAsSub ? selectedTask : null;
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Aufgabe hinzufügen");
        dlg.setHeaderText(parent != null
                ? "Neue Unteraufgabe von \"" + parent.name() + "\""
                : "Neue Root-Aufgabe in Gruppe \"" + group.name() + "\"");
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
            catch (Exception e) { log.error("failed to create task", e); showError("Aufgabe anlegen", e); }
        });
    }

    private void onEditTask()
    {
        TreeItem<TaskBean> item = centerPanel.selectedItem();
        if (item == null) return;
        editTaskName(item, () -> {
            centerPanel.fillDetail(item);
            reloadCurrentGroup();
        });
    }

    private void onDelTask()
    {
        TaskBean task = centerPanel.selectedTask();
        if (task == null || task.id() == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Aufgabe \"" + task.name() + "\" löschen?", ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Aufgabe löschen");
        confirm.setHeaderText(null);
        if (confirm.showAndWait().filter(bt -> bt == ButtonType.OK).isPresent())
        {
            try { taskClient.delete(task); reloadCurrentGroup(); }
            catch (Exception e) { log.error("failed to delete task", e); showError("Aufgabe löschen", e); }
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

        pickTask("Vorgänger hinzufügen", excluded).ifPresent(pred ->
        {
            try { taskClient.addPredecessor(centerTask, pred); reloadSidePanels(centerTask); }
            catch (Exception e) { log.error("failed to add predecessor", e); showError("Vorgänger hinzufügen", e); }
        });
    }

    private void onEditPredecessor()
    {
        TreeItem<TaskBean> item = predPanel.selectedItem();
        if (item != null) editTaskName(item, () -> predPanel.fillDetail(item));
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
        if (!isDirect) { showInfo("Vorgänger entfernen", "Nur direkte Vorgänger können hier entfernt werden."); return; }

        try { taskClient.removePredecessor(centerTask, pred); reloadSidePanels(centerTask); }
        catch (Exception e) { log.error("failed to remove predecessor", e); showError("Vorgänger entfernen", e); }
    }

    // ── successor panel ──────────────────────────────────────────────────────

    private void onAddSuccessor()
    {
        TaskBean centerTask = centerPanel.selectedTask();
        if (centerTask == null || centerTask.id() == null) return;

        List<TaskBean> current = collectFromTree(succPanel.treeView.getRoot());
        Set<Long> excluded = toIds(current);
        excluded.add(centerTask.id());

        pickTask("Nachfolger hinzufügen", excluded).ifPresent(succ ->
        {
            try { taskClient.addPredecessor(succ, centerTask); reloadSidePanels(centerTask); }
            catch (Exception e) { log.error("failed to add successor", e); showError("Nachfolger hinzufügen", e); }
        });
    }

    private void onEditSuccessor()
    {
        TreeItem<TaskBean> item = succPanel.selectedItem();
        if (item != null) editTaskName(item, () -> succPanel.fillDetail(item));
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
        if (!isDirect) { showInfo("Nachfolger entfernen", "Nur direkte Nachfolger können hier entfernt werden."); return; }

        try { taskClient.removePredecessor(succ, centerTask); reloadSidePanels(centerTask); }
        catch (Exception e) { log.error("failed to remove successor", e); showError("Nachfolger entfernen", e); }
    }

    // ── save ──────────────────────────────────────────────────────────────────

    private void saveTask(TaskHierarchyPanel panel)
    {
        TreeItem<TaskBean> sel = panel.selectedItem();
        if (sel == null || sel.getValue() == null || sel.getValue().id() == null) return;

        TaskBean task = sel.getValue();
        try
        {
            panel.applyEditableFields(task);
            TaskBean updated = taskClient.update(task);
            panel.fillDetail(sel); // resets dirty
            sel.setValue(updated);
        }
        catch (Exception e) { log.error("failed to save task data for {}", task.name(), e); showError("Speichern", e); }
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
        predPanel.clearDetail();
        succPanel.clearDetail();
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

        centerPanel.btnSave.setDisable(noCenterSel);
        predPanel  .btnSave.setDisable(noPredSelected);
        succPanel  .btnSave.setDisable(noSuccSelected);
    }

    private void clearDirty()
    {
        predPanel.clearDirty();
        centerPanel.clearDirty();
        succPanel.clearDirty();
    }

    private boolean dirty()
    {
        return predPanel.dirtyProperty().get()
            || centerPanel.dirtyProperty().get()
            || succPanel  .dirtyProperty().get();
    }

    private boolean confirmDiscardChanges()
    {
        if (!dirty()) return true;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Es gibt ungespeicherte Änderungen. Wirklich verwerfen?",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Ungespeicherte Änderungen");
        confirm.setHeaderText(null);
        return confirm.showAndWait().filter(bt -> bt == ButtonType.OK).isPresent();
    }

    private void editTaskName(TreeItem<TaskBean> item, Runnable onSuccess)
    {
        TaskBean task = item.getValue();
        if (task == null || task.id() == null) return;

        TextInputDialog dlg = new TextInputDialog(task.name());
        dlg.setTitle("Aufgabe umbenennen");
        dlg.setHeaderText(null);
        dlg.setContentText("Name:");

        dlg.showAndWait().map(String::trim).filter(s -> !s.isEmpty() && !s.equals(task.name())).ifPresent(name ->
        {
            try
            {
                task.name(name);
                TaskBean updated = taskClient.update(task);
                item.setValue(updated);
                onSuccess.run();
            }
            catch (Exception e) { log.error("failed to rename task", e); showError("Aufgabe umbenennen", e); }
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
                        .prompt("Aufgabe wählen oder tippen …")
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
        parent.getChildren().sort(Comparator.comparing(
                item -> item.getValue() == null ? "" : item.getValue().name().toLowerCase()));
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
        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showInfo(String title, String message)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
