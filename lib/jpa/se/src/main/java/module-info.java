module de.ruu.lib.jpa.se
{
	exports de.ruu.lib.jpa.se;

	requires jakarta.annotation;
	requires jakarta.inject;
	requires jakarta.interceptor;
	requires jakarta.persistence;
	requires org.slf4j;
	
	// Open for Hibernate persistence provider reflection
	// Note: org.hibernate.orm.core is a runtime dependency, not compile-time
	// The warning about "module not found" can be ignored as it's only used via service provider
	opens de.ruu.lib.jpa.se to org.hibernate.orm.core;
}