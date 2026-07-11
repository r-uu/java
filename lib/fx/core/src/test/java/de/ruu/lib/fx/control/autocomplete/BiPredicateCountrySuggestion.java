package de.ruu.lib.fx.control.autocomplete;

import java.util.function.BiPredicate;

public abstract class BiPredicateCountrySuggestion
{
	public final static BiPredicate<Country, String> BIPREDICATE_COUNTRY_SUGGESTION =
			(country, text) -> isCountryValidSuggestionForText(country, text);

	private static boolean isCountryValidSuggestionForText(Country country, String text)
	{
		return country.getName().toLowerCase().contains(text.toLowerCase());
	}
}