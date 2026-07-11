package de.ruu.lib.util.config.mp;

import java.io.File;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for initializing and managing configuration files with default values.
 * <p>
 * This class provides convenient methods to:
 * <ul>
 *   <li>create configuration files if they don't exist
 *   <li>populate them with default values
 *   <li>ensure all required properties are present without overwriting existing values
 * </ul>
 */
public class ConfigFileInitializer
{
	private static final Logger log = LoggerFactory.getLogger(ConfigFileInitializer.class);

	/**
	 * Creates and initializes a config file with default values if it doesn't exist.
	 *
	 * <p>
	 * If the file already exists, only missing properties are added with default values. Existing properties are
	 * preserved.
	 *
	 * @param configFilePath Path where the config file should be created (e.g., "postgresutil.config.properties")
	 * @param defaultValues Map of property keys to default values
	 * @return WritableFileConfigSource instance pointing to the config file
	 *
	 * @example
	 *
	 * <pre>
	 *   {@code
	 *     Map<String, String> defaults = new HashMap<>();
	 *     defaults.put("postgres.host", "localhost");
	 *     defaults.put("postgres.port", "5432");
	 *
	 *     WritableFileConfigSource config =
	 *         ConfigFileInitializer.initializeConfigFile("postgresutil.config.properties", defaults);
	 *   }
	 * </pre>
	 */
	public static WritableFileConfigSource initializeConfigFile(String configFilePath, Map<String, String> defaultValues)
	{
		File configFile = new File(configFilePath);
		boolean fileExisted = configFile.exists();

		if (fileExisted)
		{
			log.info("config file already exists at: {}", configFile.getAbsolutePath());
		}
		else
		{
			log.info("creating new config file at: {}", configFile.getAbsolutePath());
		}

		// Set system property to tell WritableFileConfigSource where to find/create the file
		System.setProperty(WritableFileConfigSource.CONFIG_FILE_NAME_KEY, configFilePath);

		// Create config source (will load existing file or start with empty properties)
		WritableFileConfigSource config = new WritableFileConfigSource();

		// Set default values (only for properties that don't exist)
		int addedCount = 0;
		for (Map.Entry<String, String> entry : defaultValues.entrySet())
		{
			if (config.getValue(entry.getKey()) == null)
			{
				config.setProperty(entry.getKey(), entry.getValue());
				addedCount++;
				log.debug("added default property: {} = {}", entry.getKey(), entry.getValue());
			}
			else
			{
				log.debug("property already exists, keeping existing value: {}", entry.getKey());
			}
		}

		// Ensure file is created even if no defaults were added
		if (!fileExisted && !configFile.exists())
		{
			// Trigger ensureLoaded by accessing properties, then save
			config.getPropertyNames(); // This calls ensureLoaded()
			config.save();
		}

		if (fileExisted)
		{
			log.info("updated config file: added {} new default properties", addedCount);
		}
		else
		{
			log.info("created config file with {} default properties", addedCount);
		}

		return config;
	}

	/**
	 * Updates an existing config file to ensure all required properties exist.
	 *
	 * <p>
	 * This is useful for migrations when new properties are added to your application.
	 *
	 * @param config Existing WritableFileConfigSource
	 * @param requiredDefaults Map of property keys to default values that must exist
	 * @return Number of properties that were added
	 */
	public static int ensureRequiredProperties(WritableFileConfigSource config, Map<String, String> requiredDefaults)
	{
		int addedCount = 0;

		for (Map.Entry<String, String> entry : requiredDefaults.entrySet())
		{
			if (config.getValue(entry.getKey()) == null)
			{
				config.setProperty(entry.getKey(), entry.getValue());
				addedCount++;
				log.info("Added missing required property: {} = {}", entry.getKey(), entry.getValue());
			}
		}

		if (addedCount > 0)
		{
			log.info("Added {} missing required properties", addedCount);
		}

		return addedCount;
	}
}
