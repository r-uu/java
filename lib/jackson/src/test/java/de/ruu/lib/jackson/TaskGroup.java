package de.ruu.lib.jackson;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.annotation.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Objects.isNull;

@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="jsonId", scope = TaskGroup.class)
public class TaskGroup
{
	private long jsonId = ThreadLocalRandom.current().nextLong();

	private String name;

	@JsonManagedReference("taskGroup-task")
	@Nullable private Set<Task> tasks;

	private TaskGroup() {} // with this no-args constructor jackson does not need a @JsonCreator annotated method

	public TaskGroup(String name) { this.name = Objects.requireNonNull(name, "name"); }

	public long         jsonId() { return jsonId; }
	public String       name()   { return name; }
	@Nullable public Set<Task> tasks()  { return tasks; }

	public boolean addTask(Task task)
	{
		if (isNull(tasks)) tasks = new HashSet<>();
		return tasks.add(task);
	}

	@Override public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof TaskGroup other)) return false;
		return Objects.equals(name, other.name);
	}

	@Override public int hashCode() { return Objects.hash(name); }

	@Override public String toString()
	{
		return "TaskGroup [jsonId=" + jsonId + ", name=" + name + "]";
	}
}
