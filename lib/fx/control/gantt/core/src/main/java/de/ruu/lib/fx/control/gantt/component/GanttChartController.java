package de.ruu.lib.fx.control.gantt.component;

import de.ruu.lib.fx.FXUtil;
import de.ruu.lib.fx.comp.FXCController.DefaultFXCController;
import de.ruu.lib.fx.control.gantt.api.GanttDataProvider;
import de.ruu.lib.fx.control.gantt.api.GanttTask;
import de.ruu.lib.fx.control.gantt.config.GanttChartConfig;
import de.ruu.lib.fx.control.gantt.rendering.GanttChartRenderer;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for the Gantt chart component.
 * 
 * <p>Manages:
 * - Loading tasks from GanttDataProvider
 * - Populating the task hierarchy TreeView
 * - Coordinating Canvas rendering
 * - Handling user interactions (selections, edits)
 */
@Dependent
public class GanttChartController extends DefaultFXCController<GanttChartComponent, GanttChartService>
{
	private static final Logger log = LoggerFactory.getLogger(GanttChartController.class);

	@FXML
	private AnchorPane root;

	@FXML
	private AnchorPane treePane;

	@FXML
	private TreeView<GanttTask> treeView;

	@FXML
	private AnchorPane canvasPane;

	@FXML
	private Canvas canvas;

	@Inject
	private GanttDataProvider dataProvider;

	private final Map<String, TreeItem<GanttTask>> treeItemsById = new HashMap<>();
	private final List<GanttTask> allTasks = new java.util.ArrayList<>();
	private GanttChartRenderer renderer;
	private GanttChartConfig config;
	private String selectedTaskId;

	@Override @FXML
	protected void initialize()
	{
		log.debug("Initializing GanttChartController");

		// Set anchors to fill container
		FXUtil.setAnchorsInAnchorPaneTo(root, 0);
		FXUtil.setAnchorsInAnchorPaneTo(treePane, 0);
		FXUtil.setAnchorsInAnchorPaneTo(canvasPane, 0);

		// Setup canvas event handlers
		canvas.setOnMouseClicked(this::onCanvasClicked);
		canvas.setOnMouseMoved(this::onCanvasMouseMoved);

		// Load and display tasks
		loadTasks();

		// Setup rendering
		setupRenderer();

		// Trigger initial render
		redrawCanvas();
	}

	/**
	 * Sets up the renderer with default or provided configuration.
	 */
	private void setupRenderer() {
		if (config == null) {
			// Default config: 30 days from today
			LocalDate today = LocalDate.now();
			config = GanttChartConfig.builder()
				.startDate(today)
				.endDate(today.plusDays(30))
				.timeUnit(GanttChartConfig.TimeUnit.HOURS)
				.build();
			log.info("Using default configuration: {} to {}", config.getStartDate(), config.getEndDate());
		}

		renderer = new GanttChartRenderer(canvas, config);
		log.debug("Renderer initialized");
	}

	private void loadTasks()
	{
		log.debug("Loading tasks from data provider");

		allTasks.clear();
		allTasks.addAll(dataProvider.loadTasks());

		// Build tree structure: find root tasks (no parent)
		List<GanttTask> rootTasks = allTasks.stream()
			.filter(task -> task.parentTaskId().isEmpty())
			.collect(Collectors.toList());

		rootTasks.forEach(this::addTaskToTree);

		log.info("Loaded {} root tasks from {} total", rootTasks.size(), allTasks.size());
	}

	private void addTaskToTree(GanttTask task)
	{
		TreeItem<GanttTask> item = new TreeItem<>(task);
		item.setValue(task);
		treeItemsById.put(task.id(), item);

		// Add to root
		if (treeView.getRoot() == null)
		{
			TreeItem<GanttTask> root = new TreeItem<>();
			root.setExpanded(true);
			treeView.setRoot(root);
		}
		treeView.getRoot().getChildren().add(item);

		// Recursively add children
		allTasks.stream()
			.filter(t -> t.parentTaskId().isPresent() && t.parentTaskId().get().equals(task.id()))
			.forEach(childTask ->
			{
				TreeItem<GanttTask> childItem = new TreeItem<>(childTask);
				childItem.setValue(childTask);
				treeItemsById.put(childTask.id(), childItem);
				item.getChildren().add(childItem);
			});
	}

	private void redrawCanvas() {
		if (renderer == null) {
			setupRenderer();
		}

		// Get only root tasks for rendering
		List<GanttTask> rootTasks = allTasks.stream()
			.filter(task -> task.parentTaskId().isEmpty())
			.collect(Collectors.toList());

		renderer.render(rootTasks, selectedTaskId);
	}

	private void onCanvasClicked(MouseEvent event) {
		List<GanttTask> rootTasks = allTasks.stream()
			.filter(task -> task.parentTaskId().isEmpty())
			.collect(Collectors.toList());

		String taskAtClick = renderer.getTaskAtPosition(event.getX(), event.getY(), rootTasks);

		if (taskAtClick != null) {
			selectedTaskId = taskAtClick;
			log.debug("Selected task: {}", selectedTaskId);

			// Update tree selection
			TreeItem<GanttTask> treeItem = treeItemsById.get(selectedTaskId);
			if (treeItem != null) {
				treeView.getSelectionModel().select(treeItem);
			}

			redrawCanvas();
		}
	}

	private void onCanvasMouseMoved(MouseEvent event) {
		// Can be used for hover effects, cursor changes, etc.
	}

	/**
	 * Public method to set configuration before rendering.
	 */
	public void setConfig(GanttChartConfig config) {
		this.config = config;
		if (renderer != null) {
			setupRenderer();
			redrawCanvas();
		}
	}
}
