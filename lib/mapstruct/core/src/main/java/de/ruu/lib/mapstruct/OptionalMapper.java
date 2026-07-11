package de.ruu.lib.mapstruct;

import java.time.LocalDate;
import java.util.Optional;

import org.mapstruct.Mapper;

/**
 * MapStruct helper to automatically convert between {@link Optional} and nullable types.
 * <p>
 * MapStruct uses method names to detect which conversion to apply.
 * To avoid type erasure conflicts, each method has a unique name.
 */
@Mapper
public interface OptionalMapper
{
	/**
	 * Unwraps an Optional&lt;String&gt; to a nullable String.
	 *
	 * @param optional the optional value
	 * @return the unwrapped value or null
	 */
	default String unwrapString(Optional<String> optional)
	{
		return optional != null ? optional.orElse(null) : null;
	}

	/**
	 * Unwraps an Optional&lt;LocalDate&gt; to a nullable LocalDate.
	 *
	 * @param optional the optional value
	 * @return the unwrapped value or null
	 */
	default LocalDate unwrapLocalDate(Optional<LocalDate> optional)
	{
		return optional != null ? optional.orElse(null) : null;
	}

	/**
	 * Wraps a nullable String into an Optional.
	 *
	 * @param value the nullable value
	 * @return an Optional containing the value, or empty
	 */
	default Optional<String> wrapString(String value)
	{
		return Optional.ofNullable(value);
	}

	/**
	 * Wraps a nullable LocalDate into an Optional.
	 *
	 * @param value the nullable value
	 * @return an Optional containing the value, or empty
	 */
	default Optional<LocalDate> wrapLocalDate(LocalDate value)
	{
		return Optional.ofNullable(value);
	}
}
