package de.ruu.lib.ws.rs;

public class TechnicalException extends RuntimeException
{
	public TechnicalException(String message, Throwable cause) { super(message, cause); }
	public TechnicalException(String message)                  { super(message); }
}