package de.ruu.lib.keycloak.admin;

/**
 * Exception thrown when Keycloak admin operations fail.
 * 
 * <p>This exception wraps underlying errors from the Keycloak Admin Client
 * or HTTP communication issues.</p>
 * 
 * @author r-uu
 * @since 2025-12-27
 */
public class KeycloakAdminException extends Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new exception with the specified message.
	 * 
	 * @param message Error message
	 */
	public KeycloakAdminException(String message)
	{
		super(message);
	}

	/**
	 * Creates a new exception with the specified message and cause.
	 * 
	 * @param message Error message
	 * @param cause Underlying cause
	 */
	public KeycloakAdminException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
