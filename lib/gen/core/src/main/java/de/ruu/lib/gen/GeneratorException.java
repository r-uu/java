package de.ruu.lib.gen;

/**
 * Exception that indicates failures during generation processes.
 */
public class GeneratorException extends Exception
{
	/**
	 * serialVersionUID as required for serialisation
	 */
	private static final long serialVersionUID = 3971022374896642753L;

	/**
	 * @param msg
	 * @param throwable
	 */
	public GeneratorException(String msg, Throwable throwable)
	{
		super(msg, throwable);
	}

	/**
	 * @param msg
	 */
	public GeneratorException(String msg)
	{
		super(msg);
	}
}