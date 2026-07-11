package de.ruu.lib.util;

import org.jspecify.annotations.NonNull;

public interface StringBuilders
{
	public static StringBuilder sb() { return new StringBuilder(); }

	public static StringBuilder sb(@NonNull String string) { return new StringBuilder(string); }

	public static boolean isNullOrEmpty(StringBuilder sb) { return sb == null || sb.toString().isEmpty(); }

	public static boolean isNullOrEmptyOrBlank(StringBuilder sb) { return isNullOrEmpty(sb) || sb.toString().isBlank(); }

	public static boolean startsWith(StringBuilder StringBuilder, String prefix)
	{
		if (isNullOrEmptyOrBlank(StringBuilder))
		{
			return false;
		}

		return StringBuilder.toString().startsWith(prefix);
	}

	public static boolean startsWithUpper(StringBuilder StringBuilder)
	{
		if (isNullOrEmptyOrBlank(StringBuilder))
		{
			return false;
		}

		return Character.isUpperCase(StringBuilder.charAt(0));
	}

	public static boolean startsWithLower(StringBuilder StringBuilder)
	{
		if (isNullOrEmptyOrBlank(StringBuilder))
		{
			return false;
		}

		return Character.isLowerCase(StringBuilder.charAt(0));
	}

	public static boolean endsWith(StringBuilder StringBuilder, String suffix)
	{
		if (isNullOrEmptyOrBlank(StringBuilder))
		{
			return false;
		}

		return StringBuilder.toString().endsWith(suffix);
	}

	/**
	 * @param input string builder to be trimmed
	 * @param charsToTrim characters to be removed from the right end of {@code input}
	 * @return trimmed {@code input} string builder
	 */
	public static StringBuilder rTrimChars(StringBuilder input, String charsToTrim)
	{
		if (isNullOrEmpty(input) || Strings.isNullOrEmpty(charsToTrim)) return input;
		return new StringBuilder(Strings.rTrimChars(input.toString(), charsToTrim));
	}

	public static StringBuilder replace(StringBuilder StringBuilder, String oldString, String newString)
	{
		return new StringBuilder(StringBuilder.toString().replace(oldString, newString));
	}
}