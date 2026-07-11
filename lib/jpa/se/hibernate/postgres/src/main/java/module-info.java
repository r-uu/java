module de.ruu.lib.jpa.se.hibernate.postgres
{
	exports de.ruu.lib.jpa.se.hibernate.postgres;

	// Open for Hibernate persistence provider reflection
	// - org.hibernate.orm.core: entity scanning and persistence operations
	opens de.ruu.lib.jpa.se.hibernate.postgres to org.hibernate.orm.core;

	requires jakarta.annotation;
	requires jakarta.cdi;
	requires jakarta.el;
	requires jakarta.inject;
	requires jakarta.persistence;
	requires java.sql;
	requires org.slf4j;
	requires microprofile.config.api;
	requires org.hibernate.orm.core;
	requires org.postgresql.jdbc;
	requires de.ruu.lib.jdbc.core;
	requires de.ruu.lib.jdbc.postgres;
	requires de.ruu.lib.jpa.core;
	requires de.ruu.lib.jpa.se;
	requires de.ruu.lib.jpa.se.hibernate;

}