package de.ruu.lib.keycloak.admin;

import java.util.Arrays;
import java.util.List;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;

import jakarta.ws.rs.core.Response;
import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for managing Keycloak clients programmatically.
 * 
 * <p>This class provides convenient wrapper methods around the Keycloak Admin Client
 * for common client management tasks such as creating clients, configuring OAuth2 flows,
 * and managing client roles.</p>
 * 
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * // Create manager instance
 * KeycloakClientManager manager = KeycloakClientManager.builder()
 *     .serverUrl("http://localhost:8080")
 *     .realm("pragma-realm")
 *     .adminUsername("admin")
 *     .adminPassword("admin")
 *     .build();
 * 
 * // Create OAuth2 client
 * String clientId = manager.createPublicClient(
 *     "my-frontend",
 *     Arrays.asList("http://localhost:3000/*"),
 *     Arrays.asList("http://localhost:3000")
 * );
 * 
 * // Close connection
 * manager.close();
 * }</pre>
 * 
 * <h2>Client Types:</h2>
 * <ul>
 *   <li><strong>Public Client:</strong> For browser-based apps (SPAs, mobile apps)</li>
 *   <li><strong>Confidential Client:</strong> For server-side apps with secret</li>
 *   <li><strong>Bearer-only Client:</strong> For REST APIs that only validate tokens</li>
 * </ul>
 * 
 * <h2>Thread Safety:</h2>
 * <p>This class is NOT thread-safe. Each thread should create its own instance.</p>
 * 
 * @author r-uu
 * @since 2025-12-27
 */
public class KeycloakClientManager implements AutoCloseable
{
	private static final Logger log = LoggerFactory.getLogger(KeycloakClientManager.class);

	private final Keycloak keycloak;
	private final String realmName;

	/**
	 * Creates a new KeycloakClientManager with the specified configuration.
	 * 
	 * <p><strong>Important:</strong> Remember to call {@link #close()} when done
	 * to release HTTP connection resources.</p>
	 * 
	 * @param serverUrl Keycloak server base URL (e.g., "http://localhost:8080")
	 * @param realm Target realm name (e.g., "pragma-realm")
	 * @param adminUsername Admin username (usually "admin")
	 * @param adminPassword Admin password
	 */
	private KeycloakClientManager(
			@NonNull String serverUrl,
			@NonNull String realm,
			@NonNull String adminUsername,
			@NonNull String adminPassword)
	{
		Objects.requireNonNull(serverUrl,      "serverUrl");
		Objects.requireNonNull(realm,          "realm");
		Objects.requireNonNull(adminUsername,  "adminUsername");
		Objects.requireNonNull(adminPassword,  "adminPassword");
		this.realmName = realm;
		this.keycloak = KeycloakBuilder.builder()
				.serverUrl(serverUrl)
				.realm("master")
				.username(adminUsername)
				.password(adminPassword)
				.clientId("admin-cli")
				.build();

		log.debug("Initialized KeycloakClientManager for realm: {}", realm);
	}

	/**
	 * Builder for creating KeycloakClientManager instances.
	 * 
	 * @return new Builder instance
	 */
	public static Builder builder()
	{
		return new Builder();
	}

	/**
	 * Creates a public OAuth2 client (for browser-based applications).
	 * 
	 * <p>Public clients are used for:</p>
	 * <ul>
	 *   <li>Single Page Applications (SPAs)</li>
	 *   <li>Mobile applications</li>
	 *   <li>Desktop applications</li>
	 * </ul>
	 * 
	 * <p>Public clients cannot keep secrets secure, so they use PKCE
	 * (Proof Key for Code Exchange) for security.</p>
	 * 
	 * @param clientId Client ID (must be unique in realm)
	 * @param redirectUris Valid redirect URIs (e.g., ["http://localhost:3000/*"])
	 * @param webOrigins Allowed CORS origins (e.g., ["http://localhost:3000"])
	 * @return Internal UUID of the created client
	 * @throws KeycloakAdminException if client creation fails
	 */
	public String createPublicClient(
			@NonNull String clientId,
			@NonNull List<String> redirectUris,
			@NonNull List<String> webOrigins) throws KeycloakAdminException
	{
		log.debug("Creating public client: {} in realm: {}", clientId, realmName);

		ClientRepresentation client = new ClientRepresentation();
		client.setClientId(clientId);
		client.setEnabled(true);
		client.setPublicClient(true);
		client.setDirectAccessGrantsEnabled(true); // Resource Owner Password Credentials
		client.setStandardFlowEnabled(true);        // Authorization Code Flow
		client.setImplicitFlowEnabled(false);       // Deprecated, don't use
		client.setRedirectUris(redirectUris);
		client.setWebOrigins(webOrigins);
		client.setProtocol("openid-connect");

		return createClient(client);
	}

