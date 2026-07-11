package de.ruu.lib.jpa.core;

import jakarta.annotation.Nullable;
import jakarta.json.bind.annotation.JsonbProperty;
import org.jspecify.annotations.NonNull;

import java.io.Serial;
import java.util.Objects;

/**
 * Base class of <b>D</b>ata <b>T</b>ransfer <b>O</b>bjects (DTOs) for corresponding {@link AbstractEntity} classes.
 * DTOs provide the following features for abstract entities:
 * <ul>
 *   <li>State transfer for entities to remote systems. Transfer is typically handled by http using rest with json but
 *       is generally technology independent.</li>
 *   <li>Preservation of encapsulation: {@link #id} and {@link #version} are injected into constructor and can not be
 *       modified afterwards.</li>
 * </ul>
 *
 * @author r-uu
 */
public abstract class AbstractDTO<E extends AbstractEntity<?>> implements Entity<Long>, DTO<E, Long>
{
	@Serial private static final long serialVersionUID = 1L;

	/**
	 * May be {@code null} if corresponding {@link AbstractEntity} was not (yet) persisted.<p>
	 * May not be modified from outside type hierarchy (from non-{@link AbstractDTO}-subclasses).<p>
	 * Not {@code final} or {@code @NonNull} because otherwise there would have to be a constructor
	 * with {@code id}-parameter
	 */
	@JsonbProperty("id")
	@Nullable private Long id;

	/**
	 * May be {@code null} if corresponding {@link AbstractEntity} was not (yet) persisted.<p>
	 * <p>may not be modified from outside type hierarchy (from non-{@link AbstractEntity}-subclasses)
	 * <p>not {@code final} or {@code @NonNull} because otherwise there has to be a constructor with {@code id}-parameter
	 */
	@JsonbProperty("version")
	@Nullable private Short version;

	/** no args default constructor for mapstruct */
	protected AbstractDTO() { }

	/**
	 * {@link DTO} instances may correspond to not (yet) persistent {@link Entity} instances. Therefore {@code null}
	 * values for the parameters are allowed.
	 * @param id      see {@link #id}
	 * @param version see {@link #version}
	 */
	protected AbstractDTO(Long id, Short version)
	{
		this();
		this.id      = id;
		this.version = version;
	}

	// java bean style accessors for those who do not work with fluent style accessors (mapstruct)
	/** bean style getter to comply with java bean conventions*/
	@Override public Long getId() { return Entity.super.getId(); }
	/** bean style getter to comply with java bean conventions*/
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
	protected void mapIdAndVersion(@NonNull Entity<Long> source)
	{
		// set fields that can not be modified from outside
		id      = source.id();
		version = source.version();
	}

	@Override public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof AbstractDTO<?> other)) return false;
		return Objects.equals(id, other.id) && Objects.equals(version, other.version);
	}

	@Override public int hashCode() { return Objects.hash(id, version); }

	@Override public String toString()
	{
		return getClass().getSimpleName() + "(id=" + id + ", version=" + version + ")";
	}
}
