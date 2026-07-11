package de.ruu.lib.util;

public final class Types
{
	/** Private constructor to prevent instantiation of utility class. */
	private Types() { throw new AssertionError("utility class"); }

	public static String packageNameFromQualifiedTypeName(String qualifiedTypeName)
	{
		int lastIndexOfPackageName = qualifiedTypeName.lastIndexOf(".");

		if (lastIndexOfPackageName < 0) return "";

		return qualifiedTypeName.substring(0, qualifiedTypeName.lastIndexOf("."));
	}

	public static String simpleNameFromQualifiedTypeName(String qualifiedTypeName)
	{
		return qualifiedTypeName.substring(qualifiedTypeName.lastIndexOf(".") + 1);
	}
}