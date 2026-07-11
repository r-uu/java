package de.ruu.lib.jpa.core.mapstruct.demo.tree;

import de.ruu.lib.jpa.core.AbstractDTO;
import de.ruu.lib.jpa.core.AbstractEntity;
import de.ruu.lib.jpa.core.Entity;
import org.jspecify.annotations.NonNull;

import java.io.Serial;

public class AbstractNode
		<D extends AbstractDTO<?>, E extends AbstractEntity<?>>
		implements Entity<Long>
{
	@Serial private static final long serialVersionUID = 1L;

	/**
	 * may be <pre>null</pre> if instance was not (yet) persisted.
	 * <p>may not be modified from outside type hierarchy (from non-{@link AbstractEntity}-subclasses)
	 * <p>not {@code final} or {@code @NonNull} because otherwise there has to be a constructor with {@code id}-parameter
	 */
	private Long id;

	/** may be <pre>null</pre> if {@link AbstractEntity} was not (yet) persisted. */
	private Short version;

	// java bean style accessors for those who do not work with fluent style accessors (mapstruct)
	/** bean style getter to comply with java bean conventions*/
	// TODO find out why default implementation in Entity2 interface is not sufficient
	@Override public Long getId() { return Entity.super.getId(); }
	/** bean style getter to comply with java bean conventions*/
	// TODO find out why default implementation in Entity2 interface is not sufficient
	@Override public Short getVersion() { return Entity.super.getVersion(); }

	@Override public Long id() { return id; }

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
	protected void mapIdAndVersion(@NonNull D source)
	{
		// set fields that can not be modified from outside
		id      = source.getId();
		version = source.getVersion();
	}

	/**
	 * to be called in mapstruct callbacks by subclasses
	 * <p>
	 * Picks the {@link AbstractDTO#getId()} and {@link AbstractDTO#getVersion()} values and assigns them to the
	 * respective fields in this class.
	 *
	 * @param source
	 * @throws NullPointerException if {@code source} is {@code null}
	 */
	protected void mapIdAndVersion(@NonNull E source)
	{
		// set fields that can not be modified from outside
		id      = source.getId();
		version = source.getVersion();
	}
}
