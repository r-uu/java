package de.ruu.lib.fx.control.gantt.api;

/**
 * Mutates tasks in the Gantt chart (CRUD operations).
 * 
 * <p>Separates reading (GanttDataProvider) from writing (GanttTaskMutator)
 * for clean architecture and independent testing.
 * 
 * <h3>Contract</h3>
 * <ul>
 *   <li>All operations should trigger appropriate events in the DataProvider</li>
 *   <li>Implementations may throw IllegalArgumentException for invalid operations</li>
 *   <li>Date conflicts (e.g., moving task to start before predecessor) should be validated</li>
 *   <li>Deletions may be cascading or rejected based on dependencies</li>
 * </ul>
 */
public interface GanttTaskMutator
{
	/**
	 * Creates a new task in the Gantt chart.
	 * 
	 * <p>The new task should be assigned a unique ID if not provided.
	 * Parent-child relationships are established via the task's parentTaskId().
	 * 
	 * @param newTask the task to create (typically a mutable implementation)
	 * @throws IllegalArgumentException if task is invalid (e.g., endDate before startDate)
	 */
	void createTask(GanttTask newTask);

	/**
	 * Updates an existing task's properties.
	 * 
	 * <p>Only updates fields provided by the updated task.
	 * Typically used for changing dates, name, or predecessor/successor relationships.
	 * 
	 * @param taskId ID of task to update
	 * @param updated the task with new values
	 * @throws IllegalArgumentException if task not found or updated values are invalid
	 */
	void updateTask(String taskId, GanttTask updated);

	/**
	 * Deletes a task from the Gantt chart.
	 * 
	 * <p>Depending on implementation:
	 * - May cascade delete child tasks
	 * - May reject deletion if task has successors (dependencies exist)
	 * - May automatically reparent children to the deleted task's parent
	 * 
	 * @param taskId ID of task to delete
	 * @throws IllegalArgumentException if task not found
	 */
	void deleteTask(String taskId);

	/**
	 * Adds a predecessor relationship: this task depends on the predecessor.
	 * 
	 * <p>The Gantt component will visualize this with a dependency arrow
	 * from predecessor to this task.
	 * 
	 * @param taskId the task that will depend on the predecessor
	 * @param predecessorId the task that must complete first
	 * @throws IllegalArgumentException if either task not found or relationship is invalid (e.g., circular)
	 */
	void addPredecessor(String taskId, String predecessorId);

	/**
	 * Removes a predecessor relationship.
	 * 
	 * @param taskId the task
	 * @param predecessorId the predecessor to unlink
	 * @throws IllegalArgumentException if either task not found or relationship doesn't exist
	 */
	void removePredecessor(String taskId, String predecessorId);
}