	/**
	 * Creates a confidential OAuth2 client (for server-side applications).
	 * 
	 * <p>Confidential clients are used for:</p>
	 * <ul>
	 *   <li>Backend services</li>
	 *   <li>Server-side web applications</li>
	 *   <li>Microservices with service accounts</li>
	 * </ul>
	 * 
	 * <p>After creation, retrieve the client secret via the Keycloak Admin Console
	 * or use {@link #clientSecret(String)}.</p>
	 * 
	 * @param clientId Client ID (must be unique in realm)
	 * @param redirectUris Valid redirect URIs
	 * @param serviceAccountEnabled Enable service account for client credentials flow
	 * @return Internal UUID of the created client
	 * @throws KeycloakAdminException if client creation fails
	 */
	public String createConfidentialClient(
			@NonNull String clientId,
			@NonNull List<String> redirectUris,
			boolean serviceAccountEnabled) throws KeycloakAdminException
	{
		log.debug("Creating confidential client: {} in realm: {}", clientId, realmName);

		ClientRepresentation client = new ClientRepresentation();
		client.setClientId(clientId);
		client.setEnabled(true);
		client.setPublicClient(false);
		client.setServiceAccountsEnabled(serviceAccountEnabled);
		client.setStandardFlowEnabled(true);
		client.setDirectAccessGrantsEnabled(false);
		client.setRedirectUris(redirectUris);
		client.setProtocol("openid-connect");

		return createClient(client);
	}

	/**
	 * Creates a bearer-only client (for REST APIs).
	 * 
	 * <p>Bearer-only clients are used for:</p>
	 * <ul>
	 *   <li>REST APIs that only validate access tokens</li>
	 *   <li>Backend services that don't initiate login</li>
	 * </ul>
	 * 
	 * @param clientId Client ID (must be unique in realm)
	 * @return Internal UUID of the created client
	 * @throws KeycloakAdminException if client creation fails
	 */
	public String createBearerOnlyClient(@NonNull String clientId) throws KeycloakAdminException
	{
		log.debug("Creating bearer-only client: {} in realm: {}", clientId, realmName);

		ClientRepresentation client = new ClientRepresentation();
		client.setClientId(clientId);
		client.setEnabled(true);
		client.setBearerOnly(true);
		client.setProtocol("openid-connect");

		return createClient(client);
	}

	/**
	 * Creates a client with custom configuration.
	 * 
	 * @param client Client representation with desired settings
	 * @return Internal UUID of the created client
	 * @throws KeycloakAdminException if client creation fails
	 */
	private String createClient(ClientRepresentation client) throws KeycloakAdminException
	{
		try (Response response = realm().clients().create(client))
		{
			if (response.getStatus() != 201)
			{
				String error = response.readEntity(String.class);
				log.error("Failed to create client: {} - Status: {} - Error: {}",
						client.getClientId(), response.getStatus(), error);
				throw new KeycloakAdminException(
						"Failed to create client: " + client.getClientId() + " (HTTP " + response.getStatus() + "): " + error);
			}
			String clientUuid = extractIdFromLocation(response.getLocation().toString());
			log.info("Client created successfully: {} (UUID: {})", client.getClientId(), clientUuid);
			return clientUuid;
		}
		catch (KeycloakAdminException e)
		{
			throw e;
		}
		catch (jakarta.ws.rs.WebApplicationException e)
		{
			log.error("Error creating client: {} - HTTP {}", client.getClientId(), e.getResponse().getStatus(), e);
			throw new KeycloakAdminException(
					"Error creating client: " + client.getClientId() + " (HTTP " + e.getResponse().getStatus() + ")", e);
		}
		catch (RuntimeException e)
		{
			log.error("Error creating client: {}", client.getClientId(), e);
			throw new KeycloakAdminException("Error creating client: " + client.getClientId(), e);
		}
	}

	/**
	 * Gets the client secret for a confidential client.
	 * 
	 * @param clientUuid Internal UUID of the client
	 * @return Client secret
	 * @throws KeycloakAdminException if retrieval fails
	 */
	public String clientSecret(@NonNull String clientUuid) throws KeycloakAdminException
	{
		log.debug("Getting client secret for UUID: {}", clientUuid);

		try
		{
			ClientResource clientResource = realm().clients().get(clientUuid);
			String secret = clientResource.getSecret().getValue();
			log.debug("Client secret retrieved for UUID: {}", clientUuid);
			return secret;
		}
		catch (jakarta.ws.rs.WebApplicationException e)
		{
			log.error("Error getting client secret for UUID: {} - HTTP {}", clientUuid, e.getResponse().getStatus(), e);
			throw new KeycloakAdminException(
					"Error getting client secret for UUID: " + clientUuid + " (HTTP " + e.getResponse().getStatus() + ")", e);
		}
		catch (RuntimeException e)
		{
			log.error("Error getting client secret for UUID: {}", clientUuid, e);
			throw new KeycloakAdminException("Error getting client secret for UUID: " + clientUuid, e);
		}
	}

