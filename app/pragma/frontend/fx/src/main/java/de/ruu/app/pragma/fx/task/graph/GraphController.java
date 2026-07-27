package de.ruu.app.pragma.fx.task.graph;

import de.ruu.app.pragma.bean.TaskBean;
import de.ruu.app.pragma.bean.TaskGroupBean;
import de.ruu.app.pragma.client.TaskClient;
import de.ruu.app.pragma.client.TaskGroupClient;
import de.ruu.app.pragma.fx.taskgroup.edit.TaskGroupEditor;
import de.ruu.lib.fx.comp.FXCController.DefaultFXCController;
import de.ruu.lib.fx.control.autocomplete.textfield.TextFieldAutoCompleteClearableWithArrowButton;
import de.ruu.lib.fx.control.autocomplete.textfield.TextFieldAutoCompleteClearableWithArrowButtonBuilder;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
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
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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

    private static final Color COLOR_PRED_SUCC    = Color.web("#9999bb");
    private static final Color COLOR_PARENT_CHILD = Color.web("#4a9a4a");

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @FXML private HBox   cbGroupsContainer;
    @FXML private Button btnManageGroups;
    @FXML private Button btnReload;
    @FXML private Label  lblStatus;
    private TextFieldAutoCompleteClearableWithArrowButton<TaskGroupBean> cbGroups;
    @FXML private ScrollPane graphContainer;
    @FXML private Button     btnSaveLayout;
    @FXML private Button     btnLoadLayout;

    @Inject private TaskGroupClient taskGroupClient;
    @Inject private TaskClient      taskClient;
    @Inject private TaskGroupEditor taskGroupEditor;

    /** Task-ID → node; populated after each group load, used for save/load layout. */
    private Map<Long, Group> currentNodeById = new HashMap<>();

    private de.ruu.app.pragma.fx.TaskGroupManagementDialog groupManagementDialog;

    private File                      lastLayoutFile;
    private Scale                     graphScale         = new Scale(1, 1, 0, 0);
    private EventHandler<KeyEvent>    keyZoomHandler;
    private EventHandler<ScrollEvent> scrollZoomHandler;

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
        cbGroups.valueProperty()
                .addListener((obs, old, sel) -> { if (sel != null) loadGroup(sel); });
        btnManageGroups.setOnAction(e -> onManageGroups());
        btnReload.setOnAction(e -> loadGroups());
        btnSaveLayout.setDisable(true);
        btnLoadLayout.setDisable(true);

        graphContainer.sceneProperty().addListener((obs, oldScene, newScene) ->
        {
            if (oldScene != null)
            {
                if (keyZoomHandler    != null) oldScene.removeEventFilter(KeyEvent.KEY_PRESSED, keyZoomHandler);
                if (scrollZoomHandler != null) oldScene.removeEventFilter(ScrollEvent.SCROLL,   scrollZoomHandler);
            }
            if (newScene != null)
            {
                keyZoomHandler = event ->
                {
                    if (!event.isControlDown()) return;
                    KeyCode code = event.getCode();
                    if      (code == KeyCode.PLUS  || code == KeyCode.EQUALS || code == KeyCode.ADD) applyZoom(1.1);
                    else if (code == KeyCode.MINUS || code == KeyCode.SUBTRACT)                      applyZoom(1.0 / 1.1);
                    else return;
                    event.consume();
                };
                scrollZoomHandler = event ->
                {
                    javafx.geometry.Bounds b = graphContainer.localToScene(graphContainer.getBoundsInLocal());
                    if (!b.contains(event.getSceneX(), event.getSceneY())) return;
                    applyZoom(event.getDeltaY() > 0 ? 1.1 : 1.0 / 1.1);
                    event.consume();
                };
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, keyZoomHandler);
                newScene.addEventFilter(ScrollEvent.SCROLL,   scrollZoomHandler);
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
        if (lblStatus != null) lblStatus.setText("Connecting ...");
        Thread.ofVirtual().start(() ->
        {
            try
            {
                List<TaskGroupBean> groups = taskGroupClient.findAll();
                Platform.runLater(() ->
                {
                    cbGroups.items(groups);
                    if (!groups.isEmpty()) cbGroups.value(groups.get(0));
                    if (lblStatus != null) lblStatus.setText("");
                });
            }
            catch (Exception e)
            {
                log.error("failed to load groups", e);
                Platform.runLater(() -> { if (lblStatus != null) lblStatus.setText("[WARN] Connection error - is the server reachable?"); });
            }
        });
    }

    private void loadGroup(TaskGroupBean group)
    {
        try
        {
            List<TaskBean> tasks = taskClient.findGroupTasksWithRelated(group);
            buildGraph(tasks);
            if (lblStatus != null) lblStatus.setText(tasks.size() + " tasks");
        }
        catch (Exception e)
        {
            log.error("failed to load group {}", group.name(), e);
            if (lblStatus != null) lblStatus.setText("Error: " + e.getMessage());
            showError("Load group", e);
        }
    }

    private void buildGraph(List<TaskBean> tasks)
    {
        Pane canvas = new Pane();
        canvas.setStyle("-fx-background-color: #1e1e2e;");

        Map<Long, TaskBean> byId     = new HashMap<>();
        Map<Long, Group>    nodeById = new HashMap<>();
        List<EdgeSpec>      edges    = new ArrayList<>();
        Set<String>         addedEdgeKeys = new HashSet<>();
        List<Long>          ghostPredIds  = new ArrayList<>();
        List<Long>          ghostSuccIds  = new ArrayList<>();

        for (TaskBean task : tasks)
            if (task.id() != null) byId.put(task.id(), task);

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
                        ghostPredIds.add(pred.id());
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
                        ghostSuccIds.add(succ.id());
                    }
                    if (from != null && addedEdgeKeys.add(task.id() + "-" + succ.id()))
                        edges.add(new EdgeSpec(from, to, EdgeType.PRED_SUCC));
                }
            });
        }

        applyLayout(nodeById, byId, ghostPredIds, ghostSuccIds);

        // add edges behind nodes
        for (EdgeSpec spec : edges)
        {
            Color color = spec.type() == EdgeType.PARENT_CHILD ? COLOR_PARENT_CHILD : COLOR_PRED_SUCC;
            canvas.getChildren().addAll(0, createArrow(spec.from(), spec.to(), color));
        }

        graphContainer.setContent(new Group(canvas));
        addZoomSupport(canvas);

        currentNodeById = new HashMap<>(nodeById);
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

        String startText = task.scheduledStart().map(d -> "from: " + d.format(DATE_FMT)).orElse("");
        String endText   = task.scheduledFinish()  .map(d -> "to: " + d.format(DATE_FMT)).orElse("");
        String dateText  = startText.isEmpty() && endText.isEmpty() ? ""
                         : startText + (startText.isEmpty() || endText.isEmpty() ? "" : "  ") + endText;

        Label lDates = new Label(dateText);
        lDates.setTextFill(Color.LIGHTGRAY);
        lDates.setStyle("-fx-font-size: 9px;");
        lDates.setMaxWidth(NODE_WIDTH - 10);

        VBox box = new VBox(2, lName, lDates);
        box.setPadding(new Insets(6, 6, 6, 8));
        box.setMaxWidth(NODE_WIDTH);

        Group node = new Group(rect, box);
        enableDrag(node);
        return node;
    }

    // ── Dragging with snap-to-grid ─────────────────────────────────────────────

    private void enableDrag(Group node)
    {
        final double[] offset = {0, 0};
        node.setOnMousePressed(e ->
        {
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

    /**
     * Recursive tree layout.
     * Root tasks (no parent and no predecessor within the group) are sorted alphabetically
     * and placed in column 0. Each node is placed at the same row as its first visual child.
     * Visual children = sub-tasks ∪ successors (within group), sorted alphabetically.
     * Ghost predecessors occupy the leftmost column; ghost successors the rightmost.
     */
    private void applyLayout(
            Map<Long, Group>  nodeById,
            Map<Long, TaskBean> byId,
            List<Long> ghostPredIds,
            List<Long> ghostSuccIds)
    {
        // identify tasks that are NOT roots (have parent or predecessor within group)
        Set<Long> nonRoots = new HashSet<>();
        for (TaskBean t : byId.values())
        {
            if (t.id() == null) continue;
            t.parentTask().ifPresent(p -> { if (p.id() != null && byId.containsKey(p.id())) nonRoots.add(t.id()); });
            t.predecessors().ifPresent(preds -> preds.forEach(pred ->
            {
                if (pred.id() != null && byId.containsKey(pred.id())) nonRoots.add(t.id());
            }));
        }

        List<TaskBean> roots = byId.values().stream()
            .filter(t -> t.id() != null && !nonRoots.contains(t.id()))
            .sorted(Comparator.comparing(t -> t.name().toLowerCase(Locale.ROOT)))
            .toList();

        int colOffset  = ghostPredIds.isEmpty() ? 0 : 1;
        Set<Long> placed = new HashSet<>();
        int[] row        = {0};
        int[] maxCol     = {colOffset};

        // ghost predecessors — leftmost column
        if (!ghostPredIds.isEmpty())
        {
            double ghostX = snap(PAD);
            for (int i = 0; i < ghostPredIds.size(); i++)
            {
                Group node = nodeById.get(ghostPredIds.get(i));
                if (node != null) { node.setTranslateX(ghostX); node.setTranslateY(snap(PAD + i * STEP)); }
            }
        }

        for (TaskBean root : roots)
            placeNode(root, colOffset, row, maxCol, byId, nodeById, placed);

        // tasks not reached from any root (cycles / orphans)
        byId.values().stream()
            .filter(t -> t.id() != null && !placed.contains(t.id()))
            .sorted(Comparator.comparing(t -> t.name().toLowerCase(Locale.ROOT)))
            .forEach(t -> placeNode(t, colOffset, row, maxCol, byId, nodeById, placed));

        // ghost successors — rightmost column
        if (!ghostSuccIds.isEmpty())
        {
            double ghostX = snap(PAD + (maxCol[0] + 1) * (NODE_WIDTH + H_GAP));
            for (int i = 0; i < ghostSuccIds.size(); i++)
            {
                Group node = nodeById.get(ghostSuccIds.get(i));
                if (node != null) { node.setTranslateX(ghostX); node.setTranslateY(snap(PAD + i * STEP)); }
            }
        }
    }

    /**
     * Places {@code task} at {@code col} and {@code row[0]}, then recursively places
     * its visual children in {@code col + 1}.  Updates {@code row[0]} and {@code maxCol[0]}.
     */
    private void placeNode(
            TaskBean task, int col,
            int[] row, int[] maxCol,
            Map<Long, TaskBean> byId, Map<Long, Group> nodeById,
            Set<Long> placed)
    {
        if (task.id() == null || !placed.add(task.id())) return;

        if (col > maxCol[0]) maxCol[0] = col;

        Group node = nodeById.get(task.id());
        if (node != null)
        {
            node.setTranslateX(snap(PAD + col * (NODE_WIDTH + H_GAP)));
            node.setTranslateY(snap(PAD + row[0] * STEP));
        }

        List<TaskBean> children = visualChildren(task, byId);
        if (children.isEmpty())
            row[0]++;
        else
            for (TaskBean child : children)
                placeNode(child, col + 1, row, maxCol, byId, nodeById, placed);
    }

    /** Sub-tasks ∪ successors of {@code task} within {@code byId}, sorted alphabetically. */
    private List<TaskBean> visualChildren(TaskBean task, Map<Long, TaskBean> byId)
    {
        Map<Long, TaskBean> seen = new LinkedHashMap<>();
        task.subTasks() .ifPresent(s -> s.stream().filter(t -> t.id() != null && byId.containsKey(t.id())).forEach(t -> seen.put(t.id(), t)));
        task.successors().ifPresent(s -> s.stream().filter(t -> t.id() != null && byId.containsKey(t.id())).forEach(t -> seen.putIfAbsent(t.id(), t)));
        return seen.values().stream()
                   .sorted(Comparator.comparing(t -> t.name().toLowerCase(Locale.ROOT)))
                   .toList();
    }

    private enum EdgeType { PARENT_CHILD, PRED_SUCC }

    private record EdgeSpec(Group from, Group to, EdgeType type) {}

    // ── Layout persistence ────────────────────────────────────────────────────

    @FXML
    private void saveLayout()
    {
        FileChooser chooser = layoutFileChooser("Layout speichern");
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
            showError("Layout speichern", e);
        }
    }

    @FXML
    private void loadLayout()
    {
        FileChooser chooser = layoutFileChooser("Layout laden");
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
            showError("Layout laden", e);
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
                new ExtensionFilter("Alle Dateien", "*.*"));
        if (lastLayoutFile != null)
        {
            chooser.setInitialDirectory(lastLayoutFile.getParentFile());
            chooser.setInitialFileName(lastLayoutFile.getName());
        }
        return chooser;
    }

    private void showError(String title, Exception e)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

}