package de.ruu.lib.jpa.core;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import org.jspecify.annotations.NonNull;

import java.io.Serial;
import java.util.Objects;

/**
 * Mapped superclass for JPA entities that implements features of {@link Entity} interface. It is useful as base class
 * for {@link Entity} types that have a corresponding {@link AbstractDTO} implementation.
 *
 * @author r-uu
 */
@MappedSuperclass
public abstract class AbstractEntity<D extends AbstractDTO<?>> implements Entity<Long>
{
	@Serial private static final long serialVersionUID = 1L;

	/**
	 * may be <pre>null</pre> if instance was not (yet) persisted.
	 * <p>may not be modified from outside type hierarchy (from non-{@link AbstractEntity}-subclasses)
	 * <p>not {@code final} or {@code @NonNull} because otherwise there has to be a constructor with {@code id}-parameter
	 */
	@Nullable
	@Id @GeneratedValue private Long id;

	/** may be <pre>null</pre> if {@link AbstractEntity} was not (yet) persisted. */
	@Nullable
	@Version @Column(nullable = false)
	private Short version;

	/** required by JPA */
	protected AbstractEntity() { }

	protected AbstractEntity(Entity<Long> entity)
	{
		id      = entity.id();
		version = entity.version();
	}

	// java bean style accessors for those who do not work with fluent style accessors (mapstruct)
	/** bean style getter to comply with java bean conventions*/
	// TODO find out why default implementation in Entity2 interface is not sufficient
	@Override public Long getId() { return Entity.super.getId(); }
	/** bean style getter to comply with java bean conventions*/
	// TODO find out why default implementation in Entity2 interface is not sufficient
	@Override public Short getVersion() { return Entity.super.getVersion(); }

	@Override public Long  id()      { return id; }
	@Override public Short version() { return version; }

	/**
	 * to be called in mapstruct callbacks by subclasses
	 * <p>
	 * Picks the {@link AbstractDTO#getId()} and {@link AbstractDTO#getVersion()} values and assigns them to the
	 * respective fields in this class.
	 *
	 * @param source
	 * @throws NullPointerException if {@code source} is {@code null}
	 */
	protected void mapIdAndVersion(@NonNull D source) { mapIdAndVersion((Entity<Long>) source); }

	protected void mapIdAndVersion(@NonNull Entity<Long> source)
	{
		// set fields that can not be modified from outside
		id      = source.getId();
		version = source.getVersion();
	}

	@Override public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof AbstractEntity<?> other)) return false;
		return Objects.equals(id, other.id) && Objects.equals(version, other.version);
	}

	@Override public int hashCode() { return Objects.hash(id, version); }

	@Override public String toString()
	{
		return getClass().getSimpleName() + "(id=" + id + ", version=" + version + ")";
	}
}
