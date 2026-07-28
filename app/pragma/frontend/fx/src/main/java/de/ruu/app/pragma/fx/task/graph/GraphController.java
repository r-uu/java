package de.ruu.app.pragma.fx.task.graph;

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
import javafx.animation.PauseTransition;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Duration;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@Dependent
class GraphController extends DefaultFXCController<Graph, GraphService> implements GraphService
{
    private static final Logger log = LogManager.getLogger(GraphController.class);

    private static final double NODE_WIDTH  = 160;
    private static final double NODE_HEIGHT =  60;
    private static final double H_GAP       =  40;
    private static final double PAD         = GraphLayout.PAD;
    private static final double ARC         =  12;
    private static final double ARROW_LEN   =  10;
    private static final double ARROW_ANG   =   0.4; // radians half-angle of arrowhead
    private static final double GRID        =  20;   // snap-to-grid resolution in pixels
    private static final double STEP        = GraphLayout.STEP;
    private static final double TIMELINE_Y  =  28;
    private static final double TASKS_TOP_Y =  20;

    private static final Color COLOR_PRED_SUCC    = Color.web("#9999bb");
    private static final Color COLOR_PARENT_CHILD = Color.web("#4a9a4a");

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("MMM yyyy");
    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("dd.MM.");

    @FXML private HBox   cbGroupsContainer;
    @FXML private Button btnManageGroups;
    @FXML private Button btnReload;
    @FXML private ComboBox<String> cbTimelineGranularity;
    @FXML private Button btnCenterToday;
    @FXML private Label  lblStatus;
    private TextFieldAutoCompleteClearableWithArrowButton<TaskGroupBean> cbGroups;
    @FXML private ScrollPane timelineScroll;
    @FXML private ScrollPane graphContainer;
    @FXML private Button     btnSaveLayout;
    @FXML private Button     btnLoadLayout;
    @FXML private BorderPane brdPaneMain;
    @FXML private VBox       vBxInspectorContainer;
    @FXML private Button     btnInspectorToggle;
    @FXML private Button     btnInspectorSave;

    @Inject private TaskGroupClient taskGroupClient;
    @Inject private TaskClient      taskClient;
    @Inject private TaskGroupEditor taskGroupEditor;
    @Inject private TaskEditor      taskEditor;

    /** Task-ID → node; populated after each group load, used for save/load layout. */
    private Map<Long, Group> currentNodeById = new HashMap<>();
    /** Task-ID → task data of current group; kept in sync for incremental inspector saves. */
    private Map<Long, TaskBean> currentTaskById = new HashMap<>();

    private de.ruu.app.pragma.fx.TaskGroupManagementDialog groupManagementDialog;

    private File                      lastLayoutFile;
    private Scale                     graphScale         = new Scale(1, 1, 0, 0);
    private EventHandler<KeyEvent>    keyZoomHandler;
    private TaskInspectorSupport      inspector;
    private TaskGroupBean             currentGroup;
    private Long                      selectedTaskId;
    private boolean                   handlingNav;
    private GraphTimeline.Granularity timelineGranularity = GraphTimeline.Granularity.WEEK;
    private GraphTimeline.Scale       currentScale;

