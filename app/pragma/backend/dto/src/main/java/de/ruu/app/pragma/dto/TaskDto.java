package de.ruu.app.pragma.dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.ruu.app.pragma.core.PersistentTask;
import de.ruu.app.pragma.core.Task;
import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "jsonId", scope = TaskDto.class)
public class TaskDto implements PersistentTask<TaskGroupDto, TaskDto>
{
    private           UUID         jsonId  = UUID.randomUUID();
    private @Nullable Long         id;
    private @Nullable Short        version;
    private @NotBlank String       name;
    private @Nullable TaskDto      parentTask;
    private           TaskGroupDto taskGroup;
    private @Nullable Set<TaskDto> subTasks;      // null = not yet loaded
    private @Nullable Set<TaskDto> predecessors;  // null = not yet loaded
    private @Nullable Set<TaskDto> successors;    // null = not yet loaded
    private @Nullable String       description;
    private @Nullable Double       workEstimateInitial;
    private @Nullable Double       workEstimateCurrent;
    private @Nullable Double       workActual;
    private @Nullable LocalDate    scheduledStart;
    private @Nullable LocalDate    scheduledFinish;
    private           Boolean      closed = false;

    /** For JSON deserialization only. */
    protected TaskDto() { name = ""; }

    public TaskDto(String name, TaskGroupDto taskGroup)
    {
        this.name = requireNonNull(name,      "name");
        requireNonNull(taskGroup, "taskGroup");
        taskGroup.addTask(this);
    }

    /** Mapping constructor — copies persisted metadata and scalar task fields from any PersistentTask. */
    public TaskDto(TaskGroupDto group, PersistentTask<?, ?> in)
    {
        this.id      = in.id();
        this.version = in.version();
        this.name    = in.name();
        // direct field set instead of group.addTask(this) — avoids circular ref
        // (group.tasks → task → taskGroup → group.tasks) during JSON serialization
        this.taskGroup = requireNonNull(group, "group");
    }

    /** Package-private — called exclusively by TaskGroupDto.addTask() to avoid recursion. */
    void taskGroupInternal(TaskGroupDto group) { this.taskGroup = group; }

    @Override public @Nullable Long  id     () { return id     ; }
    @Override public @Nullable Short version() { return version; }

    @Override public          String        name               () { return            name                ; }
    @Override public Optional<TaskDto>      parentTask         () { return ofNullable(parentTask         ); }
    @Override public Optional<Set<TaskDto>> subTasks           () { return ofNullable(subTasks           ); }
    @Override public Optional<Set<TaskDto>> predecessors       () { return ofNullable(predecessors       ); }
    @Override public Optional<Set<TaskDto>> successors         () { return ofNullable(successors         ); }
    @Override public Optional<String>       description        () { return ofNullable(description        ); }
    @Override public Optional<Double>       workEstimateInitial() { return ofNullable(workEstimateInitial); }
    @Override public Optional<Double>       workEstimateCurrent() { return ofNullable(workEstimateCurrent); }
    @Override public Optional<Double>       workActual         () { return ofNullable(workActual         ); }
    @Override public Optional<LocalDate>    scheduledStart     () { return ofNullable(scheduledStart     ); }
    @Override public Optional<LocalDate>    scheduledFinish    () { return ofNullable(scheduledFinish    ); }
    @Override public          Boolean       closed             () { return            closed              ; }

    @Override public TaskDto name               (          String    n) { name                = requireNonNull(n, "name"); return this; }
    @Override public TaskDto parentTask         (@Nullable TaskDto   p) { parentTask          =                p         ; return this; }
    @Override public TaskDto description        (@Nullable String    d) { description         =                d         ; return this; }
    @Override public TaskDto workEstimateInitial(@Nullable Double    e) { workEstimateInitial =                e         ; return this; }
    @Override public TaskDto workEstimateCurrent(@Nullable Double    e) { workEstimateCurrent =                e         ; return this; }
    @Override public TaskDto workActual         (@Nullable Double    a) { workActual          =                a         ; return this; }
    @Override public TaskDto scheduledStart     (@Nullable LocalDate s) { scheduledStart      =                s         ; return this; }
    @Override public TaskDto scheduledFinish    (@Nullable LocalDate f) { scheduledFinish     =                f         ; return this; }
    @Override public TaskDto closed             (          Boolean   c) { closed              =                c         ; return this; }

    @Override
    public TaskGroupDto taskGroup() { return taskGroup; }

    @Override
    public TaskDto taskGroup(TaskGroupDto group)
    {
        if (requireNonNull(group, "group") != this.taskGroup) group.addTask(this);
        return this;
    }

    @Override
    public void addSubTask(TaskDto child)
    {
        requireNonNull(child, "child");
        if (subTasks == null) subTasks = new LinkedHashSet<>();
        if (subTasks.add(child)) child.parentTask(this);
    }

    @Override
    public void removeSubTask(TaskDto child)
    {
        requireNonNull(child, "child");
        if (subTasks != null && subTasks.remove(child))
            child.parentTask().filter(p -> p == this).ifPresent(p -> child.parentTask(null));
    }

    @Override
    public void addPredecessor(TaskDto predecessor)
    {
        requireNonNull(predecessor, "predecessor");
        if (predecessors == null) predecessors = new LinkedHashSet<>();
        if (predecessors.add(predecessor)) predecessor.addSuccessor(this);
    }

    @Override
    public void removePredecessor(TaskDto predecessor)
    {
        requireNonNull(predecessor, "predecessor");
        if (predecessors != null && predecessors.remove(predecessor))
            predecessor.removeSuccessor(this);
    }

    @Override
    public void addSuccessor(TaskDto successor)
    {
        requireNonNull(successor, "successor");
        if (successors == null) successors = new LinkedHashSet<>();
        if (successors.add(successor)) successor.addPredecessor(this);
    }

    @Override
    public void removeSuccessor(TaskDto successor)
    {
        requireNonNull(successor, "successor");
        if (successors != null && successors.remove(successor))
            successor.removePredecessor(this);
    }
}
