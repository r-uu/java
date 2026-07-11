package de.ruu.lib.docker.health.check;

import de.ruu.lib.docker.health.HealthCheckResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Checks if Docker daemon is running.
 */
public class DockerDaemonHealthCheck implements HealthCheck
{
	private static final Logger log = LoggerFactory.getLogger(DockerDaemonHealthCheck.class);

	@Override
	public HealthCheckResult check()
	{
		log.info("Checking Docker daemon...");
		try
		{
			Process process = Runtime.getRuntime().exec(new String[]{"docker", "info"});
			int exitCode = process.waitFor();

			if (exitCode == 0)
			{
				log.info("  ✅ Docker daemon is running");
				return HealthCheckResult.success("Docker Daemon");
			}
			else
			{
				log.error("  ❌ Docker daemon is not running");
				return HealthCheckResult.failure(
					"Docker Daemon",
					"Docker daemon is not running",
					"sudo service docker start",
					"ruu-docker-daemon-start"
				);
			}
		}
		catch (Exception e)
		{
			log.error("  ❌ Cannot check Docker daemon: {}", e.getMessage());
			return HealthCheckResult.failure(
				"Docker Daemon",
				"Cannot execute docker commands: " + e.getMessage(),
				"sudo service docker start",
				"ruu-docker-daemon-start"
			);
		}
	}

	@Override
	public String getName()
	{
		return "Docker Daemon";
	}
}
