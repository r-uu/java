package de.ruu.app.pragma.jpa;

import de.ruu.app.pragma.core.ChangeCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

@Entity
@Table(name = "change_log")
public class ChangeLogJPA
{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "change_log_seq")
    @SequenceGenerator(name = "change_log_seq", sequenceName = "change_log_seq", allocationSize = 50)
    private @Nullable Long id;

    @Column(nullable = false, length = 120)
    private String entityType;

    @Column(nullable = false)
    private Long entityId;

    @Column(nullable = false, length = 200)
    private String fieldName;

    @Column(length = 4000)
    private @Nullable String oldValue;

    @Column(length = 4000)
    private @Nullable String newValue;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    @Column(length = 200)
    private @Nullable String changedBy;

    @Column(length = 4000)
    private @Nullable String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ChangeCategory category = ChangeCategory.UPDATE;

    protected ChangeLogJPA()
    {
        entityType = "";
        entityId = 0L;
        fieldName = "";
        changedAt = LocalDateTime.now();
    }

    public ChangeLogJPA(String entityType, Long entityId, String fieldName, @Nullable String oldValue, @Nullable String newValue)
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

    public ChangeLogJPA entityType(String value) { entityType = requireNonNull(value, "entityType"); return this; }
    public ChangeLogJPA entityId(Long value) { entityId = requireNonNull(value, "entityId"); return this; }
    public ChangeLogJPA fieldName(String value) { fieldName = requireNonNull(value, "fieldName"); return this; }
    public ChangeLogJPA oldValue(@Nullable String value) { oldValue = value; return this; }
    public ChangeLogJPA newValue(@Nullable String value) { newValue = value; return this; }
    public ChangeLogJPA changedAt(LocalDateTime value) { changedAt = requireNonNull(value, "changedAt"); return this; }
    public ChangeLogJPA changedBy(@Nullable String value) { changedBy = value; return this; }
    public ChangeLogJPA reason(@Nullable String value) { reason = value; return this; }
    public ChangeLogJPA category(ChangeCategory value) { category = requireNonNull(value, "category"); return this; }
}
