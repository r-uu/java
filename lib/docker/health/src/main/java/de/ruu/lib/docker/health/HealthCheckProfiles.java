package de.ruu.lib.docker.health;

import de.ruu.lib.docker.health.check.*;

/**
 * Pre-configured health check profiles for common scenarios.
 *
 * <p>Provides ready-to-use health check configurations for typical
 * application requirements.
 *
 * <p><b>Example usage:</b>
 * <pre>
 * // Full check for all services
 * HealthCheckRunner runner = HealthCheckProfiles.fullEnvironment();
 * HealthCheckRunner.RunResult result = runner.runAll();
 * boolean healthy = result.healthy();
 *
 * // Check only database services
 * HealthCheckRunner dbRunner = HealthCheckProfiles.databaseOnly();
 * boolean dbHealthy = dbRunner.runAll().healthy();
 * </pre>
 */
public class HealthCheckProfiles
{
	/**
	 * Full environment check: Docker, PostgreSQL, Keycloak, JasperReports.
	 *
	 * <p>This profile checks:
	 * <ul>
	 *   <li>Docker daemon is running</li>
	 *   <li>PostgreSQL databases: pragma, lib_test, keycloak (all in one container)</li>
	 *   <li>Keycloak server is accessible</li>
	 *   <li>Keycloak realm 'pragma-realm' exists</li>
	 *   <li>JasperReports service is accessible</li>
	 * </ul>
	 */
	public static HealthCheckRunner fullEnvironment()
	{
		return HealthCheckRunner.builder()
			.addCheck(new DockerDaemonHealthCheck())
			.addCheck(new PostgresDatabaseHealthCheck("postgres", "pragma", 5432))
			.addCheck(new PostgresDatabaseHealthCheck("postgres", "lib_test", 5432))
			.addCheck(new PostgresDatabaseHealthCheck("postgres", "keycloak", 5432))
			.addCheck(new KeycloakServerHealthCheck())
			.addCheck(new KeycloakRealmHealthCheck("pragma-realm"))
			.addCheck(new JasperReportsHealthCheck())
			.build();
	}

	/**
	 * Database-only check: PostgreSQL databases.
	 *
	 * <p>This profile checks:
	 * <ul>
	 *   <li>Docker daemon is running</li>
	 *   <li>PostgreSQL databases: pragma, lib_test, keycloak (all in one container)</li>
	 * </ul>
	 */
	public static HealthCheckRunner databaseOnly()
	{
		return HealthCheckRunner.builder()
			.addCheck(new DockerDaemonHealthCheck())
			.addCheck(new PostgresDatabaseHealthCheck("postgres", "pragma", 5432))
			.addCheck(new PostgresDatabaseHealthCheck("postgres", "lib_test", 5432))
			.addCheck(new PostgresDatabaseHealthCheck("postgres", "keycloak", 5432))
			.build();
	}

	/**
	 * Keycloak-only check: Keycloak server and realm.
	 *
	 * <p>This profile checks:
	 * <ul>
	 *   <li>Docker daemon is running</li>
	 *   <li>Keycloak server is accessible</li>
	 *   <li>Keycloak realm 'pragma-realm' exists</li>
	 * </ul>
	 */
	public static HealthCheckRunner keycloakOnly()
	{
		return HealthCheckRunner.builder()
			.addCheck(new DockerDaemonHealthCheck())
			.addCheck(new KeycloakServerHealthCheck())
			.addCheck(new KeycloakRealmHealthCheck("pragma-realm"))
			.build();
	}

	/**
	 * Minimal check: Only Docker daemon.
	 *
	 * <p>This profile checks:
	 * <ul>
	 *   <li>Docker daemon is running</li>
	 * </ul>
	 */
	public static HealthCheckRunner minimal()
	{
		return HealthCheckRunner.builder()
			.addCheck(new DockerDaemonHealthCheck())
			.build();
	}

	/**
	 * Backend check: Database + Keycloak (no JasperReports).
	 *
	 * <p>This profile checks:
	 * <ul>
	 *   <li>Docker daemon is running</li>
	 *   <li>PostgreSQL databases: pragma, keycloak (all in one container)</li>
	 *   <li>Keycloak server is accessible</li>
	 *   <li>Keycloak realm 'pragma-realm' exists</li>
	 * </ul>
	 */
	public static HealthCheckRunner backend()
	{
		return HealthCheckRunner.builder()
			.addCheck(new DockerDaemonHealthCheck())
			.addCheck(new PostgresDatabaseHealthCheck("postgres", "pragma", 5432))
			.addCheck(new PostgresDatabaseHealthCheck("postgres", "keycloak", 5432))
			.addCheck(new KeycloakServerHealthCheck())
			.addCheck(new KeycloakRealmHealthCheck("pragma-realm"))
			.build();
	}
}
