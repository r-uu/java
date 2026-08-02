package de.ruu.lib.keycloak.admin.setup;

import de.ruu.lib.keycloak.admin.KeycloakAdminException;
import de.ruu.lib.keycloak.admin.KeycloakClientManager;
import de.ruu.lib.keycloak.admin.KeycloakUserManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import jakarta.ws.rs.core.Response;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base Keycloak Realm Setup Utility (Generic, Reusable)
 *
 * <p>Generische Basis-Klasse zur automatischen Erstellung eines Keycloak Realms
 * mit Client, Rollen und Test-User. Diese Klasse definiert den Setup-Workflow,
 * erlaubt aber Subklassen, realm-spezifische Konfiguration zu überschreiben.</p>
 *
 * <h2>Verwendungsbeispiel:</h2>
 * <pre>
 *   public class MyAppRealmSetup extends BaseKeycloakRealmSetup {
 *       &#64;Override
 *       protected String getRealmName() { return "my-realm"; }
 *       
 *       &#64;Override
 *       protected String getClientId() { return "my-frontend"; }
 *       
 *       &#64;Override
 *       protected String getAudience() { return "my-backend"; }
 *       
 *       &#64;Override
 *       protected String getRealmDisplayName() { return "My App Realm"; }
 *       
 *       &#64;Override
 *       protected String[] getRequiredRoles() {
 *           return new String[]{ "user-read", "user-write", "admin" };
 *       }
 *       
 *       &#64;Override
 *       protected String getTestUsername() { return "testuser"; }
 *       
 *       &#64;Override
 *       protected String getTestPassword() { return "testpass"; }
 *   }
 *   
 *   // Dann aufrufen:
 *   public static void main(String[] args) {
 *       new MyAppRealmSetup().execute();
 *   }
 * </pre>
 *
 * <h2>Voraussetzungen:</h2>
 * <ul>
 *   <li>Keycloak Server läuft auf http://localhost:8080</li>
 *   <li>Admin Credentials: admin / admin (oder via Env-Variablen/System Properties)</li>
 * </ul>
 *
 * @author r-uu
 * @since 2026-01-19
 */
public abstract class BaseKeycloakRealmSetup
{
	private static final Logger log = LogManager.getLogger(BaseKeycloakRealmSetup.class);

	protected static final String KEYCLOAK_URL = System.getProperty("keycloak.url", "http://localhost:8080");
	protected static final String ADMIN_USER = System.getProperty("keycloak.admin.user",
			System.getenv().getOrDefault("keycloak_admin_user", "admin"));
	protected static final String ADMIN_PASSWORD = System.getProperty("keycloak.admin.password",
			System.getenv().getOrDefault("keycloak_admin_password", "admin"));

	// Abstract configuration methods - Subklassen müssen diese implementieren
	protected abstract String getRealmName();
	protected abstract String getClientId();
	protected abstract String getAudience();
	protected abstract String getRealmDisplayName();
	protected abstract String[] getRequiredRoles();
	protected abstract String getTestUsername();
	protected abstract String getTestPassword();

	// Cached config values
	private String realmName;
	private String clientId;
	private String audience;
	private String realmDisplayName;
	private String[] requiredRoles;
	private String testUsername;
	private String testPassword;

	/**
	 * Initialize and execute the realm setup
	 */
	public void execute()
	{
		initializeConfig();
		
		log.info("=== Keycloak Realm Setup ===");
		log.info("Keycloak URL: {}", KEYCLOAK_URL);
		log.info("Realm: {}", realmName);
		log.info("Client: {}", clientId);
		log.info("");

		try (Keycloak keycloak = createKeycloakClient())
		{
			createRealm(keycloak);
			createClient(keycloak);
			createRoles(keycloak);
			createGroupsClaimMapper(keycloak);
			createUpnClaimMapper(keycloak);
			createTestUser(keycloak);

			log.info("");
			log.info("=== Setup completed ===");
			log.info("[OK] Realm: {}", realmName);
			log.info("[OK] Client: {} (Public client, Direct Access Grants enabled)", clientId);
			log.info("[OK] Test user: {} / {} (with all roles)", testUsername, testPassword);
			log.info("");
			log.info("Test login command:");
			log.info("curl -X POST '{}/realms/{}/protocol/openid-connect/token' \\", KEYCLOAK_URL, realmName);
			log.info("  -H 'Content-Type: application/x-www-form-urlencoded' \\");
			log.info("  -d 'username={}' \\", testUsername);
			log.info("  -d 'password={}' \\", testPassword);
			log.info("  -d 'grant_type=password' \\");
			log.info("  -d 'client_id={}'", clientId);
		}
		catch (Exception e)
		{
			log.error("[FAIL] Setup failed: {}", e.getMessage(), e);
			System.exit(1);
		}
	}

