package de.ruu.app.pragma.rest;

import de.ruu.app.pragma.core.ChangeCategory;
import de.ruu.app.pragma.dto.MembershipDto;
import de.ruu.app.pragma.jpa.GroupJPA;
import de.ruu.app.pragma.jpa.MembershipJPA;
import de.ruu.app.pragma.jpa.UserJPA;
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

@Path("/admin/memberships")
@RequestScoped
@Transactional
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed(SecurityRoles.PRAGMA_ADMIN)
public class MembershipResource
{
    @PersistenceContext
    private EntityManager em;

    @GET
    public List<MembershipDto> findAll(@QueryParam("userId") Long userId, @QueryParam("groupId") Long groupId)
    {
        if (userId != null) {
            return em.createQuery("SELECT m FROM MembershipJPA m WHERE m.user.id = :userId ORDER BY m.id", MembershipJPA.class)
                .setParameter("userId", userId)
                .getResultList().stream().map(Mappings::toDto).toList();
        }
        if (groupId != null) {
            return em.createQuery("SELECT m FROM MembershipJPA m WHERE m.group.id = :groupId ORDER BY m.id", MembershipJPA.class)
                .setParameter("groupId", groupId)
                .getResultList().stream().map(Mappings::toDto).toList();
        }
        return em.createQuery("SELECT m FROM MembershipJPA m ORDER BY m.id", MembershipJPA.class)
            .getResultList().stream().map(Mappings::toDto).toList();
    }

    @POST
    public Response create(@Valid MembershipDto dto)
    {
        UserJPA user = requireUser(dto.userId());
        GroupJPA group = requireGroup(dto.groupId());
        MembershipJPA entity = new MembershipJPA(user, group)
            .roleInGroup(dto.roleInGroup())
            .validFrom(dto.validFrom().orElse(null))
            .validTo(dto.validTo().orElse(null))
            .active(dto.active());
        em.persist(entity);
        em.flush();
        ChangeLogSupport.logCreate(em, "Membership", entity.id(), "roleInGroup", entity.roleInGroup(), null, null);
        return Response.status(Response.Status.CREATED).entity(Mappings.toDto(entity)).build();
    }

    @PUT
    @Path("/{id}")
    public MembershipDto update(@PathParam("id") Long id, @Valid MembershipDto dto)
    {
        MembershipJPA entity = requireMembership(id);
        if (!Objects.equals(entity.version(), dto.version())) throw new WebApplicationException(Response.Status.CONFLICT);
        var oldRole = entity.roleInGroup();
        var oldValidFrom = entity.validFrom().orElse(null);
        var oldValidTo = entity.validTo().orElse(null);
        var oldActive = entity.active();
        entity.user(requireUser(dto.userId()));
        entity.group(requireGroup(dto.groupId()));
        entity.roleInGroup(dto.roleInGroup());
        entity.validFrom(dto.validFrom().orElse(null));
        entity.validTo(dto.validTo().orElse(null));
        entity.active(dto.active());
        ChangeLogSupport.logIfChanged(em, "Membership", entity.id(), "roleInGroup", oldRole, entity.roleInGroup(), ChangeCategory.MEMBERSHIP, null, null);
        ChangeLogSupport.logIfChanged(em, "Membership", entity.id(), "validFrom", oldValidFrom, entity.validFrom().orElse(null), ChangeCategory.MEMBERSHIP, null, null);
        ChangeLogSupport.logIfChanged(em, "Membership", entity.id(), "validTo", oldValidTo, entity.validTo().orElse(null), ChangeCategory.MEMBERSHIP, null, null);
        ChangeLogSupport.logIfChanged(em, "Membership", entity.id(), "active", oldActive, entity.active(), ChangeCategory.MEMBERSHIP, null, null);
        return Mappings.toDto(entity);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id)
    {
        MembershipJPA entity = requireMembership(id);
        ChangeLogSupport.logDelete(em, "Membership", entity.id(), "roleInGroup", entity.roleInGroup(), null, null);
        em.remove(entity);
        return Response.noContent().build();
    }

    private MembershipJPA requireMembership(Long id)
    {
        MembershipJPA entity = em.find(MembershipJPA.class, id);
        if (entity == null) throw new NotFoundException("Membership not found: " + id);
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
