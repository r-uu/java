package de.ruu.lib.docker.health.check;

import de.ruu.lib.docker.health.HealthCheckResult;
import de.ruu.lib.docker.health.PragmaEnvironmentSupport;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Checks if JasperReports service is running and accessible.
 */
public class JasperReportsHealthCheck implements HealthCheck
{
	private static final Logger log = LogManager.getLogger(JasperReportsHealthCheck.class);

	private final String host;
	private final int port;

	public JasperReportsHealthCheck(String host, int port)
	{
		this.host = host;
		this.port = port;
	}

	/**
	 * Convenience constructor with default values (localhost:8090).
	 */
	public JasperReportsHealthCheck()
	{
		this("localhost", 8090);
	}

	@Override
	public HealthCheckResult check()
	{
		log.info("Checking JasperReports service...");

		// Check if container is running
		if (!isContainerRunning(PragmaEnvironmentSupport.JASPERREPORTS_CONTAINER))
		{
			log.error("  [FAIL] JasperReports container is not running");
			return HealthCheckResult.failure(
				"JasperReports Container",
				"Container is not running",
				"docker start " + PragmaEnvironmentSupport.JASPERREPORTS_CONTAINER,
				"ruu-jasper-start"
			);
		}

		// Check if service is accessible
		if (!isPortListening(host, port))
		{
			log.error("  [FAIL] JasperReports service is not responding on port {}", port);
			return HealthCheckResult.failure(
				"JasperReports Service",
				"Service is not responding on port " + port,
				"docker logs jasperreports",
				"ruu-jasper-logs"
			);
		}

		log.info("  [OK] JasperReports service is running");
		return HealthCheckResult.success("JasperReports Service");
	}

	@Override
	public String getName()
	{
		return "JasperReports Service";
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
