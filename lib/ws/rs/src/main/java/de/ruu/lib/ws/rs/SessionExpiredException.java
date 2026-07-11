package de.ruu.lib.ws.rs;

/**
 * Exception thrown when authentication/authorization fails and re-login is required.
 *
 * <p>This exception is specifically designed to signal that the user's session has expired
 * and they need to re-authenticate. It allows the UI layer to display appropriate dialogs
 * for re-login without restarting the application.</p>
 *
 * <h2>Usage in Service Clients:</h2>
 * <pre>{@code
 * if (response.getStatus() == 401) {
 *     authService.logout();
 *     throw new SessionExpiredException("Your session has expired. Please login again.");
 * }
 * }</pre>
 *
 * <h2>Handling in UI Layer:</h2>
 * <pre>{@code
 * try {
 *     taskGroupService.findAll();
 * }
 * catch (SessionExpiredException e) {
 *     // Show re-login dialog
 *     SessionExpiredDialog dialog = ...;
 *     boolean success = dialog.showAndWait();
 *     if (success) {
 *         // Retry operation
 *     }
 * }
 * }</pre>
 *
 * @see TechnicalException
 */
public class SessionExpiredException extends TechnicalException
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new SessionExpiredException with the specified detail message.
	 *
	 * @param message the detail message
	 */
	public SessionExpiredException(String message)
	{
		super(message);
	}

	/**
	 * Constructs a new SessionExpiredException with the specified detail message and cause.
	 *
	 * @param message the detail message
	 * @param cause the cause
	 */
	public SessionExpiredException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Constructs a new SessionExpiredException with the specified cause.
	 *
	 * @param cause the cause
	 */
	public SessionExpiredException(Throwable cause)
	{
		super("Session expired", cause);
	}
}
