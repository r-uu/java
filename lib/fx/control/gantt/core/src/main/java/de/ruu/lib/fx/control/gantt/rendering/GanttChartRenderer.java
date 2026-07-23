package de.ruu.lib.fx.control.gantt.rendering;

import de.ruu.lib.fx.control.gantt.api.GanttTask;
import de.ruu.lib.fx.control.gantt.config.GanttChartConfig;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Renders Gantt chart visualization on a Canvas.
 * 
 * <p>Handles:
 * - Time scale header (hours/days/weeks/months)
 * - Gantt bars for each task
 * - Colors and styling
 * - Layout calculations
 * 
 * <p>Uses model coordinates internally, converts to Canvas pixel coordinates
 * based on zoom level and scroll position.
 */
public class GanttChartRenderer {
	private static final Logger log = LoggerFactory.getLogger(GanttChartRenderer.class);

	private final Canvas canvas;
	private final GanttChartConfig config;
	private final TimeScaleCalculator timeScale;

	// Rendering constants
	private static final int HEADER_HEIGHT = 60;
	private static final int ROW_HEIGHT = 30;
	private static final int LEFT_MARGIN = 10;
	private static final int TOP_MARGIN = 10;

	// Colors
	private static final Color BAR_COLOR = Color.web("#4CAF50");
	private static final Color BAR_HOVER_COLOR = Color.web("#45a049");
	private static final Color BORDER_COLOR = Color.web("#333333");
	private static final Color GRID_COLOR = Color.web("#eeeeee");
	private static final Color TEXT_COLOR = Color.web("#333333");
	private static final Color HEADER_BG = Color.web("#f5f5f5");

	public GanttChartRenderer(Canvas canvas, GanttChartConfig config) {
		this.canvas = canvas;
		this.config = config;
		this.timeScale = new TimeScaleCalculator(config);
	}

	/**
	 * Renders the complete Gantt chart.
	 * 
	 * @param tasks list of tasks to render (only root tasks with y-offset)
	 * @param selectedTaskId optional selected task ID for highlighting
	 */
	public void render(List<GanttTask> tasks, String selectedTaskId) {
		GraphicsContext gc = canvas.getGraphicsContext2D();

		// Clear canvas
		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

		// Draw time scale header
		drawTimeScaleHeader(gc);

		// Draw grid and task bars
		int yOffset = HEADER_HEIGHT + TOP_MARGIN;
		for (GanttTask task : tasks) {
			drawTask(gc, task, yOffset, selectedTaskId);
			yOffset += ROW_HEIGHT;
		}

		log.debug("Rendered {} tasks", tasks.size());
	}

	/**
	 * Draws the time scale header with hour/day/week/month labels.
	 */
	private void drawTimeScaleHeader(GraphicsContext gc) {
		double headerWidth = canvas.getWidth();

		// Header background
		gc.setFill(HEADER_BG);
		gc.fillRect(0, 0, headerWidth, HEADER_HEIGHT);

		// Header border
		gc.setStroke(BORDER_COLOR);
		gc.setLineWidth(1);
		gc.strokeRect(0, 0, headerWidth, HEADER_HEIGHT);

		// Draw time labels
		gc.setFill(TEXT_COLOR);
		gc.setFont(new Font("System", 11));

		int numIntervals = timeScale.getNumIntervals();
		double intervalWidth = (headerWidth - LEFT_MARGIN) / numIntervals;

		for (int i = 0; i < numIntervals && i < 20; i++) {
			double x = LEFT_MARGIN + (i * intervalWidth);
			String label = timeScale.getIntervalLabel(i);

			gc.fillText(label, x + 5, HEADER_HEIGHT - 15);

			// Vertical grid line
			gc.setStroke(GRID_COLOR);
			gc.setLineWidth(1);
			gc.strokeLine(x, HEADER_HEIGHT, x, canvas.getHeight());
		}
	}

	/**
	 * Draws a single task bar.
	 */
	private void drawTask(GraphicsContext gc, GanttTask task, int yOffset, String selectedTaskId) {
		if (task.startDate() == null || task.endDate() == null) {
			log.debug("Skipping task {} with missing dates", task.id());
			return;
		}

		// Calculate bar position and width
		double barX = timeScale.dateToPixel(task.startDate());
		double barWidth = timeScale.dateToPixel(task.endDate()) - barX;

		if (barWidth < 1) {
			barWidth = 10; // Minimum width for visibility
		}

		// Choose color based on selection
		Color barColor = task.id().equals(selectedTaskId) ? BAR_HOVER_COLOR : BAR_COLOR;

		// Draw bar
		gc.setFill(barColor);
		gc.fillRect(barX, yOffset, barWidth, ROW_HEIGHT - 5);

		// Draw border
		gc.setStroke(BORDER_COLOR);
		gc.setLineWidth(1);
		gc.strokeRect(barX, yOffset, barWidth, ROW_HEIGHT - 5);

		// Draw task name
		gc.setFill(Color.WHITE);
		gc.setFont(new Font("System", 10));
		gc.fillText(task.name(), barX + 5, yOffset + 18);

		log.trace("Drew task {} at x={}, width={}", task.id(), barX, barWidth);
	}

	/**
	 * Calculates which task (if any) is at the given Canvas coordinates.
	 * Returns null if no task is at that location.
	 */
	public String getTaskAtPosition(double canvasX, double canvasY, List<GanttTask> tasks) {
		if (canvasY < HEADER_HEIGHT) {
			return null; // Clicked on header
		}

		int taskIndex = (int) ((canvasY - HEADER_HEIGHT - TOP_MARGIN) / ROW_HEIGHT);
		if (taskIndex < 0 || taskIndex >= tasks.size()) {
			return null;
		}

		GanttTask task = tasks.get(taskIndex);
		if (task.startDate() == null || task.endDate() == null) {
			return null;
		}

		double barX = timeScale.dateToPixel(task.startDate());
		double barWidth = timeScale.dateToPixel(task.endDate()) - barX;

		if (canvasX >= barX && canvasX <= barX + barWidth) {
			return task.id();
		}

		return null;
	}

	public int getHeaderHeight() {
		return HEADER_HEIGHT;
	}

	public int getRowHeight() {
		return ROW_HEIGHT;
	}
}
