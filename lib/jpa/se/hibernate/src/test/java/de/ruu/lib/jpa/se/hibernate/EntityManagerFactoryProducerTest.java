package de.ruu.lib.jpa.se.hibernate;

import de.ruu.lib.jdbc.postgres.DataSourceFactory;
import de.ruu.lib.jdbc.postgres.JDBCURL;
import de.ruu.lib.junit.DisabledOnServerNotListening;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.Connection;

import static de.ruu.lib.jpa.se.hibernate.PersistenceUnitProperties.HBM2DLLAuto.CREATE;
import static org.assertj.core.api.Assertions.assertThat;

class EntityManagerFactoryProducerTest
{
	private static final Logger log = LoggerFactory.getLogger(EntityManagerFactoryProducerTest.class);

	@DisabledOnServerNotListening(propertyNameHost = "database.host", propertyNamePort = "database.port")
	@Test void entityManagerFactoryProducer() throws SQLException
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

		String persistenceUnitName = "lib_test";

		JDBCURL jdbcURL = new JDBCURL(databaseHost, databasePort, databaseName);

		log.debug("jdbc url  : {}", jdbcURL);

		// Pre-check: credentials valid? If not, skip test (assumption)
		try (Connection ignored = DriverManager.getConnection(jdbcURL.asString(), databaseUser, databasePass)) {
			log.debug("credential pre-check succeeded for user={} db={}", databaseUser, databaseName);
		} catch (SQLException e) {
			String state = e.getSQLState();
			String msg = e.getMessage() == null ? "" : e.getMessage();
			if ("28P01".equals(state) || msg.toLowerCase().contains("password authentication failed")) {
				Assumptions.assumeTrue(false, () -> "Skipping: credentials invalid for user '" + databaseUser + "' on '" + jdbcURL.asString() + "'");
			}
			throw e;
		}

		DataSource dataSource = new DataSourceFactory(jdbcURL, databaseUser, databasePass).create();

		log.debug("datasource: {}", dataSource);

		// save persistence unit info with hibernate as persistence provider
		PersistenceUnitInfo persistenceUnitInfo = new PersistenceUnitInfo(persistenceUnitName, dataSource);

		log.debug("persistence unit info\n{}", persistenceUnitInfo);

		// customise hibernate specifics
		PersistenceUnitProperties hibernateProperties =
				PersistenceUnitProperties
						.builder()
//								.dialect(PostgreSQLDialect.class)
								.formatSQL(true)
								.hbm2ddlAuto(CREATE)
								.jdbcDriver(org.postgresql.Driver.class)
								.jdbcURL(jdbcURL.asString())
								.showSQL(true)
						.build();

		log.debug("persistence unit properties\n{}", hibernateProperties);

		EntityManagerFactoryProducer producer =
				new EntityManagerFactoryProducer(persistenceUnitInfo, hibernateProperties);

		log.debug("entity manager factory producer\n{}", producer);

		try (EntityManagerFactory entityManagerFactory = producer.produce(databaseUser, databasePass))
		{
			assertThat(entityManagerFactory).isNotNull();

			try (EntityManager entityManager = entityManagerFactory.createEntityManager())
			{
				assertThat(entityManager).isNotNull();
				assertThat(entityManager.getTransaction()).isNotNull();
			}
		}
	}
}