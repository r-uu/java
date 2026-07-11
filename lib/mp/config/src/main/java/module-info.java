module de.ruu.lib.util.config.mp
{
	exports de.ruu.lib.util.config.mp;

	provides org.eclipse.microprofile.config.spi.ConfigSource with de.ruu.lib.util.config.mp.WritableFileConfigSource;

	requires de.ruu.lib.util;

	requires transitive jakarta.cdi;
	requires transitive microprofile.config.api;

	requires org.slf4j;
}
