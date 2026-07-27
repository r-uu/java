package de.ruu.lib.keycloak.admin.setup;

import de.ruu.lib.keycloak.admin.KeycloakAdminException;
import de.ruu.lib.keycloak.admin.KeycloakClientManager;
import de.ruu.lib.keycloak.admin.KeycloakRealmManager;
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
 * Keycloak Realm Setup Utility
 *
 * <p>Erstellt automatisch den benötigten Keycloak Realm mit Client und Testuser
 * für die PRAGMA Anwendung.</p>
 *
 * <h2>Erstellt folgende Ressourcen:</h2>
 * <ul>
 *   <li>Realm: pragma-realm</li>
 *   <li>Client: pragma-frontend (Public Client, Direct Access Grants)</li>
 *   <li>User: r_uu / r_uu_password</li>
 * </ul>
 *
 * <h2>Voraussetzungen:</h2>
 * <ul>
 *   <li>Keycloak Server läuft auf http://localhost:8080</li>
 *   <li>Admin Credentials: admin / admin</li>
 * </ul>
 *
 * @author r-uu
 * @since 2026-01-19
 */
public class KeycloakRealmSetup
{
	private static final Logger log = LogManager.getLogger(KeycloakRealmSetup.class);

	private static final String KEYCLOAK_URL = System.getProperty("keycloak.url", "http://localhost:8080");
	private static final String ADMIN_USER = System.getProperty("keycloak.admin.user",
			System.getenv().getOrDefault("keycloak_admin_user", "admin"));
	private static final String ADMIN_PASSWORD = System.getProperty("keycloak.admin.password",
			System.getenv().getOrDefault("keycloak_admin_password", "admin"));
	private static final String REALM_NAME = System.getProperty("keycloak.realm", "pragma-realm");
	private static final String CLIENT_ID = System.getProperty("keycloak.client.id", "pragma-frontend");
	private static final String TEST_USER = System.getProperty("app.test.user",
			System.getenv().getOrDefault("app_test_user_username", "test"));
	private static final String TEST_PASSWORD = System.getProperty("app.test.password",
			System.getenv().getOrDefault("app_test_user_password", "test"));

	public static void main(String[] args)
	{
		log.info("=== Keycloak Realm Setup ===");
		log.info("Keycloak URL: {}", KEYCLOAK_URL);
		log.info("Realm: {}", REALM_NAME);
		log.info("Client: {}", CLIENT_ID);
		log.info("");

		try (Keycloak keycloak = createKeycloakClient())
		{
			// 1. Create Realm
			createRealm(keycloak);

			// 2. Create Client
			createClient(keycloak);

			// 3. Create Roles
			createRoles(keycloak);

			// 4. Create Groups Claim Mapper (for Liberty compatibility)
			createGroupsClaimMapper(keycloak);

			// 5. Create Test User (with roles assigned)
			createTestUser(keycloak);

			log.info("");
			log.info("=== Setup completed ===");
			log.info("[OK] Realm: {}", REALM_NAME);
			log.info("[OK] Client: {} (Public client, Direct Access Grants enabled)", CLIENT_ID);
			log.info("[OK] Test user: {} / {} (with all roles)", TEST_USER, TEST_PASSWORD);
			log.info("");
			log.info("Test login command:");
			log.info("curl -X POST '{}/realms/{}/protocol/openid-connect/token' \\", KEYCLOAK_URL, REALM_NAME);
			log.info("  -H 'Content-Type: application/x-www-form-urlencoded' \\");
			log.info("  -d 'username={}' \\", TEST_USER);
			log.info("  -d 'password={}' \\", TEST_PASSWORD);
			log.info("  -d 'grant_type=password' \\");
			log.info("  -d 'client_id={}'", CLIENT_ID);
		}
		catch (Exception e)
		{
			log.error("[FAIL] Setup failed: {}", e.getMessage(), e);
			System.exit(1);
		}
	}

	private static Keycloak createKeycloakClient()
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

