package de.ruu.app.pragma.fx.task.gantt;

import de.ruu.app.pragma.bean.TaskBean;
import de.ruu.app.pragma.bean.TaskGroupBean;
import de.ruu.app.pragma.client.TaskClient;
import de.ruu.app.pragma.client.TaskGroupClient;
import de.ruu.app.pragma.fx.TaskGroupManagementDialog;
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

    // ── top bar ──────────────────────────────────────────────────────────────

    @FXML private VBox   vBxForGroup;
    @FXML private HBox   hBxForFilter;
    @FXML private Button btnManageGroups;
    @FXML private Button btnReload;
    @FXML private Label  lblStatus;

    @FXML private DatePicker dtPckrStart;
    @FXML private DatePicker dtPckrEnd;
    @FXML private Button     btnApply;

    private TaskGroupManagementDialog                                    groupManagementDialog;
		private TextFieldAutoCompleteClearableWithArrowButton<TaskGroupBean> tfaccTaskGroupSelection;


    // ── main table ───────────────────────────────────────────────────────────

    @FXML private TreeTableView<TaskBean> ttv;

    // ── detail / edit area ───────────────────────────────────────────────────

    @FXML private DatePicker dtPckrTaskStart;
    @FXML private DatePicker dtPckrTaskEnd;
    @FXML private Button     btnSaveDates;

    // ── injections ───────────────────────────────────────────────────────────

    @Inject private TaskGroupClient taskGroupClient;
    @Inject private TaskClient      taskClient;
    @Inject private TaskGroupEditor taskGroupEditor;

    // ── state ────────────────────────────────────────────────────────────────

    private List<TaskBean> currentTasks = List.of();
    private TaskBean       hoveredTask;
    private Set<Long>      overlappingPredecessorIds = Set.of();
    private Set<Long>      overlappingSuccessorIds   = Set.of();

    /** True when a user-initiated field change has not yet been saved. */
    private boolean dirty       = false;
    /** True while we are programmatically filling form fields — suppresses dirty tracking. */
    private boolean updating    = false;
    /** True while we are programmatically reverting a selection — prevents listener re-entry. */
    private boolean handlingNav = false;

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
                    dirty = false;
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
               dirty = false;
               onTaskSelected(sel);
           });

        btnSaveDates.setDisable(true);
        btnSaveDates.setOnAction(e -> saveDates());

        dtPckrTaskStart.valueProperty().addListener((obs, o, n) -> { if (!updating) dirty = true; });
        dtPckrTaskEnd  .valueProperty().addListener((obs, o, n) -> { if (!updating) dirty = true; });

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
        lblStatus.setText("Connecting ...");
        Thread.ofVirtual().start(() ->
        {
            try
            {
                List<TaskGroupBean> groups = taskGroupClient.findAll();
                Platform.runLater(() ->
                {
                    tfaccTaskGroupSelection.items(groups);
                    if (!groups.isEmpty()) tfaccTaskGroupSelection.value(groups.get(0));
                    lblStatus.setText("");
                });
            }
            catch (Exception e)
            {
                log.error("failed to load groups", e);
                Platform.runLater(() -> lblStatus.setText("[WARN] Connection error - is the server reachable?"));
            }
        });
    }

    private void loadGroup(TaskGroupBean group)
    {
        try
        {
            currentTasks = taskClient.findGroupTasksWithRelated(group);
            Platform.runLater(this::reloadTable);
        }
        catch (Exception e) { log.error("failed to load group {}", group.name(), e); }
    }

    private void reloadTable()
    {
        LocalDate start = dtPckrStart.getValue();
        LocalDate end   = dtPckrEnd  .getValue();
        if (start == null || end == null || !end.isAfter(start)) return;
        clearHoveredTask();

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
                            setGraphic(relationMarker == null ? null : createRelationMarker(relationMarker));
                            setStyle("-fx-background-color: #4a90e2;");
                        }
                    }
                });

                monthCol.getColumns().add(dayCol);
                current = current.plusDays(1);
            }

            ttv.getColumns().add(monthCol);
        }

        // Build tree from flat list using parentTask references
        Map<Long, TreeItem<TaskBean>> byId = new HashMap<>();
        for (TaskBean t : currentTasks)
        {
            if (t.id() == null) continue;
            TreeItem<TaskBean> item = new TreeItem<>(t);
            item.setExpanded(true);
            byId.put(t.id(), item);
        }
        for (TaskBean t : currentTasks)
        {
            if (t.id() == null) continue;
            TreeItem<TaskBean> item     = byId.get(t.id());
            Long               parentId = t.parentTask().map(TaskBean::id).orElse(null);
            if (parentId != null && byId.containsKey(parentId))
                byId.get(parentId).getChildren().add(item);
            else
                ttv.getRoot().getChildren().add(item);
        }
    }

    private void onTaskSelected(TreeItem<TaskBean> sel)
    {
        updating = true;
        try
        {
            if (sel == null || sel.getValue() == null)
            {
                dtPckrTaskStart.setValue(null);
                dtPckrTaskEnd  .setValue(null);
                btnSaveDates.setDisable(true);
                return;
            }
            TaskBean task = sel.getValue();
            dtPckrTaskStart.setValue(task.scheduledStart().orElse(null));
            dtPckrTaskEnd  .setValue(task.scheduledFinish()  .orElse(null));
            btnSaveDates.setDisable(task.id() == null);
        }
        finally
        {
            updating = false;
            dirty    = false;
        }
    }

    private void saveDates()
    {
        TreeItem<TaskBean> sel = ttv.getSelectionModel().getSelectedItem();
        if (sel == null || sel.getValue() == null || sel.getValue().id() == null) return;

        TaskBean task = sel.getValue();
        task.scheduledStart(dtPckrTaskStart.getValue());
        task.scheduledFinish(dtPckrTaskEnd  .getValue());

        try
        {
            TaskBean updated = taskClient.update(task);
            dirty = false;
            currentTasks = new ArrayList<>(currentTasks);
            currentTasks.replaceAll(t -> t.id() != null && t.id().equals(updated.id()) ? updated : t);
            sel.setValue(updated);
            reloadTable();
        }
        catch (Exception e)
        {
            log.error("failed to save dates", e);
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
            alert.setTitle("Save date fields");
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void setHoveredTask(TaskBean task)
    {
        // Cache only overlapping relations of the currently hovered base task.
        // During rendering we check each row against these ID sets and show p/s on matching bars.
        hoveredTask = task;
        overlappingPredecessorIds = relatedOverlapIds(task, task.predecessors());
        overlappingSuccessorIds   = relatedOverlapIds(task, task.successors());
    }

    private void clearHoveredTask()
    {
        hoveredTask = null;
        overlappingPredecessorIds = Set.of();
        overlappingSuccessorIds   = Set.of();
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
        if (!dayInTaskRange(hoveredTask, day)) return null;
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
        if (!dirty) return true;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "There are unsaved changes. Discard them?",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Unsaved changes");
        confirm.setHeaderText(null);
        return confirm.showAndWait().filter(bt -> bt == ButtonType.OK).isPresent();
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

}
