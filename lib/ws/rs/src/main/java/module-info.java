module de.ruu.lib.ws.rs
{
	exports de.ruu.lib.ws.rs;
	exports de.ruu.lib.ws.rs.filter.logging;

	opens de.ruu.lib.ws.rs to com.fasterxml.jackson.databind;

	requires transitive jakarta.ws.rs;
	requires de.ruu.lib.util;

	requires org.slf4j;
	requires org.jspecify;
	requires com.fasterxml.jackson.annotation;
	requires static com.fasterxml.jackson.databind;
}