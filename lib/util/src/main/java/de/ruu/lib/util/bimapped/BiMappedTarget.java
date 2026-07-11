package de.ruu.lib.util.bimapped;

import org.jspecify.annotations.NonNull;

import java.util.Optional;

/**
 * Interface for types that can be mapped to {@code S}.
 *
 * @param <S> {@link BiMappedSource} type
 *
 * @author r-uu
 */
public interface BiMappedTarget<S extends BiMappedSource<?>> extends BiMappable
{
	/** @return optional {@code T} object that corresponds to this {@link BiMappedSource} */
	default Optional<S> map(@NonNull Class<S> sourceType) { return biMap().lookup(this, sourceType); }
}