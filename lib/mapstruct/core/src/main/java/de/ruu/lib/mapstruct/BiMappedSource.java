package de.ruu.lib.mapstruct;

import org.jspecify.annotations.NonNull;

/**
 * Interface for types that can be mapped to {@code T}. Instances of  {@code T} on the other hand can be mapped
 * int {@link BiMappedSource} with {@link BiMappedTarget#toSource()}.
 *
 * @param <T> {@link BiMappedTarget} type
 *
 * @author r-uu
 */
public interface BiMappedSource<T extends BiMappedTarget<?>>
{
	@NonNull T toTarget();
}
