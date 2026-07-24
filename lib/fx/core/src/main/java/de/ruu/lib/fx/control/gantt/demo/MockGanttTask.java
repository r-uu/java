package de.ruu.lib.fx.control.gantt.demo;

import de.ruu.lib.fx.control.gantt.api.GanttTask;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Mock implementation of GanttTask for demonstration/testing.
 * 
 * <p>This is a mutable implementation used in the demo and unit tests.
 * In production, you would typically wrap your domain model (TaskBean, etc)
 * with an adapter implementing GanttTask.
 */
public class MockGanttTask implements GanttTask {
	private final String id;
	private String name;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private List<String> predecessorIds = List.of();
	private List<String> successorIds = List.of();
	private Optional<String> parentTaskId = Optional.empty();

	public MockGanttTask(String id, String name, LocalDateTime start, LocalDateTime end) {
		this.id = id;
		this.name = name;
		this.startDate = start;
		this.endDate = end;
	}

	@Override public String id() { return id; }
	@Override public String name() { return name; }
	@Override public LocalDateTime startDate() { return startDate; }
	@Override public LocalDateTime endDate() { return endDate; }
	@Override public Optional<List<String>> predecessorIds() { 
		return predecessorIds.isEmpty() ? Optional.empty() : Optional.of(predecessorIds); 
	}
	@Override public Optional<List<String>> successorIds() { 
		return successorIds.isEmpty() ? Optional.empty() : Optional.of(successorIds); 
	}
	@Override public Optional<String> parentTaskId() { return parentTaskId; }

	// Setters for mutability (used in demo/tests)
	public void setName(String name) { this.name = name; }
	public void setStartDate(LocalDateTime start) { this.startDate = start; }
	public void setEndDate(LocalDateTime end) { this.endDate = end; }
	public void setPredecessorIds(List<String> ids) { this.predecessorIds = ids; }
	public void setSuccessorIds(List<String> ids) { this.successorIds = ids; }
	public void setParentTaskId(Optional<String> parentId) { this.parentTaskId = parentId; }
}
