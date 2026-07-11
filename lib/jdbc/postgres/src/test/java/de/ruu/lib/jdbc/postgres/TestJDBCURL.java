package de.ruu.lib.jdbc.postgres;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.microprofile.config.ConfigProvider.getConfig;

class TestJDBCURL
{
	@Test void test()
	{
		String databaseHost = getConfig().getOptionalValue("database.host", String.class).orElse("localhost");
		int    databasePort = getConfig().getOptionalValue("database.port", Integer.class).orElse(5432);
		String databaseName = getConfig().getOptionalValue("database.name", String.class).orElse("lib_test");

		JDBCURL jdbcURL = new JDBCURL(databaseHost, databasePort, databaseName);

		assertThat(jdbcURL).isNotNull();

		assertThat(jdbcURL.host()).isEqualTo(databaseHost);
		assertThat(jdbcURL.port()).isEqualTo(databasePort);
		assertThat(jdbcURL.databaseName()).isEqualTo(databaseName);

		assertThat(jdbcURL.asString()).isEqualTo(JDBCURL.PROTOCOL + "://" + databaseHost + ":" + databasePort + "/" + databaseName);
	}
}