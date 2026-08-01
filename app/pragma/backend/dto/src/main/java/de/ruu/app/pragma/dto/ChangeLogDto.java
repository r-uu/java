package de.ruu.app.pragma.dto;

import de.ruu.app.pragma.core.ChangeCategory;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public class ChangeLogDto
{
    private @Nullable Long id;
    private String entityType;
    private Long entityId;
    private String fieldName;
    private @Nullable String oldValue;
    private @Nullable String newValue;
    private LocalDateTime changedAt;
    private @Nullable String changedBy;
    private @Nullable String reason;
    private ChangeCategory category = ChangeCategory.UPDATE;

    protected ChangeLogDto()
    {
        entityType = "";
        entityId = 0L;
        fieldName = "";
        changedAt = LocalDateTime.now();
    }

    public ChangeLogDto(String entityType, Long entityId, String fieldName, @Nullable String oldValue, @Nullable String newValue)
    {
        this.entityType = requireNonNull(entityType, "entityType");
        this.entityId = requireNonNull(entityId, "entityId");
        this.fieldName = requireNonNull(fieldName, "fieldName");
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedAt = LocalDateTime.now();
    }

    public @Nullable Long id() { return id; }
    public String entityType() { return entityType; }
    public Long entityId() { return entityId; }
    public String fieldName() { return fieldName; }
    public Optional<String> oldValue() { return ofNullable(oldValue); }
    public Optional<String> newValue() { return ofNullable(newValue); }
    public LocalDateTime changedAt() { return changedAt; }
    public Optional<String> changedBy() { return ofNullable(changedBy); }
    public Optional<String> reason() { return ofNullable(reason); }
    public ChangeCategory category() { return category; }

    public ChangeLogDto id(@Nullable Long value) { id = value; return this; }
    public ChangeLogDto entityType(String value) { entityType = requireNonNull(value, "entityType"); return this; }
    public ChangeLogDto entityId(Long value) { entityId = requireNonNull(value, "entityId"); return this; }
    public ChangeLogDto fieldName(String value) { fieldName = requireNonNull(value, "fieldName"); return this; }
    public ChangeLogDto oldValue(@Nullable String value) { oldValue = value; return this; }
    public ChangeLogDto newValue(@Nullable String value) { newValue = value; return this; }
    public ChangeLogDto changedAt(LocalDateTime value) { changedAt = requireNonNull(value, "changedAt"); return this; }
    public ChangeLogDto changedBy(@Nullable String value) { changedBy = value; return this; }
    public ChangeLogDto reason(@Nullable String value) { reason = value; return this; }
    public ChangeLogDto category(ChangeCategory value) { category = requireNonNull(value, "category"); return this; }
}
