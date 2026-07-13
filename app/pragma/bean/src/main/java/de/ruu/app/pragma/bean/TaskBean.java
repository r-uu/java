package de.ruu.app.pragma.bean;

import de.ruu.app.pragma.core.Task;
import de.ruu.app.pragma.dto.TaskDto;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public class TaskBean implements Task<TaskGroupBean, TaskBean>
{
    private @Nullable Long          id;
    private @Nullable Short         version;
    private           String        name;
    private @Nullable TaskBean      parentTask;
    private           TaskGroupBean taskGroup;
    private @Nullable Set<TaskBean> subTasks;     // null = not yet loaded
    private @Nullable Set<TaskBean> predecessors; // null = not yet loaded
    private @Nullable Set<TaskBean> successors;   // null = not yet loaded
    private @Nullable String        description;
    private @Nullable Double        workEstimateInitial;
    private @Nullable Double        workEstimateCurrent;
    private @Nullable Double        workActual;
    private @Nullable LocalDate     scheduledStart;
    private @Nullable LocalDate     scheduledFinish;
    private           Boolean       closed = false;

    public TaskBean(TaskGroupBean taskGroup, String name)
    {
        this.name = requireNonNull(name, "name");
        requireNonNull(taskGroup, "taskGroup");
        taskGroup.addTask(this);
    }

    /** Mapping constructor — copies scalar fields from a TaskDto. */
    public TaskBean(TaskGroupBean group, TaskDto in)
    {
        this.id                  = in.id();
        this.version             = in.version();
        this.name                = in.name();
        this.description         = in.description().orElse(null);
        this.scheduledStart      = in.scheduledStart().orElse(null);
        this.scheduledFinish     = in.scheduledFinish().orElse(null);
        this.workEstimateInitial = in.workEstimateInitial().orElse(null);
        this.workEstimateCurrent = in.workEstimateCurrent().orElse(null);
        this.workActual          = in.workActual().orElse(null);
        this.closed              = in.closed();
        requireNonNull(group, "group");
        group.addTask(this);
    }

    /** Package-private — called exclusively by TaskGroupBean.addTask() to avoid recursion. */
    void taskGroupInternal(TaskGroupBean group) { this.taskGroup = group; }

    @Override public @Nullable Long                    id                 () { return id;                              }
    public    @Nullable Short                          version            () { return version;                         }
    @Override public           String                  name               () { return name;                            }
    @Override public           Optional<TaskBean>      parentTask         () { return ofNullable(parentTask         ); }
    @Override public           Optional<Set<TaskBean>> subTasks           () { return ofNullable(subTasks           ); }
    @Override public           Optional<Set<TaskBean>> predecessors       () { return ofNullable(predecessors       ); }
    @Override public           Optional<Set<TaskBean>> successors         () { return ofNullable(successors         ); }
    @Override public           Optional<String>        description        () { return ofNullable(description        ); }
    @Override public           Optional<LocalDate>     scheduledStart     () { return ofNullable(scheduledStart     ); }
    @Override public           Optional<LocalDate>     scheduledFinish    () { return ofNullable(scheduledFinish    ); }
    @Override public           Optional<Double>        workEstimateInitial() { return ofNullable(workEstimateInitial); }
    @Override public           Optional<Double>        workEstimateCurrent() { return ofNullable(workEstimateCurrent); }
    @Override public           Optional<Double>        workActual         () { return ofNullable(workActual         ); }
    @Override public           Boolean                 closed             () { return closed;                      }

    @Override public TaskBean name               (          String    name               ) { this.name                = requireNonNull(name, "name"); return this; }
    @Override public TaskBean description        (@Nullable String    description        ) { this.description         = description;                  return this; }
    @Override public TaskBean workEstimateInitial(@Nullable Double    workEstimateInitial) { this.workEstimateInitial = workEstimateInitial;          return this; }
    @Override public TaskBean workEstimateCurrent(@Nullable Double    workEstimateCurrent) { this.workEstimateCurrent = workEstimateCurrent;          return this; }
    @Override public TaskBean workActual         (@Nullable Double    workActual         ) { this.workActual          = workActual;                   return this; }
    @Override public TaskBean scheduledStart     (@Nullable LocalDate scheduledStart     ) { this.scheduledStart      = scheduledStart;               return this; }
    @Override public TaskBean scheduledFinish    (@Nullable LocalDate scheduledFinish    ) { this.scheduledFinish     = scheduledFinish;              return this; }
    @Override public TaskBean closed             (          Boolean   closed             ) { this.closed              = closed;                       return this; }
    @Override public TaskBean parentTask         (@Nullable TaskBean  parent             ) { this.parentTask          = parent;                       return this; }

    @Override
    public TaskGroupBean taskGroup() { return taskGroup; }

    @Override
    public TaskBean taskGroup(TaskGroupBean group)
    {
        if (requireNonNull(group, "group") != this.taskGroup) group.addTask(this);
        return this;
    }

    @Override
    public void addSubTask(TaskBean child)
    {
        requireNonNull(child, "child");
        if (subTasks == null) subTasks = new LinkedHashSet<>();
        if (subTasks.add(child)) child.parentTask(this);
    }

    @Override
    public void removeSubTask(TaskBean child)
    {
        requireNonNull(child, "child");
        if (subTasks != null && subTasks.remove(child))
            child.parentTask().filter(p -> p == this).ifPresent(p -> child.parentTask(null));
    }

    @Override
    public void addPredecessor(TaskBean predecessor)
    {
        requireNonNull(predecessor, "predecessor");
        if (predecessors == null) predecessors = new LinkedHashSet<>();
        if (predecessors.add(predecessor)) predecessor.addSuccessor(this);
    }

    @Override
    public void removePredecessor(TaskBean predecessor)
    {
        requireNonNull(predecessor, "predecessor");
        if (predecessors != null && predecessors.remove(predecessor))
            predecessor.removeSuccessor(this);
    }

    @Override
    public void addSuccessor(TaskBean successor)
    {
        requireNonNull(successor, "successor");
        if (successors == null) successors = new LinkedHashSet<>();
        if (successors.add(successor)) successor.addPredecessor(this);
    }

    @Override
    public void removeSuccessor(TaskBean successor)
    {
        requireNonNull(successor, "successor");
        if (successors != null && successors.remove(successor))
            successor.removePredecessor(this);
    }
}