	/**
	 * Initialize configuration from abstract methods (called once at start)
	 */
	private void initializeConfig()
	{
		this.realmName = getRealmName();
		this.clientId = getClientId();
		this.audience = getAudience();
		this.realmDisplayName = getRealmDisplayName();
		this.requiredRoles = getRequiredRoles();
		this.testUsername = getTestUsername();
		this.testPassword = getTestPassword();
	}

	protected Keycloak createKeycloakClient()
	{
		log.info("Connecting to Keycloak server...");
		return KeycloakBuilder.builder()
				.serverUrl(KEYCLOAK_URL)
				.realm("master")
				.username(ADMIN_USER)
				.password(ADMIN_PASSWORD)
				.clientId("admin-cli")
				.build();
	}

	protected void createRealm(Keycloak keycloak)
	{
		log.info("Checking realm '{}'...", realmName);

		try
		{
			RealmRepresentation existing = keycloak.realm(realmName).toRepresentation();
			if (!Boolean.TRUE.equals(existing.isDuplicateEmailsAllowed()))
			{
				log.info("Enabling duplicate emails for realm '{}'...", realmName);
				existing.setDuplicateEmailsAllowed(true);
				keycloak.realm(realmName).update(existing);
				log.info("[OK] Duplicate emails enabled");
			}
			log.info("[OK] Realm '{}' already exists", realmName);
		}
		catch (Exception e)
		{
			log.info("Creating realm '{}'...", realmName);

			RealmRepresentation realm = new RealmRepresentation();
			realm.setRealm(realmName);
			realm.setEnabled(true);
			realm.setDisplayName(realmDisplayName);
			realm.setRegistrationAllowed(false);
			realm.setResetPasswordAllowed(true);
			realm.setDuplicateEmailsAllowed(true);

			realm.setAccessTokenLifespan(1800);          // 30 minutes
			realm.setSsoSessionIdleTimeout(1800);        // 30 minutes
			realm.setSsoSessionMaxLifespan(36000);       // 10 hours
			realm.setOfflineSessionIdleTimeout(2592000); // 30 days
			realm.setAccessTokenLifespanForImplicitFlow(900); // 15 minutes

			log.info("Configured token lifespans:");
			log.info("  Access token: {} minutes", 1800 / 60);
			log.info("  SSO session idle: {} minutes", 1800 / 60);
			log.info("  SSO session max: {} hours", 36000 / 3600);

			try
			{
				keycloak.realms().create(realm);
				log.info("[OK] Realm '{}' created successfully", realmName);
			}
			catch (Exception ex)
			{
				log.error("Error creating realm '{}'", realmName, ex);
				throw new RuntimeException("Failed to create realm: " + realmName, ex);
			}
		}
	}

