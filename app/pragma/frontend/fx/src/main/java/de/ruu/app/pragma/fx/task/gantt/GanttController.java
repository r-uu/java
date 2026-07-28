package de.ruu.app.pragma.fx.task.gantt;

import de.ruu.app.pragma.bean.TaskBean;
import de.ruu.app.pragma.bean.TaskGroupBean;
import de.ruu.app.pragma.client.TaskClient;
import de.ruu.app.pragma.client.TaskGroupClient;
import de.ruu.app.pragma.fx.TaskGroupManagementDialog;
import de.ruu.app.pragma.fx.task.TaskUiSupport;
import de.ruu.app.pragma.fx.task.edit.TaskEditor;
import de.ruu.app.pragma.fx.task.inspector.TaskInspectorSupport;
import de.ruu.app.pragma.fx.taskgroup.edit.TaskGroupEditor;
import de.ruu.lib.fx.FXUtil;
import de.ruu.lib.fx.comp.FXCController.DefaultFXCController;
import de.ruu.lib.fx.control.autocomplete.textfield.TextFieldAutoCompleteClearableWithArrowButton;
import de.ruu.lib.fx.control.autocomplete.textfield.TextFieldAutoCompleteClearableWithArrowButtonBuilder;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Dependent
class GanttController extends DefaultFXCController<Gantt, GanttService> implements GanttService
{
    private static final Logger log = LogManager.getLogger(GanttController.class);

    private static final DateTimeFormatter DE_FORMAT    = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DAY_FORMAT   = DateTimeFormatter.ofPattern("dd");
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MMM yyyy");
    private static final String DAY_FILL_STYLE =
        "-fx-background-color: #4a90e2; -fx-background-insets: 0;";
    private static final String DAY_FILL_SELECTED_STYLE =
        "-fx-background-color: #ff9f43; -fx-background-insets: 0; -fx-border-color: #8a4f00; -fx-border-width: 0.5;";

    // ── top bar ──────────────────────────────────────────────────────────────

    @FXML private VBox   vBxForGroup;
    @FXML private HBox   hBxForFilter;
    @FXML private Button btnManageGroups;
    @FXML private Button btnReload;
    @FXML private Label  lblStatus;

    @FXML private DatePicker dtPckrStart;
    @FXML private DatePicker dtPckrEnd;
    @FXML private Button     btnApply;
    @FXML private Button     btnInspectorToggle;

    private TaskGroupManagementDialog                                    groupManagementDialog;
		private TextFieldAutoCompleteClearableWithArrowButton<TaskGroupBean> tfaccTaskGroupSelection;


    // ── main table ───────────────────────────────────────────────────────────

    @FXML private TreeTableView<TaskBean> ttv;
    @FXML private BorderPane brdPaneMain;
    @FXML private VBox       vBxInspectorContainer;
    @FXML private Button     btnInspectorSave;

    // ── injections ───────────────────────────────────────────────────────────

    @Inject private TaskGroupClient taskGroupClient;
    @Inject private TaskClient      taskClient;
    @Inject private TaskGroupEditor taskGroupEditor;
    @Inject private TaskEditor      taskEditor;

    // ── state ────────────────────────────────────────────────────────────────

    private List<TaskBean> currentTasks = List.of();
    private TaskBean       selectedTask;
    private TaskBean       hoveredTask;
    private Set<Long>      overlappingPredecessorIds = Set.of();
    private Set<Long>      overlappingSuccessorIds   = Set.of();
    /** True while we are programmatically reverting a selection — prevents listener re-entry. */
    private boolean handlingNav = false;
    private TaskInspectorSupport inspector;

    // ── initialization ───────────────────────────────────────────────────────

