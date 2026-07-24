package de.ruu.lib.fx.control.gantt.demo;

import de.ruu.lib.fx.control.gantt.api.GanttDataProvider;
import de.ruu.lib.fx.control.gantt.api.GanttTask;
import jakarta.enterprise.context.Dependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory mock implementation of GanttDataProvider for demonstrations.
 * 
 * <p>Stores all tasks in memory. Used for:
 * - Standalone demo application
 * - Unit testing the Gantt component
 * - Integration testing
 * 
 * <p>All mutations are immediately applied to the in-memory store.
 */
@Dependent
public class MockGanttDataProvider implements GanttDataProvider {
	private static final Logger log = LoggerFactory.getLogger(MockGanttDataProvider.class);
	
	private final Map<String, MockGanttTask> tasksById = new HashMap<>();
	private int taskCounter = 0;

	public MockGanttDataProvider() {
		// Load sample data
		initSampleData();
	}

	private void initSampleData() {
		LocalDateTime now = LocalDateTime.now();

		// Root tasks
		MockGanttTask project1 = new MockGanttTask("t1", "Design Phase", 
			now.plusDays(0), now.plusDays(5));
		MockGanttTask project2 = new MockGanttTask("t2", "Implementation Phase", 
			now.plusDays(6), now.plusDays(15));
		MockGanttTask project3 = new MockGanttTask("t3", "Testing Phase", 
			now.plusDays(16), now.plusDays(20));

		// Add predecessor relationships
		project2.setPredecessorIds(List.of("t1"));
		project3.setPredecessorIds(List.of("t2"));

		// Subtasks under Design
		MockGanttTask task1_1 = new MockGanttTask("t1.1", "Requirements", 
			now.plusDays(0), now.plusDays(2));
		task1_1.setParentTaskId(java.util.Optional.of("t1"));
		
		MockGanttTask task1_2 = new MockGanttTask("t1.2", "Mockups", 
			now.plusDays(2), now.plusDays(5));
		task1_2.setParentTaskId(java.util.Optional.of("t1"));
		task1_2.setPredecessorIds(List.of("t1.1"));

		// Subtasks under Implementation
		MockGanttTask task2_1 = new MockGanttTask("t2.1", "Backend API", 
			now.plusDays(6), now.plusDays(12));
		task2_1.setParentTaskId(java.util.Optional.of("t2"));
		
		MockGanttTask task2_2 = new MockGanttTask("t2.2", "Frontend UI", 
			now.plusDays(10), now.plusDays(15));
		task2_2.setParentTaskId(java.util.Optional.of("t2"));

		// Add all to store
		tasksById.put("t1", project1);
		tasksById.put("t2", project2);
		tasksById.put("t3", project3);
		tasksById.put("t1.1", task1_1);
		tasksById.put("t1.2", task1_2);
		tasksById.put("t2.1", task2_1);
		tasksById.put("t2.2", task2_2);

		log.info("Initialized mock data with {} tasks", tasksById.size());
	}

	@Override
	public List<GanttTask> loadTasks() {
		return new ArrayList<>(tasksById.values());
	}

	@Override
	public void refreshTasks() {
		log.info("Refreshing tasks (mock: no-op)");
		// In a real implementation, would reload from data source
	}

	@Override
	public void createTask(GanttTask newTask) {
		if (tasksById.containsKey(newTask.id())) {
			throw new IllegalArgumentException("Task already exists: " + newTask.id());
		}
		if (!(newTask instanceof MockGanttTask)) {
			throw new IllegalArgumentException("Only MockGanttTask supported");
		}
		tasksById.put(newTask.id(), (MockGanttTask) newTask);
		log.info("Created task: {}", newTask.id());
	}

	@Override
	public void updateTask(String taskId, GanttTask updated) {
		MockGanttTask existing = tasksById.get(taskId);
		if (existing == null) {
			throw new IllegalArgumentException("Task not found: " + taskId);
		}
		if (!(updated instanceof MockGanttTask)) {
			throw new IllegalArgumentException("Only MockGanttTask supported");
		}
		MockGanttTask updatedTask = (MockGanttTask) updated;
		existing.setName(updatedTask.name());
		existing.setStartDate(updatedTask.startDate());
		existing.setEndDate(updatedTask.endDate());
		log.info("Updated task: {}", taskId);
	}

	@Override
	public void deleteTask(String taskId) {
		if (!tasksById.containsKey(taskId)) {
			throw new IllegalArgumentException("Task not found: " + taskId);
		}
		tasksById.remove(taskId);
		log.info("Deleted task: {}", taskId);
	}

	@Override
	public void addPredecessor(String taskId, String predecessorId) {
		MockGanttTask task = tasksById.get(taskId);
		MockGanttTask predecessor = tasksById.get(predecessorId);
		
		if (task == null) throw new IllegalArgumentException("Task not found: " + taskId);
		if (predecessor == null) throw new IllegalArgumentException("Predecessor not found: " + predecessorId);

		List<String> preds = new ArrayList<>(task.predecessorIds().orElse(List.of()));
		if (!preds.contains(predecessorId)) {
			preds.add(predecessorId);
			task.setPredecessorIds(preds);
			log.info("Added predecessor {} to task {}", predecessorId, taskId);
		}
	}

	@Override
	public void removePredecessor(String taskId, String predecessorId) {
		MockGanttTask task = tasksById.get(taskId);
		if (task == null) throw new IllegalArgumentException("Task not found: " + taskId);

		List<String> preds = new ArrayList<>(task.predecessorIds().orElse(List.of()));
		if (preds.remove(predecessorId)) {
			task.setPredecessorIds(preds);
			log.info("Removed predecessor {} from task {}", predecessorId, taskId);
		}
	}
}
