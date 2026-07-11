package de.ruu.lib.docker.health.fix;

import de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Auto-fix strategy for setting up missing Keycloak realm.
 *
 * <p>Calls {@link KeycloakRealmSetup#main(String[])} directly instead of using Maven.</p>
 */
public class KeycloakRealmSetupStrategy implements AutoFixStrategy
{
	private static final Logger log = LoggerFactory.getLogger(KeycloakRealmSetupStrategy.class);

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
			log.debug("Calling KeycloakRealmSetup.main() directly (no Maven process)");

			// Call KeycloakRealmSetup.main() directly - much more robust than spawning Maven process
			// This avoids "mvn: command not found" issues and is faster
			KeycloakRealmSetup.main(new String[0]);

			log.info("✅ Keycloak realm setup completed successfully");

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
		return "Sets up Keycloak realm by calling KeycloakRealmSetup.main() directly";
	}
}
