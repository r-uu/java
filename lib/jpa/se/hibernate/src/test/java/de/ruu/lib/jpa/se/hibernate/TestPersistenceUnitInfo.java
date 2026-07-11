package de.ruu.lib.jpa.se.hibernate;

import de.ruu.lib.jdbc.postgres.DataSourceFactory;
import de.ruu.lib.jdbc.postgres.JDBCURL;
import org.eclipse.microprofile.config.ConfigProvider;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

class TestPersistenceUnitInfo
{
	@Test void test()
	{
		String databaseHost =
				ConfigProvider
						.getConfig()
						.getOptionalValue("database.host", String .class)
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

		String persistenceUnitName = "modules";

		DataSourceFactory dataSourceFactory = new DataSourceFactory(jdbcURL, databaseUser, databasePass);
		DataSource        dataSource        = dataSourceFactory.create();

		PersistenceUnitInfo persistenceUnitInfo =
				new PersistenceUnitInfo(persistenceUnitName, dataSource);

		assertThat(persistenceUnitInfo.getPersistenceUnitName()).isEqualTo(persistenceUnitName);
		assertThat(persistenceUnitInfo.getPersistenceProvider()).isEqualTo(HibernatePersistenceProvider.class);
	}
}