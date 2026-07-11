package de.ruu.lib.keycloak.admin.setup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keycloak Audience Mapper Setup
 *
 * <p>Configures the audience mapper for the pragma-frontend client
 * so that JWT tokens contain the correct audience "pragma-backend".</p>
 *
 * @author r-uu
 * @since 2026-01-21
 */
public class KeycloakAudienceMapper
{
	private static final Logger log = LoggerFactory.getLogger(KeycloakAudienceMapper.class);

	private static final String KEYCLOAK_URL = System.getProperty("keycloak.url", "http://localhost:8080");
	private static final String ADMIN_USER = System.getProperty("keycloak.admin.user", "admin");
	private static final String ADMIN_PASSWORD = System.getProperty("keycloak.admin.password",
			System.getenv().getOrDefault("KEYCLOAK_ADMIN_PASSWORD", "changeme_in_local_env"));
	private static final String REALM_NAME = System.getProperty("keycloak.realm", "pragma-realm");
	private static final String CLIENT_ID = System.getProperty("keycloak.client.id", "pragma-frontend");
	private static final String AUDIENCE = System.getProperty("keycloak.audience", "pragma-backend");

	static void main(String[] args)
	{
		log.info("=== Keycloak Audience Mapper Setup ===");
		log.info("Keycloak URL: {}", KEYCLOAK_URL);
		log.info("Realm: {}", REALM_NAME);
		log.info("Client: {}", CLIENT_ID);
		log.info("Target Audience: {}", AUDIENCE);
		log.info("");

		try (Keycloak keycloak = createKeycloakClient())
		{
			// Get client
			List<ClientRepresentation> clients = keycloak.realm(REALM_NAME)
					.clients()
					.findByClientId(CLIENT_ID);

			if (clients.isEmpty())
			{
				log.error("Client '{}' not found in realm '{}'", CLIENT_ID, REALM_NAME);
				System.exit(1);
			}

			ClientRepresentation client = clients.getFirst();
			String clientUuid = client.getId();
			log.info("Found client '{}' with UUID: {}", CLIENT_ID, clientUuid);

			// Check if audience mapper already exists
			List<ProtocolMapperRepresentation> mappers = keycloak.realm(REALM_NAME)
					.clients()
					.get(clientUuid)
					.getProtocolMappers()
					.getMappers();

			boolean audienceMapperExists = mappers.stream()
					.anyMatch(mapper -> "audience-mapper".equals(mapper.getName())
							|| "oidc-audience-mapper".equals(mapper.getProtocolMapper()));

			if (audienceMapperExists)
			{
				log.info("Audience mapper already exists for client '{}'", CLIENT_ID);

				// Check if it has the correct audience
				for (ProtocolMapperRepresentation mapper : mappers)
				{
					if ("oidc-audience-mapper".equals(mapper.getProtocolMapper()))
					{
						Map<String, String> config = mapper.getConfig();
						String includedClientAudience = config.get("included.client.audience");
						String includedCustomAudience = config.get("included.custom.audience");

						log.info("Existing audience mapper configuration:");
						log.info("  Name: {}", mapper.getName());
						log.info("  Included Client Audience: {}", includedClientAudience);
						log.info("  Included Custom Audience: {}", includedCustomAudience);

						if (AUDIENCE.equals(includedCustomAudience) || AUDIENCE.equals(includedClientAudience))
						{
							log.info("✅ Audience mapper is already correctly configured with audience '{}'", AUDIENCE);
							return;
						}
					}
				}
			}

			// Create audience mapper
			log.info("Creating audience mapper for client '{}'...", CLIENT_ID);

			ProtocolMapperRepresentation audienceMapper = new ProtocolMapperRepresentation();
			audienceMapper.setName("audience-mapper");
			audienceMapper.setProtocol("openid-connect");
			audienceMapper.setProtocolMapper("oidc-audience-mapper");

			Map<String, String> config = new HashMap<>();
			config.put("included.custom.audience", AUDIENCE);
			config.put("access.token.claim", "true");
			config.put("id.token.claim", "false");

			audienceMapper.setConfig(config);

			keycloak.realm(REALM_NAME)
					.clients()
					.get(clientUuid)
					.getProtocolMappers()
					.createMapper(audienceMapper);

			log.info("✅ Audience mapper created successfully");
			log.info("   Mapper Name: audience-mapper");
			log.info("   Audience: {}", AUDIENCE);
			log.info("   Added to: Access Token");
			log.info("");
			log.info("=== Setup abgeschlossen ===");
			log.info("Der Client '{}' generiert jetzt JWT-Tokens mit Audience '{}'", CLIENT_ID, AUDIENCE);
		}
		catch (Exception e)
		{
			log.error("Error during audience mapper setup", e);
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
}
