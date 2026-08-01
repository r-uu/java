package de.ruu.app.pragma.bean;

import de.ruu.app.pragma.core.AssignmentTargetType;
import de.ruu.app.pragma.core.AssignmentType;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public class TaskAssignmentBean
{
    private @Nullable Long id;
    private @Nullable Short version;
    private Long taskId;
    private AssignmentType assignmentType = AssignmentType.ASSIGNEE;
    private AssignmentTargetType targetType = AssignmentTargetType.USER;
    private @Nullable Long userId;
    private @Nullable Long groupId;
    private @Nullable Double share;
    private @Nullable Integer priority;
    private @Nullable LocalDate validFrom;
    private @Nullable LocalDate validTo;
    private @Nullable String note;
    private boolean active = true;

    public TaskAssignmentBean(Long taskId)
    {
        this.taskId = requireNonNull(taskId, "taskId");
    }

    public TaskAssignmentBean(TaskAssignmentBean in)
    {
        this.id = in.id;
        this.version = in.version;
        this.taskId = in.taskId;
        this.assignmentType = in.assignmentType;
        this.targetType = in.targetType;
        this.userId = in.userId;
        this.groupId = in.groupId;
        this.share = in.share;
        this.priority = in.priority;
        this.validFrom = in.validFrom;
        this.validTo = in.validTo;
        this.note = in.note;
        this.active = in.active;
    }

    public @Nullable Long id() { return id; }
    public @Nullable Short version() { return version; }
    public Long taskId() { return taskId; }
    public AssignmentType assignmentType() { return assignmentType; }
    public AssignmentTargetType targetType() { return targetType; }
    public Optional<Long> userId() { return ofNullable(userId); }
    public Optional<Long> groupId() { return ofNullable(groupId); }
    public Optional<Double> share() { return ofNullable(share); }
    public Optional<Integer> priority() { return ofNullable(priority); }
    public Optional<LocalDate> validFrom() { return ofNullable(validFrom); }
    public Optional<LocalDate> validTo() { return ofNullable(validTo); }
    public Optional<String> note() { return ofNullable(note); }
    public boolean active() { return active; }

    public TaskAssignmentBean id(@Nullable Long value) { id = value; return this; }
    public TaskAssignmentBean version(@Nullable Short value) { version = value; return this; }
    public TaskAssignmentBean taskId(Long value) { taskId = requireNonNull(value, "taskId"); return this; }
    public TaskAssignmentBean assignmentType(AssignmentType value) { assignmentType = requireNonNull(value, "assignmentType"); return this; }
    public TaskAssignmentBean targetType(AssignmentTargetType value) { targetType = requireNonNull(value, "targetType"); return this; }
    public TaskAssignmentBean userId(@Nullable Long value) { userId = value; return this; }
    public TaskAssignmentBean groupId(@Nullable Long value) { groupId = value; return this; }
    public TaskAssignmentBean share(@Nullable Double value) { share = value; return this; }
    public TaskAssignmentBean priority(@Nullable Integer value) { priority = value; return this; }
    public TaskAssignmentBean validFrom(@Nullable LocalDate value) { validFrom = value; return this; }
    public TaskAssignmentBean validTo(@Nullable LocalDate value) { validTo = value; return this; }
    public TaskAssignmentBean note(@Nullable String value) { note = value; return this; }
    public TaskAssignmentBean active(boolean value) { active = value; return this; }
}
