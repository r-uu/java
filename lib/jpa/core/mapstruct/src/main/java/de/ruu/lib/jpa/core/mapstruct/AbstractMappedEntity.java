package de.ruu.lib.jpa.core.mapstruct;

import de.ruu.lib.jpa.core.AbstractDTO;
import de.ruu.lib.jpa.core.AbstractEntity;
import de.ruu.lib.jpa.core.Entity;
import de.ruu.lib.mapstruct.BiMappedSource;
import org.jspecify.annotations.NonNull;

/**
 * Methods of this class are meant as callback hooks for mapstruct mappers and should be used to map values into private
 * / protected fields of {@code this} instance that can not be populated by mapstruct automatically because of their
 * restricted visibility or for other reasons such as mapping collections, optional values or necessary translation of
 * one value range into another.
 */
public abstract class AbstractMappedEntity<D extends AbstractMappedDTO<?>> extends AbstractEntity<D>
		implements BiMappedSource<D>
{
	/**
	 * maps values from {@code input} into hidden fields of {@code this}.
	 *
	 * @param input provides values for hidden fields returned by {@link AbstractDTO#id()} and
	 * {@link AbstractDTO#version()}.
	 */
	protected void beforeMapping(@NonNull Entity<Long> input)
	{
		mapIdAndVersion(input);
	}

	@SuppressWarnings("unused")
	protected void afterMapping(@NonNull Entity<Long> input)
	{
	};
}