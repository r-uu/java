package de.ruu.lib.keycloak.admin;

import java.util.List;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.representations.idm.RoleRepresentation;

import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for managing Keycloak realm roles programmatically.
 * 
 * <p>This class provides convenient wrapper methods around the Keycloak Admin Client
 * for common realm role management tasks such as creating roles, deleting roles,
 * and managing role hierarchies.</p>
 * 
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * // Create manager instance
 * KeycloakRealmManager manager = KeycloakRealmManager.builder()
 *     .serverUrl("http://localhost:8080")
 *     .realm("pragma-realm")
 *     .adminUsername("admin")
 *     .adminPassword("admin")
 *     .build();
 * 
 * // Create realm roles
 * manager.createRole("admin", "Administrator role");
 * manager.createRole("user", "Regular user role");
 * manager.createRole("task-admin", "Task administrator");
 * 
 * // Add composite roles (role inheritance)
 * manager.addCompositeRole("admin", "task-admin", "user");
 * 
 * // Close connection
 * manager.close();
 * }</pre>
 * 
 * <h2>Role Types:</h2>
 * <ul>
 *   <li><strong>Realm Roles:</strong> Global roles available across all clients</li>
 *   <li><strong>Client Roles:</strong> Specific to individual clients (not managed here)</li>
 *   <li><strong>Composite Roles:</strong> Roles that inherit permissions from other roles</li>
 * </ul>
 * 
 * <h2>Thread Safety:</h2>
 * <p>This class is NOT thread-safe. Each thread should create its own instance.</p>
 * 
 * @author r-uu
 * @since 2025-12-27
 */
public class KeycloakRealmManager implements AutoCloseable
{
	private static final Logger log = LoggerFactory.getLogger(KeycloakRealmManager.class);

	private final Keycloak keycloak;
	private final String realmName;

	/**
	 * Creates a new KeycloakRealmManager with the specified configuration.
	 * 
	 * <p><strong>Important:</strong> Remember to call {@link #close()} when done
	 * to release HTTP connection resources.</p>
	 * 
	 * @param serverUrl Keycloak server base URL (e.g., "http://localhost:8080")
	 * @param realm Target realm name (e.g., "pragma-realm")
	 * @param adminUsername Admin username (usually "admin")
	 * @param adminPassword Admin password
	 */
	private KeycloakRealmManager(
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

		log.debug("Initialized KeycloakRealmManager for realm: {}", realm);
	}

	/**
	 * Builder for creating KeycloakRealmManager instances.
	 * 
	 * @return new Builder instance
	 */
	public static Builder builder()
	{
		return new Builder();
	}

	/**
	 * Creates a new realm role.
	 * 
	 * <p>Realm roles are global roles that can be assigned to users and
	 * are available across all clients in the realm.</p>
	 * 
	 * @param roleName Role name (must be unique in realm)
	 * @param description Role description (optional, can be null)
	 * @throws KeycloakAdminException if role creation fails
	 */
	public void createRole(@NonNull String roleName, String description) throws KeycloakAdminException
	{
		log.debug("Creating realm role: {} in realm: {}", roleName, realmName);

		RoleRepresentation role = new RoleRepresentation();
		role.setName(roleName);
		role.setDescription(description);

		try
		{
			realm().roles().create(role);
			log.info("Realm role created successfully: {}", roleName);
		}
		catch (jakarta.ws.rs.WebApplicationException e)
		{
			log.error("Error creating role: {} - HTTP {}", roleName, e.getResponse().getStatus(), e);
			throw new KeycloakAdminException(
					"Error creating role: " + roleName + " (HTTP " + e.getResponse().getStatus() + ")", e);
		}
		catch (RuntimeException e)
		{
			log.error("Error creating role: {}", roleName, e);
			throw new KeycloakAdminException("Error creating role: " + roleName, e);
		}
	}

	/**
	 * Creates a realm role with just a name (no description).
	 * 
	 * @param roleName Role name (must be unique in realm)
	 * @throws KeycloakAdminException if role creation fails
	 */
	public void createRole(@NonNull String roleName) throws KeycloakAdminException
	{
		createRole(roleName, null);
	}

	/**
	 * Deletes a realm role.
	 * 
	 * @param roleName Role name to delete
	 * @throws KeycloakAdminException if deletion fails
	 */
	public void deleteRole(@NonNull String roleName) throws KeycloakAdminException
	{
		log.debug("Deleting realm role: {} from realm: {}", roleName, realmName);

		try
		{
			realm().roles().deleteRole(roleName);
			log.info("Realm role deleted successfully: {}", roleName);
		}
		catch (jakarta.ws.rs.WebApplicationException e)
		{
			log.error("Error deleting role: {} - HTTP {}", roleName, e.getResponse().getStatus(), e);
			throw new KeycloakAdminException(
					"Error deleting role: " + roleName + " (HTTP " + e.getResponse().getStatus() + ")", e);
		}
		catch (RuntimeException e)
		{
			log.error("Error deleting role: {}", roleName, e);
			throw new KeycloakAdminException("Error deleting role: " + roleName, e);
		}
	}

	/**
	 * Finds a realm role by name.
	 * 
	 * @param roleName Role name to search for
	 * @return RoleRepresentation if found, null otherwise
	 */
	public RoleRepresentation findRoleByName(@NonNull String roleName)
	{
		log.debug("Searching for realm role: {}", roleName);

		try
		{
			RoleRepresentation role = realm().roles().get(roleName).toRepresentation();
			log.debug("Realm role found: {}", roleName);
			return role;
		}
		catch (RuntimeException e)
		{
			log.debug("Realm role not found: {}", roleName);
			return null;
		}
	}

