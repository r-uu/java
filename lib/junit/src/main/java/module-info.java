module de.ruu.lib.junit
{
	exports de.ruu.lib.junit;

	requires jakarta.inject;
	requires java.desktop;
	requires org.junit.platform.commons;
	requires org.apache.logging.log4j;
	requires org.eclipse.microprofile.config;
	requires de.ruu.lib.util;

	requires org.junit.jupiter.api;
}