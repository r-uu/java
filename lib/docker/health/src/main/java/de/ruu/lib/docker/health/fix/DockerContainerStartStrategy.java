package de.ruu.lib.docker.health.fix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Auto-fix strategy for starting stopped Docker containers.
 *
 * <p>Uses {@code docker compose up -d <container-name>} to start containers.</p>
 */
public class DockerContainerStartStrategy implements AutoFixStrategy
{
	private static final Logger log = LoggerFactory.getLogger(DockerContainerStartStrategy.class);

	private final String dockerComposeDir;
	private final int healthWaitSeconds;

	/**
	 * Creates strategy with default docker-compose directory and 30s health wait time.
	 */
	public DockerContainerStartStrategy()
	{
		this("~/develop/github/main/config/shared/docker", 30);
	}

	/**
	 * Creates strategy with custom configuration.
	 *
	 * @param dockerComposeDir directory containing docker-compose.yml
	 * @param healthWaitSeconds seconds to wait for container to become healthy
	 */
	public DockerContainerStartStrategy(String dockerComposeDir, int healthWaitSeconds)
	{
		this.dockerComposeDir = dockerComposeDir;
		this.healthWaitSeconds = healthWaitSeconds;
	}

	@Override
	public boolean canHandle(String serviceName)
	{
		return serviceName != null && serviceName.endsWith(" Container");
	}

	@Override
	public boolean fix(String serviceName)
	{
		// Extract container name from service name (e.g., "Keycloak Container" -> "keycloak")
		String containerName = extractContainerName(serviceName);
		if (containerName == null)
		{
			log.error("Cannot extract container name from: {}", serviceName);
			return false;
		}

		try
		{
			log.info("Starting Docker container: {}", containerName);

			String command = String.format(
				"cd %s && docker compose up -d %s",
				dockerComposeDir,
				containerName
			);

			ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
			pb.redirectErrorStream(true);

			Process process = pb.start();

			// Log output
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
			{
				reader.lines().forEach(line -> log.debug("docker-compose: {}", line));
			}

			int exitCode = process.waitFor();

			if (exitCode == 0)
			{
				log.info("✅ Container {} started successfully", containerName);

				// Wait for container to become healthy
				log.info("Waiting {}s for container to become healthy...", healthWaitSeconds);
				Thread.sleep(healthWaitSeconds * 1000L);

				return true;
			}
			else
			{
				log.error("❌ Failed to start container {} (exit code: {})", containerName, exitCode);
				return false;
			}
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			log.error("Interrupted while starting container: {}", e.getMessage());
			return false;
		}
		catch (Exception e)
		{
			log.error("Failed to start container {}: {}", containerName, e.getMessage());
			return false;
		}
	}

	@Override
	public String description()
	{
		return "Starts stopped Docker containers using docker-compose";
	}

	/**
	 * Extracts container name from service name.
	 * Example: "Keycloak Container" -> "keycloak"
	 */
	private String extractContainerName(String serviceName)
	{
		if (serviceName == null || !serviceName.endsWith(" Container"))
		{
			return null;
		}

		// Remove " Container" suffix and convert to lowercase
		String name = serviceName.substring(0, serviceName.length() - " Container".length());
		return name.toLowerCase().trim();
	}
}
