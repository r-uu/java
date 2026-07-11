module de.ruu.lib.docker.health
{
	requires org.slf4j;
	requires java.sql;
	requires org.postgresql.jdbc;  // PostgreSQL JDBC driver (automatic module)
	requires de.ruu.lib.util.config.mp;
	requires static de.ruu.lib.keycloak.admin;  // Optional - nur für Auto-Fix Strategy

	exports de.ruu.lib.docker.health;
	exports de.ruu.lib.docker.health.check;
	exports de.ruu.lib.docker.health.fix;
}