	protected void createClient(Keycloak keycloak) throws KeycloakAdminException
	{
		log.info("Checking client '{}'...", clientId);

		try (KeycloakClientManager clientManager = KeycloakClientManager.builder()
				.serverUrl(KEYCLOAK_URL)
				.realm(realmName)
				.adminUsername(ADMIN_USER)
				.adminPassword(ADMIN_PASSWORD)
				.build())
		{
			org.keycloak.representations.idm.ClientRepresentation existingClient =
				clientManager.findClientByClientId(clientId);

			if (existingClient != null)
			{
				String clientUuid = existingClient.getId();
				log.info("[OK] Client '{}' already exists (UUID: {})", clientId, clientUuid);

				try
				{
					org.keycloak.representations.idm.ClientRepresentation client =
						keycloak.realm(realmName).clients().get(clientUuid).toRepresentation();

					if (!Boolean.TRUE.equals(client.isDirectAccessGrantsEnabled()))
					{
						log.info("Enabling Direct Access Grants for client '{}'...", clientId);
						client.setDirectAccessGrantsEnabled(true);
						client.setPublicClient(true);
						keycloak.realm(realmName).clients().get(clientUuid).update(client);
						log.info("[OK] Direct Access Grants enabled");
					}
					else
					{
						log.info("[OK] Direct Access Grants already enabled");
					}
				}
				catch (Exception ex)
				{
					log.warn("Could not check or set Direct Access Grants: {}", ex.getMessage());
				}

				ensureAudienceMapper(keycloak, clientUuid);
			}
			else
			{
				log.info("Creating client '{}'...", clientId);

				String clientUuid = clientManager.createPublicClient(
						clientId,
						Arrays.asList("*"),  // redirectUris
						Arrays.asList("*")   // webOrigins
				);

				log.info("[OK] Client '{}' created (UUID: {})", clientId, clientUuid);

				try
				{
					org.keycloak.representations.idm.ClientRepresentation client =
						keycloak.realm(realmName).clients().get(clientUuid).toRepresentation();
					client.setDirectAccessGrantsEnabled(true);
					client.setPublicClient(true);
					keycloak.realm(realmName).clients().get(clientUuid).update(client);
					log.info("[OK] Direct Access Grants enabled for client '{}'", clientId);
				}
				catch (Exception ex)
				{
					log.error("ERROR: Could not enable Direct Access Grants: {}", ex.getMessage());
					throw new KeycloakAdminException("Could not enable Direct Access Grants", ex);
				}

				createAudienceMapper(keycloak, clientUuid);
			}
		}
	}

	private void ensureAudienceMapper(Keycloak keycloak, String clientUuid)
	{
		log.info("Checking audience mapper...");

		try
		{
			List<ProtocolMapperRepresentation> mappers = keycloak.realm(realmName)
				.clients()
				.get(clientUuid)
				.getProtocolMappers()
				.getMappers();

			boolean hasCorrectAudienceMapper = mappers.stream()
				.filter(mapper -> "oidc-audience-mapper".equals(mapper.getProtocolMapper()))
				.anyMatch(mapper -> {
					Map<String, String> config = mapper.getConfig();
					String customAudience = config.get("included.custom.audience");
					return audience.equals(customAudience);
				});

			if (hasCorrectAudienceMapper)
			{
				log.info("[OK] Audience mapper already exists");
				return;
			}

			createAudienceMapper(keycloak, clientUuid);
		}
		catch (Exception ex)
		{
			log.error("Error checking or creating audience mapper: {}", ex.getMessage());
		}
	}

	private void createAudienceMapper(Keycloak keycloak, String clientUuid)
	{
		log.info("Creating audience mapper for client...");

		try
		{
			ProtocolMapperRepresentation audienceMapper = new ProtocolMapperRepresentation();
			audienceMapper.setName("audience-mapper");
			audienceMapper.setProtocol("openid-connect");
			audienceMapper.setProtocolMapper("oidc-audience-mapper");

			Map<String, String> config = new HashMap<>();
			config.put("included.custom.audience", audience);
			config.put("access.token.claim", "true");
			config.put("id.token.claim", "false");

			audienceMapper.setConfig(config);

			keycloak.realm(realmName)
				.clients()
				.get(clientUuid)
				.getProtocolMappers()
				.createMapper(audienceMapper);

			log.info("[OK] Audience mapper created");
			log.info("   Audience: {}", audience);
			log.info("   Added to: access token");
		}
		catch (Exception ex)
		{
			log.error("ERROR: Could not create audience mapper: {}", ex.getMessage());
		}
	}

