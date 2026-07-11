package de.ruu.lib.util;

import static de.ruu.lib.util.BooleanFunctions.not;

public interface StringBuffers
{
	public static StringBuffer sb() { return new StringBuffer(); }

	public static StringBuffer sb(String string) { return new StringBuffer(string); }

	public static boolean isNullOrEmpty   (StringBuffer sb) { return sb == null || sb.toString().isEmpty(); }
	public static boolean isNotNullOrEmpty(StringBuffer sb) { return not(isNullOrEmpty(sb)); }

	public static boolean isNullOrEmptyOrBlank(StringBuffer sb) { return isNullOrEmpty(sb) || sb.toString().isBlank(); }

	public static boolean startsWith(StringBuffer stringBuffer, String prefix)
	{
		if (isNullOrEmptyOrBlank(stringBuffer))
		{
			return false;
		}

		return stringBuffer.toString().startsWith(prefix);
	}

	public static boolean startsWithUpper(StringBuffer stringBuffer)
	{
		if (isNullOrEmptyOrBlank(stringBuffer))
		{
			return false;
		}

		return Character.isUpperCase(stringBuffer.charAt(0));
	}

	public static boolean startsWithLower(StringBuffer stringBuffer)
	{
		if (isNullOrEmptyOrBlank(stringBuffer))
		{
			return false;
		}

		return Character.isLowerCase(stringBuffer.charAt(0));
	}

	public static boolean endsWith(StringBuffer stringBuffer, String suffix)
	{
		if (isNullOrEmptyOrBlank(stringBuffer))
		{
			return false;
		}

		return stringBuffer.toString().endsWith(suffix);
	}

	/**
	 * @param input string buffer to be trimmed
	 * @param charsToTrim characters to be removed from the left end of {@code input}
	 * @return trimmed {@code input} string buffer
	 */
	public static StringBuffer lTrimChars(StringBuffer input, String charsToTrim)
	{
		if (isNullOrEmpty(input) || Strings.isNullOrEmpty(charsToTrim)) return input;
		return new StringBuffer(Strings.lTrimChars(input.toString(), charsToTrim));
	}

	/**
	 * @param input string buffer to be trimmed
	 * @param charsToTrim characters to be removed from the right end of {@code input}
	 * @return trimmed {@code input} string buffer
	 */
	public static StringBuffer rTrimChars(StringBuffer input, String charsToTrim)
	{
		if (isNullOrEmpty(input) || Strings.isNullOrEmpty(charsToTrim)) return input;
		return new StringBuffer(Strings.rTrimChars(input.toString(), charsToTrim));
	}

	/**
	 * @param input string buffer to be trimmed
	 * @param charsToTrim characters to be removed from the right and left end of {@code input}
	 * @return trimmed {@code input} string buffer
	 */
	public static StringBuffer lrTrimChars(StringBuffer input, String charsToTrim)
	{
		if (isNullOrEmpty(input) || Strings.isNullOrEmpty(charsToTrim)) return input;
		return new StringBuffer(Strings.lrTrimChars(input.toString(), charsToTrim));
	}

	public static StringBuffer replace(StringBuffer stringBuffer, String oldString, String newString)
	{
		return new StringBuffer(stringBuffer.toString().replace(oldString, newString));
	}
}