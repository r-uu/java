/**
 * JSON-B utilities module.
 */
module de.ruu.lib.jsonb
{
	exports de.ruu.lib.jsonb;

	requires de.ruu.lib.util;

	requires java.desktop;
	requires jakarta.json;
	requires jakarta.json.bind;
	requires jakarta.ws.rs;

	// Open for JSON-B serialization/deserialization reflection
	// - org.eclipse.yasson: JSON-B implementation runtime reflection
	opens de.ruu.lib.jsonb.recursion to org.eclipse.yasson;
}