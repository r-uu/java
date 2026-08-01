package de.ruu.app.pragma.dto;

import de.ruu.app.pragma.core.AssignmentTargetType;
import de.ruu.app.pragma.core.AssignmentType;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public class TaskAssignmentDto
{
    private @Nullable Long id;
    private @Nullable Short version;
    private @NotNull Long taskId;
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

    protected TaskAssignmentDto()
    {
        taskId = 0L;
    }

    public TaskAssignmentDto(Long taskId)
    {
        this.taskId = requireNonNull(taskId, "taskId");
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

    public TaskAssignmentDto id(@Nullable Long value) { id = value; return this; }
    public TaskAssignmentDto version(@Nullable Short value) { version = value; return this; }
    public TaskAssignmentDto taskId(Long value) { taskId = requireNonNull(value, "taskId"); return this; }
    public TaskAssignmentDto assignmentType(AssignmentType value) { assignmentType = requireNonNull(value, "assignmentType"); return this; }
    public TaskAssignmentDto targetType(AssignmentTargetType value) { targetType = requireNonNull(value, "targetType"); return this; }
    public TaskAssignmentDto userId(@Nullable Long value) { userId = value; return this; }
    public TaskAssignmentDto groupId(@Nullable Long value) { groupId = value; return this; }
    public TaskAssignmentDto share(@Nullable Double value) { share = value; return this; }
    public TaskAssignmentDto priority(@Nullable Integer value) { priority = value; return this; }
    public TaskAssignmentDto validFrom(@Nullable LocalDate value) { validFrom = value; return this; }
    public TaskAssignmentDto validTo(@Nullable LocalDate value) { validTo = value; return this; }
    public TaskAssignmentDto note(@Nullable String value) { note = value; return this; }
    public TaskAssignmentDto active(boolean value) { active = value; return this; }
}
