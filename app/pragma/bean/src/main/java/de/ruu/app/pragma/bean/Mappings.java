package de.ruu.app.pragma.bean;

import de.ruu.app.pragma.dto.TaskDto;
import de.ruu.app.pragma.dto.TaskGroupDto;
import de.ruu.app.pragma.dto.UserDto;
import de.ruu.app.pragma.dto.AuthAccountDto;
import de.ruu.app.pragma.dto.GroupDto;
import de.ruu.app.pragma.dto.MembershipDto;
import de.ruu.app.pragma.dto.TaskAssignmentDto;
import de.ruu.app.pragma.dto.UserAvailabilityDto;
import de.ruu.app.pragma.dto.ChangeLogDto;
import de.ruu.app.pragma.dto.UserWorkloadDto;
import de.ruu.app.pragma.dto.TaskOverrunDto;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class Mappings
{
    private Mappings() {}

    // ── DTO → Bean ─────────────────────────────────────────────────────────

    public static TaskGroupBean toBean(TaskGroupDto in)
    {
        return toBean(in, new IdentityHashMap<>());
    }

    public static TaskBean toBean(TaskDto in)
    {
        return toBean(in, new IdentityHashMap<>());
    }

    /** Maps a list of DTOs sharing the same context (avoids duplicate group beans). */
    public static List<TaskBean> toBean(List<TaskDto> in)
    {
        Map<Object, Object> ctx = new IdentityHashMap<>();
        return in.stream().map(dto -> toBean(dto, ctx)).toList();
    }

    static TaskGroupBean toBean(TaskGroupDto in, Map<Object, Object> ctx)
    {
        TaskGroupBean cached = (TaskGroupBean) ctx.get(in);
        if (cached != null) return cached;

        TaskGroupBean out = new TaskGroupBean(in);
        ctx.put(in, out);

        in.tasks().ifPresent(tasks -> tasks.forEach(t -> toBean(t, ctx)));

        return out;
    }

    static TaskBean toBean(TaskDto in, Map<Object, Object> ctx)
    {
        TaskBean cached = (TaskBean) ctx.get(in);
        if (cached != null) return cached;

        TaskGroupBean group = (TaskGroupBean) ctx.get(in.taskGroup());
        if (group == null) group = toBean(in.taskGroup(), ctx);

        TaskBean out = new TaskBean(group, in);
        ctx.put(in, out);

        in.parentTask().ifPresent(p -> {
            TaskBean parentBean = (TaskBean) ctx.get(p);
            if (parentBean == null) parentBean = toBean(p, ctx);
            out.parentTask(parentBean);
        });

        in.subTasks().ifPresent(children -> children.forEach(child -> {
            TaskBean childBean = (TaskBean) ctx.get(child);
            if (childBean == null) childBean = toBean(child, ctx);
            out.addSubTask(childBean);
        }));

        in.predecessors().ifPresent(preds -> preds.forEach(pred -> {
            TaskBean predBean = (TaskBean) ctx.get(pred);
            if (predBean == null) predBean = toBean(pred, ctx);
            out.addPredecessor(predBean);
        }));

        in.successors().ifPresent(succs -> succs.forEach(succ -> {
            TaskBean succBean = (TaskBean) ctx.get(succ);
            if (succBean == null) succBean = toBean(succ, ctx);
            out.addSuccessor(succBean);
        }));

        return out;
    }

    // ── Bean → DTO ─────────────────────────────────────────────────────────

    public static TaskGroupDto toDto(TaskGroupBean in)
    {
        return toDto(in, new IdentityHashMap<>());
    }

    public static TaskDto toDto(TaskBean in)
    {
        return toDto(in, new IdentityHashMap<>());
    }

    static TaskGroupDto toDto(TaskGroupBean in, Map<Object, Object> ctx)
    {
        TaskGroupDto cached = (TaskGroupDto) ctx.get(in);
        if (cached != null) return cached;

        TaskGroupDto out = new TaskGroupDto(in);
        ctx.put(in, out);

        in.tasks().ifPresent(tasks -> tasks.forEach(t -> toDto(t, ctx)));

        return out;
    }

    static TaskDto toDto(TaskBean in, Map<Object, Object> ctx)
    {
        TaskDto cached = (TaskDto) ctx.get(in);
        if (cached != null) return cached;

        TaskGroupDto group = (TaskGroupDto) ctx.get(in.taskGroup());
        if (group == null) group = toDto(in.taskGroup(), ctx);

        TaskDto out = new TaskDto(group, in);
        ctx.put(in, out);

        out.description (in.description().orElse(null));
        out.scheduledStart(in.scheduledStart().orElse(null));
        out.scheduledFinish(in.scheduledFinish()  .orElse(null));
        out.status      (in.status());
        out.priority    (in.priority());

        in.parentTask().ifPresent(p -> {
            TaskDto parentDto = (TaskDto) ctx.get(p);
            if (parentDto == null) parentDto = toDto(p, ctx);
            out.parentTask(parentDto);
        });

        in.subTasks().ifPresent(children -> children.forEach(child -> {
            TaskDto childDto = (TaskDto) ctx.get(child);
            if (childDto == null) childDto = toDto(child, ctx);
            out.addSubTask(childDto);
        }));

        in.predecessors().ifPresent(preds -> preds.forEach(pred -> {
            TaskDto predDto = (TaskDto) ctx.get(pred);
            if (predDto == null) predDto = toDto(pred, ctx);
            out.addPredecessor(predDto);
        }));

        in.successors().ifPresent(succs -> succs.forEach(succ -> {
            TaskDto succDto = (TaskDto) ctx.get(succ);
            if (succDto == null) succDto = toDto(succ, ctx);
            out.addSuccessor(succDto);
        }));

        return out;
    }

    public static UserBean toBean(UserDto in)
    {
        return new UserBean(in.username(), in.displayName(), in.email())
            .id(in.id())
            .version(in.version())
            .keycloakUserId(in.keycloakUserId().orElse(null))
            .password(in.password().orElse(null))
            .active(in.active());
    }

    public static UserDto toDto(UserBean in)
    {
        return new UserDto(in.username(), in.displayName(), in.email())
            .id(in.id())
            .version(in.version())
            .keycloakUserId(in.keycloakUserId().orElse(null))
            .password(in.password().orElse(null))
            .active(in.active());
    }

    public static AuthAccountBean toBean(AuthAccountDto in)
    {
        return new AuthAccountBean(in.userId(), in.passwordHash())
            .id(in.id())
            .version(in.version())
            .loginEnabled(in.loginEnabled())
            .lastLoginAt(in.lastLoginAt().orElse(null));
    }

    public static AuthAccountDto toDto(AuthAccountBean in)
    {
        return new AuthAccountDto(in.userId(), in.passwordHash())
            .id(in.id())
            .version(in.version())
            .loginEnabled(in.loginEnabled())
            .lastLoginAt(in.lastLoginAt().orElse(null));
    }

    public static GroupBean toBean(GroupDto in)
    {
        return new GroupBean(in.name())
            .id(in.id())
            .version(in.version())
            .description(in.description().orElse(null))
            .active(in.active());
    }

    public static GroupDto toDto(GroupBean in)
    {
        return new GroupDto(in.name())
            .id(in.id())
            .version(in.version())
            .description(in.description().orElse(null))
            .active(in.active());
    }

    public static MembershipBean toBean(MembershipDto in)
    {
        return new MembershipBean(in.userId(), in.groupId())
            .id(in.id())
            .version(in.version())
            .roleInGroup(in.roleInGroup())
            .validFrom(in.validFrom().orElse(null))
            .validTo(in.validTo().orElse(null))
            .active(in.active());
    }

    public static MembershipDto toDto(MembershipBean in)
    {
        return new MembershipDto(in.userId(), in.groupId())
            .id(in.id())
            .version(in.version())
            .roleInGroup(in.roleInGroup())
            .validFrom(in.validFrom().orElse(null))
            .validTo(in.validTo().orElse(null))
            .active(in.active());
    }

    public static TaskAssignmentBean toBean(TaskAssignmentDto in)
    {
        return new TaskAssignmentBean(in.taskId())
            .id(in.id())
            .version(in.version())
            .assignmentType(in.assignmentType())
            .targetType(in.targetType())
            .userId(in.userId().orElse(null))
            .groupId(in.groupId().orElse(null))
            .share(in.share().orElse(null))
            .priority(in.priority().orElse(null))
            .validFrom(in.validFrom().orElse(null))
            .validTo(in.validTo().orElse(null))
            .note(in.note().orElse(null))
            .active(in.active());
    }

    public static TaskAssignmentDto toDto(TaskAssignmentBean in)
    {
        return new TaskAssignmentDto(in.taskId())
            .id(in.id())
            .version(in.version())
            .assignmentType(in.assignmentType())
            .targetType(in.targetType())
            .userId(in.userId().orElse(null))
            .groupId(in.groupId().orElse(null))
            .share(in.share().orElse(null))
            .priority(in.priority().orElse(null))
            .validFrom(in.validFrom().orElse(null))
            .validTo(in.validTo().orElse(null))
            .note(in.note().orElse(null))
            .active(in.active());
    }

    public static UserAvailabilityBean toBean(UserAvailabilityDto in)
    {
        return new UserAvailabilityBean(in.userId(), in.fromDate(), in.toDate(), in.capacityHoursPerDay())
            .id(in.id())
            .version(in.version())
            .availabilityType(in.availabilityType())
            .note(in.note().orElse(null));
    }

    public static UserAvailabilityDto toDto(UserAvailabilityBean in)
    {
        return new UserAvailabilityDto(in.userId(), in.fromDate(), in.toDate(), in.capacityHoursPerDay())
            .id(in.id())
            .version(in.version())
            .availabilityType(in.availabilityType())
            .note(in.note().orElse(null));
    }

    public static ChangeLogBean toBean(ChangeLogDto in)
    {
        return new ChangeLogBean(in.entityType(), in.entityId(), in.fieldName(), in.oldValue().orElse(null), in.newValue().orElse(null))
            .id(in.id())
            .changedAt(in.changedAt())
            .changedBy(in.changedBy().orElse(null))
            .reason(in.reason().orElse(null))
            .category(in.category());
    }

    public static ChangeLogDto toDto(ChangeLogBean in)
    {
        return new ChangeLogDto(in.entityType(), in.entityId(), in.fieldName(), in.oldValue().orElse(null), in.newValue().orElse(null))
            .id(in.id())
            .changedAt(in.changedAt())
            .changedBy(in.changedBy().orElse(null))
            .reason(in.reason().orElse(null))
            .category(in.category());
    }

    public static UserWorkloadBean toBean(UserWorkloadDto in)
    {
        return new UserWorkloadBean(in.userId(), in.username(), in.displayName(), in.capacityHoursPerDay(), in.assignedHours(), in.overbookedHours());
    }

    public static TaskOverrunBean toBean(TaskOverrunDto in)
    {
        return new TaskOverrunBean(in.taskId(), in.taskName(), in.estimateHours(), in.actualHours(), in.overrunHours());
    }
}
