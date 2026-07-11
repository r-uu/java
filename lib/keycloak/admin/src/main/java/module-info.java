/**
 * Keycloak Admin Client module for user and realm management.
 */
module de.ruu.lib.keycloak.admin
{
	requires keycloak.admin.client;
	requires keycloak.client.common.synced;
	requires jakarta.ws.rs;
	requires jakarta.xml.bind;
	requires transitive org.glassfish.jaxb.runtime;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.datatype.jdk8;
	requires com.fasterxml.jackson.datatype.jsr310;
	requires org.jspecify;
	requires org.slf4j;

	exports de.ruu.lib.keycloak.admin;
	exports de.ruu.lib.keycloak.admin.setup;
	exports de.ruu.lib.keycloak.admin.validation;
}