	protected void createRoles(Keycloak keycloak)
	{
		log.info("Creating required realm roles...");

		int created = 0;
		int existing = 0;

		for (String roleName : requiredRoles)
		{
			try
			{
				try
				{
					keycloak.realm(realmName).roles().get(roleName).toRepresentation();
					existing++;
					log.info("  [OK] Role '{}' already exists", roleName);
				}
				catch (jakarta.ws.rs.NotFoundException e)
				{
					org.keycloak.representations.idm.RoleRepresentation role =
						new org.keycloak.representations.idm.RoleRepresentation();
					role.setName(roleName);
					role.setDescription("Role for " + roleName + " operations");

					keycloak.realm(realmName).roles().create(role);
					created++;
					log.info("  [OK] Role '{}' created", roleName);
				}
			}
			catch (Exception ex)
			{
				log.error("  [FAIL] Error creating role '{}': {}", roleName, ex.getMessage());
			}
		}

		log.info("[OK] Role creation completed: {} created, {} already existed", created, existing);
	}

	protected void createGroupsClaimMapper(Keycloak keycloak)
	{
		log.info("Creating 'groups' claim mapper for client '{}'...", clientId);

		try
		{
			String clientUuid = keycloak.realm(realmName).clients()
					.findByClientId(clientId).get(0).getId();

			java.util.List<org.keycloak.representations.idm.ProtocolMapperRepresentation> existingMappers =
					keycloak.realm(realmName).clients().get(clientUuid)
							.getProtocolMappers().getMappers();

			for (org.keycloak.representations.idm.ProtocolMapperRepresentation existingMapper : existingMappers)
			{
				if ("groups-claim-mapper".equals(existingMapper.getName()))
				{
					log.info("  [OK] 'groups' claim mapper already exists");
					return;
				}
			}

			org.keycloak.representations.idm.ProtocolMapperRepresentation mapper =
					new org.keycloak.representations.idm.ProtocolMapperRepresentation();

			mapper.setName("groups-claim-mapper");
			mapper.setProtocol("openid-connect");
			mapper.setProtocolMapper("oidc-usermodel-realm-role-mapper");

			java.util.Map<String, String> config = new java.util.HashMap<>();
			config.put("claim.name", "groups");
			config.put("jsonType.label", "String");
			config.put("multivalued", "true");
			config.put("id.token.claim", "true");
			config.put("access.token.claim", "true");
			config.put("userinfo.token.claim", "true");

			mapper.setConfig(config);

			keycloak.realm(realmName).clients().get(clientUuid)
					.getProtocolMappers().createMapper(mapper);

			log.info("  [OK] 'groups' claim mapper created successfully");
		}
		catch (Exception ex)
		{
			log.error("ERROR: Could not create groups claim mapper: {}", ex.getMessage(), ex);
			throw new RuntimeException("Failed to create groups claim mapper", ex);
		}
	}

	protected void createUpnClaimMapper(Keycloak keycloak)
	{
		log.info("Creating 'upn' claim mapper for client '{}'...", clientId);

		try
		{
			String clientUuid = keycloak.realm(realmName).clients()
					.findByClientId(clientId).get(0).getId();

			java.util.List<org.keycloak.representations.idm.ProtocolMapperRepresentation> existingMappers =
					keycloak.realm(realmName).clients().get(clientUuid)
							.getProtocolMappers().getMappers();

			for (org.keycloak.representations.idm.ProtocolMapperRepresentation existingMapper : existingMappers)
			{
				if ("upn-claim-mapper".equals(existingMapper.getName()))
				{
					log.info("  [OK] 'upn' claim mapper already exists");
					return;
				}
			}

			org.keycloak.representations.idm.ProtocolMapperRepresentation mapper =
					new org.keycloak.representations.idm.ProtocolMapperRepresentation();
			mapper.setName("upn-claim-mapper");
			mapper.setProtocol("openid-connect");
			mapper.setProtocolMapper("oidc-usermodel-property-mapper");

			java.util.Map<String, String> config = new java.util.HashMap<>();
			config.put("user.attribute", "username");
			config.put("claim.name", "upn");
			config.put("jsonType.label", "String");
			config.put("id.token.claim", "true");
			config.put("access.token.claim", "true");
			config.put("userinfo.token.claim", "true");
			mapper.setConfig(config);

			keycloak.realm(realmName).clients().get(clientUuid)
					.getProtocolMappers().createMapper(mapper);

			log.info("  [OK] 'upn' claim mapper created successfully");
		}
		catch (Exception ex)
		{
			log.error("ERROR: Could not create upn claim mapper: {}", ex.getMessage(), ex);
			throw new RuntimeException("Failed to create upn claim mapper", ex);
		}
	}

