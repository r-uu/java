package de.ruu.lib.keycloak.admin;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.UserRepresentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for KeycloakUserManager.
 * 
 * <p><strong>Prerequisites:</strong></p>
 * <ul>
 *   <li>Keycloak must be running on localhost:8080</li>
 *   <li>Admin credentials: admin / admin</li>
 *   <li>Realm "pragma-realm" must exist</li>
 *   <li>Role "task-admin" must exist in the realm</li>
 * </ul>
 * 
 * <p><strong>Note:</strong> These tests are disabled by default. Remove @Disabled
 * annotation and ensure Keycloak is running to execute them.</p>
 * 
 * @author r-uu
 * @since 2025-12-27
 */
@Disabled("Requires Keycloak running on localhost:8080")
class KeycloakUserManagerIT
{
	private static final Logger log = LoggerFactory.getLogger(KeycloakUserManagerIT.class);

	private KeycloakUserManager manager;
	private String createdUserId;

	@BeforeEach
	void setUp()
	{
		manager = KeycloakUserManager.builder()
				.serverUrl("http://localhost:8080")
				.realm("pragma-realm")
				.adminUsername("admin")
				.adminPassword("admin")
				.build();
	}

	@AfterEach
	void tearDown() throws KeycloakAdminException
	{
		// Cleanup: delete created user if exists
		if (createdUserId != null)
		{
			try
			{
				manager.deleteUser(createdUserId);
				log.info("Cleaned up test user: {}", createdUserId);
			}
			catch (Exception e)
			{
				log.warn("Failed to cleanup test user: {}", createdUserId, e);
			}
		}

		if (manager != null)
		{
			manager.close();
		}
	}

	@Test
	void testCreateUser() throws KeycloakAdminException
	{
		String username = "testuser-" + System.currentTimeMillis();
		String email = username + "@example.com";

		createdUserId = manager.createUser(username, email);

		assertThat(createdUserId).isNotNull();
		assertThat(createdUserId).isNotEmpty();

		// Verify user exists
		UserRepresentation user = manager.findUserByUsername(username);
		assertThat(user).isNotNull();
		assertThat(user.getUsername()).isEqualTo(username);
		assertThat(user.getEmail()).isEqualTo(email);
		assertThat(user.isEnabled()).isTrue();
		assertThat(user.isEmailVerified()).isTrue();
	}

	@Test
	void testCreateUserWithPasswordAndRoles() throws KeycloakAdminException
	{
		String username = "testuser-" + System.currentTimeMillis();
		String email = username + "@example.com";
		String password = "test-password";

		createdUserId = manager.createUser(username, email, password, "task-admin");

		assertThat(createdUserId).isNotNull();

		// Verify user exists
		UserRepresentation user = manager.findUserByUsername(username);
		assertThat(user).isNotNull();
		assertThat(user.getUsername()).isEqualTo(username);
	}

	@Test
	void testSetPassword() throws KeycloakAdminException
	{
		String username = "testuser-" + System.currentTimeMillis();
		String email = username + "@example.com";

		createdUserId = manager.createUser(username, email);

		// Should not throw exception
		assertThatCode(() -> manager.setPassword(createdUserId, "new-password", false))
				.doesNotThrowAnyException();
	}

	@Test
	void testDeleteUser() throws KeycloakAdminException
	{
		String username = "testuser-" + System.currentTimeMillis();
		String email = username + "@example.com";

		String userId = manager.createUser(username, email);

		// Delete user
		manager.deleteUser(userId);
		createdUserId = null; // Prevent cleanup attempt

		// Verify user no longer exists
		UserRepresentation user = manager.findUserByUsername(username);
		assertThat(user).isNull();
	}

	@Test
	void testFindNonExistentUser()
	{
		UserRepresentation user = manager.findUserByUsername("this-user-does-not-exist-12345");
		assertThat(user).isNull();
	}
}
