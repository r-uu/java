package de.ruu.lib.fx.control.autocomplete;

import java.util.function.Function;

import javafx.scene.Node;

public abstract class FunctionCountryGraphicsProvider
{
	public final static Function<Country, Node> FUNCTION_COUNTRY_GRAPHICS_PROVIDER =
			country -> getCountryGraphics(country);

	private static Node getCountryGraphics(Country country) { return null; }
}