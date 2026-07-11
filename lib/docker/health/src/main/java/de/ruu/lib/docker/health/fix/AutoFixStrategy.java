package de.ruu.lib.docker.health.fix;

/**
 * Strategy interface for automatically fixing failed health checks.
 *
 * <p>Implementations provide specific fix logic for different types of health check failures.
 * For example, starting a stopped Docker container or setting up missing Keycloak realm.</p>
 *
 * @see AutoFixRunner
 */
public interface AutoFixStrategy
{
	/**
	 * Checks if this strategy can handle the given service failure.
	 *
	 * @param serviceName the name of the failed service (e.g., "Keycloak Container")
	 * @return {@code true} if this strategy can fix this service
	 */
	boolean canHandle(String serviceName);

	/**
	 * Attempts to fix the service failure.
	 *
	 * @param serviceName the name of the failed service
	 * @return {@code true} if fix was successful, {@code false} otherwise
	 */
	boolean fix(String serviceName);

	/**
	 * Returns a human-readable description of what this strategy does.
	 *
	 * @return description (e.g., "Starts Keycloak Docker container")
	 */
	String description();
}
