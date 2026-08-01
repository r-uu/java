package de.ruu.app.pragma.rest;

import de.ruu.app.pragma.core.ChangeCategory;
import de.ruu.app.pragma.jpa.ChangeLogJPA;
import jakarta.persistence.EntityManager;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Objects;

final class ChangeLogSupport
{
    private ChangeLogSupport()
    {
    }

    static void logCreate(EntityManager em, String entityType, Long entityId, String fieldName, @Nullable Object newValue, @Nullable String changedBy, @Nullable String reason)
    {
        persist(em, entityType, entityId, fieldName, null, stringify(newValue), ChangeCategory.CREATE, changedBy, reason);
    }

    static void logDelete(EntityManager em, String entityType, Long entityId, String fieldName, @Nullable Object oldValue, @Nullable String changedBy, @Nullable String reason)
    {
        persist(em, entityType, entityId, fieldName, stringify(oldValue), null, ChangeCategory.DELETE, changedBy, reason);
    }

    static void logIfChanged(EntityManager em, String entityType, Long entityId, String fieldName, @Nullable Object oldValue, @Nullable Object newValue, ChangeCategory category, @Nullable String changedBy, @Nullable String reason)
    {
        if (Objects.equals(oldValue, newValue)) return;
        persist(em, entityType, entityId, fieldName, stringify(oldValue), stringify(newValue), category, changedBy, reason);
    }

    private static void persist(EntityManager em, String entityType, Long entityId, String fieldName, @Nullable String oldValue, @Nullable String newValue, ChangeCategory category, @Nullable String changedBy, @Nullable String reason)
    {
        ChangeLogJPA log = new ChangeLogJPA(entityType, entityId, fieldName, oldValue, newValue)
            .category(category)
            .changedBy(changedBy)
            .reason(reason)
            .changedAt(LocalDateTime.now());
        em.persist(log);
    }

    private static @Nullable String stringify(@Nullable Object value)
    {
        return value == null ? null : value.toString();
    }
}
