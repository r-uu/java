package de.ruu.lib.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jLogProvider
{
	/** Private constructor to prevent instantiation of utility class. */
	private Slf4jLogProvider() { throw new AssertionError("utility class"); }

	/**
	 * Convenience method to obtain a logger using the given class as context.
	 *
	 * @param clazz the class to use as context
	 * @return a logger using the given class as context
	 */
	static Logger logger(Class<?> clazz) { return LoggerFactory.getLogger(clazz); }
}