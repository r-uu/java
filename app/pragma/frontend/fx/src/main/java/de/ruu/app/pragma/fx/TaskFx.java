package de.ruu.app.pragma.fx;

import de.ruu.app.pragma.core.PersistentTask;
import de.ruu.app.pragma.core.Task;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public class TaskFx implements PersistentTask<TaskGroupFx, TaskFx>
{
    private final LongProperty                        id                  = new SimpleLongProperty();
    private @Nullable Short                           version;
    private final StringProperty                      name                = new SimpleStringProperty();
    private final ObjectProperty<@Nullable TaskFx>    parentTask          = new SimpleObjectProperty<>();
    private final ObjectProperty<TaskGroupFx>         taskGroup           = new SimpleObjectProperty<>();
    private @Nullable ObservableSet<TaskFx>           subTasks;     // null = not loaded
    private @Nullable ObservableSet<TaskFx>           predecessors; // null = not loaded
    private @Nullable ObservableSet<TaskFx>           successors;   // null = not loaded
    private final StringProperty                      description         = new SimpleStringProperty();
    private final DoubleProperty                      workEstimateInitial = new SimpleDoubleProperty();
    private final DoubleProperty                      workEstimateCurrent = new SimpleDoubleProperty();
    private final DoubleProperty                      workActual          = new SimpleDoubleProperty();
    private final ObjectProperty<@Nullable LocalDate> scheduledStart      = new SimpleObjectProperty<>();
    private final ObjectProperty<@Nullable LocalDate> scheduledEnd        = new SimpleObjectProperty<>();
    private final BooleanProperty                     closed              = new SimpleBooleanProperty(false);

    public LongProperty                        idProperty                 () { return id;                  }
    public StringProperty                      nameProperty               () { return name;                }
    public ObjectProperty<@Nullable TaskFx>    parentTaskProperty         () { return parentTask;          }
    public ObjectProperty<TaskGroupFx>         taskGroupProperty          () { return taskGroup;           }
    public @Nullable ObservableSet<TaskFx>     subTasksObservable         () { return subTasks;            }
    public @Nullable ObservableSet<TaskFx>     predecessorsObservable     () { return predecessors;        }
    public @Nullable ObservableSet<TaskFx>     successorsObservable       () { return successors;          }
    public StringProperty                      descriptionProperty        () { return description;         }
    public DoubleProperty                      workEstimateInitialProperty() { return workEstimateInitial; }
    public DoubleProperty                      workEstimateCurrentProperty() { return workEstimateCurrent; }
    public DoubleProperty                      workActualProperty         () { return workActual;          }
    public ObjectProperty<@Nullable LocalDate> scheduledStartProperty     () { return scheduledStart;      }
    public ObjectProperty<@Nullable LocalDate> scheduledEndProperty       () { return scheduledEnd;        }
    public BooleanProperty                     closedProperty             () { return closed;              }

    public TaskFx(String name, TaskGroupFx taskGroup)
    {
        this.name.set(requireNonNull(name, "name"));
        requireNonNull(taskGroup, "taskGroup");
        taskGroup.addTask(this);
    }

    /** Mapping constructor — copies persisted metadata and scalar task fields from any PersistentTask. */
    public TaskFx(TaskGroupFx group, PersistentTask<?, ?> in)
    {
        if (in.id() != null) this.id.set(in.id());
        this.version = in.version();
        this.name.set(in.name());
        this.description.set(in.description().orElse(null));
        this.scheduledStart.set(in.scheduledStart().orElse(null));
        this.scheduledEnd.set(in.scheduledFinish().orElse(null));
        this.closed.set(in.closed());
        requireNonNull(group, "group");
        group.addTask(this);
    }

    /** Package-private — called exclusively by TaskGroupFx.addTask() to avoid recursion. */
    void taskGroupInternal(TaskGroupFx group) { this.taskGroup.set(group); }

    @Override public @Nullable Long  id      () { return id.get(); }
    @Override public @Nullable Short version () { return version ; }

    @Override public          String      name               () { return            name               .get() ; }
    @Override public          TaskGroupFx taskGroup          () { return            taskGroup          .get() ; }
    @Override public Optional<String>     description        () { return ofNullable(description        .get()); }
    @Override public Optional<Double>     workEstimateInitial() { return ofNullable(workEstimateInitial.get()); }
    @Override public Optional<Double>     workEstimateCurrent() { return ofNullable(workEstimateCurrent.get()); }
    @Override public Optional<Double>     workActual         () { return ofNullable(workActual         .get()); }
    @Override public Optional<LocalDate>  scheduledStart     () { return ofNullable(scheduledStart     .get()); }
    @Override public Optional<LocalDate>  scheduledFinish    () { return ofNullable(scheduledEnd       .get()); }
    @Override public          Boolean     closed             () { return            closed             .get() ; }

    @Override public TaskFx name               (          String    n) { name               .set(requireNonNull(n, "name")); return this; }
    @Override public TaskFx description        (@Nullable String    d) { description        .set(d)                        ; return this; }
    @Override public TaskFx workEstimateInitial(@Nullable Double    i) { workEstimateInitial.set(i)                        ; return this; }
    @Override public TaskFx workEstimateCurrent(@Nullable Double    c) { workEstimateCurrent.set(c)                        ; return this; }
    @Override public TaskFx workActual         (@Nullable Double    a) { workActual         .set(a)                        ; return this; }
    @Override public TaskFx scheduledStart     (@Nullable LocalDate s) { scheduledStart     .set(s)                        ; return this; }
    @Override public TaskFx scheduledFinish    (@Nullable LocalDate e) { scheduledEnd       .set(e)                        ; return this; }
    @Override public TaskFx closed             (          Boolean   c) { closed             .set(c)                        ; return this; }

    @Override public TaskFx taskGroup(TaskGroupFx group)
    {
        if (requireNonNull(group, "group") != this.taskGroup.get()) group.addTask(this);
        return this;
    }

    @Override
    public Optional<TaskFx> parentTask() { return ofNullable(parentTask.get()); }

    @Override
    public TaskFx parentTask(@Nullable TaskFx parent) { this.parentTask.set(parent); return this; }

    @Override public Optional<Set<TaskFx>> subTasks()     { return ofNullable(subTasks);     }
    @Override public Optional<Set<TaskFx>> predecessors() { return ofNullable(predecessors); }
    @Override public Optional<Set<TaskFx>> successors()   { return ofNullable(successors);   }

    @Override
    public void addSubTask(TaskFx child)
    {
        requireNonNull(child, "child");
        if (subTasks == null) subTasks = FXCollections.observableSet(new LinkedHashSet<>());
        if (subTasks.add(child)) child.parentTask(this);
    }

    @Override
    public void removeSubTask(TaskFx child)
    {
        requireNonNull(child, "child");
        if (subTasks != null && subTasks.remove(child))
            child.parentTask().filter(p -> p == this).ifPresent(p -> child.parentTask(null));
    }

    @Override
    public void addPredecessor(TaskFx predecessor)
    {
        requireNonNull(predecessor, "predecessor");
        if (predecessors == null) predecessors = FXCollections.observableSet(new LinkedHashSet<>());
        if (predecessors.add(predecessor)) predecessor.addSuccessor(this);
    }

    @Override
    public void removePredecessor(TaskFx predecessor)
    {
        requireNonNull(predecessor, "predecessor");
        if (predecessors != null && predecessors.remove(predecessor))
            predecessor.removeSuccessor(this);
    }

    @Override
    public void addSuccessor(TaskFx successor)
    {
        requireNonNull(successor, "successor");
        if (successors == null) successors = FXCollections.observableSet(new LinkedHashSet<>());
        if (successors.add(successor)) successor.addPredecessor(this);
    }

    @Override
    public void removeSuccessor(TaskFx successor)
    {
        requireNonNull(successor, "successor");
        if (successors != null && successors.remove(successor))
            successor.removePredecessor(this);
    }
}
