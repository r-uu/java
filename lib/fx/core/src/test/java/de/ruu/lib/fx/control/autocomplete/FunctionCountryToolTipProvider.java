package de.ruu.lib.fx.control.autocomplete;

import javafx.scene.control.Tooltip;

import java.util.function.Function;

public abstract class FunctionCountryToolTipProvider
{
	public final static Function<Country, Tooltip> FUNCTION_TOOL_TIP_PROVIDER =
			country -> new Tooltip(country.getName());
}