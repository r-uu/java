package de.ruu.lib.fx.control.autocomplete;

import java.util.function.BiPredicate;

public abstract class BiPredicateCountryConverter
{
	public final static BiPredicate<Country, String> BIPREDICATE_COUNTRY_CONVERTER =
			(country, text) -> doesTextIdentifyCountry(country, text);

	private static boolean doesTextIdentifyCountry(Country country, String text)
	{
		return country.getName().equals(text);
	}
}