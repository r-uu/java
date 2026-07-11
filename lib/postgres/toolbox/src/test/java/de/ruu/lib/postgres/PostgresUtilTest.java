package de.ruu.lib.postgres;

import de.ruu.lib.util.config.mp.WritableFileConfigSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class PostgresUtilTest
{
	@TempDir Path tempDir;

	/** Tests the postgres-specific initialization with default path. */
	@Test
	void testInitializePostgresUtilConfigDefaultPath()
	{
		WritableFileConfigSource config = PostgresToolBox.initializePostgresUtilConfig();

		File configFile = new File("postgresutil.config.properties");

		try
		{
			assertThat(configFile.exists()).as("Config file should be created").isEqualTo(true);
			assertThat(config.getPropertyNames().size()).as("Should have all postgres properties").isGreaterThanOrEqualTo(9);

			// Check some default values
			assertThat(config.getValue("postgres.host")).as("Should have default host").isEqualTo("localhost");
			assertThat(config.getValue("postgres.port")).as("Should have default port").isEqualTo("5432");
			assertThat(config.getValue("postgres.database")).as("Should have default database").isEqualTo("mydb");
			assertThat(config.getValue("postgres.username")).as("Should have default username").isEqualTo("admin");
			assertThat(config.getValue("postgres.schema")).as("Should have default schema").isEqualTo("public");
			assertThat(config.getValue("postgres.ssl.enabled")).as("Should have default SSL setting").isEqualTo("false");
		}
		finally
		{
			configFile.delete();
		}
	}

	/**
	 * Tests the postgres-specific initialization with custom path.
	 */
	@Test
	void testInitializePostgresUtilConfigCustomPath()
	{
		File configFile = tempDir.resolve("custom-postgres.config").toFile();

		WritableFileConfigSource config = PostgresToolBox.initializePostgresUtilConfig(configFile.getAbsolutePath());

		assertThat(configFile.exists()).as("Config file should be created at custom path").isEqualTo(true);
		assertThat(config.getPropertyNames().size()).as("Should have all postgres properties").isGreaterThanOrEqualTo(9);

		// Verify it's a valid postgres config
		assertThat(config.getValue("postgres.host")).as("Should have postgres.host property").isNotNull();
		assertThat(config.getValue("postgres.port")).as("Should have postgres.port property").isNotNull();
	}
}
