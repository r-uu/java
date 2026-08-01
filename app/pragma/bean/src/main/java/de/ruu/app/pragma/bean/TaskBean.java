package de.ruu.app.pragma.bean;

import de.ruu.app.pragma.core.PersistentTask;
import de.ruu.app.pragma.core.TaskPriority;
import de.ruu.app.pragma.core.TaskStatus;
import de.ruu.app.pragma.core.Task;
import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public class TaskBean implements PersistentTask<TaskGroupBean, TaskBean>
{
    private @Nullable Long          id;
    private @Nullable Short         version;
    private @NotBlank String        name;
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
    private           TaskStatus    status = TaskStatus.NEW;
    private           TaskPriority  priority = TaskPriority.NORMAL;

    public TaskBean(TaskGroupBean taskGroup, String name)
    {
        this.name = requireNonNull(name, "name");
        requireNonNull(taskGroup, "taskGroup");
        taskGroup.addTask(this);
    }

    /** Mapping constructor — copies persisted metadata and scalar task fields from any PersistentTask. */
    public TaskBean(TaskGroupBean group, PersistentTask<?, ?> in)
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
        this.status              = in.status();
        this.priority            = in.priority();
        requireNonNull(group, "group");
        group.addTask(this);
    }

    /** Package-private — called exclusively by TaskGroupBean.addTask() to avoid recursion. */
    void taskGroupInternal(TaskGroupBean group) { this.taskGroup = group; }

    @Override public @Nullable Long  id     () { return id;      }
    @Override public @Nullable Short version() { return version; }

    @Override public          String         name               () { return name                           ; }
    @Override public Optional<TaskBean>      parentTask         () { return ofNullable(parentTask         ); }
    @Override public Optional<Set<TaskBean>> subTasks           () { return ofNullable(subTasks           ); }
    @Override public Optional<Set<TaskBean>> predecessors       () { return ofNullable(predecessors       ); }
    @Override public Optional<Set<TaskBean>> successors         () { return ofNullable(successors         ); }
    @Override public Optional<String>        description        () { return ofNullable(description        ); }
    @Override public Optional<LocalDate>     scheduledStart     () { return ofNullable(scheduledStart     ); }
    @Override public Optional<LocalDate>     scheduledFinish    () { return ofNullable(scheduledFinish    ); }
    @Override public Optional<Double>        workEstimateInitial() { return ofNullable(workEstimateInitial); }
    @Override public Optional<Double>        workEstimateCurrent() { return ofNullable(workEstimateCurrent); }
    @Override public Optional<Double>        workActual         () { return ofNullable(workActual         ); }
    @Override public          TaskStatus     status             () { return status                         ; }
    @Override public          TaskPriority   priority           () { return priority                       ; }

    @Override public TaskBean name               (          String       n) { name                = requireNonNull(n, "name"); return this; }
    @Override public TaskBean parentTask         (@Nullable TaskBean     p) { parentTask          =                p         ; return this; }
    @Override public TaskBean description        (@Nullable String       d) { description         =                d         ; return this; }
    @Override public TaskBean workEstimateInitial(@Nullable Double       e) { workEstimateInitial =                e         ; return this; }
    @Override public TaskBean workEstimateCurrent(@Nullable Double       e) { workEstimateCurrent =                e         ; return this; }
    @Override public TaskBean workActual         (@Nullable Double       a) { workActual          =                a         ; return this; }
    @Override public TaskBean scheduledStart     (@Nullable LocalDate    s) { scheduledStart      =                s         ; return this; }
    @Override public TaskBean scheduledFinish    (@Nullable LocalDate    f) { scheduledFinish     =                f         ; return this; }
    @Override public TaskBean status             (          TaskStatus   s) { status              =                s         ; return this; }
    @Override public TaskBean priority           (          TaskPriority p) { priority            =                p         ; return this; }

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