package de.ruu.app.pragma.jpa;

import de.ruu.app.pragma.core.TaskEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

@Entity
@Table(name = "task")
public class TaskJPA implements TaskEntity<TaskGroupJPA, TaskJPA>
{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "task_seq")
    @SequenceGenerator(name = "task_seq", sequenceName = "task_seq", allocationSize = 50)
    private @Nullable Long id;

    @Version
    private @Nullable Short version;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id")
    private @Nullable TaskJPA parentTask;

    // EAGER: taskGroup is always loaded — Optional.empty() never occurs for a valid task
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "task_group_id", nullable = false)
    private TaskGroupJPA taskGroup;

    @OneToMany(mappedBy = "parentTask", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private @Nullable Set<TaskJPA> subTasks;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "task_predecessors_successors",
        joinColumns        = @JoinColumn(name = "idPredecessor"),
        inverseJoinColumns = @JoinColumn(name = "idSuccessor"  )
    )
    private @Nullable Set<TaskJPA> predecessors;

    @ManyToMany(mappedBy = "predecessors", fetch = FetchType.LAZY)
    private @Nullable Set<TaskJPA> successors;

    @Column(length = 4000)
    private @Nullable String    description;

    @Column
    private @Nullable Double workEstimateInitial;

    @Column
    private @Nullable Double workEstimateCurrent;

    @Column
    private @Nullable Double workActual;

    @Column
    private @Nullable LocalDate scheduledStart;

    @Column
    private @Nullable LocalDate scheduledFinish;

    @Column(nullable = false, columnDefinition = "boolean not null default false")
    private           Boolean   closed = false;

    /** JPA-required no-arg constructor. */
    protected TaskJPA() { name = ""; taskGroup = new TaskGroupJPA(); }

    public TaskJPA(String name, TaskGroupJPA taskGroup)
    {
        this.name = requireNonNull(name,      "name");
        requireNonNull(taskGroup, "taskGroup");
        taskGroup.addTask(this);
    }

    /** Package-private — called exclusively by TaskGroupJPA.addTask() to avoid recursion. */
    void taskGroupInternal(TaskGroupJPA group) { this.taskGroup = group; }

    @Override public @Nullable Long  id     () { return id     ; }
    @Override public @Nullable Short version() { return version; }

    @Override public String                 name               () { return            name                ; }
    @Override public Optional<    TaskJPA>  parentTask         () { return ofNullable(parentTask         ); }
    @Override public Optional<Set<TaskJPA>> subTasks           () { return ofNullable(subTasks           ); }
    @Override public Optional<Set<TaskJPA>> predecessors       () { return ofNullable(predecessors       ); }
    @Override public Optional<Set<TaskJPA>> successors         () { return ofNullable(successors         ); }
    @Override public Optional<String>       description        () { return ofNullable(description        ); }
    @Override public Optional<Double>       workEstimateInitial() { return ofNullable(workEstimateInitial); }
    @Override public Optional<Double>       workEstimateCurrent() { return ofNullable(workEstimateCurrent); }
    @Override public Optional<Double>       workActual         () { return ofNullable(workActual         ); }
    @Override public Optional<LocalDate>    scheduledStart     () { return ofNullable(scheduledStart     ); }
    @Override public Optional<LocalDate>    scheduledFinish    () { return ofNullable(scheduledFinish    ); }
    @Override public Boolean                closed             () { return            closed              ; }

    @Override public TaskJPA name               (          String    n) { name                = requireNonNull(n, "name"); return this; }
    @Override public TaskJPA parentTask         (@Nullable TaskJPA   p) { parentTask          =                p         ; return this; }
    @Override public TaskJPA description        (@Nullable String    d) { description         =                d         ; return this; }
    @Override public TaskJPA workEstimateInitial(@Nullable Double    i) { workEstimateInitial =                i         ; return this; }
    @Override public TaskJPA workEstimateCurrent(@Nullable Double    c) { workEstimateCurrent =                c         ; return this; }
    @Override public TaskJPA workActual         (@Nullable Double    a) { workActual          =                a         ; return this; }
    @Override public TaskJPA scheduledStart     (@Nullable LocalDate s) { scheduledStart      =                s         ; return this; }
    @Override public TaskJPA scheduledFinish    (@Nullable LocalDate e) { scheduledFinish     =                e         ; return this; }
    @Override public TaskJPA closed             (          Boolean   c) { closed              =                c         ; return this; }

    @Override
    public TaskGroupJPA taskGroup() { return taskGroup; }

    @Override
    public TaskJPA taskGroup(TaskGroupJPA group)
    {
        if (requireNonNull(group, "group") != this.taskGroup) group.addTask(this);
        return this;
    }

    @Override
    public void addSubTask(TaskJPA child)
    {
        requireNonNull(child, "child");
        if (subTasks == null) subTasks = new LinkedHashSet<>();
        if (subTasks.add(child)) child.parentTask(this);
    }

    @Override
    public void removeSubTask(TaskJPA child)
    {
        requireNonNull(child, "child");
        if (subTasks != null && subTasks.remove(child))
            child.parentTask().filter(p -> p == this).ifPresent(p -> child.parentTask(null));
    }

    @Override
    public void addPredecessor(TaskJPA predecessor)
    {
        requireNonNull(predecessor, "predecessor");
        if (predecessors == null) predecessors = new LinkedHashSet<>();
        if (predecessors.add(predecessor)) predecessor.addSuccessor(this);
    }

    @Override
    public void removePredecessor(TaskJPA predecessor)
    {
        requireNonNull(predecessor, "predecessor");
        if (predecessors != null && predecessors.remove(predecessor))
            predecessor.removeSuccessor(this);
    }

    @Override
    public void addSuccessor(TaskJPA successor)
    {
        requireNonNull(successor, "successor");
        if (successors == null) successors = new LinkedHashSet<>();
        if (successors.add(successor)) successor.addPredecessor(this);
    }

    @Override
    public void removeSuccessor(TaskJPA successor)
    {
        requireNonNull(successor, "successor");
        if (successors != null && successors.remove(successor))
            successor.removePredecessor(this);
    }
}
