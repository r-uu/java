package de.ruu.lib.docker.health;

/**
 * Shared paths and commands for the current Pragma development environment.
 */
public final class PragmaEnvironmentSupport
{
	public static final String REPOSITORY_HOME            = "~/develop/github/java";
	public static final String PRAGMA_DIR                 = REPOSITORY_HOME + "/app/pragma";
	public static final String PRAGMA_COMPOSE_FILE        = PRAGMA_DIR + "/docker-compose.yml";
	public static final String KEYCLOAK_ADMIN_POM         = REPOSITORY_HOME + "/lib/keycloak/admin/pom.xml";
	public static final String PRAGMA_POSTGRES_CONTAINER  = "pragma-postgres";
	public static final String PRAGMA_POSTGRES_SERVICE    = "postgres";
	public static final String PRAGMA_KEYCLOAK_CONTAINER  = "pragma-keycloak";
	public static final String PRAGMA_KEYCLOAK_SERVICE    = "keycloak";
	public static final String JASPERREPORTS_CONTAINER    = "jasperreports";

	private PragmaEnvironmentSupport() {}

	public static String composeUpCommand(String serviceName)
	{
		return "docker compose -f " + PRAGMA_COMPOSE_FILE + " up -d " + serviceName;
	}

	public static String composeRestartCommand(String serviceName)
	{
		return "docker compose -f " + PRAGMA_COMPOSE_FILE + " restart " + serviceName;
	}

	public static String keycloakSetupCommand()
	{
		return "cd " + PRAGMA_DIR
			+ " && mvn -f " + KEYCLOAK_ADMIN_POM + " -am exec:java"
			+ " -Dexec.mainClass=de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup"
			+ " -Dkeycloak.admin.user=admin"
			+ " -Dkeycloak.admin.password=admin"
			+ " -Dkeycloak.realm=pragma-realm"
			+ " -Dkeycloak.client.id=pragma-frontend";
	}
}
