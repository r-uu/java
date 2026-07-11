package de.ruu.lib.keycloak.admin;

import java.util.Arrays;
import java.util.List;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import jakarta.ws.rs.core.Response;

import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for managing Keycloak users programmatically.
 * 
 * <p>This class provides convenient wrapper methods around the Keycloak Admin Client
 * for common user management tasks such as creating users, setting passwords, and
 * assigning roles.</p>
 * 
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * // Create manager instance
 * KeycloakUserManager manager = KeycloakUserManager.builder()
 *     .serverUrl("http://localhost:8080")
 *     .realm("pragma-realm")
 *     .adminUsername("admin")
 *     .adminPassword("admin")
 *     .build();
 * 
 * // Create user with password and roles
 * String userId = manager.createUser("testuser", "test@example.com", "password123", "task-admin");
 * 
 * // Close connection
 * manager.close();
 * }</pre>
 * 
 * <h2>Prerequisites:</h2>
 * <ul>
 *   <li>Keycloak server must be running</li>
 *   <li>Admin credentials must be valid</li>
 *   <li>Target realm must exist</li>
 * </ul>
 * 
 * <h2>Thread Safety:</h2>
 * <p>This class is NOT thread-safe. Each thread should create its own instance.</p>
 * 
 * @author r-uu
 * @since 2025-12-27
 */
public class KeycloakUserManager implements AutoCloseable
{
	private static final Logger log = LoggerFactory.getLogger(KeycloakUserManager.class);

	private final Keycloak keycloak;
	private final String realmName;

	/**
	 * Creates a new KeycloakUserManager with the specified configuration.
	 * 
	 * <p><strong>Important:</strong> Remember to call {@link #close()} when done
	 * to release HTTP connection resources.</p>
	 * 
	 * @param serverUrl Keycloak server base URL (e.g., "http://localhost:8080")
	 * @param realm Target realm name (e.g., "pragma-realm")
	 * @param adminUsername Admin username (usually "admin")
	 * @param adminPassword Admin password
	 */
	private KeycloakUserManager(
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
				.realm("master") // Always authenticate against master realm
				.username(adminUsername)
				.password(adminPassword)
				.clientId("admin-cli")
				.build();

		log.debug("Initialized KeycloakUserManager for realm: {}", realm);
	}

	/**
	 * Builder for creating KeycloakUserManager instances.
	 * 
	 * @return new Builder instance
	 */
	public static Builder builder()
	{
		return new Builder();
	}

	/**
	 * Creates a new user in Keycloak.
	 * 
	 * <p>Creates a user with the specified details. The user will be enabled and
	 * email will be marked as verified by default.</p>
	 * 
	 * @param username Username (must be unique in realm)
	 * @param email Email address
	 * @return User ID of the created user
	 * @throws KeycloakAdminException if user creation fails
	 */
	public String createUser(@NonNull String username, @NonNull String email) throws KeycloakAdminException
	{
		log.debug("Creating user: {} in realm: {}", username, realmName);

		UserRepresentation user = new UserRepresentation();
		user.setUsername(username);
		user.setEmail(email);
		user.setEmailVerified(true);
		user.setEnabled(true);

		try (Response response = realm().users().create(user))
		{
			if (response.getStatus() != 201)
			{
				String error = response.readEntity(String.class);
				log.error("Failed to create user: {} - Status: {} - Error: {}", username, response.getStatus(), error);
				throw new KeycloakAdminException(
						"Failed to create user: " + username + " (HTTP " + response.getStatus() + "): " + error);
			}
			String userId = extractUserIdFromLocation(response.getLocation().toString());
			log.info("User created successfully: {} (ID: {})", username, userId);
			return userId;
		}
		catch (KeycloakAdminException e)
		{
			throw e;
		}
		catch (jakarta.ws.rs.WebApplicationException e)
		{
			log.error("Error creating user: {} - HTTP {}", username, e.getResponse().getStatus(), e);
			throw new KeycloakAdminException(
					"Error creating user: " + username + " (HTTP " + e.getResponse().getStatus() + ")", e);
		}
		catch (RuntimeException e)
		{
			log.error("Error creating user: {}", username, e);
			throw new KeycloakAdminException("Error creating user: " + username, e);
		}
	}

	/**
	 * Creates a user with password and assigns roles in one operation.
	 * 
	 * <p>Convenience method that combines user creation, password setup, and
	 * role assignment.</p>
	 * 
	 * @param username Username (must be unique in realm)
	 * @param email Email address
	 * @param password User password (will not be temporary)
	 * @param roleNames Roles to assign (realm-level roles)
	 * @return User ID of the created user
	 * @throws KeycloakAdminException if any operation fails
	 */
	public String createUser(
			@NonNull String username,
			@NonNull String email,
			@NonNull String password,
			String... roleNames) throws KeycloakAdminException
	{
		String userId = createUser(username, email);
		setPassword(userId, password, false);

		if (roleNames != null && roleNames.length > 0)
		{
			assignRoles(userId, roleNames);
		}

		return userId;
	}

