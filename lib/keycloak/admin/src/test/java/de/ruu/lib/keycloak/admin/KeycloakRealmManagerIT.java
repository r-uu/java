package de.ruu.lib.keycloak.admin;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.RoleRepresentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Integration tests for KeycloakRealmManager.
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
class KeycloakRealmManagerIT
{
	private static final Logger log = LoggerFactory.getLogger(KeycloakRealmManagerIT.class);

	private KeycloakRealmManager manager;
	private List<String> createdRoles;

	@BeforeEach
	void setUp()
	{
		manager = KeycloakRealmManager.builder()
				.serverUrl("http://localhost:8080")
				.realm("pragma-realm")
				.adminUsername("admin")
				.adminPassword("admin")
				.build();
		
		createdRoles = new ArrayList<>();
	}

	@AfterEach
	void tearDown() throws KeycloakAdminException
	{
		// Cleanup: delete created roles
		for (String roleName : createdRoles)
		{
			try
			{
				manager.deleteRole(roleName);
				log.info("Cleaned up test role: {}", roleName);
			}
			catch (Exception e)
			{
				log.warn("Failed to cleanup test role: {}", roleName, e);
			}
		}

		if (manager != null)
		{
			manager.close();
		}
	}

	@Test
	void testCreateRole() throws KeycloakAdminException
	{
		String roleName = "test-role-" + System.currentTimeMillis();
		createdRoles.add(roleName);

		manager.createRole(roleName, "Test role description");

		// Verify role exists
		RoleRepresentation role = manager.findRoleByName(roleName);
		assertThat(role).isNotNull();
		assertThat(role.getName()).isEqualTo(roleName);
		assertThat(role.getDescription()).isEqualTo("Test role description");
	}

	@Test
	void testCreateRoleWithoutDescription() throws KeycloakAdminException
	{
		String roleName = "test-role-nodesc-" + System.currentTimeMillis();
		createdRoles.add(roleName);

		manager.createRole(roleName);

		// Verify role exists
		RoleRepresentation role = manager.findRoleByName(roleName);
		assertThat(role).isNotNull();
		assertThat(role.getName()).isEqualTo(roleName);
	}

	@Test
	void testDeleteRole() throws KeycloakAdminException
	{
		String roleName = "test-delete-role-" + System.currentTimeMillis();

		manager.createRole(roleName, "Role to be deleted");

		// Delete role
		manager.deleteRole(roleName);

		// Verify role no longer exists
		RoleRepresentation role = manager.findRoleByName(roleName);
		assertThat(role).isNull();
	}

	@Test
	void testAddCompositeRole() throws KeycloakAdminException
	{
		String parentRole = "test-parent-" + System.currentTimeMillis();
		String childRole1 = "test-child1-" + System.currentTimeMillis();
		String childRole2 = "test-child2-" + System.currentTimeMillis();

		createdRoles.add(parentRole);
		createdRoles.add(childRole1);
		createdRoles.add(childRole2);

		// Create roles
		manager.createRole(parentRole, "Parent role");
		manager.createRole(childRole1, "Child role 1");
		manager.createRole(childRole2, "Child role 2");

		// Add composite roles
		manager.addCompositeRole(parentRole, childRole1, childRole2);

		// Verify
		List<RoleRepresentation> composites = manager.compositeRoles(parentRole);
		assertThat(composites).hasSize(2);

		List<String> compositeNames = composites.stream()
				.map(RoleRepresentation::getName)
				.toList();
		assertThat(compositeNames).contains(childRole1);
		assertThat(compositeNames).contains(childRole2);
	}

	@Test
	void testRemoveCompositeRole() throws KeycloakAdminException
	{
		String parentRole = "test-parent-remove-" + System.currentTimeMillis();
		String childRole = "test-child-remove-" + System.currentTimeMillis();

		createdRoles.add(parentRole);
		createdRoles.add(childRole);

		// Create roles
		manager.createRole(parentRole, "Parent role");
		manager.createRole(childRole, "Child role");

		// Add composite role
		manager.addCompositeRole(parentRole, childRole);

		// Verify added
		List<RoleRepresentation> composites = manager.compositeRoles(parentRole);
		assertThat(composites).hasSize(1);

		// Remove composite role
		manager.removeCompositeRole(parentRole, childRole);

		// Verify removed
		composites = manager.compositeRoles(parentRole);
		assertThat(composites).hasSize(0);
	}

	@Test
	void testUpdateRoleDescription() throws KeycloakAdminException
	{
		String roleName = "test-update-role-" + System.currentTimeMillis();
		createdRoles.add(roleName);

		manager.createRole(roleName, "Original description");

		// Update description
		manager.updateRoleDescription(roleName, "Updated description");

		// Verify
		RoleRepresentation role = manager.findRoleByName(roleName);
		assertThat(role.getDescription()).isEqualTo("Updated description");
	}

	@Test
	void testGetAllRoles()
	{
		List<RoleRepresentation> roles = manager.allRoles();
		assertThat(roles).isNotNull();
		assertThat(roles).isNotEmpty();

		// Should contain default roles
		List<String> roleNames = roles.stream()
				.map(RoleRepresentation::getName)
				.toList();
		assertThat(roleNames).contains("offline_access");
	}

	@Test
	void testFindNonExistentRole()
	{
		RoleRepresentation role = manager.findRoleByName("this-role-does-not-exist-12345");
		assertThat(role).isNull();
	}
}
