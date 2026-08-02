package de.ruu.app.pragma.rest.keycloak;

import de.ruu.lib.keycloak.admin.setup.BaseKeycloakRealmSetup;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * PRAGMA-specific Keycloak Realm Setup
 *
 * <p>Erstellt den Keycloak Realm für die PRAGMA Anwendung mit allen
 * Pragma-spezifischen Konfigurationen (Realm Name, Client, Rollen, etc.).</p>
 *
 * <h2>Erstellt folgende Ressourcen:</h2>
 * <ul>
 *   <li>Realm: pragma-realm</li>
 *   <li>Client: pragma-frontend (Public Client, Direct Access Grants)</li>
 *   <li>Audience: pragma-backend (in JWT Tokens)</li>
 *   <li>Rollen: taskgroup-*, task-*, pragma-admin</li>
 *   <li>Test-User: r-uu / r-uu (mit allen Rollen)</li>
 * </ul>
 *
 * <h2>Aufruf:</h2>
 * <pre>
 *   mvn -pl app/pragma/backend/rest exec:java \
 *     -Dexec.mainClass="de.ruu.app.pragma.rest.keycloak.PragmaRealmSetup"
 * </pre>
 *
 * @author r-uu
 * @since 2026-01-19
 */
public class PragmaRealmSetup extends BaseKeycloakRealmSetup
{
	private static final Logger log = LogManager.getLogger(PragmaRealmSetup.class);

	public static void main(String[] args)
	{
		try
		{
			new PragmaRealmSetup().execute();
		}
		catch (Exception e)
		{
			log.error("[FAIL] Setup failed: {}", e.getMessage(), e);
			System.exit(1);
		}
	}

	@Override
	protected String getRealmName()
	{
		return System.getProperty("keycloak.realm",
				System.getenv().getOrDefault("keycloak_realm", "pragma-realm"));
	}

	@Override
	protected String getClientId()
	{
		return System.getProperty("keycloak.client.id",
				System.getenv().getOrDefault("keycloak_client_id", "pragma-frontend"));
	}

	@Override
	protected String getAudience()
	{
		return System.getProperty("keycloak.audience",
				System.getenv().getOrDefault("keycloak_audience", "pragma-backend"));
	}

	@Override
	protected String getRealmDisplayName()
	{
		return "PRAGMA Application Realm";
	}

	@Override
	protected String[] getRequiredRoles()
	{
		return new String[]{
			"taskgroup-read",
			"taskgroup-create",
			"taskgroup-update",
			"taskgroup-delete",
			"task-read",
			"task-create",
			"task-update",
			"task-delete",
			"pragma-admin"
		};
	}

	@Override
	protected String getTestUsername()
	{
		return System.getProperty("app.test.user",
				System.getenv().getOrDefault("app_test_user_username", "r-uu"));
	}

	@Override
	protected String getTestPassword()
	{
		return System.getProperty("app.test.password",
				System.getenv().getOrDefault("app_test_user_password", "r-uu"));
	}
}
