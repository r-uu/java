package de.ruu.lib.jpa.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import org.jspecify.annotations.NonNull;

import java.io.Serializable;
import java.util.Optional;

import static java.util.Objects.nonNull;

/**
 * Defines common features primary key ({@link #getId()}) and version {@link #getVersion()} for JPA entities and their
 * corresponding DTOs.
 *
 * @param <I> type of primary key (id), it has to be serializable
 *
 * @author r-uu
 */
public interface Entity<I extends Serializable> extends Serializable
{
	/** name of the field (property) that represents primary key. */
	String P_ID = "id";

	// fluent style accessors
	/** @return primary key, may be {@code null}, {@code null} indicates that entity was not (yet) persisted. */
	@Nullable I     id     ();
	/** @return version    , may be {@code null}, {@code null} indicates that entity was not (yet) persisted. */
	@Nullable Short version();

	// java bean style accessors for those who do not work with fluent style accessors (mapstruct)
	/** @return primary key, may be {@code null}, {@code null} indicates that entity was not (yet) persisted. */
	default @Nullable I     getId     () { return id();      };
	/** @return version    , may be {@code null}, {@code null} indicates that entity was not (yet) persisted. */
	default @Nullable Short getVersion() { return version(); };

	/** @return optional primary key, {@link Optional#empty()} indicates that entity was not (yet) persisted. */
	default public @NonNull Optional<I>     optionalId()      { return Optional.ofNullable(id     ()); }
	/** @return optional version,     {@link Optional#empty()} indicates that entity was not (yet) persisted. */
	default public @NonNull Optional<Short> optionalVersion() { return Optional.ofNullable(version()); }

	default EntityInfo entityInfo() { return new EntityInfo<>(this); }
	@JsonIgnore
	default boolean isPersisted() { return nonNull(id()) && nonNull(version()); }

	class EntityInfo<I extends Serializable> implements Entity<I>
	{
		@Nullable private I     id;
		@Nullable private Short version;

		private EntityInfo(@Nullable I id, @Nullable Short version)
		{
			this.id      = id;
			this.version = version;
		}

		public EntityInfo(@NonNull Entity<I> entity) { this(entity.id(), entity.version()); }

		@Override public @Nullable I     id     () { return id;      }
		@Override public @Nullable Short version() { return version; }
	}
}