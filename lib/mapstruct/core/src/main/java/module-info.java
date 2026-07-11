module de.ruu.lib.mapstruct
{
	exports de.ruu.lib.mapstruct;


	// Make MapStruct available transitively to consumers using its annotations
	requires transitive org.mapstruct;
	requires de.ruu.lib.util;

	opens de.ruu.lib.mapstruct;
}