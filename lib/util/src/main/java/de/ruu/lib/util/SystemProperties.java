package de.ruu.lib.util;

public final class SystemProperties
{
	/** Private constructor to prevent instantiation of utility class. */
	private SystemProperties() { throw new AssertionError("utility class"); }

	public static String userName()      { return System.getProperty("user.name"); }
	public static String userHome()      { return System.getProperty("user.home"); }
	public static String lineSeparator() { return System.getProperty("line.separator"); }
}