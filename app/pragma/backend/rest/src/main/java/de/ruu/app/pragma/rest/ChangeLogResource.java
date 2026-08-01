package de.ruu.app.pragma.rest;

import de.ruu.app.pragma.dto.ChangeLogDto;
import de.ruu.app.pragma.jpa.ChangeLogJPA;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/admin/change-log")
@RequestScoped
@Transactional
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed(SecurityRoles.PRAGMA_ADMIN)
public class ChangeLogResource
{
    @PersistenceContext
    private EntityManager em;

    @GET
    public List<ChangeLogDto> findAll(
        @QueryParam("entityType") String entityType,
        @QueryParam("entityId") Long entityId,
        @QueryParam("size") Integer size)
    {
        int max = (size != null && size > 0) ? size : 500;
        if (entityType != null && !entityType.isBlank() && entityId != null) {
            return em.createQuery("""
                    SELECT c
                      FROM ChangeLogJPA c
                     WHERE c.entityType = :entityType
                       AND c.entityId = :entityId
                     ORDER BY c.changedAt DESC
                    """, ChangeLogJPA.class)
                .setParameter("entityType", entityType)
                .setParameter("entityId", entityId)
                .setMaxResults(max)
                .getResultList().stream().map(Mappings::toDto).toList();
        }
        if (entityType != null && !entityType.isBlank()) {
            return em.createQuery("""
                    SELECT c
                      FROM ChangeLogJPA c
                     WHERE c.entityType = :entityType
                     ORDER BY c.changedAt DESC
                    """, ChangeLogJPA.class)
                .setParameter("entityType", entityType)
                .setMaxResults(max)
                .getResultList().stream().map(Mappings::toDto).toList();
        }
        return em.createQuery("SELECT c FROM ChangeLogJPA c ORDER BY c.changedAt DESC", ChangeLogJPA.class)
            .setMaxResults(max)
            .getResultList().stream().map(Mappings::toDto).toList();
    }
}
