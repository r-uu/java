module de.ruu.lib.jpa.se.hibernate
{
	exports de.ruu.lib.jpa.se.hibernate;

	requires de.ruu.lib.jpa.core;
	requires java.sql;
	requires java.desktop;
	requires jakarta.persistence;
	requires org.hibernate.orm.core;

	requires org.slf4j;
}