    @Override
    @FXML
    protected void initialize()
    {
        cbGroups = TextFieldAutoCompleteClearableWithArrowButtonBuilder.<TaskGroupBean>create()
                .items(List.of())
                .suggestionFilter((g, text) -> g.name().toLowerCase().contains(text.toLowerCase()))
                .comparator(Comparator.comparing(TaskGroupBean::name))
                .textProvider(TaskGroupBean::name)
                .prompt("group …")
                .build();
        cbGroups.setMaxWidth(Double.MAX_VALUE);
        cbGroupsContainer.getChildren().add(cbGroups);
        cbTimelineGranularity.getItems().setAll("Day", "Week", "Month");
        cbTimelineGranularity.setValue("Week");
        cbTimelineGranularity.valueProperty().addListener((obs, old, value) -> {
            if (value == null) return;
            timelineGranularity = switch (value)
            {
                case "Day" -> GraphTimeline.Granularity.DAY;
                case "Month" -> GraphTimeline.Granularity.MONTH;
                default -> GraphTimeline.Granularity.WEEK;
            };
            if (currentGroup != null) loadGroup(currentGroup);
        });
        cbGroups.valueProperty()
                .addListener((obs, old, sel) -> {
                    if (handlingNav || sel == null) return;
                    if (!confirmDiscardChanges()) {
                        handlingNav = true;
                        cbGroups.value(old);
                        handlingNav = false;
                        return;
                    }
                    inspector.clearDirty();
                    loadGroup(sel);
                });
        btnManageGroups.setOnAction(e -> onManageGroups());
        btnReload.setOnAction(e -> loadGroups());
        btnCenterToday.setOnAction(e -> centerOnDate(LocalDate.now()));
        btnSaveLayout.setDisable(true);
        btnLoadLayout.setDisable(true);
        btnCenterToday.setDisable(true);
        timelineScroll.hvalueProperty().bindBidirectional(graphContainer.hvalueProperty());
        inspector = new TaskInspectorSupport(
            brdPaneMain,
            vBxInspectorContainer,
            btnInspectorToggle,
            btnInspectorSave,
            taskEditor.localRoot(),
            taskEditor.service(),
            taskClient::update,
            updated -> {
                TaskBean previous = updated.id() == null ? null : currentTaskById.get(updated.id());
                selectedTaskId = updated.id();
                if (updated.id() != null) currentTaskById.put(updated.id(), updated);
                if (hasStructuralRelationChanges(previous, updated) && currentGroup != null)
                {
                    loadGroup(currentGroup);
                    return;
                }
                updateNodeContent(updated);
                if (updated.id() != null) updateSelectionStyles();
            },
            e -> {
                log.error("failed to save task", e);
                TaskUiSupport.showError("Save", e);
            });
        inspector.initialize();

        graphContainer.sceneProperty().addListener((obs, oldScene, newScene) ->
        {
            if (oldScene != null)
            {
                if (keyZoomHandler    != null) oldScene.removeEventFilter(KeyEvent.KEY_PRESSED, keyZoomHandler);
            }
            if (newScene != null)
            {
                // Intentional UX: mouse wheel is reserved for ScrollPane navigation.
                // Zoom stays available via Ctrl +/- to avoid accidental scale changes.
                keyZoomHandler = event ->
                {
                    if (!event.isControlDown()) return;
                    KeyCode code = event.getCode();
                    if      (code == KeyCode.PLUS  || code == KeyCode.EQUALS || code == KeyCode.ADD) applyZoom(1.1);
                    else if (code == KeyCode.MINUS || code == KeyCode.SUBTRACT)                      applyZoom(1.0 / 1.1);
                    else return;
                    event.consume();
                };
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, keyZoomHandler);
            }
        });

