package de.ruu.lib.jdbc.postgres;

import de.ruu.lib.junit.DisabledOnServerNotListening;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class TestDataSourceFactory
{
	private static final Logger log = LoggerFactory.getLogger(TestDataSourceFactory.class);

	@DisabledOnServerNotListening(propertyNameHost = "database.host", propertyNamePort = "database.port")
	@Test void testDataSourceFactory()
	{
		String databaseHost =
				ConfigProvider
					.getConfig()
					.getOptionalValue("database.host", String.class)
					.orElse("localhost");
		int    databasePort =
				ConfigProvider
					.getConfig()
					.getOptionalValue("database.port", Integer.class)
					.orElse(5432);
		String databaseName =
				ConfigProvider
					.getConfig()
					.getOptionalValue("database.name", String.class)
					.orElse("lib_test");
		String databaseUser =
				ConfigProvider
					.getConfig()
					.getOptionalValue("database.user", String.class)
					.orElse("lib_test");
		String databasePass =
				ConfigProvider
					.getConfig()
					.getOptionalValue("database.pass", String.class)
					.orElse("lib_test");

		JDBCURL jdbcURL = new JDBCURL(databaseHost, databasePort, databaseName);

		log.debug("jdbc url: {} (user={})", jdbcURL, databaseUser);

		DataSourceFactory dataSourceFactory = new DataSourceFactory(jdbcURL, databaseUser, databasePass);
		DataSource        dataSource        = dataSourceFactory.create();

		assertThat(dataSource).isNotNull();
		try (var connection = dataSource.getConnection())
		{
			assertThat(connection).isNotNull();
		}
		catch (SQLException e)
		{
			org.junit.jupiter.api.Assumptions.assumeTrue(false, "Skipping due to DB connect/auth failure: " + e.getMessage());
		}
	}
}