	/**
	 * Gets all realm roles.
	 * 
	 * @return List of all realm roles
	 */
	public List<RoleRepresentation> allRoles()
	{
		log.debug("Getting all realm roles from realm: {}", realmName);
		return realm().roles().list();
	}

	/**
	 * Adds composite roles to a parent role (role inheritance).
	 * 
	 * <p>Composite roles allow role hierarchies. When a user has a composite role,
	 * they automatically inherit all permissions from the child roles.</p>
	 * 
	 * <p>Example: An "admin" role can be composed of "user" and "task-admin" roles,
	 * so admins automatically have all user and task-admin permissions.</p>
	 * 
	 * @param parentRoleName Parent role that will inherit from child roles
	 * @param childRoleNames Child roles to add to the parent role
	 * @throws KeycloakAdminException if adding composites fails
	 */
	public void addCompositeRole(@NonNull String parentRoleName, @NonNull String... childRoleNames) 
			throws KeycloakAdminException
	{
		log.debug("Adding composite roles to {}: {}", parentRoleName, String.join(", ", childRoleNames));

		try
		{
			RoleResource parentRole = realm().roles().get(parentRoleName);
			
			List<RoleRepresentation> childRoles = new java.util.ArrayList<>();
			for (String childRoleName : childRoleNames)
			{
				RoleRepresentation childRole = realm().roles().get(childRoleName).toRepresentation();
				childRoles.add(childRole);
			}
			
			parentRole.addComposites(childRoles);
			log.info("Composite roles added to {}: {}", parentRoleName, String.join(", ", childRoleNames));
		}
		catch (jakarta.ws.rs.WebApplicationException e)
		{
			log.error("Error adding composite roles to {} - HTTP {}", parentRoleName, e.getResponse().getStatus(), e);
			throw new KeycloakAdminException(
					"Error adding composite roles to: " + parentRoleName + " (HTTP " + e.getResponse().getStatus() + ")", e);
		}
		catch (RuntimeException e)
		{
			log.error("Error adding composite roles to {}", parentRoleName, e);
			throw new KeycloakAdminException("Error adding composite roles to: " + parentRoleName, e);
		}
	}

	/**
	 * Removes composite roles from a parent role.
	 * 
	 * @param parentRoleName Parent role to remove child roles from
	 * @param childRoleNames Child roles to remove
	 * @throws KeycloakAdminException if removing composites fails
	 */
	public void removeCompositeRole(@NonNull String parentRoleName, @NonNull String... childRoleNames) 
			throws KeycloakAdminException
	{
		log.debug("Removing composite roles from {}: {}", parentRoleName, String.join(", ", childRoleNames));

		try
		{
			RoleResource parentRole = realm().roles().get(parentRoleName);
			
			List<RoleRepresentation> childRoles = new java.util.ArrayList<>();
			for (String childRoleName : childRoleNames)
			{
				RoleRepresentation childRole = realm().roles().get(childRoleName).toRepresentation();
				childRoles.add(childRole);
			}
			
			parentRole.deleteComposites(childRoles);
			log.info("Composite roles removed from {}: {}", parentRoleName, String.join(", ", childRoleNames));
		}
		catch (jakarta.ws.rs.WebApplicationException e)
		{
			log.error("Error removing composite roles from {} - HTTP {}", parentRoleName, e.getResponse().getStatus(), e);
			throw new KeycloakAdminException(
					"Error removing composite roles from: " + parentRoleName + " (HTTP " + e.getResponse().getStatus() + ")", e);
		}
		catch (RuntimeException e)
		{
			log.error("Error removing composite roles from {}", parentRoleName, e);
			throw new KeycloakAdminException("Error removing composite roles from: " + parentRoleName, e);
		}
	}

	/**
	 * Gets all composite roles of a parent role.
	 * 
	 * @param roleName Role name
	 * @return List of composite (child) roles
	 */
	public List<RoleRepresentation> compositeRoles(@NonNull String roleName)
	{
		log.debug("Getting composite roles for: {}", roleName);
		return new java.util.ArrayList<>(realm().roles().get(roleName).getRoleComposites());
	}

	/**
	 * Updates a role's description.
	 * 
	 * @param roleName Role name
	 * @param description New description
	 * @throws KeycloakAdminException if update fails
	 */
	public void updateRoleDescription(@NonNull String roleName, String description) 
			throws KeycloakAdminException
	{
		log.debug("Updating description for role: {}", roleName);

		try
		{
			RoleResource roleResource = realm().roles().get(roleName);
			RoleRepresentation role = roleResource.toRepresentation();
			role.setDescription(description);
			roleResource.update(role);
			log.info("Role description updated: {}", roleName);
		}
		catch (jakarta.ws.rs.WebApplicationException e)
		{
			log.error("Error updating role description: {} - HTTP {}", roleName, e.getResponse().getStatus(), e);
			throw new KeycloakAdminException(
					"Error updating role: " + roleName + " (HTTP " + e.getResponse().getStatus() + ")", e);
		}
		catch (RuntimeException e)
		{
			log.error("Error updating role description: {}", roleName, e);
			throw new KeycloakAdminException("Error updating role: " + roleName, e);
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
			log.debug("KeycloakRealmManager closed");
		}
	}

	/**
	 * Builder for KeycloakRealmManager instances.
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

		public KeycloakRealmManager build()
		{
			return new KeycloakRealmManager(serverUrl, realm, adminUsername, adminPassword);
		}
	}
}
