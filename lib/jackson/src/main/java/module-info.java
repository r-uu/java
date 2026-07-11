module de.ruu.lib.jackson
{
	exports de.ruu.lib.jackson;

	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.datatype.jsr310;
	requires jakarta.ws.rs;
	opens de.ruu.lib.jackson to com.fasterxml.jackson.databind;
}