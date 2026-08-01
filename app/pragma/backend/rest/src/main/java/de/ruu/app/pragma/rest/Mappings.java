package de.ruu.app.pragma.rest;

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
import de.ruu.app.pragma.jpa.TaskGroupJPA;
import de.ruu.app.pragma.jpa.TaskJPA;
import de.ruu.app.pragma.jpa.UserJPA;
import de.ruu.app.pragma.jpa.AuthAccountJPA;
import de.ruu.app.pragma.jpa.GroupJPA;
import de.ruu.app.pragma.jpa.MembershipJPA;
import de.ruu.app.pragma.jpa.TaskAssignmentJPA;
import de.ruu.app.pragma.jpa.UserAvailabilityJPA;
import de.ruu.app.pragma.jpa.ChangeLogJPA;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

final class Mappings
{
    private Mappings() {}

    // ── JPA → DTO ──────────────────────────────────────────────────────────

    static TaskGroupDto toDto(TaskGroupJPA in)
    {
        return toDto(in, new IdentityHashMap<>());
    }

    static TaskDto toDto(TaskJPA in)
    {
        return toDto(in, new IdentityHashMap<>());
    }

    static List<TaskDto> toDto(List<TaskJPA> in)
    {
        Map<Object, Object> ctx = new IdentityHashMap<>();
        return in.stream().map(t -> toDto(t, ctx)).toList();
    }

    static TaskGroupDto toDto(TaskGroupJPA in, Map<Object, Object> ctx)
    {
        TaskGroupDto cached = (TaskGroupDto) ctx.get(in);
        if (cached != null) return cached;

        TaskGroupDto out = new TaskGroupDto(in);
        ctx.put(in, out);

        in.tasks().ifPresent(tasks -> tasks.forEach(t -> toDto(t, ctx)));

        return out;
    }

    static TaskDto toDto(TaskJPA in, Map<Object, Object> ctx)
    {
        TaskDto cached = (TaskDto) ctx.get(in);
        if (cached != null) return cached;

        TaskGroupDto group = (TaskGroupDto) ctx.get(in.taskGroup());
        if (group == null) group = toDto(in.taskGroup(), ctx);

        TaskDto out = new TaskDto(group, in);
        ctx.put(in, out);

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

        out.description (in.description().orElse(null));
        out.scheduledStart(in.scheduledStart().orElse(null));
        out.scheduledFinish(in.scheduledFinish()  .orElse(null));
        out.status      (in.status());
        out.priority    (in.priority());

        return out;
    }

    static UserDto toDto(UserJPA in)
    {
        return new UserDto(in.username(), in.displayName(), in.email())
            .id(in.id())
            .version(in.version())
            .keycloakUserId(in.keycloakUserId().orElse(null))
            .active(in.active());
    }

    static AuthAccountDto toDto(AuthAccountJPA in)
    {
        return new AuthAccountDto(in.user().id(), in.passwordHash())
            .id(in.id())
            .version(in.version())
            .loginEnabled(in.loginEnabled())
            .lastLoginAt(in.lastLoginAt().orElse(null));
    }

    static GroupDto toDto(GroupJPA in)
    {
        return new GroupDto(in.name())
            .id(in.id())
            .version(in.version())
            .description(in.description().orElse(null))
            .active(in.active());
    }

    static MembershipDto toDto(MembershipJPA in)
    {
        return new MembershipDto(in.user().id(), in.group().id())
            .id(in.id())
            .version(in.version())
            .roleInGroup(in.roleInGroup())
            .validFrom(in.validFrom().orElse(null))
            .validTo(in.validTo().orElse(null))
            .active(in.active());
    }

    static TaskAssignmentDto toDto(TaskAssignmentJPA in)
    {
        return new TaskAssignmentDto(in.task().id())
            .id(in.id())
            .version(in.version())
            .assignmentType(in.assignmentType())
            .targetType(in.targetType())
            .userId(in.user().map(UserJPA::id).orElse(null))
            .groupId(in.group().map(GroupJPA::id).orElse(null))
            .share(in.share().orElse(null))
            .priority(in.priority().orElse(null))
            .validFrom(in.validFrom().orElse(null))
            .validTo(in.validTo().orElse(null))
            .note(in.note().orElse(null))
            .active(in.active());
    }

    static UserAvailabilityDto toDto(UserAvailabilityJPA in)
    {
        return new UserAvailabilityDto(in.user().id(), in.fromDate(), in.toDate(), in.capacityHoursPerDay())
            .id(in.id())
            .version(in.version())
            .availabilityType(in.availabilityType())
            .note(in.note().orElse(null));
    }

    static ChangeLogDto toDto(ChangeLogJPA in)
    {
        return new ChangeLogDto(in.entityType(), in.entityId(), in.fieldName(), in.oldValue().orElse(null), in.newValue().orElse(null))
            .id(in.id())
            .changedAt(in.changedAt())
            .changedBy(in.changedBy().orElse(null))
            .reason(in.reason().orElse(null))
            .category(in.category());
    }

    static UserWorkloadDto toDtoUserWorkload(UserJPA user, double capacityHoursPerDay, double assignedHours)
    {
        return new UserWorkloadDto(
            user.id(),
            user.username(),
            user.displayName(),
            capacityHoursPerDay,
            assignedHours,
            Math.max(0d, assignedHours - capacityHoursPerDay));
    }

    static TaskOverrunDto toDtoTaskOverrun(TaskJPA task)
    {
        Double estimate = task.workEstimateCurrent().orElse(task.workEstimateInitial().orElse(null));
        Double actual = task.workActual().orElse(null);
        Double overrun = (estimate == null || actual == null) ? null : actual - estimate;
        return new TaskOverrunDto(task.id(), task.name(), estimate, actual, overrun);
    }
}
