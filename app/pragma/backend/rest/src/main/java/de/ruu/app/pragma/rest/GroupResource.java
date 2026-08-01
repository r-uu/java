package de.ruu.app.pragma.rest;

import de.ruu.app.pragma.core.ChangeCategory;
import de.ruu.app.pragma.dto.GroupDto;
import de.ruu.app.pragma.jpa.GroupJPA;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
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

@Path("/admin/groups")
@RequestScoped
@Transactional
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed(SecurityRoles.PRAGMA_ADMIN)
public class GroupResource
{
    @PersistenceContext
    private EntityManager em;

    @GET
    public List<GroupDto> findAll(@QueryParam("activeOnly") Boolean activeOnly)
    {
        String jpql = Boolean.TRUE.equals(activeOnly)
            ? "SELECT g FROM GroupJPA g WHERE g.active = true ORDER BY g.name"
            : "SELECT g FROM GroupJPA g ORDER BY g.name";
        return em.createQuery(jpql, GroupJPA.class).getResultList().stream().map(Mappings::toDto).toList();
    }

    @GET
    @Path("/{id}")
    public GroupDto findById(@PathParam("id") Long id)
    {
        return Mappings.toDto(requireGroup(id));
    }

    @POST
    public Response create(@Valid GroupDto dto)
    {
        GroupJPA entity = new GroupJPA(dto.name())
            .description(dto.description().orElse(null))
            .active(dto.active());
        em.persist(entity);
        em.flush();
        ChangeLogSupport.logCreate(em, "Group", entity.id(), "name", entity.name(), null, null);
        return Response.status(Response.Status.CREATED).entity(Mappings.toDto(entity)).build();
    }

    @PUT
    @Path("/{id}")
    public GroupDto update(@PathParam("id") Long id, @Valid GroupDto dto)
    {
        GroupJPA entity = requireGroup(id);
        if (!Objects.equals(entity.version(), dto.version())) throw new WebApplicationException(Response.Status.CONFLICT);
        String oldName = entity.name();
        String oldDescription = entity.description().orElse(null);
        boolean oldActive = entity.active();
        entity.name(dto.name());
        entity.description(dto.description().orElse(null));
        entity.active(dto.active());
        ChangeLogSupport.logIfChanged(em, "Group", entity.id(), "name", oldName, entity.name(), ChangeCategory.UPDATE, null, null);
        ChangeLogSupport.logIfChanged(em, "Group", entity.id(), "description", oldDescription, entity.description().orElse(null), ChangeCategory.UPDATE, null, null);
        ChangeLogSupport.logIfChanged(em, "Group", entity.id(), "active", oldActive, entity.active(), ChangeCategory.UPDATE, null, null);
        return Mappings.toDto(entity);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id)
    {
        GroupJPA entity = requireGroup(id);
        ChangeLogSupport.logDelete(em, "Group", entity.id(), "name", entity.name(), null, null);
        em.remove(entity);
        return Response.noContent().build();
    }

    private GroupJPA requireGroup(Long id)
    {
        GroupJPA entity = em.find(GroupJPA.class, id);
        if (entity == null) throw new NotFoundException("Group not found: " + id);
        return entity;
    }
}
