package de.ruu.app.pragma.rest;

import de.ruu.app.pragma.dto.AuthAccountDto;
import de.ruu.app.pragma.jpa.AuthAccountJPA;
import de.ruu.app.pragma.jpa.UserJPA;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
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

@Path("/admin/auth-accounts")
@RequestScoped
@Transactional
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed(SecurityRoles.PRAGMA_ADMIN)
public class AuthAccountResource
{
    @PersistenceContext
    private EntityManager em;

    @GET
    public List<AuthAccountDto> findAll(@QueryParam("userId") Long userId)
    {
        if (userId != null) {
            return em.createQuery("SELECT a FROM AuthAccountJPA a WHERE a.user.id = :userId ORDER BY a.id", AuthAccountJPA.class)
                .setParameter("userId", userId)
                .getResultList().stream().map(Mappings::toDto).toList();
        }
        return em.createQuery("SELECT a FROM AuthAccountJPA a ORDER BY a.id", AuthAccountJPA.class)
            .getResultList().stream().map(Mappings::toDto).toList();
    }

    @POST
    public Response create(@Valid AuthAccountDto dto)
    {
        AuthAccountJPA entity = new AuthAccountJPA(requireUser(dto.userId()), dto.passwordHash())
            .loginEnabled(dto.loginEnabled())
            .lastLoginAt(dto.lastLoginAt().orElse(null));
        em.persist(entity);
        return Response.status(Response.Status.CREATED).entity(Mappings.toDto(entity)).build();
    }

    @PUT
    @Path("/{id}")
    public AuthAccountDto update(@PathParam("id") Long id, @Valid AuthAccountDto dto)
    {
        AuthAccountJPA entity = requireAccount(id);
        if (!Objects.equals(entity.version(), dto.version())) throw new WebApplicationException(Response.Status.CONFLICT);
        entity.user(requireUser(dto.userId()));
        entity.passwordHash(dto.passwordHash());
        entity.loginEnabled(dto.loginEnabled());
        entity.lastLoginAt(dto.lastLoginAt().orElse(null));
        return Mappings.toDto(entity);
    }

    private AuthAccountJPA requireAccount(Long id)
    {
        AuthAccountJPA entity = em.find(AuthAccountJPA.class, id);
        if (entity == null) throw new NotFoundException("AuthAccount not found: " + id);
        return entity;
    }

    private UserJPA requireUser(Long id)
    {
        UserJPA entity = em.find(UserJPA.class, id);
        if (entity == null) throw new NotFoundException("User not found: " + id);
        return entity;
    }
}
