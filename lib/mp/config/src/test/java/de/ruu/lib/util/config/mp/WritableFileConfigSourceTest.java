package de.ruu.lib.util.config.mp;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for {@link WritableFileConfigSource}.
 *
 * <p>
 * Tests cover:
 * <ul>
 * <li>Lazy loading of configuration properties from file</li>
 * <li>Reading property values from the config source</li>
 * <li>Writing new properties and persisting them to file</li>
 * <li>Removing properties and persisting changes</li>
 * <li>Getting all property names and the properties map</li>
 * <li>Default behaviour when config file doesn't exist</li>
 * </ul>
 */
class WritableFileConfigSourceTest
{
	private static final Logger log = LoggerFactory.getLogger(WritableFileConfigSourceTest.class);

	@TempDir
	Path tempDir;

	private File testConfigFile;
	private String originalConfigFileProperty;

	/**
	 * Sets up a temporary config file for each test. Stores the original system property value to restore it later.
	 */
	@BeforeEach
	void beforeEach() throws IOException
	{
		testConfigFile = tempDir.resolve("test-config.properties").toFile();

		// store original property value
		originalConfigFileProperty = System.getProperty(WritableFileConfigSource.CONFIG_FILE_NAME_KEY);

		// set system property to point to our test file
		System.setProperty(WritableFileConfigSource.CONFIG_FILE_NAME_KEY, testConfigFile.getAbsolutePath());

		// create a test config file with initial properties
		Properties props = new Properties();
		props.setProperty("test.property.one", "value1");
		props.setProperty("test.property.two", "value2");
		props.setProperty("test.number", "42");

		try (FileOutputStream fos = new FileOutputStream(testConfigFile))
		{
			props.store(fos, "Test configuration");
		}

		log.debug("Test config file created at: {}", testConfigFile.getAbsolutePath());
	}

	/** cleans up after each test by restoring the original system property. */
	@AfterEach
	void afterEach()
	{
		// Restore original property value
		if (originalConfigFileProperty != null)
		{
			System.setProperty(WritableFileConfigSource.CONFIG_FILE_NAME_KEY, originalConfigFileProperty);
		}
		else
		{
			System.clearProperty(WritableFileConfigSource.CONFIG_FILE_NAME_KEY);
		}
	}

	/** tests that the config source returns the correct name including the file path. */
	@Test
	void testGetName()
	{
		WritableFileConfigSource source = new WritableFileConfigSource();

		String name = source.getName();

		assertThat(name).as("Name should contain class name").contains("WritableFileConfigSource");
		assertThat(name).as("Name should contain file path").contains(testConfigFile.getAbsolutePath());
	}

	/**
	 * Tests that the config source has the expected ordinal value. Higher ordinal means higher priority in MicroProfile
	 * Config.
	 */
	@Test
	void testGetOrdinal()
	{
		WritableFileConfigSource source = new WritableFileConfigSource();

		int ordinal = source.getOrdinal();

		assertThat(ordinal).as("Ordinal should be 500").isEqualTo(500);
	}

	/** tests that properties are loaded from the file when first accessed. */
	@Test
	void testLazyLoadingOfProperties()
	{
		WritableFileConfigSource source = new WritableFileConfigSource();

		// Properties should be loaded on first access
		String value = source.getValue("test.property.one");

		assertThat(value).as("Property should be loaded from file").isEqualTo("value1");
	}

	/** Tests retrieving a property value by key. */
	@Test
	void testGetValue()
	{
		WritableFileConfigSource source = new WritableFileConfigSource();

		String value1 = source.getValue("test.property.one");
		String value2 = source.getValue("test.property.two");
		String value3 = source.getValue("test.number");

		assertThat(value1).as("First property value should match").isEqualTo("value1");
		assertThat(value2).as("Second property value should match").isEqualTo("value2");
		assertThat(value3).as("Numeric property value should match").isEqualTo("42");
	}

	/** tests retrieving a non-existent property returns null. */
	@Test
	void testGetValueNonExistent()
	{
		WritableFileConfigSource source = new WritableFileConfigSource();

		String value = source.getValue("non.existent.key");

		assertThat(value).as("Non-existent property should return null").isNull();
	}

	/** tests getting all property names. */
	@Test
	void testGetPropertyNames()
	{
		WritableFileConfigSource source = new WritableFileConfigSource();

		Set<String> propertyNames = source.getPropertyNames();

		assertThat(propertyNames).as("Property names should not be null").isNotNull();
		assertThat(propertyNames).as("Should contain all loaded properties").hasSize(3);
		assertThat(propertyNames).as("Should contain first property").contains("test.property.one");
		assertThat(propertyNames).as("Should contain second property").contains("test.property.two");
		assertThat(propertyNames).as("Should contain third property").contains("test.number");
	}

