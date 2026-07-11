package de.ruu.lib.mapstruct;

import org.jspecify.annotations.NonNull;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.TargetType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Used as {@link org.mapstruct.Context} parameter in mapstruct mappings, leverages mapstruct annotations {@link
 * BeforeMapping}, {@link AfterMapping}, {@link TargetType} and {@link MappingTarget} for methods.
 */
public class ReferenceCycleTracking
{
//	private final Map<Object, Object> map = new IdentityHashMap<>();
	private final Map<Object, Object> map = new HashMap<>();

	/**
	 * @param source may be {@code null}
	 * @param targetType
	 * @return
	 * @param <T>
	 */
	@BeforeMapping public <T> T get(Object source, @TargetType @NonNull Class<T> targetType)
	{
		Objects.requireNonNull(targetType, "targetType");
		return targetType.cast(map.get(source));
	}

	/**
	 * Annotated with {@link BeforeMapping} to make sure, {@code source} and {@code target} are available in the context
	 * as soon as possible.
	 *
	 * @param source
	 * @param target
	 */
	@BeforeMapping public void put(@NonNull Object source, @MappingTarget @NonNull Object target)
	{
		Objects.requireNonNull(source, "source");
		Objects.requireNonNull(target, "target");
		map.put(source, target);
	}

	/** @return immutable map based on {@link #map} */
	public Map<Object, Object> map() { return Map.copyOf(map); }
}
