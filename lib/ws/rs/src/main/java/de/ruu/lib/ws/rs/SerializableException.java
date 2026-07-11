package de.ruu.lib.ws.rs;

import java.util.Arrays;
import java.util.List;

public class SerializableException
{
	private final String                  type;
	private final String                  message;
	private final List<StackTraceElement> stackTrace;
	private final SerializableException   cause;

	public SerializableException(Throwable t)
	{
		this.type       = t.getClass().getName();
		this.message    = t.getMessage();
		this.stackTrace = Arrays.asList(t.getStackTrace());
		this.cause      = (t.getCause() != null ? new SerializableException(t.getCause()) : null);
	}

	public String                  getType()       { return type; }
	public String                  getMessage()    { return message; }
	public List<StackTraceElement> getStackTrace() { return stackTrace; }
	public SerializableException   getCause()      { return cause; }
}