	private static void createRealm(Keycloak keycloak)
	{
		log.info("Checking realm '{}'...", REALM_NAME);

		try
		{
			// Check whether the realm already exists
			keycloak.realm(REALM_NAME).toRepresentation();
			log.info("[OK] Realm '{}' already exists", REALM_NAME);
		}
		catch (Exception e)
		{
			log.info("Creating realm '{}'...", REALM_NAME);

			RealmRepresentation realm = new RealmRepresentation();
			realm.setRealm(REALM_NAME);
			realm.setEnabled(true);
			realm.setDisplayName("PRAGMA Default Realm");
			realm.setRegistrationAllowed(false);
			realm.setResetPasswordAllowed(true);

			// ===== Token Lifespan Configuration (prevents immediate session expiry) =====
			// Access Token: 30 minutes (1800 seconds) - prevents frequent re-authentication
			// Refresh Token: 8 hours (28800 seconds) - allows long-running sessions
			// SSO Session Idle: 30 minutes - must match or exceed access token
			// SSO Session Max: 10 hours - maximum session lifetime
			realm.setAccessTokenLifespan(1800);          // 30 minutes
			realm.setSsoSessionIdleTimeout(1800);        // 30 minutes
			realm.setSsoSessionMaxLifespan(36000);       // 10 hours
			realm.setOfflineSessionIdleTimeout(2592000); // 30 days
			realm.setAccessTokenLifespanForImplicitFlow(900); // 15 minutes
			// Note: Refresh token lifespan is controlled by SSO Session Idle Timeout

			log.info("Configured token lifespans:");
			log.info("  Access token: {} minutes", 1800 / 60);
			log.info("  SSO session idle: {} minutes", 1800 / 60);
			log.info("  SSO session max: {} hours", 36000 / 3600);

			try
			{
				keycloak.realms().create(realm);
				log.info("[OK] Realm '{}' created successfully", REALM_NAME);
			}
			catch (Exception ex)
			{
				log.error("Error creating realm '{}'", REALM_NAME, ex);
				throw new RuntimeException("Failed to create realm: " + REALM_NAME, ex);
			}
		}
	}