	/**
	 * Deletes a client from Keycloak.
	 * 
	 * @param clientUuid Internal UUID of the client
	 * @throws KeycloakAdminException if deletion fails
	 */
	public void deleteClient(@NonNull String clientUuid) throws KeycloakAdminException
	{
		log.debug("Deleting client UUID: {}", clientUuid);

		try
		{
			realm().clients().get(clientUuid).remove();
			log.info("Client deleted successfully: {}", clientUuid);
		}
		catch (jakarta.ws.rs.WebApplicationException e)
		{
			log.error("Error deleting client UUID: {} - HTTP {}", clientUuid, e.getResponse().getStatus(), e);
			throw new KeycloakAdminException(
					"Error deleting client: " + clientUuid + " (HTTP " + e.getResponse().getStatus() + ")", e);
		}
		catch (RuntimeException e)
		{
			log.error("Error deleting client UUID: {}", clientUuid, e);
			throw new KeycloakAdminException("Error deleting client: " + clientUuid, e);
		}
	}

	/**
	 * Finds a client by client ID.
	 * 
	 * @param clientId Client ID to search for
	 * @return ClientRepresentation if found, null otherwise
	 */
	public ClientRepresentation findClientByClientId(@NonNull String clientId)
	{
		log.debug("Searching for client: {}", clientId);

		List<ClientRepresentation> clients = realm().clients().findByClientId(clientId);
		if (clients.isEmpty())
		{
			log.debug("Client not found: {}", clientId);
			return null;
		}

		log.debug("Client found: {}", clientId);
		return clients.get(0);
	}

	/**
	 * Enables or disables Direct Access Grants for a client.
	 * 
	 * <p>Direct Access Grants enable the Resource Owner Password Credentials flow,
	 * which allows applications to exchange username/password directly for tokens.
	 * This is useful for desktop/mobile apps but should be avoided for web apps.</p>
	 * 
	 * @param clientUuid Internal UUID of the client
	 * @param enabled true to enable, false to disable
	 * @throws KeycloakAdminException if update fails
	 */
	public void setDirectAccessGrantsEnabled(@NonNull String clientUuid, boolean enabled) 
			throws KeycloakAdminException
	{
		log.debug("Setting direct access grants for client {}: {}", clientUuid, enabled);

		try
		{
			ClientResource clientResource = realm().clients().get(clientUuid);
			ClientRepresentation client = clientResource.toRepresentation();
			client.setDirectAccessGrantsEnabled(enabled);
			clientResource.update(client);
			log.info("Direct access grants {} for client: {}", 
					enabled ? "enabled" : "disabled", clientUuid);
		}
		catch (jakarta.ws.rs.WebApplicationException e)
		{
			log.error("Error updating direct access grants for client: {} - HTTP {}", clientUuid, e.getResponse().getStatus(), e);
			throw new KeycloakAdminException(
					"Error updating client: " + clientUuid + " (HTTP " + e.getResponse().getStatus() + ")", e);
		}
		catch (RuntimeException e)
		{
			log.error("Error updating direct access grants for client: {}", clientUuid, e);
			throw new KeycloakAdminException("Error updating client: " + clientUuid, e);
		}
	}

	/**
	 * Gets the realm resource for the configured realm.
	 * 
	 * @return RealmResource instance
	 */
	private RealmResource realm()
	{
		return keycloak.realm(realmName);
	}

	/**
	 * Extracts UUID from Location header URL.
	 * 
	 * <p>Keycloak returns the UUID in the Location header after creation:
	 * {@code http://localhost:8080/admin/realms/my-realm/clients/<uuid>}</p>
	 * 
	 * @param location Location header URL
	 * @return UUID
	 */
	private String extractIdFromLocation(String location)
	{
		return location.substring(location.lastIndexOf('/') + 1);
	}

	/**
	 * Closes the Keycloak admin client connection.
	 * 
	 * <p>Always call this method when done to release HTTP connection resources.</p>
	 */
	@Override
	public void close()
	{
		if (keycloak != null)
		{
			keycloak.close();
			log.debug("KeycloakClientManager closed");
		}
	}

	/**
	 * Builder for KeycloakClientManager instances.
	 */
	public static class Builder
	{
		private String serverUrl;
		private String realm;
		private String adminUsername;
		private String adminPassword;

		public Builder serverUrl(String serverUrl)
		{
			this.serverUrl = serverUrl;
			return this;
		}

		public Builder realm(String realm)
		{
			this.realm = realm;
			return this;
		}

		public Builder adminUsername(String adminUsername)
		{
			this.adminUsername = adminUsername;
			return this;
		}

		public Builder adminPassword(String adminPassword)
		{
			this.adminPassword = adminPassword;
			return this;
		}

		public KeycloakClientManager build()
		{
			return new KeycloakClientManager(serverUrl, realm, adminUsername, adminPassword);
		}
	}
}
