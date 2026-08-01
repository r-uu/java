package de.ruu.app.pragma.rest;

import de.ruu.app.pragma.core.ChangeCategory;
import de.ruu.app.pragma.dto.UserAvailabilityDto;
import de.ruu.app.pragma.jpa.UserAvailabilityJPA;
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

@Path("/admin/user-availabilities")
@RequestScoped
@Transactional
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed(SecurityRoles.PRAGMA_ADMIN)
public class UserAvailabilityResource
{
    @PersistenceContext
    private EntityManager em;

    @GET
    public List<UserAvailabilityDto> findAll(@QueryParam("userId") Long userId)
    {
        if (userId != null) {
            return em.createQuery("SELECT a FROM UserAvailabilityJPA a WHERE a.user.id = :userId ORDER BY a.fromDate, a.id", UserAvailabilityJPA.class)
                .setParameter("userId", userId)
                .getResultList().stream().map(Mappings::toDto).toList();
        }
        return em.createQuery("SELECT a FROM UserAvailabilityJPA a ORDER BY a.user.id, a.fromDate, a.id", UserAvailabilityJPA.class)
            .getResultList().stream().map(Mappings::toDto).toList();
    }

    @POST
    public Response create(@Valid UserAvailabilityDto dto)
    {
        validateRange(dto);
        UserAvailabilityJPA entity = new UserAvailabilityJPA(
            requireUser(dto.userId()),
            dto.fromDate(),
            dto.toDate(),
            dto.capacityHoursPerDay())
            .availabilityType(dto.availabilityType())
            .note(dto.note().orElse(null));
        em.persist(entity);
        em.flush();
        ChangeLogSupport.logCreate(em, "UserAvailability", entity.id(), "capacityHoursPerDay", entity.capacityHoursPerDay(), null, null);
        return Response.status(Response.Status.CREATED).entity(Mappings.toDto(entity)).build();
    }

    @PUT
    @Path("/{id}")
    public UserAvailabilityDto update(@PathParam("id") Long id, @Valid UserAvailabilityDto dto)
    {
        validateRange(dto);
        UserAvailabilityJPA entity = requireAvailability(id);
        if (!Objects.equals(entity.version(), dto.version())) throw new WebApplicationException(Response.Status.CONFLICT);
        var oldCapacity = entity.capacityHoursPerDay();
        var oldType = entity.availabilityType();
        entity.user(requireUser(dto.userId()));
        entity.fromDate(dto.fromDate());
        entity.toDate(dto.toDate());
        entity.capacityHoursPerDay(dto.capacityHoursPerDay());
        entity.availabilityType(dto.availabilityType());
        entity.note(dto.note().orElse(null));
        ChangeLogSupport.logIfChanged(em, "UserAvailability", entity.id(), "capacityHoursPerDay", oldCapacity, entity.capacityHoursPerDay(), ChangeCategory.AVAILABILITY, null, null);
        ChangeLogSupport.logIfChanged(em, "UserAvailability", entity.id(), "availabilityType", oldType, entity.availabilityType(), ChangeCategory.AVAILABILITY, null, null);
        return Mappings.toDto(entity);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id)
    {
        UserAvailabilityJPA entity = requireAvailability(id);
        ChangeLogSupport.logDelete(em, "UserAvailability", entity.id(), "capacityHoursPerDay", entity.capacityHoursPerDay(), null, null);
        em.remove(entity);
        return Response.noContent().build();
    }

    private static void validateRange(UserAvailabilityDto dto)
    {
        if (dto.toDate().isBefore(dto.fromDate())) throw new BadRequestException("toDate must be >= fromDate");
    }

    private UserAvailabilityJPA requireAvailability(Long id)
    {
        UserAvailabilityJPA entity = em.find(UserAvailabilityJPA.class, id);
        if (entity == null) throw new NotFoundException("UserAvailability not found: " + id);
        return entity;
    }

    private UserJPA requireUser(Long id)
    {
        UserJPA entity = em.find(UserJPA.class, id);
        if (entity == null) throw new NotFoundException("User not found: " + id);
        return entity;
    }
}
