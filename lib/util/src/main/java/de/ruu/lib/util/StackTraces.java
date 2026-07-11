package de.ruu.lib.util;

import java.util.Arrays;

public class StackTraces
{
	/** Private constructor to prevent instantiation of utility class. */
	private StackTraces() { throw new AssertionError("utility class"); }

	public static String toString(Throwable t) { return Arrays.toString(t.getStackTrace()).replaceAll(", ", "\n"); }
}
