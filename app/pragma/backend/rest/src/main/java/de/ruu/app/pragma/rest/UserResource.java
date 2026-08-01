package de.ruu.app.pragma.rest;

import de.ruu.app.pragma.core.ChangeCategory;
import de.ruu.app.pragma.dto.UserDto;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;

@Path("/admin/users")
@RequestScoped
@Transactional
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed(SecurityRoles.PRAGMA_ADMIN)
public class UserResource
{
    private static final Logger log = LogManager.getLogger(UserResource.class);

    @PersistenceContext
    private EntityManager em;

    @GET
    public List<UserDto> findAll(
        @QueryParam("activeOnly") Boolean activeOnly,
        @QueryParam("syncFromKeycloak") Boolean syncFromKeycloak,
        @QueryParam("syncBestEffort") Boolean syncBestEffort)
    {
        ensureUserSchema();
        if (Boolean.TRUE.equals(syncFromKeycloak)) {
            try {
                KeycloakUserSyncService.syncUsers(em);
            }
            catch (RuntimeException e) {
                if (!Boolean.TRUE.equals(syncBestEffort)) throw e;
                log.warn("Keycloak user sync failed, serving local users only: {}", e.toString());
            }
        }
        String jpql = Boolean.TRUE.equals(activeOnly)
            ? "SELECT u FROM UserJPA u WHERE u.active = true ORDER BY u.username"
            : "SELECT u FROM UserJPA u ORDER BY u.username";
        return em.createQuery(jpql, UserJPA.class).getResultList().stream().map(Mappings::toDto).toList();
    }

    @GET
    @Path("/{id}")
    public UserDto findById(@PathParam("id") Long id)
    {
        ensureUserSchema();
        return Mappings.toDto(requireUser(id));
    }

    @POST
    public Response create(@Valid UserDto dto)
    {
        ensureUserSchema();
        UserJPA entity = KeycloakUserSyncService.createInKeycloakAndSync(em, dto);
        em.flush();
        ChangeLogSupport.logCreate(em, "User", entity.id(), "username", entity.username(), null, null);
        ChangeLogSupport.logCreate(em, "User", entity.id(), "keycloakUserId", entity.keycloakUserId().orElse(null), null, null);
        ChangeLogSupport.logCreate(em, "User", entity.id(), "displayName", entity.displayName(), null, null);
        ChangeLogSupport.logCreate(em, "User", entity.id(), "email", entity.email(), null, null);
        ChangeLogSupport.logCreate(em, "User", entity.id(), "active", entity.active(), null, null);
        return Response.status(Response.Status.CREATED).entity(Mappings.toDto(entity)).build();
    }

    @PUT
    @Path("/{id}")
    public UserDto update(@PathParam("id") Long id, @Valid UserDto dto)
    {
        ensureUserSchema();
        UserJPA entity = requireUser(id);
        if (!Objects.equals(entity.version(), dto.version())) throw new WebApplicationException(Response.Status.CONFLICT);
        String oldUsername = entity.username();
        String oldKeycloakUserId = entity.keycloakUserId().orElse(null);
        String oldDisplayName = entity.displayName();
        String oldEmail = entity.email();
        boolean oldActive = entity.active();
        UserJPA updated = KeycloakUserSyncService.updateInKeycloakAndSync(em, entity, dto);
        ChangeLogSupport.logIfChanged(em, "User", updated.id(), "username", oldUsername, updated.username(), ChangeCategory.UPDATE, null, null);
        ChangeLogSupport.logIfChanged(em, "User", updated.id(), "keycloakUserId", oldKeycloakUserId, updated.keycloakUserId().orElse(null), ChangeCategory.UPDATE, null, null);
        ChangeLogSupport.logIfChanged(em, "User", updated.id(), "displayName", oldDisplayName, updated.displayName(), ChangeCategory.UPDATE, null, null);
        ChangeLogSupport.logIfChanged(em, "User", updated.id(), "email", oldEmail, updated.email(), ChangeCategory.UPDATE, null, null);
        ChangeLogSupport.logIfChanged(em, "User", updated.id(), "active", oldActive, updated.active(), ChangeCategory.UPDATE, null, null);
        return Mappings.toDto(updated);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id)
    {
        ensureUserSchema();
        UserJPA entity = requireUser(id);
        ChangeLogSupport.logDelete(em, "User", entity.id(), "username", entity.username(), null, null);
        KeycloakUserSyncService.deleteInKeycloakAndLocal(em, entity);
        return Response.noContent().build();
    }

    private UserJPA requireUser(Long id)
    {
        UserJPA entity = em.find(UserJPA.class, id);
        if (entity == null) throw new NotFoundException("User not found: " + id);
        return entity;
    }

    private static void ensureUserSchema()
    {
        try
        {
            UserSchemaBootstrap.ensureDuplicateEmailsAllowed();
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Failed to prepare user schema", e);
        }
    }

}