        loadGroups();
    }

    @FXML
    private void onManageGroups()
    {
        if (groupManagementDialog == null)
            groupManagementDialog = new de.ruu.app.pragma.fx.TaskGroupManagementDialog(taskGroupClient, taskGroupEditor, this::loadGroups);
        groupManagementDialog.showAndWait();
    }

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
                        graphContainer.setContent(new Pane());
                        currentTaskById.clear();
                        currentNodeById.clear();
                        selectedTaskId = null;
                        currentScale = null;
                        btnSaveLayout.setDisable(true);
                        btnLoadLayout.setDisable(true);
                        btnCenterToday.setDisable(true);
                        timelineScroll.setContent(new Pane());
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
        currentGroup = group;
        if (lblStatus != null) lblStatus.setText("Loading tasks ...");
        Thread.ofVirtual().start(() ->
        {
            try
            {
                List<TaskBean> tasks = taskClient.findGroupTasksWithRelated(group);
                String status = tasks.isEmpty()
                    ? "[INFO] Group has no tasks."
                    : tasks.size() + " tasks  |  wheel = scroll, Ctrl +/- = zoom";
                Platform.runLater(() ->
                {
                    if (!isCurrentGroup(group)) return;
                    if (lblStatus != null) lblStatus.setText(status);
                    PauseTransition deferBuild = new PauseTransition(Duration.millis(10));
                    deferBuild.setOnFinished(e ->
                    {
                        if (!isCurrentGroup(group)) return;
                        buildGraph(tasks);
                    });
                    deferBuild.play();
                });
            }
            catch (Exception e)
            {
                log.error("failed to load group {}", group.name(), e);
                Platform.runLater(() ->
                {
                    if (!isCurrentGroup(group)) return;
                    if (lblStatus != null) lblStatus.setText("Error: " + e.getMessage());
                    TaskUiSupport.showError("Load group", e);
                });
            }
        });
    }

    private boolean isCurrentGroup(TaskGroupBean group)
    {
        if (group == null || currentGroup == null) return false;
        if (group.id() != null && currentGroup.id() != null) return group.id().equals(currentGroup.id());
        return group.name().equals(currentGroup.name());
    }

    private void buildGraph(List<TaskBean> tasks)
    {
        Pane canvas = new Pane();
        canvas.setStyle("-fx-background-color: #1e1e2e;");

        Map<Long, TaskBean> byId     = new HashMap<>();
        Map<Long, TaskBean> allById  = new HashMap<>();
        Map<Long, Group>    nodeById = new HashMap<>();
        List<EdgeSpec>      edges    = new ArrayList<>();
        Set<String>         addedEdgeKeys = new HashSet<>();

        for (TaskBean task : tasks)
            if (task.id() != null) byId.put(task.id(), task);
        allById.putAll(byId);
        currentTaskById = new HashMap<>(byId);

        for (TaskBean task : tasks)
        {
            if (task.id() == null) continue;
            Group node = createNode(task);
            nodeById.put(task.id(), node);
            canvas.getChildren().add(node);
        }

        // edges from predecessors — also creates ghost nodes for cross-group predecessors
        for (TaskBean task : tasks)
        {
            if (task.id() == null) continue;
            task.predecessors().ifPresent(preds ->
            {
                for (TaskBean pred : preds)
                {
                    if (pred.id() == null) continue;
                    Group from = nodeById.get(pred.id());
                    Group to   = nodeById.get(task.id());
                    if (from == null)
                    {
                        from = createNode(pred);
                        nodeById.put(pred.id(), from);
                        canvas.getChildren().add(from);
                        allById.putIfAbsent(pred.id(), pred);
                    }
                    if (to != null && addedEdgeKeys.add(pred.id() + "-" + task.id()))
                        edges.add(new EdgeSpec(from, to, EdgeType.PRED_SUCC));
                }
            });
        }

        // edges from sub-task (parent → child) relationships
        for (TaskBean task : tasks)
        {
            if (task.id() == null) continue;
            task.subTasks().ifPresent(subs ->
            {
                for (TaskBean sub : subs)
                {
                    if (sub.id() == null) continue;
                    Group from = nodeById.get(task.id());
                    Group to   = nodeById.get(sub.id());
                    if (from != null && to != null && addedEdgeKeys.add(task.id() + "-" + sub.id()))
                        edges.add(new EdgeSpec(from, to, EdgeType.PARENT_CHILD));
                }
            });
        }

        // edges from successors — also creates ghost nodes for cross-group successors
        for (TaskBean task : tasks)
        {
            if (task.id() == null) continue;
            task.successors().ifPresent(succs ->
            {
                for (TaskBean succ : succs)
                {
                    if (succ.id() == null) continue;
                    Group from = nodeById.get(task.id());
                    Group to   = nodeById.get(succ.id());
                    if (to == null)
                    {
                        to = createNode(succ);
                        nodeById.put(succ.id(), to);
                        canvas.getChildren().add(to);
                        allById.putIfAbsent(succ.id(), succ);
                    }
                    if (from != null && addedEdgeKeys.add(task.id() + "-" + succ.id()))
                        edges.add(new EdgeSpec(from, to, EdgeType.PRED_SUCC));
                }
            });
        }

        TimeScale scale = applyLayout(nodeById, allById);
        currentScale = scale.toTimelineScale();
        Pane timelineCanvas = new Pane();
        timelineCanvas.setStyle("-fx-background-color: #1e1e2e;");
        drawTimeline(timelineCanvas, scale);
        double timelineWidth = Math.max(scale.axisEndX() + PAD, dateX(scale.maxDate(), scale) + PAD);
        timelineCanvas.setMinWidth(timelineWidth);
        timelineCanvas.setPrefWidth(timelineWidth);
        timelineCanvas.setMaxWidth(timelineWidth);
        timelineScroll.setContent(new Group(timelineCanvas));

        // add edges behind nodes
        for (EdgeSpec spec : edges)
        {
            Color color = spec.type() == EdgeType.PARENT_CHILD ? COLOR_PARENT_CHILD : COLOR_PRED_SUCC;
            canvas.getChildren().addAll(0, createArrow(spec.from(), spec.to(), color));
        }

        graphContainer.setContent(new Group(canvas));
        addZoomSupport(canvas);

        currentNodeById = new HashMap<>(nodeById);
        updateSelectionStyles();
        btnCenterToday.setDisable(currentScale == null || currentNodeById.isEmpty());
        if (selectedTaskId != null && allById.containsKey(selectedTaskId))
            inspector.showTask(allById.get(selectedTaskId));
        else
        {
            selectedTaskId = null;
            inspector.clearTask();
        }
        btnSaveLayout.setDisable(currentNodeById.isEmpty());
        btnLoadLayout.setDisable(currentNodeById.isEmpty());
    }

    // ── Node creation ─────────────────────────────────────────────────────────

    private Group createNode(TaskBean task)
    {
        Rectangle rect = new Rectangle(NODE_WIDTH, NODE_HEIGHT);
        rect.setArcWidth (ARC);
        rect.setArcHeight(ARC);
        rect.setFill     (Color.web("#3c5a8a"));
        rect.setStroke   (Color.web("#6699cc"));
        rect.setStrokeWidth(1.5);

        Label lName = new Label(task.name());
        lName.setTextFill(Color.WHITE);
        lName.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        lName.setMaxWidth(NODE_WIDTH - 10);

        Label lDates = new Label(dateText(task));
        lDates.setTextFill(Color.LIGHTGRAY);
        lDates.setStyle("-fx-font-size: 9px;");
        lDates.setMaxWidth(NODE_WIDTH - 10);

        VBox box = new VBox(2, lName, lDates);
        box.setPadding(new Insets(6, 6, 6, 8));
        box.setMaxWidth(NODE_WIDTH);

        Group node = new Group(rect, box);
        enableDrag(node, task);
        return node;
    }

    // ── Dragging with snap-to-grid ─────────────────────────────────────────────

    private void enableDrag(Group node, TaskBean task)
    {
        final double[] offset = {0, 0};
        node.setOnMousePressed(e ->
        {
            if (task.id() != null && !selectTask(task))
            {
                e.consume();
                return;
            }
            offset[0] = e.getSceneX() - node.getTranslateX();
            offset[1] = e.getSceneY() - node.getTranslateY();
            node.toFront();
            e.consume();
        });
        node.setOnMouseDragged(e ->
        {
            node.setTranslateX(e.getSceneX() - offset[0]);
            node.setTranslateY(e.getSceneY() - offset[1]);
            e.consume();
        });
        node.setOnMouseReleased(e ->
        {
            node.setTranslateX(snap(node.getTranslateX()));
            node.setTranslateY(snap(node.getTranslateY()));
            e.consume();
        });
    }

    private double snap(double value) { return Math.round(value / GRID) * GRID; }

    // ── Directed orthogonal connector ─────────────────────────────────────────

    /**
     * Creates an orthogonal (right-angle) connector from {@code from} to {@code to}.
     * The path is recomputed on every translateX/Y change, so drag keeps it orthogonal.
     */
    private List<javafx.scene.Node> createArrow(Group from, Group to, Color color)
    {
        Path    path = new Path();
        Polygon head = new Polygon();
        path.setStroke(color);
        path.setStrokeWidth(1.5);
        path.setFill(Color.TRANSPARENT);
        head.setFill(color);

        Runnable update = () -> routeOrthogonal(path, head, from, to);

        from.translateXProperty().addListener((o, ov, nv) -> update.run());
        from.translateYProperty().addListener((o, ov, nv) -> update.run());
        to.translateXProperty()  .addListener((o, ov, nv) -> update.run());
        to.translateYProperty()  .addListener((o, ov, nv) -> update.run());

        update.run();
        return List.of(path, head);
    }

    private void routeOrthogonal(Path path, Polygon head, Group from, Group to)
    {
        double srcRight = from.getTranslateX() + NODE_WIDTH;
        double srcCY    = from.getTranslateY() + NODE_HEIGHT / 2.0;
        double tgtLeft  = to  .getTranslateX();
        double tgtRight = to  .getTranslateX() + NODE_WIDTH;
        double tgtCY    = to  .getTranslateY() + NODE_HEIGHT / 2.0;

        path.getElements().clear();

        if (srcRight <= tgtLeft)
        {
            // standard: source left of target — 3-segment L-route, enter from left
            double midX = (srcRight + tgtLeft) / 2.0;
            path.getElements().addAll(
                new MoveTo(srcRight, srcCY),
                new LineTo(midX,    srcCY),
                new LineTo(midX,    tgtCY),
                new LineTo(tgtLeft, tgtCY)
            );
            arrowTip(head, tgtLeft, tgtCY, 0.0);
        }
        else
        {
            // backward or same-column: bypass to the right, enter target from right edge
            double bypass = Math.max(srcRight, tgtRight) + H_GAP / 2.0;
            path.getElements().addAll(
                new MoveTo(srcRight, srcCY),
                new LineTo(bypass,   srcCY),
                new LineTo(bypass,   tgtCY),
                new LineTo(tgtRight, tgtCY)
            );
            arrowTip(head, tgtRight, tgtCY, Math.PI);
        }
    }

    private void arrowTip(Polygon head, double tipX, double tipY, double angle)
    {
        double lx = tipX - Math.cos(angle - ARROW_ANG) * ARROW_LEN;
        double ly = tipY - Math.sin(angle - ARROW_ANG) * ARROW_LEN;
        double rx = tipX - Math.cos(angle + ARROW_ANG) * ARROW_LEN;
        double ry = tipY - Math.sin(angle + ARROW_ANG) * ARROW_LEN;
        head.getPoints().setAll(tipX, tipY, lx, ly, rx, ry);
    }

    // ── Layout ────────────────────────────────────────────────────────────────

    private TimeScale applyLayout(Map<Long, Group> nodeById, Map<Long, TaskBean> byId)
    {
        GraphTimeline.Scale axis = GraphTimeline.createScale(byId.values(), timelineGranularity, PAD, TIMELINE_Y, TASKS_TOP_Y);
        TimeScale baseScale = TimeScale.from(axis);
        Map<Long, Double> yPos = new HashMap<>();
        Comparator<TaskBean> taskOrder = taskOrderComparator();

        List<Long> ids = byId.values().stream()
            .filter(task -> task.id() != null && nodeById.containsKey(task.id()))
            .sorted(taskOrder)
            .map(TaskBean::id)
            .toList();

        List<Double> targets = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++)
        {
            Long id = ids.get(i);
            // Primary row order follows scheduled start; predecessor anchors keep
            // dependent tasks visually close while overlap resolution preserves readability.
            double byScheduleRow = baseScale.tasksTopY() + i * STEP;
            double byPred = GraphLayout.avgPredY(id, byId, yPos);
            targets.add(Math.max(byScheduleRow, byPred));
        }
        List<Double> resolved = GraphLayout.resolveOverlaps(targets);

        for (int i = 0; i < ids.size(); i++)
        {
            Long id = ids.get(i);
            TaskBean task = byId.get(id);
            Group node = nodeById.get(id);
            if (task == null || node == null) continue;
            double x = snap(dateX(task.scheduledStart().orElse(baseScale.minDate()), baseScale));
            double y = snap(resolved.get(i));
            node.setTranslateX(x);
            node.setTranslateY(y);
            yPos.put(id, y);
        }
        double maxX = ids.stream()
            .map(byId::get)
            .filter(java.util.Objects::nonNull)
            .mapToDouble(task -> dateX(task.scheduledStart().orElse(baseScale.minDate()), baseScale))
            .max()
            .orElse(PAD);
        return baseScale.withAxisEndX(maxX + NODE_WIDTH + PAD);
    }

    private double dateX(LocalDate date, TimeScale scale)
    {
        return GraphTimeline.dateX(date, scale.toTimelineScale());
    }

    private void drawTimeline(Pane canvas, TimeScale scale)
    {
        LocalDate minDate = scale.minDate();
        LocalDate maxDate = scale.maxDate();
        double axisStartX = scale.axisStartX();
        double axisY = scale.axisY();
        double axisEndX = Math.max(scale.axisEndX(), dateX(maxDate, scale));

        javafx.scene.shape.Line axis = new javafx.scene.shape.Line(axisStartX, axisY, axisEndX, axisY);
        axis.setStroke(Color.web("#4f6d97"));
        axis.setStrokeWidth(1.5);
        canvas.getChildren().add(axis);

        switch (timelineGranularity)
        {
            case DAY -> drawDayTicks(canvas, scale);
            case WEEK -> drawWeekTicks(canvas, scale);
            case MONTH -> drawMonthTicks(canvas, scale);
        }

        // Today marker is clamped to the visible range to keep orientation stable
        // even when the loaded data does not include the current date.
        double todayX = dateX(LocalDate.now().isBefore(minDate) ? minDate : LocalDate.now().isAfter(maxDate) ? maxDate : LocalDate.now(), scale);
        javafx.scene.shape.Line today = new javafx.scene.shape.Line(todayX, axisY - 8, todayX, axisY + 16);
        today.setStroke(Color.web("#d4a45b"));
        today.setStrokeWidth(1.2);
        canvas.getChildren().add(today);
    }

    private void drawDayTicks(Pane canvas, TimeScale scale)
    {
        LocalDate minDate = scale.minDate();
        LocalDate maxDate = scale.maxDate();
        int i = 0;
        for (LocalDate tick = minDate; !tick.isAfter(maxDate); tick = tick.plusDays(1), i++)
        {
            double x = dateX(tick, scale);
            javafx.scene.shape.Line mark = new javafx.scene.shape.Line(x, scale.axisY() - 4, x, scale.axisY() + 8);
            mark.setStroke(Color.web("#6f86a8"));
            mark.setStrokeWidth(0.8);
            canvas.getChildren().add(mark);
            if (i % 7 == 0)
            {
                Label label = new Label(tick.format(DAY_FMT));
                label.setStyle("-fx-font-size: 9px; -fx-text-fill: #a9bfd8;");
                label.setLayoutX(x + 2);
                label.setLayoutY(scale.axisY() - 17);
                canvas.getChildren().add(label);
            }
        }
    }

    private void drawWeekTicks(Pane canvas, TimeScale scale)
    {
        LocalDate minDate = scale.minDate();
        LocalDate maxDate = scale.maxDate();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        LocalDate weekStart = minDate.minusDays(minDate.getDayOfWeek().getValue() - 1L);
        for (LocalDate tick = weekStart; !tick.isAfter(maxDate); tick = tick.plusWeeks(1))
        {
            if (tick.isBefore(minDate)) continue;
            double x = dateX(tick, scale);
            javafx.scene.shape.Line mark = new javafx.scene.shape.Line(x, scale.axisY() - 5, x, scale.axisY() + 9);
            mark.setStroke(Color.web("#6f86a8"));
            mark.setStrokeWidth(0.9);
            Label label = new Label("CW " + tick.get(weekFields.weekOfWeekBasedYear()));
            label.setStyle("-fx-font-size: 10px; -fx-text-fill: #a9bfd8;");
            label.setLayoutX(x + 3);
            label.setLayoutY(scale.axisY() - 18);
            canvas.getChildren().addAll(mark, label);
        }
    }

    private void drawMonthTicks(Pane canvas, TimeScale scale)
    {
        LocalDate minDate = scale.minDate();
        LocalDate maxDate = scale.maxDate();
        LocalDate monthTick = minDate.withDayOfMonth(1);
        if (monthTick.isBefore(minDate)) monthTick = monthTick.plusMonths(1);
        if (monthTick.isAfter(maxDate)) monthTick = minDate;

        while (!monthTick.isAfter(maxDate))
        {
            double x = dateX(monthTick, scale);
            javafx.scene.shape.Line tick = new javafx.scene.shape.Line(x, scale.axisY() - 6, x, scale.axisY() + 10);
            tick.setStroke(Color.web("#6f86a8"));
            tick.setStrokeWidth(1.0);

            Label label = new Label(monthTick.format(MONTH_FMT));
            label.setStyle("-fx-font-size: 10px; -fx-text-fill: #a9bfd8;");
            label.setLayoutX(x + 4);
            label.setLayoutY(scale.axisY() - 18);

            canvas.getChildren().addAll(tick, label);
            monthTick = monthTick.plusMonths(1).withDayOfMonth(1);
        }
    }

    private Comparator<TaskBean> taskOrderComparator()
    {
        return Comparator
            .comparing((TaskBean t) -> t.scheduledStart().orElse(null), Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(TaskBean::name, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(t -> t.id() == null ? Long.MAX_VALUE : t.id());
    }

    private enum EdgeType { PARENT_CHILD, PRED_SUCC }

    private record EdgeSpec(Group from, Group to, EdgeType type) {}
    private record TimeScale(LocalDate minDate, LocalDate maxDate, double dayWidth, double axisStartX, double axisY, double tasksTopY, double axisEndX)
    {
        private GraphTimeline.Scale toTimelineScale()
        {
            return new GraphTimeline.Scale(minDate, maxDate, dayWidth, axisStartX, axisY, tasksTopY);
        }

        private TimeScale withAxisEndX(double axisEndX)
        {
            return new TimeScale(minDate, maxDate, dayWidth, axisStartX, axisY, tasksTopY, axisEndX);
        }

        private static TimeScale from(GraphTimeline.Scale scale)
        {
            return new TimeScale(scale.minDate(), scale.maxDate(), scale.dayWidth(), scale.axisStartX(), scale.axisY(), scale.tasksTopY(), 0);
        }
    }

    // ── Layout persistence ────────────────────────────────────────────────────

    @FXML
    private void saveLayout()
    {
        FileChooser chooser = layoutFileChooser("Save layout");
        File file = chooser.showSaveDialog(graphContainer.getScene().getWindow());
        if (file == null) return;

        lastLayoutFile = file;
        Properties props = new Properties();
        currentNodeById.forEach((id, node) ->
            props.setProperty(id.toString(),
                    node.getTranslateX() + "," + node.getTranslateY()));

        try (FileWriter w = new FileWriter(file))
        {
            props.store(w, "pragma graph layout");
        }
        catch (IOException e)
        {
            log.error("failed to save layout to {}", file, e);
            TaskUiSupport.showError("Save layout", e);
        }
    }

    @FXML
    private void loadLayout()
    {
        FileChooser chooser = layoutFileChooser("Load layout");
        if (lastLayoutFile != null) chooser.setInitialDirectory(lastLayoutFile.getParentFile());
        File file = chooser.showOpenDialog(graphContainer.getScene().getWindow());
        if (file == null) return;

        lastLayoutFile = file;
        Properties props = new Properties();
        try (FileReader r = new FileReader(file))
        {
            props.load(r);
        }
        catch (IOException e)
        {
            log.error("failed to load layout from {}", file, e);
            TaskUiSupport.showError("Load layout", e);
            return;
        }

        int applied = 0;
        for (String key : props.stringPropertyNames())
        {
            try
            {
                Long  id    = Long.parseLong(key.trim());
                Group node  = currentNodeById.get(id);
                if (node == null) continue;
                String[] xy = props.getProperty(key).split(",", 2);
                node.setTranslateX(snap(Double.parseDouble(xy[0].trim())));
                node.setTranslateY(snap(Double.parseDouble(xy[1].trim())));
                applied++;
            }
            catch (NumberFormatException | ArrayIndexOutOfBoundsException ex)
            {
                log.warn("skipping invalid layout entry: {}={}", key, props.getProperty(key));
            }
        }
        if (lblStatus != null) lblStatus.setText(currentNodeById.size() + " tasks  (layout: " + applied + " nodes)");
    }

    private void addZoomSupport(Pane canvas)
    {
        // Scale with pivot (0,0): zooming in grows the canvas toward bottom-right,
        // keeping the top-left visible — setScaleX/Y uses the node's center as pivot,
        // which pushes nodes off-screen when zooming in.
        graphScale = new Scale(1, 1, 0, 0);
        canvas.getTransforms().add(graphScale);
    }

    private void applyZoom(double factor)
    {
        double next = Math.max(0.1, Math.min(graphScale.getX() * factor, 5.0));
        graphScale.setX(next);
        graphScale.setY(next);
    }

    private FileChooser layoutFileChooser(String title)
    {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().addAll(
                new ExtensionFilter("Pragma Graph Layout (*.pgraph)", "*.pgraph"),
                new ExtensionFilter("All files", "*.*"));
        if (lastLayoutFile != null)
        {
            chooser.setInitialDirectory(lastLayoutFile.getParentFile());
            chooser.setInitialFileName(lastLayoutFile.getName());
        }
        return chooser;
    }

    private void centerOnDate(LocalDate date)
    {
        if (currentScale == null || graphContainer.getContent() == null) return;
        double x = dateX(date, TimeScale.from(currentScale));
        javafx.geometry.Bounds viewport = graphContainer.getViewportBounds();
        javafx.geometry.Bounds content = graphContainer.getContent().getLayoutBounds();
        if (content.getWidth() <= viewport.getWidth()) return;
        double targetLeft = Math.max(0, x - viewport.getWidth() / 2.0);
        double maxLeft = content.getWidth() - viewport.getWidth();
        graphContainer.setHvalue(Math.min(1.0, Math.max(0.0, targetLeft / maxLeft)));
    }

    private boolean selectTask(TaskBean task)
    {
        if (task.id() == null) return false;
        if (task.id().equals(selectedTaskId)) return true;
        if (!confirmDiscardChanges()) return false;
        selectedTaskId = task.id();
        TaskBean selected = currentTaskById.getOrDefault(task.id(), task);
        inspector.clearDirty();
        inspector.showTask(selected);
        updateSelectionStyles();
        return true;
    }

    private void updateNodeContent(TaskBean task)
    {
        if (task == null || task.id() == null) return;
        Group node = currentNodeById.get(task.id());
        if (node == null || node.getChildren().size() < 2) return;
        if (!(node.getChildren().get(1) instanceof VBox box)) return;
        if (box.getChildren().size() < 2) return;
        if (box.getChildren().get(0) instanceof Label nameLabel) nameLabel.setText(task.name());
        if (box.getChildren().get(1) instanceof Label datesLabel) datesLabel.setText(dateText(task));
    }

    private String dateText(TaskBean task)
    {
        String startText = task.scheduledStart().map(d -> "from: " + d.format(DATE_FMT)).orElse("");
        String endText   = task.scheduledFinish()  .map(d -> "to: " + d.format(DATE_FMT)).orElse("");
        return startText.isEmpty() && endText.isEmpty() ? ""
            : startText + (startText.isEmpty() || endText.isEmpty() ? "" : "  ") + endText;
    }

    private boolean hasStructuralRelationChanges(TaskBean before, TaskBean after)
    {
        if (before == null || after == null) return false;
        Long beforeParentId = before.parentTask().map(TaskBean::id).orElse(null);
        Long afterParentId = after.parentTask().map(TaskBean::id).orElse(null);
        if (!java.util.Objects.equals(beforeParentId, afterParentId)) return true;
        return !relationIds(before.predecessors()).equals(relationIds(after.predecessors()))
            || !relationIds(before.successors()).equals(relationIds(after.successors()))
            || !relationIds(before.subTasks()).equals(relationIds(after.subTasks()));
    }

    private Set<Long> relationIds(java.util.Optional<Set<TaskBean>> relations)
    {
        return relations.orElse(Set.of()).stream()
            .map(TaskBean::id)
            .filter(java.util.Objects::nonNull)
            .collect(java.util.stream.Collectors.toSet());
    }

    private void updateSelectionStyles()
    {
        currentNodeById.forEach((id, group) ->
        {
            if (group == null || group.getChildren().isEmpty()) return;
            if (!(group.getChildren().get(0) instanceof Rectangle rect)) return;
            boolean selected = selectedTaskId != null && selectedTaskId.equals(id);
            if (selected)
            {
                rect.setStroke(Color.web("#ffd166"));
                rect.setStrokeWidth(3.0);
            }
            else
            {
                rect.setStroke(Color.web("#6699cc"));
                rect.setStrokeWidth(1.5);
            }
        });
    }

    private boolean confirmDiscardChanges()
    {
        return TaskUiSupport.confirmDiscardChanges(inspector.dirty());
    }

}