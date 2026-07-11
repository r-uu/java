package de.ruu.lib.mapstruct;

import org.jspecify.annotations.NonNull;

/**
 * Interface for types that can be mapped to {@code S}. Instances of  {@code S} on the other hand can be mapped
 *  * int {@link BiMappedTarget} with {@link BiMappedSource#toTarget()}.
 *
 * @param <S> {@link BiMappedSource} type
 *
 * @author r-uu
 */
public interface BiMappedTarget<S extends BiMappedSource<?>>
{
	@NonNull S toSource();
}
