package de.ruu.lib.util;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

/**
 * Base type for event classes. Makes sure {@link #source()} never returns {@code null}.
 * @author ruu
 */
public abstract class AbstractEvent<S, D>
{
	private S source;
	private D data;

	/**
	 * @param source must not be null
	 * @param data
	 * @throws NullPointerException if {@code source} is {@code null}
	 */
	protected AbstractEvent(S source, D data)
	{
		requireNonNull(source, "source must not be null");
		this.source = source;
		this.data   = data;
	}

	protected AbstractEvent(S source) { this(source, null); }

	public S source() { return source; }
	public Optional<D> data() { return Optional.ofNullable(data); }
}