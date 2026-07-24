package de.ruu.lib.fx.control.gantt.api;

import java.util.List;

/**
 * Data provider for the Gantt chart component.
 * 
 * <p>Combines reading (load tasks) and writing (mutations) of task data.
 * This is the primary integration point between the Gantt component and
 * any application's data source (database, REST API, in-memory, etc).
 * 
 * <h3>Usage in Component</h3>
 * The Gantt component will:
 * <ol>
 *   <li>Call loadTasks() to get initial data</li>
 *   <li>Listen for change events (if implemented)</li>
 *   <li>Call mutation methods (createTask, updateTask, etc) on user interaction</li>
 *   <li>Call refreshTasks() when needed to reload from source</li>
 * </ol>
 * 
 * <h3>Injectable Usage</h3>
 * This interface is designed to be injected via CDI:
 * <pre>
 * @Dependent
 * public class PragmaGanttDataProvider implements GanttDataProvider {
 *     @Inject private TaskClient taskClient;
 *     // ... implementation
 * }
 * </pre>
 */
public interface GanttDataProvider extends GanttTaskMutator
{
	/**
	 * Loads all tasks from the data source.
	 * 
	 * <p>Called once at component initialization and on refreshTasks().
	 * Should include all tasks, including those in hierarchies.
	 * 
	 * @return list of all GanttTasks (possibly empty, never null)
	 */
	List<GanttTask> loadTasks();

	/**
	 * Reloads tasks from the data source.
	 * 
	 * <p>Used to sync with remote changes or after bulk operations.
	 * Component will rebuild its tree/canvas after this is called.
	 * 
	 * <p>Implementations should fire appropriate change events
	 * so listeners can react to the refresh.
	 */
	void refreshTasks();
}
