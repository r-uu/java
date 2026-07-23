package de.ruu.lib.fx.control.gantt.api;

import java.util.List;
import java.util.Optional;

/**
 * Provides hierarchical access to Gantt tasks.
 * 
 * <p>This interface defines how the Gantt component navigates the task tree.
 * Implementations can be backed by any data structure (flat list, actual tree,
 * database, REST API, etc).
 * 
 * <h3>Contract</h3>
 * <ul>
 *   <li>All task IDs returned must correspond to tasks that can be retrieved via getTask()</li>
 *   <li>getRootTasks() returns all top-level tasks (those with no parent)</li>
 *   <li>getChildren() returns only direct children, not descendants</li>
 *   <li>Once resolved, tasks should remain consistent within a request/render cycle</li>
 * </ul>
 * 
 * <h3>Example Implementation</h3>
 * <pre>
 * public class PragmaGanttHierarchy implements GanttTaskHierarchy {
 *     private final Map&lt;String, GanttTask&gt; tasksById;
 *     private final Map&lt;String, List&lt;GanttTask&gt;&gt; childrenByParent;
 *     
 *     public PragmaGanttHierarchy(List&lt;GanttTask&gt; allTasks) {
 *         this.tasksById = allTasks.stream()
 *             .collect(toMap(GanttTask::id, identity()));
 *         this.childrenByParent = allTasks.stream()
 *             .filter(t -&gt; t.parentTaskId().isPresent())
 *             .collect(groupingBy(t -&gt; t.parentTaskId().get()));
 *     }
 *     
 *     @Override
 *     public GanttTask getTask(String id) {
 *         return tasksById.get(id); // or throw if not found
 *     }
 *     
 *     @Override
 *     public List&lt;GanttTask&gt; getRootTasks() {
 *         return tasksById.values().stream()
 *             .filter(t -&gt; t.parentTaskId().isEmpty())
 *             .collect(toList());
 *     }
 *     
 *     @Override
 *     public List&lt;GanttTask&gt; getChildren(GanttTask parent) {
 *         return childrenByParent.getOrDefault(parent.id(), List.of());
 *     }
 * }
 * </pre>
 */
public interface GanttTaskHierarchy
{
	/**
	 * Retrieves a task by its ID.
	 * 
	 * @param id task ID to look up
	 * @return the GanttTask with this ID
	 * @throws IllegalArgumentException if task not found
	 */
	GanttTask getTask(String id);

	/**
	 * Retrieves all root-level tasks (no parent).
	 * These become the top-level nodes in the TreeView.
	 * 
	 * @return list of root tasks (possibly empty, never null)
	 */
	List<GanttTask> getRootTasks();

	/**
	 * Retrieves all direct children of a task.
	 * Should return only immediate children, not descendants.
	 * 
	 * @param parent the parent task
	 * @return list of child tasks (possibly empty, never null)
	 */
	List<GanttTask> getChildren(GanttTask parent);

	/**
	 * Quick check: does this task have children?
	 * Optimization to avoid calling getChildren() if not needed.
	 * 
	 * @param task the task to check
	 * @return true if task has at least one child
	 */
	boolean hasChildren(GanttTask task);
}