	/**
	 * Sets or resets a user's password.
	 * 
	 * @param userId User ID
	 * @param password New password
	 * @param temporary If true, user must change password on next login
	 * @throws KeycloakAdminException if password reset fails
	 */
	public void setPassword(@NonNull String userId, @NonNull String password, boolean temporary)
			throws KeycloakAdminException
	{
		log.debug("Setting password for user ID: {} (temporary: {})", userId, temporary);

		CredentialRepresentation credential = new CredentialRepresentation();
		credential.setType(CredentialRepresentation.PASSWORD);
		credential.setValue(password);
		credential.setTemporary(temporary);

		try
		{
			realm().users().get(userId).resetPassword(credential);
			log.info("Password set successfully for user ID: {}", userId);
		}
		catch (jakarta.ws.rs.WebApplicationException e)
		{
			log.error("Error setting password for user ID: {} - HTTP {}", userId, e.getResponse().getStatus(), e);
			throw new KeycloakAdminException(
					"Error setting password for user ID: " + userId + " (HTTP " + e.getResponse().getStatus() + ")", e);
		}
		catch (RuntimeException e)
		{
			log.error("Error setting password for user ID: {}", userId, e);
			throw new KeycloakAdminException("Error setting password for user ID: " + userId, e);
		}
	}

	/**
	 * Assigns realm-level roles to a user.
	 * 
	 * @param userId User ID
	 * @param roleNames Names of roles to assign
	 * @throws KeycloakAdminException if role assignment fails
	 */
	public void assignRoles(@NonNull String userId, @NonNull String... roleNames) throws KeycloakAdminException
	{
		log.debug("Assigning roles to user ID {}: {}", userId, Arrays.toString(roleNames));

		try
		{
			UserResource userResource = realm().users().get(userId);
			List<RoleRepresentation> roles = Arrays.stream(roleNames)
					.map(roleName -> realm().roles().get(roleName).toRepresentation())
					.toList();

			userResource.roles().realmLevel().add(roles);
			log.info("Roles assigned successfully to user ID {}: {}", userId, Arrays.toString(roleNames));
		}
		catch (jakarta.ws.rs.WebApplicationException e)
		{
			log.error("Error assigning roles to user ID {} - HTTP {}", userId, e.getResponse().getStatus(), e);
			throw new KeycloakAdminException(
					"Error assigning roles to user ID: " + userId + " (HTTP " + e.getResponse().getStatus() + ")", e);
		}
		catch (RuntimeException e)
		{
			log.error("Error assigning roles to user ID {}: {}", userId, Arrays.toString(roleNames), e);
			throw new KeycloakAdminException("Error assigning roles to user ID: " + userId, e);
		}
	}

	/**
	 * Deletes a user from Keycloak.
	 * 
	 * @param userId User ID to delete
	 * @throws KeycloakAdminException if deletion fails
	 */
	public void deleteUser(@NonNull String userId) throws KeycloakAdminException
	{
		log.debug("Deleting user ID: {}", userId);

		try (Response response = realm().users().delete(userId))
		{
			if (response.getStatus() != 204)
			{
				String error = response.readEntity(String.class);
				log.error("Failed to delete user ID {}: Status {} - {}", userId, response.getStatus(), error);
				throw new KeycloakAdminException(
						"Failed to delete user: " + userId + " (HTTP " + response.getStatus() + "): " + error);
			}
			log.info("User deleted successfully: {}", userId);
		}
		catch (KeycloakAdminException e)
		{
			throw e;
		}
		catch (jakarta.ws.rs.WebApplicationException e)
		{
			log.error("Error deleting user ID: {} - HTTP {}", userId, e.getResponse().getStatus(), e);
			throw new KeycloakAdminException(
					"Error deleting user: " + userId + " (HTTP " + e.getResponse().getStatus() + ")", e);
		}
		catch (RuntimeException e)
		{
			log.error("Error deleting user ID: {}", userId, e);
			throw new KeycloakAdminException("Error deleting user: " + userId, e);
		}
	}

	/**
	 * Finds a user by username.
	 * 
	 * @param username Username to search for
	 * @return UserRepresentation if found, null otherwise
	 */
	public UserRepresentation findUserByUsername(@NonNull String username)
	{
		log.debug("Searching for user: {}", username);

		List<UserRepresentation> users = realm().users().search(username, true); // exact match
		if (users.isEmpty())
		{
			log.debug("User not found: {}", username);
			return null;
		}

		log.debug("User found: {}", username);
		return users.get(0);
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
	 * Extracts user ID from Location header URL.
	 * 
	 * <p>Keycloak returns the user ID in the Location header after creation:
	 * {@code http://localhost:8080/admin/realms/my-realm/users/<user-id>}</p>
	 * 
	 * @param location Location header URL
	 * @return User ID
	 */
	private String extractUserIdFromLocation(String location)
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
			log.debug("KeycloakUserManager closed");
		}
	}

	/**
	 * Builder for KeycloakUserManager instances.
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

		public KeycloakUserManager build()
		{
			return new KeycloakUserManager(serverUrl, realm, adminUsername, adminPassword);
		}
	}
}
