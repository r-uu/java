package de.ruu.lib.keycloak.admin.setup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.RealmRepresentation;

import java.util.List;

/**
 * Keycloak Realm Cleanup Utility
 *
 * <p>Deletes old/unused realms from Keycloak.</p>
 *
 * @author r-uu
 * @since 2026-01-21
 */
public class KeycloakRealmCleanup
{
	private static final Logger log = LoggerFactory.getLogger(KeycloakRealmCleanup.class);

	private static final String KEYCLOAK_URL = System.getProperty("keycloak.url", "http://localhost:8080");
	private static final String ADMIN_USER = System.getProperty("keycloak.admin.user", "admin");
	private static final String ADMIN_PASSWORD = System.getProperty("keycloak.admin.password",
			System.getenv().getOrDefault("KEYCLOAK_ADMIN_PASSWORD", "changeme_in_local_env"));

	public static void main(String[] args)
	{
		log.info("=== Keycloak Realm Cleanup ===");
		log.info("Keycloak URL: {}", KEYCLOAK_URL);
		log.info("");

		try (Keycloak keycloak = createKeycloakClient())
		{
			// List all realms
			List<RealmRepresentation> realms = keycloak.realms().findAll();

			log.info("Existing realms:");
			for (RealmRepresentation realm : realms)
			{
				log.info("  - {} (Display Name: {})", realm.getRealm(), realm.getDisplayName());
			}
			log.info("");

			// Delete realm_default if it exists
			if (realmExists(keycloak, "realm_default"))
			{
				log.info("Deleting realm 'realm_default'...");
				keycloak.realm("realm_default").remove();
				log.info("✅ Realm 'realm_default' deleted");
			}
			else
			{
				log.info("✓ Realm 'realm_default' does not exist - nothing to delete");
			}

			log.info("");
			log.info("=== Cleanup abgeschlossen ===");
		}
		catch (Exception e)
		{
			log.error("Error during cleanup", e);
			System.exit(1);
		}
	}

	private static Keycloak createKeycloakClient()
	{
		log.info("Verbinde mit Keycloak Server...");
		return KeycloakBuilder.builder()
				.serverUrl(KEYCLOAK_URL)
				.realm("master")
				.username(ADMIN_USER)
				.password(ADMIN_PASSWORD)
				.clientId("admin-cli")
				.build();
	}

	private static boolean realmExists(Keycloak keycloak, String realmName)
	{
		try
		{
			keycloak.realm(realmName).toRepresentation();
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
}
