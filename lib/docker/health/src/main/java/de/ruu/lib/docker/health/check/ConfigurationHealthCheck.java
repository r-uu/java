package de.ruu.lib.docker.health.check;

import de.ruu.lib.docker.health.HealthCheckResult;
import de.ruu.lib.util.config.mp.ConfigHealthCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Health check for MicroProfile Config properties.
 * Validates that all required configuration properties are present.
 *
 * <p><b>Checks:</b>
 * <ul>
 *   <li>Database connection properties (pragma, lib_test)</li>
 *   <li>Keycloak authentication properties</li>
 *   <li>Service URLs</li>
 *   <li>Testing mode properties</li>
 * </ul>
 */
public class ConfigurationHealthCheck implements HealthCheck
{
	private static final Logger log = LoggerFactory.getLogger(ConfigurationHealthCheck.class);

	@Override
	public String getName()
	{
		return "Configuration";
	}

	@Override
	public HealthCheckResult check()
	{
		log.info("Checking configuration properties...");

		try
		{
			ConfigHealthCheck configCheck = new ConfigHealthCheck();
			ConfigHealthCheck.Result result = configCheck.validate();

			if (result.healthy())
			{
				log.info("  ✅ All required configuration properties are present");
				return HealthCheckResult.success("Configuration");
			}
			else
			{
				log.error("  ❌ Configuration validation failed:");
				result.errors().forEach(error -> log.error("    {}", error));

				return HealthCheckResult.failure(
						"Configuration",
						"Missing or invalid properties: " + String.join(", ", result.errors()),
						"Check testing.properties in project root",
						"ruu-config-validate"
				);
			}
		}
		catch (Exception e)
		{
			log.error("  ❌ Configuration check failed with exception", e);
			return HealthCheckResult.failure(
					"Configuration",
					"Failed to validate configuration: " + e.getMessage(),
					"Ensure testing.properties exists in project root",
					"ruu-config-setup"
			);
		}
	}
}
