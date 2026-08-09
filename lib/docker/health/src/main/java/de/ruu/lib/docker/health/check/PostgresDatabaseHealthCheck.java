package de.ruu.lib.docker.health.check;

import de.ruu.lib.docker.health.HealthCheckResult;
import de.ruu.lib.docker.health.PragmaEnvironmentSupport;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Checks if a PostgreSQL database is accessible.
 */
public class PostgresDatabaseHealthCheck implements HealthCheck
{
	private static final Logger log = LogManager.getLogger(PostgresDatabaseHealthCheck.class);

	private final String containerName;
	private final String composeServiceName;
	private final String databaseName;
	private final int    port;
	private final String username;
	private final String password;

	public PostgresDatabaseHealthCheck(
		String containerName,
		String composeServiceName,
		String databaseName,
		int port,
		String username,
		String password)
	{
		this.containerName      = containerName;
		this.composeServiceName = composeServiceName;
		this.databaseName       = databaseName;
		this.port               = port;
		this.username           = username;
		this.password           = password;
	}

	public PostgresDatabaseHealthCheck(String containerName, String databaseName, int port, String username, String password)
	{
		this(containerName, containerName, databaseName, port, username, password);
	}

	/**
	 * Convenience constructor with default credentials from .env:
	 * - For pragma DB: pragma / pragma
	 * - For keycloak DB: keycloak / keycloak
	 * - For lib_test DB: lib_test / lib_test
	 */
	public PostgresDatabaseHealthCheck(String containerName, String databaseName, int port)
	{
		// Determine credentials based on database name
		String user, pass;
		if ("keycloak".equals(databaseName))
		{
			user = "keycloak";
			pass = "keycloak";
		}
		else if ("lib_test".equals(databaseName))
		{
			user = "lib_test";
			pass = "lib_test";
		}
		else
		{
			// pragma
			user = "pragma";
			pass = "pragma";
		}
		this(containerName, containerName, databaseName, port, user, pass);
	}

	public PostgresDatabaseHealthCheck(String containerName, String composeServiceName, String databaseName, int port)
	{
		this(containerName, composeServiceName, databaseName, port, credentialsFor(databaseName)[0],
			credentialsFor(databaseName)[1]);
	}

	private static String[] credentialsFor(String databaseName)
	{
		if ("keycloak".equals(databaseName)) return new String[]{"keycloak", "keycloak"};
		if ("lib_test".equals(databaseName)) return new String[]{"lib_test", "lib_test"};
		return new String[]{"pragma", "pragma"};
	}

	@Override
	public HealthCheckResult check()
	{
		log.info("Checking database '{}' in container '{}'...", databaseName, containerName);

		// First check if container is running
		if (!isContainerRunning(containerName))
		{
			log.error("  [FAIL] Container '{}' is not running", containerName);
			return HealthCheckResult.failure(
				"PostgreSQL Container: " + containerName,
				"Container is not running",
				PragmaEnvironmentSupport.composeUpCommand(composeServiceName),
				"ruu-docker-up"
			);
		}

		// Check if database exists
		try
		{
			String jdbcUrl = "jdbc:postgresql://localhost:" + port + "/" + databaseName;
			try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password))
			{
				log.info("  [OK] Database '{}' is accessible", databaseName);
				return HealthCheckResult.success("Database: " + databaseName);
			}
		}
		catch (Exception e)
		{
			log.error("  [FAIL] Cannot connect to database '{}': {}", databaseName, e.getMessage());

			// All databases are in the same container now (postgres)
			String fixCommand = PragmaEnvironmentSupport.composeRestartCommand(
				PragmaEnvironmentSupport.PRAGMA_POSTGRES_SERVICE);
			String alias = "ruu-docker-restart-postgres";

			return HealthCheckResult.failure(
				"Database: " + databaseName,
				"Cannot connect to database: " + e.getMessage(),
				fixCommand,
				alias
			);
		}
	}

	@Override
	public String getName()
	{
		return "PostgreSQL Database: " + databaseName;
	}

	private boolean isContainerRunning(String containerName)
	{
		try
		{
			Process process = Runtime.getRuntime().exec(
				new String[]{"docker", "inspect", "-f", "{{.State.Running}}", containerName}
			);

			try (java.io.BufferedReader reader = new java.io.BufferedReader(
					new java.io.InputStreamReader(process.getInputStream())))
			{
				String line = reader.readLine();
				return "true".equals(line);
			}
		}
		catch (Exception e)
		{
			return false;
		}
	}
}
