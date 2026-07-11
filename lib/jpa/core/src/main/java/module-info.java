module de.ruu.lib.jpa.core
{
	exports de.ruu.lib.jpa.core;
	exports de.ruu.lib.jpa.core.criteria;

	requires com.fasterxml.jackson.annotation;
	requires de.ruu.lib.util;
	requires jakarta.annotation;
	requires jakarta.json.bind;
	requires jakarta.persistence;
	requires java.sql;
	requires org.jspecify;

	// Opens for reflection by JPA providers (Hibernate) and CDI (Weld)
	// Unrestricted: Liberty opens need no specific module target
	opens de.ruu.lib.jpa.core;
}