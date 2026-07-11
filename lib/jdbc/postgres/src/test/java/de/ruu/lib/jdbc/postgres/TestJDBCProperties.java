package de.ruu.lib.jdbc.postgres;

import org.junit.jupiter.api.Test;
import org.postgresql.Driver;

import static org.assertj.core.api.Assertions.assertThat;

class TestJDBCProperties
{
	@Test void test()
	{
		String host     = "localhost";
		int    port     = 5432;
		String database = "lib_test";

		JDBCProperties jdbcProperties =
				new JDBCProperties(
						new JDBCURL(host, port, database), database, database);

		assertThat(jdbcProperties).isNotNull();

		assertThat(jdbcProperties.user()).isEqualTo(database);
		assertThat(jdbcProperties.password()).isEqualTo(database);
		assertThat(jdbcProperties.driver()).isEqualTo(Driver.class.getName());
	}
}