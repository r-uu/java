package de.ruu.lib.ws.rs.filter.logging;

import jakarta.ws.rs.core.MultivaluedMap;

import static de.ruu.lib.util.StringBuilders.sb;

public abstract class Util
{
	public static String toString(MultivaluedMap<String, ?> headers)
	{
		StringBuilder result = sb("headers");
		headers.keySet().forEach(k -> result.append("\n" + k + "=" + headers.get(k)));
		return result.toString();
	}
}