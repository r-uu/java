package de.ruu.lib.util.config.mp;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class ConfigHealthCheckTest
{
	private Config testConfig;

	@BeforeEach
	void setUp()
	{
		// Create a test config with all required properties
		Map<String, String> props = new HashMap<>();

		// Database properties
		props.put("db.pragma.host", "localhost");
		props.put("db.pragma.port", "5432");
		props.put("db.pragma.database", "pragma");
		props.put("db.pragma.username", "pragma");
		props.put("db.pragma.password", "pragma");

		props.put("db.lib_test.host", "localhost");
		props.put("db.lib_test.port", "5432");
		props.put("db.lib_test.database", "lib_test");
		props.put("db.lib_test.username", "lib_test");
		props.put("db.lib_test.password", "lib_test");

		// Keycloak properties
		props.put("keycloak.url", "http://localhost:8080");
		props.put("keycloak.realm", "pragma-realm");
		props.put("keycloak.client.id", "pragma-frontend");
		props.put("keycloak.admin.username", "admin");
		props.put("keycloak.admin.password", "admin");

		// Service URLs
		props.put("service.backend.url", "http://localhost:9080/pragma");
		props.put("service.jasperreports.url", "http://localhost:8090");

		// Testing properties
		props.put("testing", "true");
		props.put("keycloak.test.user", "test");
		props.put("keycloak.test.password", "test");

		testConfig = new TestConfig(props);
	}

	@AfterEach
	void tearDown()
	{
		ConfigProviderResolver.instance().releaseConfig(testConfig);
	}

	@Test
	void testValidConfigurationPasses()
	{
		ConfigHealthCheck check = new ConfigHealthCheck(testConfig);
		ConfigHealthCheck.Result result = check.validate();

		if (!result.healthy())
		{
			System.out.println("Validation errors:");
			result.errors().forEach(System.out::println);
		}

		assertThat(result.healthy()).as("Valid configuration should pass").isTrue();
		assertThat(result.errors()).as("Should have no errors").isEmpty();
	}

	@Test
	void testMissingPropertyFails()
	{
		// Create config with missing property
		Map<String, String> props = new HashMap<>();
		props.put("db.pragma.host", "localhost");
		// Missing other db.pragma properties

		Config incompleteConfig = new TestConfig(props);
		ConfigHealthCheck check = new ConfigHealthCheck(incompleteConfig);
		ConfigHealthCheck.Result result = check.validate();

		assertThat(result.healthy()).as("Incomplete configuration should fail").isFalse();
		assertThat(result.errors()).as("Should have errors").isNotEmpty();
		assertThat(result.errors().stream()
				.anyMatch(e -> e.contains("db.pragma.port") || e.contains("db.pragma.database")))
				.as("Should report missing db.pragma properties").isTrue();
	}

	@Test
	void testEmptyPropertyFails()
	{
		Map<String, String> props = new HashMap<>();
		props.put("db.pragma.host", "");  // Empty value

		Config emptyValueConfig = new TestConfig(props);
		ConfigHealthCheck check = new ConfigHealthCheck(emptyValueConfig);
		ConfigHealthCheck.Result result = check.validate();

		assertThat(result.healthy()).as("Empty property values should fail").isFalse();
	}

	@Test
	void testPasswordsMaskedInOutput()
	{
		ConfigHealthCheck check = new ConfigHealthCheck(testConfig);
		ConfigHealthCheck.Result result = check.validate();

		Map<String, String> validated = result.validatedProperties();

		// Passwords should be masked
		assertThat(validated.get("db.pragma.password"))
				.as("Database password should be masked")
				.startsWith("***");
		assertThat(validated.get("keycloak.admin.password"))
				.as("Keycloak password should be masked")
				.startsWith("***");

		// Non-passwords should not be masked
		assertThat(validated.get("db.pragma.host"))
				.as("Host should not be masked")
				.isEqualTo("localhost");
	}

	@Test
	void testWeakPasswordWarningInProduction()
	{
		Map<String, String> props = new HashMap<>(getAllRequiredProperties());
		props.put("testing", "false");  // Production mode
		props.put("db.pragma.password", "weak");  // Weak password

		Config prodConfig = new TestConfig(props);
		ConfigHealthCheck check = new ConfigHealthCheck(prodConfig);
		ConfigHealthCheck.Result result = check.validate();

		assertThat(result.warnings())
				.as("Should have warnings about weak passwords")
				.isNotEmpty();
	}

	@Test
	void testNoPasswordWarningInTestingMode()
	{
		ConfigHealthCheck check = new ConfigHealthCheck(testConfig);
		ConfigHealthCheck.Result result = check.validate();

		// In testing mode, weak passwords are allowed without warnings
		assertThat(result.warnings().isEmpty() ||
				result.warnings().stream().noneMatch(w -> w.contains("password")))
				.as("Should not warn about passwords in testing mode")
				.isTrue();
	}

	@Test
	void testThrowIfUnhealthy()
	{
		Map<String, String> props = new HashMap<>();
		// Missing all properties

		Config emptyConfig = new TestConfig(props);
		ConfigHealthCheck check = new ConfigHealthCheck(emptyConfig);
		ConfigHealthCheck.Result result = check.validate();

		assertThatThrownBy(result::throwIfUnhealthy)
				.as("Should throw exception for unhealthy config")
				.isInstanceOf(IllegalStateException.class);
	}

	/**
	 * Helper to get all required properties for testing.
	 */
	private Map<String, String> getAllRequiredProperties()
	{
		Map<String, String> props = new HashMap<>();

		props.put("db.pragma.host", "localhost");
		props.put("db.pragma.port", "5432");
		props.put("db.pragma.database", "pragma");
		props.put("db.pragma.user", "pragma");
		props.put("db.pragma.password", "pragma");

		props.put("db.lib_test.host", "localhost");
		props.put("db.lib_test.port", "5432");
		props.put("db.lib_test.database", "lib_test");
		props.put("db.lib_test.user", "pragma");
		props.put("db.lib_test.password", "pragma");

		props.put("keycloak.url", "http://localhost:8080");
		props.put("keycloak.realm", "pragma-realm");
		props.put("keycloak.client.id", "pragma-frontend");
		props.put("keycloak.admin.user", "admin");
		props.put("keycloak.admin.password", "admin");

		props.put("service.backend.url", "http://localhost:9080/pragma");
		props.put("service.jasperreports.url", "http://localhost:8090");

		props.put("testing", "true");
		props.put("keycloak.test.user", "pragma");
		props.put("keycloak.test.password", "pragma");

		return props;
	}
}