    @Override @FXML protected void initialize()
    {
        tfaccTaskGroupSelection = TextFieldAutoCompleteClearableWithArrowButtonBuilder.<TaskGroupBean>create()
                .items(List.of())
                .suggestionFilter((g, text) -> g.name().toLowerCase().contains(text.toLowerCase()))
                .comparator(Comparator.comparing(TaskGroupBean::name))
                .textProvider(TaskGroupBean::name)
                .prompt("group …")
                .build();
        tfaccTaskGroupSelection.setMaxWidth(Double.MAX_VALUE);
        vBxForGroup.getChildren().add(tfaccTaskGroupSelection);
        tfaccTaskGroupSelection.valueProperty()
                .addListener((obs, old, sel) -> {
                    if (handlingNav || sel == null) return;
                    if (!confirmDiscardChanges()) {
                        handlingNav = true;
                        tfaccTaskGroupSelection.value(old);
                        handlingNav = false;
                        return;
                    }
                    inspector.clearDirty();
                    loadGroup(sel);
                });

        btnManageGroups.setOnAction(e -> onManageGroups());
        btnReload.setOnAction(e -> loadGroups());

        FXUtil.wrapInTitledBorder("group",  vBxForGroup);
        FXUtil.wrapInTitledBorder("filter", hBxForFilter);

        configureDatePicker(dtPckrStart);
        configureDatePicker(dtPckrEnd);
        dtPckrStart.setValue(LocalDate.of(LocalDate.now().getYear(), 1, 1));
        dtPckrEnd  .setValue(LocalDate.of(LocalDate.now().getYear(), 3, 31));

        btnApply.setOnAction(e -> reloadTable());

        ttv.setShowRoot(false);
        ttv.setRoot(new TreeItem<>());
        ttv.setColumnResizePolicy(TreeTableView.UNCONSTRAINED_RESIZE_POLICY);
        // Clear all hover markers once the pointer leaves the table completely.
        // (Clearing in cell-level mouse-exit is unstable because refresh() recreates cells.)
        ttv.setOnMouseExited(evt -> {
            if (hoveredTask != null)
            {
                clearHoveredTask();
                ttv.refresh();
            }
        });
        ttv.getSelectionModel().selectedItemProperty()
           .addListener((obs, old, sel) -> {
               if (handlingNav) return;
               if (!confirmDiscardChanges()) {
                   handlingNav = true;
                   ttv.getSelectionModel().select(old);
                   handlingNav = false;
                   return;
               }
               inspector.clearDirty();
               onTaskSelected(sel);
           });
        inspector = new TaskInspectorSupport(
           brdPaneMain,
           vBxInspectorContainer,
           btnInspectorToggle,
           btnInspectorSave,
           taskEditor.localRoot(),
           taskEditor.service(),
           taskClient::update,
           updated -> {
               currentTasks = new ArrayList<>(currentTasks);
               currentTasks.replaceAll(t -> t.id() != null && t.id().equals(updated.id()) ? updated : t);
               TreeItem<TaskBean> selected = ttv.getSelectionModel().getSelectedItem();
               if (selected != null && selected.getValue() != null && updated.id() != null
                   && updated.id().equals(selected.getValue().id()))
                   selected.setValue(updated);
               selectedTask = updated;
               reloadTable();
           },
           e -> {
               log.error("failed to save task", e);
               TaskUiSupport.showError("Save", e);
           });
        inspector.initialize();

        loadGroups();
    }

    @FXML
    private void onManageGroups()
    {
        if (groupManagementDialog == null)
            groupManagementDialog = new TaskGroupManagementDialog(taskGroupClient, taskGroupEditor, this::loadGroups);
        groupManagementDialog.showAndWait();
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
                    tfaccTaskGroupSelection.items(groups);
                    if (!groups.isEmpty())
                    {
                        tfaccTaskGroupSelection.value(groups.get(0));
                        TaskUiSupport.clearStatus(lblStatus);
                    }
                    else
                    {
                        currentTasks = List.of();
                        selectedTask = null;
                        ttv.setRoot(new TreeItem<>());
                        ttv.getColumns().clear();
                        inspector.clearTask();
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
            currentTasks = taskClient.findGroupTasksWithRelated(group);
            Platform.runLater(this::reloadTable);
            if (lblStatus != null)
            {
                if (currentTasks.isEmpty()) lblStatus.setText("[INFO] Group has no tasks.");
                else lblStatus.setText(currentTasks.size() + " tasks");
            }
        }
        catch (Exception e) { log.error("failed to load group {}", group.name(), e); }
    }

