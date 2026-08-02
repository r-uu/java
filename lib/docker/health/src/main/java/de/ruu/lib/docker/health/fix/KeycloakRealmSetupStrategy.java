package de.ruu.lib.docker.health.fix;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Auto-fix strategy for setting up missing Keycloak realm.
 *
 * <p>Dynamically loads and calls the application-specific realm setup class
 * (configured via environment or system property). This allows each application
 * to define its own realm configuration without changing this generic strategy.</p>
 *
 * <h2>Configuration:</h2>
 * <ul>
 *   <li>System property: <code>keycloak.setup.class</code></li>
 *   <li>Environment variable: <code>KEYCLOAK_SETUP_CLASS</code></li>
 *   <li>Default (for Pragma): <code>de.ruu.app.pragma.rest.keycloak.PragmaRealmSetup</code></li>
 * </ul>
 */
public class KeycloakRealmSetupStrategy implements AutoFixStrategy
{
	private static final Logger log = LogManager.getLogger(KeycloakRealmSetupStrategy.class);

	private static final String DEFAULT_SETUP_CLASS = "de.ruu.app.pragma.rest.keycloak.PragmaRealmSetup";

	@Override
	public boolean canHandle(String serviceName)
	{
		return "Keycloak Realm".equals(serviceName);
	}

	@Override
	public boolean fix(String serviceName)
	{
		try
		{
			log.info("Setting up Keycloak realm...");

			String setupClassName = System.getProperty("keycloak.setup.class",
					System.getenv().getOrDefault("KEYCLOAK_SETUP_CLASS", DEFAULT_SETUP_CLASS));
			log.debug("Loading realm setup class: {}", setupClassName);

			// Dynamically load and instantiate the setup class
			Class<?> setupClass = Class.forName(setupClassName);
			Object setupInstance = setupClass.getDeclaredConstructor().newInstance();

			// Check if it has an execute() method
			if (setupInstance instanceof Runnable)
			{
				((Runnable) setupInstance).run();
			}
			else
			{
				// Try to call execute() method directly
				setupClass.getMethod("execute").invoke(setupInstance);
			}

			log.info("[OK] Keycloak realm setup completed successfully");

			// Wait a bit for Keycloak to process changes
			Thread.sleep(2000);

			return true;
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			log.error("Interrupted during realm setup: {}", e.getMessage());
			return false;
		}
		catch (Exception e)
		{
			log.error("Failed to setup Keycloak realm: {}", e.getMessage(), e);
			return false;
		}
	}

	@Override
	public String description()
	{
		return "Sets up Keycloak realm by calling application-specific setup class";
	}
}
