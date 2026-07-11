package de.ruu.lib.docker.health.check;

import de.ruu.lib.docker.health.HealthCheckResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Checks if Keycloak server is running and accessible.
 */
public class KeycloakServerHealthCheck implements HealthCheck
{
	private static final Logger log = LoggerFactory.getLogger(KeycloakServerHealthCheck.class);

	private final String host;
	private final int port;

	public KeycloakServerHealthCheck(String host, int port)
	{
		this.host = host;
		this.port = port;
	}

	/**
	 * Convenience constructor with default values (localhost:8080).
	 */
	public KeycloakServerHealthCheck()
	{
		this("localhost", 8080);
	}

	@Override
	public HealthCheckResult check()
	{
		log.info("Checking Keycloak server...");

		// Check if container is running
		if (!isContainerRunning("keycloak"))
		{
			log.error("  ❌ Keycloak container is not running");
			return HealthCheckResult.failure(
				"Keycloak Container",
				"Container is not running",
				"cd ~/develop/github/main/config/shared/docker && docker compose up -d keycloak",
				"ruu-keycloak-start"
			);
		}

		// Check if server is accessible
		if (!isPortListening(host, port))
		{
			log.error("  ❌ Keycloak server is not responding on port {}", port);
			return HealthCheckResult.failure(
				"Keycloak Server",
				"Keycloak server is not responding on port " + port,
				"docker logs keycloak",
				"ruu-keycloak-logs"
			);
		}

		log.info("  ✅ Keycloak server is running");
		return HealthCheckResult.success("Keycloak Server");
	}

	@Override
	public String getName()
	{
		return "Keycloak Server";
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

	private boolean isPortListening(String host, int port)
	{
		try (java.net.Socket socket = new java.net.Socket(host, port))
		{
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
}
