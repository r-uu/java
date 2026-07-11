package de.ruu.lib.docker.health.check;

import de.ruu.lib.docker.health.HealthCheckResult;

/**
 * Base interface for all health checks.
 */
public interface HealthCheck
{
	/**
	 * Performs the health check.
	 *
	 * @return the result of the health check
	 */
	HealthCheckResult check();

	/**
	 * Returns the name of this health check.
	 *
	 * @return health check name (e.g., "Docker Daemon", "PostgreSQL Database")
	 */
	String getName();
}