	protected void createTestUser(Keycloak keycloak) throws KeycloakAdminException
	{
		log.info("Checking test user '{}'...", testUsername);

		try (KeycloakUserManager userManager = KeycloakUserManager.builder()
				.serverUrl(KEYCLOAK_URL)
				.realm(realmName)
				.adminUsername(ADMIN_USER)
				.adminPassword(ADMIN_PASSWORD)
				.build())
		{
			UserRepresentation existingUser = userManager.findUserByUsername(testUsername);

			if (existingUser != null)
			{
				String userId = existingUser.getId();
				log.info("[OK] User '{}' already exists (ID: {})", testUsername, userId);

				log.info("Updating user configuration...");

				org.keycloak.representations.idm.CredentialRepresentation credential =
					new org.keycloak.representations.idm.CredentialRepresentation();
				credential.setType(org.keycloak.representations.idm.CredentialRepresentation.PASSWORD);
				credential.setValue(testPassword);
				credential.setTemporary(false);
				keycloak.realm(realmName).users().get(userId).resetPassword(credential);
				log.info("[OK] Password set for user '{}'", testUsername);

				try
				{
					UserRepresentation user = keycloak.realm(realmName).users().get(userId).toRepresentation();
					user.setRequiredActions(new java.util.ArrayList<>());
					user.setEmailVerified(true);
					user.setEnabled(true);
					user.setFirstName("Test");
					user.setLastName("User");
					keycloak.realm(realmName).users().get(userId).update(user);
					log.info("[OK] User '{}' updated (Required Actions deleted)", testUsername);
				}
				catch (Exception ex)
				{
					log.warn("Warning updating user: {}", ex.getMessage());
				}

				assignRolesToUser(keycloak, userId, testUsername);
			}
			else
			{
				log.info("Creating test user '{}'...", testUsername);

				String userId = userManager.createUser(
						testUsername,
						testUsername + "@example.com",
						testPassword
				);

				log.info("[OK] User '{}' created (ID: {})", testUsername, userId);

				org.keycloak.representations.idm.CredentialRepresentation credential =
					new org.keycloak.representations.idm.CredentialRepresentation();
				credential.setType(org.keycloak.representations.idm.CredentialRepresentation.PASSWORD);
				credential.setValue(testPassword);
				credential.setTemporary(false);
				keycloak.realm(realmName).users().get(userId).resetPassword(credential);
				log.info("[OK] Password set for user '{}'", testUsername);

				try
				{
					UserRepresentation user = keycloak.realm(realmName).users().get(userId).toRepresentation();
					user.setRequiredActions(new java.util.ArrayList<>());
					user.setEmailVerified(true);
					user.setEnabled(true);
					user.setFirstName("Test");
					user.setLastName("User");
					keycloak.realm(realmName).users().get(userId).update(user);
					log.info("[OK] Required actions cleared for user '{}'", testUsername);
				}
				catch (Exception ex)
				{
					log.warn("Warning while clearing required actions: {}", ex.getMessage());
				}

				assignRolesToUser(keycloak, userId, testUsername);
			}
		}
	}

	protected void assignRolesToUser(Keycloak keycloak, String userId, String username)
	{
		log.info("Assigning roles to user...");

		java.util.List<String> rolesToAssign = new java.util.ArrayList<>(java.util.Arrays.asList(requiredRoles));

		int assigned = 0;

		for (String roleName : rolesToAssign)
		{
			try
			{
				org.keycloak.representations.idm.RoleRepresentation role =
					keycloak.realm(realmName).roles().get(roleName).toRepresentation();

				keycloak.realm(realmName).users().get(userId).roles().realmLevel()
					.add(Arrays.asList(role));

				assigned++;
				log.info("  [OK] Role '{}' assigned", roleName);
			}
			catch (Exception ex)
			{
				log.warn("  [WARN] Could not assign role '{}': {}", roleName, ex.getMessage());
			}
		}

		log.info("[OK] Role assignment completed: {} roles assigned", assigned);
	}
}
