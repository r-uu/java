package de.ruu.lib.util.bimapped;

import org.jspecify.annotations.NonNull;

import java.util.Optional;

/**
 * Interface for types that can be mapped to {@code T}.
 *
 * @param <T> {@link BiMappedTarget} type
 *
 * @author r-uu
 */
public interface BiMappedSource<T extends BiMappedTarget<?>> extends BiMappable
{
	/** @return optional {@code T} object that corresponds to this {@link BiMappedTarget} */
	default Optional<T> map(@NonNull Class<T> targetType) { return biMap().lookup(this, targetType); }
}