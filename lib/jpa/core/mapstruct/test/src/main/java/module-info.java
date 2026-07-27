module de.ruu.lib.jpa.core.mapstruct.test
{
	exports de.ruu.lib.jpa.core.mapstruct.test;

	requires static java.compiler;

	requires org.apache.logging.log4j;
	requires java.desktop; // for java.beans used by AbstractMappedDTO/Entity

	requires de.ruu.lib.jpa.core;
	requires de.ruu.lib.jpa.core.mapstruct;
	requires de.ruu.lib.mapstruct;
	requires de.ruu.lib.util;

	opens de.ruu.lib.jpa.core.mapstruct.test to org.mapstruct;
}