	private static void createClient(Keycloak keycloak) throws KeycloakAdminException
	{
		log.info("Checking client '{}'...", CLIENT_ID);

		try (KeycloakClientManager clientManager = KeycloakClientManager.builder()
				.serverUrl(KEYCLOAK_URL)
				.realm(REALM_NAME)
				.adminUsername(ADMIN_USER)
				.adminPassword(ADMIN_PASSWORD)
				.build())
		{
			// Check if client exists
			org.keycloak.representations.idm.ClientRepresentation existingClient =
				clientManager.findClientByClientId(CLIENT_ID);

			if (existingClient != null)
			{
				String clientUuid = existingClient.getId();
				log.info("[OK] Client '{}' already exists (UUID: {})", CLIENT_ID, clientUuid);

				// Ensure Direct Access Grants is enabled
				try
				{
					org.keycloak.representations.idm.ClientRepresentation client =
						keycloak.realm(REALM_NAME).clients().get(clientUuid).toRepresentation();

					if (!Boolean.TRUE.equals(client.isDirectAccessGrantsEnabled()))
					{
						log.info("Enabling Direct Access Grants for client '{}'...", CLIENT_ID);
						client.setDirectAccessGrantsEnabled(true);
						client.setPublicClient(true);
						keycloak.realm(REALM_NAME).clients().get(clientUuid).update(client);
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

				// Check and create Audience Mapper if needed
				ensureAudienceMapper(keycloak, clientUuid);
			}
			else
			{
				// Client does not exist, create it
				log.info("Creating client '{}'...", CLIENT_ID);

				String clientUuid = clientManager.createPublicClient(
						CLIENT_ID,
						Arrays.asList("*"),  // redirectUris
						Arrays.asList("*")   // webOrigins
				);

				log.info("[OK] Client '{}' created (UUID: {})", CLIENT_ID, clientUuid);

				// Direct Access Grants explizit aktivieren
				try
				{
					org.keycloak.representations.idm.ClientRepresentation client =
						keycloak.realm(REALM_NAME).clients().get(clientUuid).toRepresentation();
					client.setDirectAccessGrantsEnabled(true);
					client.setPublicClient(true);
					keycloak.realm(REALM_NAME).clients().get(clientUuid).update(client);
					log.info("[OK] Direct Access Grants enabled for client '{}'", CLIENT_ID);
				}
				catch (Exception ex)
				{
					log.error("ERROR: Could not enable Direct Access Grants: {}", ex.getMessage());
					throw new KeycloakAdminException("Could not enable Direct Access Grants", ex);
				}

				// Audience Mapper erstellen
				createAudienceMapper(keycloak, clientUuid);
			}
		}
	}

	/**
	 * Stellt sicher, dass der Audience Mapper für den Client existiert.
	 * Erstellt ihn, falls er noch nicht vorhanden ist.
	 */
	private static void ensureAudienceMapper(Keycloak keycloak, String clientUuid)
	{
		log.info("Checking audience mapper...");

		try
		{
			// Hole existierende Mapper
			List<ProtocolMapperRepresentation> mappers = keycloak.realm(REALM_NAME)
				.clients()
				.get(clientUuid)
				.getProtocolMappers()
				.getMappers();

			// Check whether the audience mapper exists
			boolean hasCorrectAudienceMapper = mappers.stream()
				.filter(mapper -> "oidc-audience-mapper".equals(mapper.getProtocolMapper()))
				.anyMatch(mapper -> {
					Map<String, String> config = mapper.getConfig();
					String customAudience = config.get("included.custom.audience");
					return "pragma-backend".equals(customAudience);
				});

			if (hasCorrectAudienceMapper)
			{
				log.info("[OK] Audience mapper already exists");
				return;
			}

			// Mapper does not exist or has the wrong audience → create it
			log.info("Creating audience mapper...");
			createAudienceMapper(keycloak, clientUuid);
		}
		catch (Exception ex)
		{
			log.error("Error checking or creating audience mapper: {}", ex.getMessage());
		}
	}

	/**
	 * Erstellt den Audience Mapper für den Client.
	 * Der Mapper fügt die Audience "pragma-backend" zum Access Token hinzu.
	 */
	private static void createAudienceMapper(Keycloak keycloak, String clientUuid)
	{
		log.info("Creating audience mapper for client...");

		try
		{
			ProtocolMapperRepresentation audienceMapper = new ProtocolMapperRepresentation();
			audienceMapper.setName("audience-mapper");
			audienceMapper.setProtocol("openid-connect");
			audienceMapper.setProtocolMapper("oidc-audience-mapper");

			Map<String, String> config = new HashMap<>();
			config.put("included.custom.audience", "pragma-backend");
			config.put("access.token.claim", "true");
			config.put("id.token.claim", "false");

			audienceMapper.setConfig(config);

			keycloak.realm(REALM_NAME)
				.clients()
				.get(clientUuid)
				.getProtocolMappers()
				.createMapper(audienceMapper);

			log.info("[OK] Audience mapper created");
			log.info("   Audience: pragma-backend");
			log.info("   Added to: access token");
		}
		catch (Exception ex)
		{
			log.error("ERROR: Could not create audience mapper: {}", ex.getMessage());
			log.error("   JWT tokens may have the wrong audience!");
			log.error("   Please configure it manually or run KeycloakAudienceMapper");
		}
	}

	/**
	 * Erstellt alle erforderlichen Realm-Rollen für die Anwendung.
	 */
	private static void createRoles(Keycloak keycloak)
	{
		log.info("Creating required realm roles...");

		// All required roles (based on @RolesAllowed annotations in backend)
		String[] requiredRoles = {
			"taskgroup-read",
			"taskgroup-create",
			"taskgroup-update",
			"taskgroup-delete",
			"task-read",
			"task-create",
			"task-update",
			"task-delete"
		};

		int created = 0;
		int existing = 0;

		for (String roleName : requiredRoles)
		{
			try
			{
				// Check if role already exists
				try
				{
					keycloak.realm(REALM_NAME).roles().get(roleName).toRepresentation();
					existing++;
					log.info("  [OK] Role '{}' already exists", roleName);
				}
				catch (jakarta.ws.rs.NotFoundException e)
				{
					// Role does not exist → create
					org.keycloak.representations.idm.RoleRepresentation role =
						new org.keycloak.representations.idm.RoleRepresentation();
					role.setName(roleName);
					role.setDescription("Role for " + roleName + " operations");

					keycloak.realm(REALM_NAME).roles().create(role);
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

	/**
	 * Creates a "groups" claim mapper that adds realm roles as top-level "groups" claim.
	 * Liberty Server expects roles in a top-level claim (not nested in realm_access).
	 */
	private static void createGroupsClaimMapper(Keycloak keycloak)
	{
		log.info("Creating 'groups' claim mapper for client '{}'...", CLIENT_ID);

		try
		{
			// Get client representation
			log.debug("  Looking up client UUID for '{}'...", CLIENT_ID);
			String clientUuid = keycloak.realm(REALM_NAME).clients()
					.findByClientId(CLIENT_ID).get(0).getId();
			log.debug("  Client UUID: {}", clientUuid);

			// Check if mapper already exists
			log.debug("  Checking existing mappers...");
			java.util.List<org.keycloak.representations.idm.ProtocolMapperRepresentation> existingMappers =
					keycloak.realm(REALM_NAME).clients().get(clientUuid)
							.getProtocolMappers().getMappers();

			for (org.keycloak.representations.idm.ProtocolMapperRepresentation existingMapper : existingMappers)
			{
				if ("groups-claim-mapper".equals(existingMapper.getName()))
				{
					log.info("  [OK] 'groups' claim mapper already exists");
					return;
				}
			}

			// Create mapper representation
			log.debug("  Creating mapper representation...");
			org.keycloak.representations.idm.ProtocolMapperRepresentation mapper =
					new org.keycloak.representations.idm.ProtocolMapperRepresentation();

			mapper.setName("groups-claim-mapper");
			mapper.setProtocol("openid-connect");
			mapper.setProtocolMapper("oidc-usermodel-realm-role-mapper");

			// Configuration
			java.util.Map<String, String> config = new java.util.HashMap<>();
			config.put("claim.name", "groups");  // Liberty expects a top-level "groups" claim
			config.put("jsonType.label", "String");
			config.put("multivalued", "true");  // Roles are an array
			config.put("id.token.claim", "true");
			config.put("access.token.claim", "true");
			config.put("userinfo.token.claim", "true");

			mapper.setConfig(config);

			// Add mapper to client
			log.debug("  Adding mapper to client...");
			keycloak.realm(REALM_NAME).clients().get(clientUuid)
					.getProtocolMappers().createMapper(mapper);

			log.info("  [OK] 'groups' claim mapper created successfully");
			log.info("     Roles are now written into the token as a top-level 'groups' claim");
			log.info("     Liberty Server can read roles now!");
		}
		catch (Exception ex)
		{
			log.error("ERROR: Could not create groups claim mapper: {}", ex.getMessage(), ex);
			log.error("   Liberty Server may not be able to read roles!");
			log.error("   Please configure it manually");
			throw new RuntimeException("Failed to create groups claim mapper", ex);
		}
	}

	private static void createTestUser(Keycloak keycloak) throws KeycloakAdminException
	{
		log.info("Checking test user '{}'...", TEST_USER);

		try (KeycloakUserManager userManager = KeycloakUserManager.builder()
				.serverUrl(KEYCLOAK_URL)
				.realm(REALM_NAME)
				.adminUsername(ADMIN_USER)
				.adminPassword(ADMIN_PASSWORD)
				.build())
		{
			UserRepresentation existingUser = userManager.findUserByUsername(TEST_USER);

			if (existingUser != null)
			{
				// User already exists
				String userId = existingUser.getId();
				log.info("[OK] User '{}' already exists (ID: {})", TEST_USER, userId);

				// Update password and delete Required Actions
				log.info("Updating user configuration...");

				// Set password directly via Keycloak API
				org.keycloak.representations.idm.CredentialRepresentation credential =
					new org.keycloak.representations.idm.CredentialRepresentation();
				credential.setType(org.keycloak.representations.idm.CredentialRepresentation.PASSWORD);
				credential.setValue(TEST_PASSWORD);
				credential.setTemporary(false);
				keycloak.realm(REALM_NAME).users().get(userId).resetPassword(credential);
				log.info("[OK] Password set for user '{}'", TEST_USER);

				// Explicitly delete Required Actions via Keycloak API
				try
				{
					UserRepresentation user = keycloak.realm(REALM_NAME).users().get(userId).toRepresentation();
					user.setRequiredActions(new java.util.ArrayList<>());  // Empty list
					user.setEmailVerified(true);
					user.setEnabled(true);
					user.setFirstName("Test");  // firstName is required for Keycloak User Profile
					user.setLastName("User");   // lastName is required for Keycloak User Profile
					keycloak.realm(REALM_NAME).users().get(userId).update(user);
					log.info("[OK] User '{}' updated (Required Actions deleted)", TEST_USER);
				}
				catch (Exception ex)
				{
					log.warn("Warning updating user: {}", ex.getMessage());
				}

				// Assign roles (for existing user)
				assignRolesToUser(keycloak, userId);
			}
			else
			{
				// User does not exist, create it
				log.info("Creating test user '{}'...", TEST_USER);

				String userId = userManager.createUser(
						TEST_USER,
						TEST_USER + "@example.com",
						TEST_PASSWORD
						// Keine Rollen
				);

				log.info("[OK] User '{}' created (ID: {})", TEST_USER, userId);

				// Set the password again explicitly for safety
				org.keycloak.representations.idm.CredentialRepresentation credential =
					new org.keycloak.representations.idm.CredentialRepresentation();
				credential.setType(org.keycloak.representations.idm.CredentialRepresentation.PASSWORD);
				credential.setValue(TEST_PASSWORD);
				credential.setTemporary(false);
				keycloak.realm(REALM_NAME).users().get(userId).resetPassword(credential);
				log.info("[OK] Password set for user '{}'", TEST_USER);

				// Explicitly clear required actions
				try
				{
					UserRepresentation user = keycloak.realm(REALM_NAME).users().get(userId).toRepresentation();
					user.setRequiredActions(new java.util.ArrayList<>());  // Empty list
					user.setEmailVerified(true);
					user.setEnabled(true);
					user.setFirstName("Test");  // firstName is required for the Keycloak user profile
					user.setLastName("User");   // lastName is required for the Keycloak user profile
					keycloak.realm(REALM_NAME).users().get(userId).update(user);
					log.info("[OK] Required actions cleared for user '{}'", TEST_USER);
				}
				catch (Exception ex)
				{
					log.warn("Warning while clearing required actions: {}", ex.getMessage());
				}

				// Assign roles for the new user
				assignRolesToUser(keycloak, userId);
			}
		}
	}

	/**
	 * Weist dem User alle erforderlichen Rollen zu.
	 */
	private static void assignRolesToUser(Keycloak keycloak, String userId)
	{
		log.info("Assigning roles to user...");

		String[] requiredRoles = {
			"taskgroup-read",
			"taskgroup-create",
			"taskgroup-update",
			"taskgroup-delete",
			"task-read",
			"task-create",
			"task-update",
			"task-delete"
		};

		int assigned = 0;

		for (String roleName : requiredRoles)
		{
			try
			{
				// Get role representation
				org.keycloak.representations.idm.RoleRepresentation role =
					keycloak.realm(REALM_NAME).roles().get(roleName).toRepresentation();

				// Assign the role to the user
				keycloak.realm(REALM_NAME).users().get(userId).roles().realmLevel()
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