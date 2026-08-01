package de.ruu.app.pragma.rest;

import de.ruu.app.pragma.dto.TaskGroupDto;
import de.ruu.app.pragma.core.ChangeCategory;
import de.ruu.app.pragma.jpa.TaskGroupJPA;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.validation.Valid;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Objects;

@Path("/task-groups")
@RequestScoped
@Transactional
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({SecurityRoles.TASKGROUP_READ, SecurityRoles.PRAGMA_ADMIN})
public class TaskGroupResource
{
    @PersistenceContext
    private EntityManager em;

    @GET
    public List<TaskGroupDto> findAll(
            @QueryParam("page") Integer page,
            @QueryParam("size") Integer size)
    {
        int offset = page != null && page > 0 ? page * effectiveSize(size) : 0;
        return em.createQuery("SELECT g FROM TaskGroupJPA g", TaskGroupJPA.class)
                 .setFirstResult(offset)
                 .setMaxResults(effectiveSize(size))
                 .getResultList()
                 .stream()
                 .map(Mappings::toDto)
                 .toList();
    }

    private static int effectiveSize(Integer size) { return (size != null && size > 0) ? size : Integer.MAX_VALUE; }

    @GET
    @Path("/{id}")
    public TaskGroupDto findById(@PathParam("id") Long id)
    {
        return Mappings.toDto(requireGroup(id));
    }

    @POST
    @RolesAllowed({SecurityRoles.TASKGROUP_CREATE, SecurityRoles.PRAGMA_ADMIN})
    public Response create(@Valid TaskGroupDto dto)
    {
        TaskGroupJPA entity = new TaskGroupJPA(dto.name());
        em.persist(entity);
        em.flush();
        ChangeLogSupport.logCreate(em, "TaskGroup", entity.id(), "name", entity.name(), null, null);
        return Response.status(Response.Status.CREATED).entity(Mappings.toDto(entity)).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({SecurityRoles.TASKGROUP_UPDATE, SecurityRoles.PRAGMA_ADMIN})
    public TaskGroupDto update(@PathParam("id") Long id, @Valid TaskGroupDto dto)
    {
        TaskGroupJPA entity = requireGroup(id);
        if (!Objects.equals(entity.version(), dto.version()))
            throw new WebApplicationException(Response.Status.CONFLICT);
        String oldName = entity.name();
        entity.name(dto.name());
        ChangeLogSupport.logIfChanged(em, "TaskGroup", entity.id(), "name", oldName, entity.name(), ChangeCategory.UPDATE, null, null);
        return Mappings.toDto(entity);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({SecurityRoles.TASKGROUP_DELETE, SecurityRoles.PRAGMA_ADMIN})
    public Response delete(@PathParam("id") Long id)
    {
        TaskGroupJPA entity = requireGroup(id);
        ChangeLogSupport.logDelete(em, "TaskGroup", entity.id(), "name", entity.name(), null, null);
        em.remove(entity);
        return Response.noContent().build();
    }

    private TaskGroupJPA requireGroup(Long id)
    {
        TaskGroupJPA entity = em.find(TaskGroupJPA.class, id);
        if (entity == null) throw new NotFoundException("TaskGroup not found: " + id);
        return entity;
    }
}
