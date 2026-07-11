package de.ruu.lib.util.config.mp;

import org.junit.jupiter.api.AfterEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ConfigFileInitializer}.
 *
 * <p>
 * Tests cover:
 * <ul>
 * <li>Creating new config files with default values</li>
 * <li>Preserving existing values when adding new defaults</li>
 * <li>Postgres-specific utility methods</li>
 * <li>Ensuring required properties exist</li>
 * </ul>
 */
class ConfigFileInitializerTest
{
	private static final Logger log = LoggerFactory.getLogger(ConfigFileInitializerTest.class);

	@TempDir
	Path tempDir;

	@AfterEach
	void cleanup()
	{
		System.clearProperty(WritableFileConfigSource.CONFIG_FILE_NAME_KEY);
	}

	/**
	 * Tests creating a new config file with custom default values.
	 */
	@Test
	void testInitializeConfigFileCreatesNewFile()
	{
		File configFile = tempDir.resolve("test.config").toFile();

		Map<String, String> defaults = new HashMap<>();
		defaults.put("app.name", "TestApp");
		defaults.put("app.version", "1.0.0");
		defaults.put("app.debug", "false");

		WritableFileConfigSource config = ConfigFileInitializer.initializeConfigFile(configFile.getAbsolutePath(),
				defaults);

		assertThat(configFile.exists()).as("Config file should be created").isEqualTo(true);
		assertThat(config.getPropertyNames()).as("Should have all default properties").hasSize(3);
		assertThat(config.getValue("app.name")).as("Should have correct value for app.name").isEqualTo("TestApp");
		assertThat(config.getValue("app.version")).as("Should have correct value for app.version").isEqualTo("1.0.0");
		assertThat(config.getValue("app.debug")).as("Should have correct value for app.debug").isEqualTo("false");
	}

	/**
	 * Tests that existing values are preserved when initializing.
	 */
	@Test
	void testInitializeConfigFilePreservesExistingValues()
	{
		File configFile = tempDir.resolve("test.config").toFile();

		// First, create a config with some values
		Map<String, String> initialDefaults = new HashMap<>();
		initialDefaults.put("app.name", "InitialName");
		initialDefaults.put("app.version", "1.0.0");

		WritableFileConfigSource config1 = ConfigFileInitializer.initializeConfigFile(configFile.getAbsolutePath(),
				initialDefaults);

		// Modify one value
		config1.setProperty("app.name", "ModifiedName");

		// Now initialize again with different defaults
		Map<String, String> newDefaults = new HashMap<>();
		newDefaults.put("app.name", "NewDefaultName"); // Should NOT overwrite
		newDefaults.put("app.version", "2.0.0"); // Should NOT overwrite
		newDefaults.put("app.debug", "true"); // Should be added

		WritableFileConfigSource config2 = ConfigFileInitializer.initializeConfigFile(configFile.getAbsolutePath(),
				newDefaults);

		assertThat(config2.getValue("app.name")).as("Existing modified value should be preserved").isEqualTo("ModifiedName");
		assertThat(config2.getValue("app.version")).as("Existing value should be preserved").isEqualTo("1.0.0");
		assertThat(config2.getValue("app.debug")).as("New property should be added").isEqualTo("true");
	}

	/**
	 * Tests ensuring required properties exist.
	 */
	@Test
	void testEnsureRequiredProperties()
	{
		File configFile = tempDir.resolve("test.config").toFile();

		// Create initial config with some properties
		Map<String, String> initialDefaults = new HashMap<>();
		initialDefaults.put("prop.a", "valueA");
		initialDefaults.put("prop.b", "valueB");

		WritableFileConfigSource config = ConfigFileInitializer.initializeConfigFile(configFile.getAbsolutePath(),
				initialDefaults);

		// Define required properties (some exist, some don't)
		Map<String, String> requiredDefaults = new HashMap<>();
		requiredDefaults.put("prop.a", "defaultA"); // Already exists
		requiredDefaults.put("prop.c", "valueC"); // New
		requiredDefaults.put("prop.d", "valueD"); // New

		int addedCount = ConfigFileInitializer.ensureRequiredProperties(config, requiredDefaults);

		assertThat(addedCount).as("Should have added 2 properties").isEqualTo(2);
		assertThat(config.getValue("prop.a")).as("Should preserve existing value").isEqualTo("valueA");
		assertThat(config.getValue("prop.c")).as("Should have added prop.c").isEqualTo("valueC");
		assertThat(config.getValue("prop.d")).as("Should have added prop.d").isEqualTo("valueD");
		assertThat(config.getPropertyNames()).as("Should have 4 properties total").hasSize(4);
	}

	/**
	 * Tests ensuring required properties when all already exist.
	 */
	@Test
	void testEnsureRequiredPropertiesAllExist()
	{
		File configFile = tempDir.resolve("test.config").toFile();

		Map<String, String> initialDefaults = new HashMap<>();
		initialDefaults.put("prop.a", "valueA");
		initialDefaults.put("prop.b", "valueB");

		WritableFileConfigSource config = ConfigFileInitializer.initializeConfigFile(configFile.getAbsolutePath(),
				initialDefaults);

		// All required properties already exist
		Map<String, String> requiredDefaults = new HashMap<>();
		requiredDefaults.put("prop.a", "defaultA");
		requiredDefaults.put("prop.b", "defaultB");

		int addedCount = ConfigFileInitializer.ensureRequiredProperties(config, requiredDefaults);

		assertThat(addedCount).as("Should not have added any properties").isEqualTo(0);
		assertThat(config.getPropertyNames()).as("Should still have 2 properties").hasSize(2);
	}

	/**
	 * Tests initialization with empty defaults map.
	 */
	@Test
	void testInitializeConfigFileWithEmptyDefaults()
	{
		File configFile = tempDir.resolve("test.config").toFile();

		Map<String, String> emptyDefaults = new HashMap<>();

		WritableFileConfigSource config = ConfigFileInitializer.initializeConfigFile(configFile.getAbsolutePath(),
				emptyDefaults);

		assertThat(configFile.exists()).as("Config file should be created even with empty defaults").isEqualTo(true);
		assertThat(config.getPropertyNames()).as("Should have no properties").hasSize(0);
	}
}
