package de.ruu.lib.util;

import static de.ruu.lib.util.BooleanFunctions.not;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import jakarta.annotation.Nullable;
import org.jspecify.annotations.NonNull;

public interface Collections
{
	/**
	 * @param <T> der Elementtyp der Collection
	 * @param elems if {@code null}, an empty list will be returned
	 * @return a mutable set with {@code elems} as items, modifications of the set will <b>not</b> change {@code elems}
	 *         array
	 */
	@SafeVarargs
	static <T> Set<T> asSet(T... elems)
	{
		if (elems == null) return new HashSet<>();
		return new HashSet<>(Arrays.asList(elems));
	}

	/**
	 * @param <T> der Elementtyp der Collection
	 * @param elems if <code>null</code>, an empty list will be returned
	 * @return a mutable list with {@code elems} as items, modifications of the set will <b>not</b> change {@code elems}
	 *         array
	 */
	@SafeVarargs
	static <T> List<T> asList(T... elems)
	{
		if (elems == null) return new ArrayList<>();
		return new ArrayList<>(Arrays.asList(elems));
	}

	static <T> List<T> asList(Iterable<T> iterable)
	{
		if (iterable == null) return new ArrayList<>();

		List<T> result = new ArrayList<>();

		for (T t : iterable) { result.add(t); }

		return result;
	}

	@SuppressWarnings("unchecked")
	static <T> T[] asArray(@NonNull Class<?> clazz, @NonNull Collection<T> collection)
	{
		return collection.toArray((T[]) Array.newInstance(clazz, collection.size()));
	}


	static boolean isNullOrEmpty(Collection<?> collection)
	{
		if (Objects.isNull(collection)) return true;
		return collection.isEmpty();
	}

	static boolean isNotNullOrEmpty(Collection<?> collection)
	{
		return not(isNullOrEmpty(collection));
	}
}