	/** tests getting all properties as a map. */
	@Test
	void testGetProperties()
	{
		WritableFileConfigSource source = new WritableFileConfigSource();

		Map<String, String> properties = source.getProperties();

		assertThat(properties).as("Properties map should not be null").isNotNull();
		assertThat(properties.entrySet()).as("Properties map should contain all entries").hasSize(3);
		assertThat(properties).as("Properties map should contain correct value").containsEntry("test.property.one", "value1");
		assertThat(properties).as("Properties map should contain correct value").containsEntry("test.property.two", "value2");
		assertThat(properties).as("Properties map should contain correct value").containsEntry("test.number", "42");
	}

	/** tests that the returned properties map is unmodifiable. */
	@Test
	void testGetPropertiesIsUnmodifiable()
	{
		WritableFileConfigSource source = new WritableFileConfigSource();

		Map<String, String> properties = source.getProperties();

		try
		{
			properties.put("new.key", "new.value");
			assertThat(false).as("Should have thrown UnsupportedOperationException").isTrue();
		}
		catch (UnsupportedOperationException e)
		{
			// Expected - map should be unmodifiable
			assertThat(true).as("Exception should be thrown when trying to modify").isTrue();
		}
	}

	/** tests setting a new property and verifying it's persisted to file. */
	@Test
	void testSetProperty() throws IOException
	{
		WritableFileConfigSource source = new WritableFileConfigSource();

		// Set a new property
		source.setProperty("new.property", "new.value");

		// Verify it's in the config source
		String value = source.getValue("new.property");
		assertThat(value).as("New property should be retrievable").isEqualTo("new.value");

		// Verify it's persisted to file
		Properties fileProps = new Properties();
		try (var fis = new java.io.FileInputStream(testConfigFile))
		{
			fileProps.load(fis);
		}

		assertThat(fileProps.getProperty("new.property")).as("New property should be persisted to file").isEqualTo("new.value");
	}

	/** tests updating an existing property value. */
	@Test
	void testUpdateProperty() throws IOException
	{
		WritableFileConfigSource source = new WritableFileConfigSource();

		// Update existing property
		source.setProperty("test.property.one", "updated.value");

		// Verify it's updated
		String value = source.getValue("test.property.one");
		assertThat(value).as("Property should be updated").isEqualTo("updated.value");

		// Verify it's persisted to file
		Properties fileProps = new Properties();
		try (var fis = new java.io.FileInputStream(testConfigFile))
		{
			fileProps.load(fis);
		}

		assertThat(fileProps.getProperty("test.property.one")).as("Updated property should be persisted to file").isEqualTo("updated.value");
	}

	/** tests removing a property. */
	@Test
	void testRemoveProperty() throws IOException
	{
		WritableFileConfigSource source = new WritableFileConfigSource();

		// Remove property
		source.removeProperty("test.property.one");

		// Verify it's removed from config source
		String value = source.getValue("test.property.one");
		assertThat(value).as("Removed property should return null").isNull();

		// Verify other properties still exist
		assertThat(source.getValue("test.property.two")).as("Other properties should still exist").isEqualTo("value2");

		// Verify removal is persisted to file
		Properties fileProps = new Properties();
		try (var fis = new java.io.FileInputStream(testConfigFile))
		{
			fileProps.load(fis);
		}

		assertThat(fileProps.containsKey("test.property.one")).as("Removed property should not be in file").isEqualTo(false);
		assertThat(fileProps.getProperty("test.property.two")).as("Other properties should still be in file").isEqualTo("value2");
	}

	/**
	 * Tests behavior when config file doesn't exist. Should not throw an exception, just log a warning.
	 */
	@Test
	void testMissingConfigFile()
	{
		// Delete the test config file
		testConfigFile.delete();

		WritableFileConfigSource source = new WritableFileConfigSource();

		// Should not throw exception, just have empty properties
		Map<String, String> properties = source.getProperties();

		assertThat(properties.entrySet()).as("Properties should be empty when file doesn't exist").hasSize(0);
	}

	/** tests that multiple operations maintain consistency. */
	@Test
	void testMultipleOperations() throws IOException
	{
		WritableFileConfigSource source = new WritableFileConfigSource();

		// Add a property
		source.setProperty("prop.a", "valueA");

		// Update another
		source.setProperty("test.number", "100");

		// Remove one
		source.removeProperty("test.property.two");

		// Verify state
		assertThat(source.getValue("prop.a")).as("New property should exist").isEqualTo("valueA");
		assertThat(source.getValue("test.number")).as("Updated property should have new value").isEqualTo("100");
		assertThat(source.getValue("test.property.two")).as("Removed property should be null").isNull();
		assertThat(source.getValue("test.property.one")).as("Unchanged property should still exist").isEqualTo("value1");

		// Verify file persistence
		Properties fileProps = new Properties();
		try (var fis = new java.io.FileInputStream(testConfigFile))
		{
			fileProps.load(fis);
		}

		assertThat(fileProps.size()).as("File should have 3 properties").isEqualTo(3);
		assertThat(fileProps.getProperty("prop.a")).as("File should contain new property").isEqualTo("valueA");
		assertThat(fileProps.getProperty("test.number")).as("File should contain updated property").isEqualTo("100");
		assertThat(fileProps.containsKey("test.property.two")).as("File should not contain removed property").isEqualTo(false);
	}
}
