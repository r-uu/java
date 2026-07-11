package de.ruu.lib.keycloak.admin;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.ClientRepresentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

/**
 * Integration tests for KeycloakClientManager.
 * 
 * <p><strong>Prerequisites:</strong></p>
 * <ul>
 *   <li>Keycloak must be running on localhost:8080</li>
 *   <li>Admin credentials: admin / admin</li>
 *   <li>Realm "pragma-realm" must exist</li>
 * </ul>
 * 
 * <p><strong>Note:</strong> These tests are disabled by default. Remove @Disabled
 * annotation and ensure Keycloak is running to execute them.</p>
 * 
 * @author r-uu
 * @since 2025-12-27
 */
@Disabled("Requires Keycloak running on localhost:8080")
class KeycloakClientManagerIT
{
	private static final Logger log = LoggerFactory.getLogger(KeycloakClientManagerIT.class);

	private KeycloakClientManager manager;
	private String createdClientUuid;

	@BeforeEach
	void setUp()
	{
		manager = KeycloakClientManager.builder()
				.serverUrl("http://localhost:8080")
				.realm("pragma-realm")
				.adminUsername("admin")
				.adminPassword("admin")
				.build();
	}

	@AfterEach
	void tearDown() throws KeycloakAdminException
	{
		// Cleanup: delete created client if exists
		if (createdClientUuid != null)
		{
			try
			{
				manager.deleteClient(createdClientUuid);
				log.info("Cleaned up test client: {}", createdClientUuid);
			}
			catch (Exception e)
			{
				log.warn("Failed to cleanup test client: {}", createdClientUuid, e);
			}
		}

		if (manager != null)
		{
			manager.close();
		}
	}

	@Test
	void testCreatePublicClient() throws KeycloakAdminException
	{
		String clientId = "test-public-client-" + System.currentTimeMillis();

		createdClientUuid = manager.createPublicClient(
				clientId,
				List.of("http://localhost:3000/*"),
				List.of("http://localhost:3000")
		);

		assertThat(createdClientUuid).isNotNull();
		assertThat(createdClientUuid).isNotEmpty();

		// Verify client exists
		ClientRepresentation client = manager.findClientByClientId(clientId);
		assertThat(client).isNotNull();
		assertThat(client.getClientId()).isEqualTo(clientId);
		assertThat(client.isPublicClient()).isTrue();
		assertThat(client.isDirectAccessGrantsEnabled()).isTrue();
	}

	@Test
	void testCreateConfidentialClient() throws KeycloakAdminException
	{
		String clientId = "test-confidential-client-" + System.currentTimeMillis();

		createdClientUuid = manager.createConfidentialClient(
				clientId,
				Arrays.asList("http://localhost:8080/callback"),
				true
		);

		assertThat(createdClientUuid).isNotNull();

		// Verify client exists
		ClientRepresentation client = manager.findClientByClientId(clientId);
		assertThat(client).isNotNull();
		assertThat(client.getClientId()).isEqualTo(clientId);
		assertThat(client.isPublicClient()).isFalse();
		assertThat(client.isServiceAccountsEnabled()).isTrue();

		// Get client secret
		String secret = manager.clientSecret(createdClientUuid);
		assertThat(secret).isNotNull();
		assertThat(secret).isNotEmpty();
	}

	@Test
	void testCreateBearerOnlyClient() throws KeycloakAdminException
	{
		String clientId = "test-bearer-client-" + System.currentTimeMillis();

		createdClientUuid = manager.createBearerOnlyClient(clientId);

		assertThat(createdClientUuid).isNotNull();

		// Verify client exists
		ClientRepresentation client = manager.findClientByClientId(clientId);
		assertThat(client).isNotNull();
		assertThat(client.getClientId()).isEqualTo(clientId);
		assertThat(client.isBearerOnly()).isTrue();
	}

	@Test
	void testSetDirectAccessGrantsEnabled() throws KeycloakAdminException
	{
		String clientId = "test-client-" + System.currentTimeMillis();

		createdClientUuid = manager.createPublicClient(
				clientId,
				List.of("http://localhost:3000/*"),
				List.of("http://localhost:3000")
		);

		// Disable direct access grants
		manager.setDirectAccessGrantsEnabled(createdClientUuid, false);

		// Verify
		ClientRepresentation client = manager.findClientByClientId(clientId);
		assertThat(client.isDirectAccessGrantsEnabled()).isFalse();

		// Re-enable
		manager.setDirectAccessGrantsEnabled(createdClientUuid, true);

		// Verify
		client = manager.findClientByClientId(clientId);
		assertThat(client.isDirectAccessGrantsEnabled()).isTrue();
	}

	@Test
	void testDeleteClient() throws KeycloakAdminException
	{
		String clientId = "test-delete-client-" + System.currentTimeMillis();

		String clientUuid = manager.createPublicClient(
				clientId,
				List.of("http://localhost:3000/*"),
				List.of("http://localhost:3000")
		);

		// Delete client
		manager.deleteClient(clientUuid);
		createdClientUuid = null; // Prevent cleanup attempt

		// Verify client no longer exists
		ClientRepresentation client = manager.findClientByClientId(clientId);
		assertThat(client).isNull();
	}

	@Test
	void testFindNonExistentClient()
	{
		ClientRepresentation client = manager.findClientByClientId("this-client-does-not-exist-12345");
		assertThat(client).isNull();
	}
}
