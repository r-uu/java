module de.ruu.lib.jpa.core.mapstruct {
	exports de.ruu.lib.jpa.core.mapstruct;

	requires static java.compiler; // needed for MapStruct generated code

	requires org.slf4j;

	requires de.ruu.lib.jpa.core;
	requires de.ruu.lib.mapstruct;
	requires de.ruu.lib.util;
	requires java.desktop; // for java.beans used by SimpleMappedDTO/Entity

	opens de.ruu.lib.jpa.core.mapstruct to org.mapstruct;
}