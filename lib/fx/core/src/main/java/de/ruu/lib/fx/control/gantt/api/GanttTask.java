package de.ruu.lib.fx.control.gantt.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Represents a single task in a Gantt chart.
 * 
 * <p>This interface abstracts the task concept, allowing different applications
 * to adapt their domain models (TaskBean, TaskEntity, etc.) to work with the
 * Gantt component without modification.
 * 
 * <h3>Contract</h3>
 * All methods must return consistent, non-null values for the lifetime of the
 * object (unless explicitly Optional).
 * 
 * <h3>Example Implementation</h3>
 * <pre>
 * public class PragmaTaskAdapter implements GanttTask {
 *     private final TaskBean delegate;
 *     
 *     public PragmaTaskAdapter(TaskBean taskBean) {
 *         this.delegate = taskBean;
 *     }
 *     
 *     @Override public String id() { return delegate.id(); }
 *     @Override public String name() { return delegate.name(); }
 *     @Override public LocalDateTime startDate() { return delegate.startDate(); }
 *     @Override public LocalDateTime endDate() { return delegate.endDate(); }
 *     // ... etc
 * }
 * </pre>
 */
public interface GanttTask
{
	/**
	 * Unique identifier for this task.
	 * @return non-null, unique task ID
	 */
	String id();

	/**
	 * Human-readable name of this task.
	 * @return non-null task name
	 */
	String name();

	/**
	 * Scheduled start date and time of this task.
	 * @return non-null start datetime
	 */
	LocalDateTime startDate();

	/**
	 * Scheduled end date and time of this task.
	 * Must be >= startDate().
	 * @return non-null end datetime
	 */
	LocalDateTime endDate();

	/**
	 * IDs of tasks that must complete before this task can start.
	 * Empty if no predecessors exist.
	 * @return Optional list of predecessor task IDs
	 */
	Optional<List<String>> predecessorIds();

	/**
	 * IDs of tasks that cannot start until this task completes.
	 * Empty if no successors exist.
	 * @return Optional list of successor task IDs
	 */
	Optional<List<String>> successorIds();

	/**
	 * ID of the parent task in the hierarchy (for tree structure).
	 * Empty if this is a root-level task.
	 * @return Optional parent task ID
	 */
	Optional<String> parentTaskId();
}