    private void reloadTable()
    {
        LocalDate start = dtPckrStart.getValue();
        LocalDate end   = dtPckrEnd  .getValue();
        if (start == null || end == null || !end.isAfter(start)) return;
        clearHoveredTask();
        Long selectedId = selectedTask != null ? selectedTask.id() : null;

        ttv.getColumns().clear();
        ttv.getRoot().getChildren().clear();

        // Task name column (fixed)
        TreeTableColumn<TaskBean, String> nameCol = new TreeTableColumn<>("Task");
        nameCol.setPrefWidth(200);
        nameCol.setMinWidth(100);
        nameCol.setResizable(true);
        nameCol.setStyle("-fx-font-weight: normal;");
        nameCol.setCellValueFactory(cdf ->
                new SimpleStringProperty(cdf.getValue().getValue() == null
                        ? "" : cdf.getValue().getValue().name()));
        ttv.getColumns().add(nameCol);

        // Nested month → day columns (analogous to jeerah GanttTableController)
        LocalDate current = start;
        while (!current.isAfter(end))
        {
            YearMonth month = YearMonth.from(current);

            TreeTableColumn<TaskBean, String> monthCol = new TreeTableColumn<>(MONTH_FORMAT.format(current));
            monthCol.setStyle("-fx-font-weight: normal; -fx-alignment: center;");
            monthCol.setResizable(false);
            monthCol.setReorderable(false);
            monthCol.setSortable(false);

            while (!current.isAfter(end) && YearMonth.from(current).equals(month))
            {
                final LocalDate date = current;

                TreeTableColumn<TaskBean, String> dayCol = new TreeTableColumn<>(DAY_FORMAT.format(date));
                dayCol.setPrefWidth(24);
                dayCol.setMinWidth(24);
                dayCol.setMaxWidth(24);
                dayCol.setResizable(false);
                dayCol.setReorderable(false);
                dayCol.setSortable(false);
                dayCol.setStyle("-fx-font-weight: normal; -fx-alignment: center;");

                dayCol.setCellValueFactory(cdf -> {
                    TaskBean task = cdf.getValue().getValue();
                    if (task == null) return new SimpleStringProperty("");
                    LocalDate ps = task.scheduledStart ().orElse(null);
                    LocalDate pe = task.scheduledFinish().orElse(null);
                    // We pass a non-empty marker string for "task covers this day".
                    // updateItem(...) below does not check for a specific value; it only checks empty vs non-empty.
                    if (ps != null && pe != null && !date.isBefore(ps) && !date.isAfter(pe))
                        return new SimpleStringProperty("filled");
                    return new SimpleStringProperty("");
                });

                dayCol.setCellFactory(col -> new TreeTableCell<>()
                {
                    @Override protected void updateItem(String item, boolean empty)
                    {
                        super.updateItem(item, empty);
                        if (empty || item == null || item.isEmpty())
                        {
                            setText(null);
                            setGraphic(null);
                            // Leaving a task bar (blue cell) into a non-bar cell should remove markers.
                            setOnMouseEntered(evt -> {
                                if (hoveredTask != null)
                                {
                                    clearHoveredTask();
                                    ttv.refresh();
                                }
                            });
                            setOnMouseExited(null);
                            setStyle("");
                        }
                        else
                        {
                            // Any non-empty item means: this day is covered by the row's task bar.
                            setText(null);
                            setAlignment(Pos.CENTER);
                            setOnMouseEntered(evt -> {
                                TaskBean rowTask = getTreeTableRow() != null ? getTreeTableRow().getItem() : null;
                                if (rowTask != null && rowTask != hoveredTask)
                                {
                                    setHoveredTask(rowTask);
                                    ttv.refresh();
                                }
                            });
                            setOnMouseExited(null);
                            String relationMarker = relationMarkerFor(
                                getTreeTableRow() != null ? getTreeTableRow().getItem() : null, date);
                            boolean rowSelected = getTreeTableRow() != null && getTreeTableRow().isSelected();
                            setGraphic(relationMarker == null ? null : createRelationMarker(relationMarker));
                            setStyle(rowSelected ? DAY_FILL_SELECTED_STYLE : DAY_FILL_STYLE);
                        }
                    }
                });

                monthCol.getColumns().add(dayCol);
                current = current.plusDays(1);
            }

            ttv.getColumns().add(monthCol);
        }

        TreeItem<TaskBean> root = buildSuperSubTree(currentTasks);
        ttv.setRoot(root);
        if (selectedId != null)
            findById(root, selectedId).ifPresent(item -> ttv.getSelectionModel().select(item));
    }

