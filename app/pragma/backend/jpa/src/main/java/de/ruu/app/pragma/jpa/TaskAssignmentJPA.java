package de.ruu.app.pragma.jpa;

import de.ruu.app.pragma.core.AssignmentTargetType;
import de.ruu.app.pragma.core.AssignmentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

@Entity
@Table(name = "task_assignment")
public class TaskAssignmentJPA
{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "task_assignment_seq")
    @SequenceGenerator(name = "task_assignment_seq", sequenceName = "task_assignment_seq", allocationSize = 50)
    private @Nullable Long id;

    @Version
    private @Nullable Short version;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private TaskJPA task;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AssignmentType assignmentType = AssignmentType.ASSIGNEE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssignmentTargetType targetType = AssignmentTargetType.USER;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private @Nullable UserJPA user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id")
    private @Nullable GroupJPA group;

    @Column
    private @Nullable Double share;

    @Column(name = "assignment_priority")
    private @Nullable Integer priority;

    @Column
    private @Nullable LocalDate validFrom;

    @Column
    private @Nullable LocalDate validTo;

    @Column(length = 4000)
    private @Nullable String note;

    @Column(nullable = false)
    private boolean active = true;

    protected TaskAssignmentJPA()
    {
        task = new TaskJPA();
    }

    public TaskAssignmentJPA(TaskJPA task, AssignmentType assignmentType, AssignmentTargetType targetType)
    {
        this.task = requireNonNull(task, "task");
        this.assignmentType = requireNonNull(assignmentType, "assignmentType");
        this.targetType = requireNonNull(targetType, "targetType");
    }

    public @Nullable Long id() { return id; }
    public @Nullable Short version() { return version; }
    public TaskJPA task() { return task; }
    public AssignmentType assignmentType() { return assignmentType; }
    public AssignmentTargetType targetType() { return targetType; }
    public Optional<UserJPA> user() { return ofNullable(user); }
    public Optional<GroupJPA> group() { return ofNullable(group); }
    public Optional<Double> share() { return ofNullable(share); }
    public Optional<Integer> priority() { return ofNullable(priority); }
    public Optional<LocalDate> validFrom() { return ofNullable(validFrom); }
    public Optional<LocalDate> validTo() { return ofNullable(validTo); }
    public Optional<String> note() { return ofNullable(note); }
    public boolean active() { return active; }

    public TaskAssignmentJPA task(TaskJPA value) { task = requireNonNull(value, "task"); return this; }
    public TaskAssignmentJPA assignmentType(AssignmentType value) { assignmentType = requireNonNull(value, "assignmentType"); return this; }
    public TaskAssignmentJPA targetType(AssignmentTargetType value) { targetType = requireNonNull(value, "targetType"); return this; }
    public TaskAssignmentJPA user(@Nullable UserJPA value) { user = value; return this; }
    public TaskAssignmentJPA group(@Nullable GroupJPA value) { group = value; return this; }
    public TaskAssignmentJPA share(@Nullable Double value) { share = value; return this; }
    public TaskAssignmentJPA priority(@Nullable Integer value) { priority = value; return this; }
    public TaskAssignmentJPA validFrom(@Nullable LocalDate value) { validFrom = value; return this; }
    public TaskAssignmentJPA validTo(@Nullable LocalDate value) { validTo = value; return this; }
    public TaskAssignmentJPA note(@Nullable String value) { note = value; return this; }
    public TaskAssignmentJPA active(boolean value) { active = value; return this; }
}
