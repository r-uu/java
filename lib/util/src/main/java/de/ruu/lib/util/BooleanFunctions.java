package de.ruu.lib.util;

/**
 * Fluent style boolean functions / predicates.
 * @author r-uu
 */
public interface BooleanFunctions
{
	public static boolean is   (boolean b) { return  b; }
	public static boolean isNot(boolean b) { return !b; }
	public static boolean   not(boolean b) { return !b; }
}