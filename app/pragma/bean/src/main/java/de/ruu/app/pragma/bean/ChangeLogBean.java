package de.ruu.app.pragma.bean;

import de.ruu.app.pragma.core.ChangeCategory;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public class ChangeLogBean
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

    public ChangeLogBean(String entityType, Long entityId, String fieldName, @Nullable String oldValue, @Nullable String newValue)
    {
        this.entityType = requireNonNull(entityType, "entityType");
        this.entityId = requireNonNull(entityId, "entityId");
        this.fieldName = requireNonNull(fieldName, "fieldName");
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedAt = LocalDateTime.now();
    }

    public ChangeLogBean(ChangeLogBean in)
    {
        this.id = in.id;
        this.entityType = in.entityType;
        this.entityId = in.entityId;
        this.fieldName = in.fieldName;
        this.oldValue = in.oldValue;
        this.newValue = in.newValue;
        this.changedAt = in.changedAt;
        this.changedBy = in.changedBy;
        this.reason = in.reason;
        this.category = in.category;
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

    public ChangeLogBean id(@Nullable Long value) { id = value; return this; }
    public ChangeLogBean entityType(String value) { entityType = requireNonNull(value, "entityType"); return this; }
    public ChangeLogBean entityId(Long value) { entityId = requireNonNull(value, "entityId"); return this; }
    public ChangeLogBean fieldName(String value) { fieldName = requireNonNull(value, "fieldName"); return this; }
    public ChangeLogBean oldValue(@Nullable String value) { oldValue = value; return this; }
    public ChangeLogBean newValue(@Nullable String value) { newValue = value; return this; }
    public ChangeLogBean changedAt(LocalDateTime value) { changedAt = requireNonNull(value, "changedAt"); return this; }
    public ChangeLogBean changedBy(@Nullable String value) { changedBy = value; return this; }
    public ChangeLogBean reason(@Nullable String value) { reason = value; return this; }
    public ChangeLogBean category(ChangeCategory value) { category = requireNonNull(value, "category"); return this; }
}