    private void onTaskSelected(TreeItem<TaskBean> sel)
    {
        if (sel == null || sel.getValue() == null)
        {
            selectedTask = null;
            refreshOverlapCache();
            inspector.clearTask();
            ttv.refresh();
            return;
        }
        TaskBean task = sel.getValue();
        selectedTask = task;
        refreshOverlapCache();
        inspector.showTask(task);
        ttv.refresh();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void setHoveredTask(TaskBean task)
    {
        // Cache only overlapping relations of the currently hovered base task.
        // During rendering we check each row against these ID sets and show p/s on matching bars.
        hoveredTask = task;
        refreshOverlapCache();
    }

    private void clearHoveredTask()
    {
        hoveredTask = null;
        refreshOverlapCache();
    }

    private void refreshOverlapCache()
    {
        TaskBean baseTask = hoveredTask != null ? hoveredTask : selectedTask;
        if (baseTask == null)
        {
            overlappingPredecessorIds = Set.of();
            overlappingSuccessorIds   = Set.of();
            return;
        }
        overlappingPredecessorIds = relatedOverlapIds(baseTask, baseTask.predecessors());
        overlappingSuccessorIds   = relatedOverlapIds(baseTask, baseTask.successors());
    }

    private Set<Long> relatedOverlapIds(TaskBean baseTask, Optional<Set<TaskBean>> relatedTasks)
    {
        Set<Long> result = new HashSet<>();
        relatedTasks.ifPresent(tasks -> tasks.stream()
            .filter(related -> overlaps(baseTask, related))
            .map(TaskBean::id)
            .filter(Objects::nonNull)
            .forEach(result::add));
        return result;
    }

    private String relationMarkerFor(TaskBean rowTask, LocalDate day)
    {
        // A marker is row- and day-scoped:
        // - p: this row's task is an overlapping predecessor of the currently hovered task
        // - s: this row's task is an overlapping successor of the currently hovered task
        // and the concrete day must also be inside the hovered task interval.
        // This restricts markers to the real day-level intersection, not the whole related bar.
        if (rowTask == null) return null;
        Long rowTaskId = rowTask.id();
        if (rowTaskId == null) return null;
        TaskBean baseTask = hoveredTask != null ? hoveredTask : selectedTask;
        if (!dayInTaskRange(baseTask, day)) return null;
        if (overlappingPredecessorIds.contains(rowTaskId)) return "p";
        if (overlappingSuccessorIds  .contains(rowTaskId)) return "s";
        return null;
    }

    private Label createRelationMarker(String text)
    {
        Label marker = new Label(text);
        marker.setStyle("-fx-font-size: 8px; -fx-font-weight: bold; -fx-text-fill: #000000;"
            + "-fx-background-color: #f1c40f; -fx-background-radius: 2; -fx-padding: 0 2 0 2;");
        return marker;
    }

    private boolean overlaps(TaskBean left, TaskBean right)
    {
        LocalDate leftStart  = left.scheduledStart ().orElse(null);
        LocalDate leftEnd    = left.scheduledFinish().orElse(null);
        LocalDate rightStart = right.scheduledStart ().orElse(null);
        LocalDate rightEnd   = right.scheduledFinish().orElse(null);
        if (leftStart == null || leftEnd == null || rightStart == null || rightEnd == null) return false;
        return !leftEnd.isBefore(rightStart) && !rightEnd.isBefore(leftStart);
    }

    private boolean dayInTaskRange(TaskBean task, LocalDate day)
    {
        if (task == null || day == null) return false;
        LocalDate start = task.scheduledStart ().orElse(null);
        LocalDate end   = task.scheduledFinish().orElse(null);
        if (start == null || end == null) return false;
        return !day.isBefore(start) && !day.isAfter(end);
    }

    private boolean confirmDiscardChanges()
    {
        return TaskUiSupport.confirmDiscardChanges(inspector.dirty());
    }

    private void configureDatePicker(DatePicker dp)
    {
        dp.setConverter(new StringConverter<>()
        {
            @Override public String toString(LocalDate d) { return d != null ? DE_FORMAT.format(d) : ""; }
            @Override public LocalDate fromString(String s)
            {
                try   { return s != null && !s.isEmpty() ? LocalDate.parse(s, DE_FORMAT) : null; }
                catch (DateTimeParseException e) { return null; }
            }
        });
    }

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

    private Optional<TreeItem<TaskBean>> findById(TreeItem<TaskBean> root, Long id)
    {
        if (root == null || id == null) return Optional.empty();
        for (TreeItem<TaskBean> child : root.getChildren())
        {
            TaskBean value = child.getValue();
            if (value != null && id.equals(value.id())) return Optional.of(child);
            Optional<TreeItem<TaskBean>> nested = findById(child, id);
            if (nested.isPresent()) return nested;
        }
        return Optional.empty();
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

}
