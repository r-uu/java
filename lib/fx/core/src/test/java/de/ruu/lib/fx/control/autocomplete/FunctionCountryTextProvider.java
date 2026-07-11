package de.ruu.lib.fx.control.autocomplete;

import java.util.function.Function;

public abstract class FunctionCountryTextProvider
{
	public final static Function<Country, String> FUNCTION_COUNTRY_TEXT_PROVIDER =
			country -> getCountryName(country);

	private static String getCountryName(Country country)
	{
		if (country == null) return "";
		return country.getName();
	}
}