package de.ruu.lib.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Objects.isNull;

public abstract class Reflection
{
	/** Private constructor to prevent instantiation of utility class. */
	private Reflection() { throw new AssertionError("utility class"); }

	public static Set<Method> allMethods(Object object) { return allMethods(object.getClass()); }

	/**
	 * recursive implementation!
	 *
	 * @param clazz
	 * @return
	 */
	public static Set<Method> allMethods(Class<?> clazz)
	{
		Set<Method> result = new HashSet<>();

		result.addAll(asList(clazz.getDeclaredMethods()));

		if (isNull(clazz.getSuperclass()) == false) result.addAll(allMethods(clazz.getSuperclass()));

		return result;
	}

	/**
	 * recursive implementation!
	 *
	 * @param clazz
	 * @return
	 */
	public static Set<Field> allFields(Class<?> clazz)
	{
		Set<Field> result = new HashSet<>();

		result.addAll(asList(clazz.getDeclaredFields()));

		if (isNull(clazz.getSuperclass()) == false) { result.addAll(allFields(clazz.getSuperclass())); }

		return result;
	}

	/**
	 * recursive implementation!
	 *
	 * @param clazz
	 * @return first parameterized type in parents of {@code clazz}
	 */
	public static Optional<ParameterizedType> firstParameterizedTypeInParents(Class<?> clazz)
	{
		if (clazz.getGenericSuperclass() instanceof ParameterizedType)
		{
			return Optional.of((ParameterizedType) clazz.getGenericSuperclass());
		}

		if (isNull(clazz.getSuperclass()) == false) return firstParameterizedTypeInParents(clazz.getSuperclass());

		return Optional.empty();
	}
}