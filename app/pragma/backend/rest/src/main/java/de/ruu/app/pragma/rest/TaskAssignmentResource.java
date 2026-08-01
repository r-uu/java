package de.ruu.app.pragma.rest;

import de.ruu.app.pragma.core.AssignmentTargetType;
import de.ruu.app.pragma.core.AssignmentType;
import de.ruu.app.pragma.core.ChangeCategory;
import de.ruu.app.pragma.dto.TaskAssignmentDto;
import de.ruu.app.pragma.jpa.GroupJPA;
import de.ruu.app.pragma.jpa.TaskAssignmentJPA;
import de.ruu.app.pragma.jpa.TaskJPA;
import de.ruu.app.pragma.jpa.UserJPA;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Objects;

@Path("/admin/task-assignments")
@RequestScoped
@Transactional
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed(SecurityRoles.PRAGMA_ADMIN)
public class TaskAssignmentResource
{
    @PersistenceContext
    private EntityManager em;

    @GET
    public List<TaskAssignmentDto> findAll(@QueryParam("taskId") Long taskId)
    {
        if (taskId != null) {
            return em.createQuery("SELECT a FROM TaskAssignmentJPA a WHERE a.task.id = :taskId ORDER BY a.id", TaskAssignmentJPA.class)
                .setParameter("taskId", taskId)
                .getResultList().stream().map(Mappings::toDto).toList();
        }
        return em.createQuery("SELECT a FROM TaskAssignmentJPA a ORDER BY a.id", TaskAssignmentJPA.class)
            .getResultList().stream().map(Mappings::toDto).toList();
    }

    @POST
    public Response create(@Valid TaskAssignmentDto dto)
    {
        TaskJPA task = requireTask(dto.taskId());
        TaskAssignmentJPA entity = new TaskAssignmentJPA(task, dto.assignmentType(), dto.targetType())
            .share(dto.share().orElse(null))
            .priority(dto.priority().orElse(null))
            .validFrom(dto.validFrom().orElse(null))
            .validTo(dto.validTo().orElse(null))
            .note(dto.note().orElse(null))
            .active(dto.active());
        applyTarget(entity, dto);
        validateResponsibleConstraint(entity, null);
        em.persist(entity);
        em.flush();
        ChangeLogSupport.logCreate(em, "TaskAssignment", entity.id(), "assignmentType", entity.assignmentType(), null, null);
        return Response.status(Response.Status.CREATED).entity(Mappings.toDto(entity)).build();
    }

    @PUT
    @Path("/{id}")
    public TaskAssignmentDto update(@PathParam("id") Long id, @Valid TaskAssignmentDto dto)
    {
        TaskAssignmentJPA entity = requireAssignment(id);
        if (!Objects.equals(entity.version(), dto.version())) throw new WebApplicationException(Response.Status.CONFLICT);
        var oldType = entity.assignmentType();
        var oldTarget = entity.targetType();
        entity.task(requireTask(dto.taskId()));
        entity.assignmentType(dto.assignmentType());
        entity.targetType(dto.targetType());
        entity.share(dto.share().orElse(null));
        entity.priority(dto.priority().orElse(null));
        entity.validFrom(dto.validFrom().orElse(null));
        entity.validTo(dto.validTo().orElse(null));
        entity.note(dto.note().orElse(null));
        entity.active(dto.active());
        applyTarget(entity, dto);
        validateResponsibleConstraint(entity, entity.id());
        ChangeLogSupport.logIfChanged(em, "TaskAssignment", entity.id(), "assignmentType", oldType, entity.assignmentType(), ChangeCategory.ASSIGNMENT, null, null);
        ChangeLogSupport.logIfChanged(em, "TaskAssignment", entity.id(), "targetType", oldTarget, entity.targetType(), ChangeCategory.ASSIGNMENT, null, null);
        return Mappings.toDto(entity);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id)
    {
        TaskAssignmentJPA entity = requireAssignment(id);
        ChangeLogSupport.logDelete(em, "TaskAssignment", entity.id(), "assignmentType", entity.assignmentType(), null, null);
        em.remove(entity);
        return Response.noContent().build();
    }

    private void validateResponsibleConstraint(TaskAssignmentJPA entity, Long ignoredAssignmentId)
    {
        if (entity.assignmentType() != AssignmentType.RESPONSIBLE || !entity.active()) return;
        Long count = em.createQuery("""
                SELECT COUNT(a)
                  FROM TaskAssignmentJPA a
                 WHERE a.task.id = :taskId
                   AND a.assignmentType = :assignmentType
                   AND a.active = true
                   AND (:ignoredId IS NULL OR a.id <> :ignoredId)
                """, Long.class)
            .setParameter("taskId", entity.task().id())
            .setParameter("assignmentType", AssignmentType.RESPONSIBLE)
            .setParameter("ignoredId", ignoredAssignmentId)
            .getSingleResult();
        if (count > 0) throw new BadRequestException("Task already has an active RESPONSIBLE assignment");
    }

    private void applyTarget(TaskAssignmentJPA entity, TaskAssignmentDto dto)
    {
        if (dto.targetType() == AssignmentTargetType.USER) {
            Long userId = dto.userId().orElseThrow(() -> new BadRequestException("userId is required for USER targetType"));
            entity.user(requireUser(userId));
            entity.group(null);
            return;
        }
        Long groupId = dto.groupId().orElseThrow(() -> new BadRequestException("groupId is required for GROUP targetType"));
        entity.group(requireGroup(groupId));
        entity.user(null);
    }

    private TaskAssignmentJPA requireAssignment(Long id)
    {
        TaskAssignmentJPA entity = em.find(TaskAssignmentJPA.class, id);
        if (entity == null) throw new NotFoundException("TaskAssignment not found: " + id);
        return entity;
    }

    private TaskJPA requireTask(Long id)
    {
        TaskJPA entity = em.find(TaskJPA.class, id);
        if (entity == null) throw new NotFoundException("Task not found: " + id);
        return entity;
    }

    private UserJPA requireUser(Long id)
    {
        UserJPA entity = em.find(UserJPA.class, id);
        if (entity == null) throw new NotFoundException("User not found: " + id);
        return entity;
    }

    private GroupJPA requireGroup(Long id)
    {
        GroupJPA entity = em.find(GroupJPA.class, id);
        if (entity == null) throw new NotFoundException("Group not found: " + id);
        return entity;
    }
}
