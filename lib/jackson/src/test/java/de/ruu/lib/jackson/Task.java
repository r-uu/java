package de.ruu.lib.jackson;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Objects.isNull;

@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="jsonId", scope = Task.class)
public class Task
{
	private static final Logger log = LoggerFactory.getLogger(Task.class);

	private final long jsonId = ThreadLocalRandom.current().nextLong();
	/** may be <pre>null</pre> if corresponding jpa instance was not (yet) persisted. */
	@Nullable private Long   id;
	/** may be <pre>null</pre> if corresponding jpa instance was not (yet) persisted. */
	@Nullable private Short  version;
	private String name;

	@Nullable private String    description;
	@Nullable private LocalDate start;
	@Nullable private LocalDate end;
	@Nullable private Boolean   closed;

	@JsonBackReference("taskGroup-task")
	private TaskGroup group;

	@Nullable private Task      superTask;
	@Nullable private Set<Task> subTasks;
	@Nullable private Set<Task> predecessors;
	@Nullable private Set<Task> successors;

	private Task() {} // with this no-args constructor jackson does not need a @JsonCreator annotated method

	public Task(TaskGroup group, String name)
	{
		this.group = Objects.requireNonNull(group, "group");
		this.name  = Objects.requireNonNull(name,  "name");

		group.addTask(this);
	}

	public long      jsonId()      { return jsonId; }
	@Nullable public Long id()            { return id; }
	@Nullable public Short version()      { return version; }
	public String    name()        { return name; }
	@Nullable public String description() { return description; }
	@Nullable public LocalDate start()    { return start; }
	@Nullable public LocalDate end()      { return end; }
	@Nullable public Boolean closed()     { return closed; }
	public TaskGroup group()       { return group; }
	@Nullable public Task superTask()     { return superTask; }
	@Nullable public Set<Task> subTasks()      { return subTasks; }
	@Nullable public Set<Task> predecessors()  { return predecessors; }
	@Nullable public Set<Task> successors()    { return successors; }

	public void description(String description) { this.description = description; }
	public void start(LocalDate start)          { this.start = start; }
	public void end(LocalDate end)              { this.end = end; }
	public void closed(Boolean closed)          { this.closed = Objects.requireNonNull(closed, "closed"); }

	public boolean superTask(Task task) { return task.addSubTask(this); }

	public boolean addSubTask(Task task)
	{
		if (isNull(subTasks)) subTasks = new HashSet<>();
		task.superTask = this;
		return subTasks.add(task);
	}

	public boolean addPredecessor(Task task)
	{
		if (    predecessors == null)      predecessors = new HashSet<>();
		if (task.successors  == null) task.successors   = new HashSet<>();

		if (task.successors.add(this))
				if (predecessors.add(task)) return true;
				else task.successors.remove(this); // rollback
		return false;
	}

	public boolean addSuccessor(Task task)
	{
		if (     successors   == null)      successors   = new HashSet<>();
		if (task.predecessors == null) task.predecessors = new HashSet<>();

		if (task.predecessors.add(this))
				if (successors.add(task)) return true;
				else task.predecessors.remove(this); // rollback
		return false;
	}

	@Override public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof Task other)) return false;
		return Objects.equals(id,          other.id)
			&& Objects.equals(version,     other.version)
			&& Objects.equals(name,        other.name)
			&& Objects.equals(description, other.description)
			&& Objects.equals(start,       other.start)
			&& Objects.equals(end,         other.end)
			&& Objects.equals(closed,      other.closed);
	}

	@Override public int hashCode()
	{
		return Objects.hash(id, version, name, description, start, end, closed);
	}

	@Override public String toString()
	{
		return "Task [jsonId=" + jsonId
				+ ", id=" + id
				+ ", version=" + version
				+ ", name=" + name
				+ ", description=" + description
				+ ", start=" + start
				+ ", end=" + end
				+ ", closed=" + closed
				+ "]";
	}
}
