package de.ruu.lib.util.json;

public abstract class Sanitiser
{
	/** Private constructor to prevent instantiation of utility class. */
	private Sanitiser() { throw new AssertionError("utility class"); }

	private static final String ESCAPED_DOUBLE_QUOTE   = "\\\"";
	private static final String ESCAPED_NEWLINE        = "\\n";

	private static final String NEWLINE                = "\n";
	private static final String DOUBLE_QUOTE           = "\"";
	private static final String SQUARE_BRACKET_OPENING = "[";
	private static final String SQUARE_BRACKET_CLOSING = "]";

//	private static final String DOUBLE_QUOTE__NEWLINE                = DOUBLE_QUOTE           + NEWLINE;
	private static final String DOUBLE_QUOTE__SQUARE_BRACKET_OPENING = DOUBLE_QUOTE           + SQUARE_BRACKET_OPENING;
	private static final String SQUARE_BRACKET_CLOSING__DOUBLE_QUOTE = SQUARE_BRACKET_CLOSING + DOUBLE_QUOTE;

	public final static String sanitise(String param)
	{
//		log.debug("in\n" + param);
		if (param.contains(ESCAPED_NEWLINE))
		{
//			log.debug("replacing escaped newline with new line");
			param = param.replace(ESCAPED_NEWLINE, NEWLINE);
		}
		if (param.contains(ESCAPED_DOUBLE_QUOTE))
		{
//			log.debug("replacing escaped double quote with double quote");
			param = param.replace(ESCAPED_DOUBLE_QUOTE, DOUBLE_QUOTE);
		}
//		if (param.contains(DOUBLE_QUOTE__NEWLINE))
//		{
////			log.debug("replacing double quote + new line with new line");
//			param = param.replace(DOUBLE_QUOTE__NEWLINE, NEWLINE);
//		}
		if (param.contains(DOUBLE_QUOTE__SQUARE_BRACKET_OPENING))
		{
//		log.debug("replacing double quote + square bracket opening with square bracket opening");
			param = param.replace(DOUBLE_QUOTE__SQUARE_BRACKET_OPENING, SQUARE_BRACKET_OPENING);
		}
		if (param.contains(SQUARE_BRACKET_CLOSING__DOUBLE_QUOTE))
		{
//		log.debug("replacing square bracket closing + double quote with square bracket closing");
			param = param.replace(SQUARE_BRACKET_CLOSING__DOUBLE_QUOTE, SQUARE_BRACKET_CLOSING);
		}
//		log.debug("out\n" + param);
		return param;
	}
}