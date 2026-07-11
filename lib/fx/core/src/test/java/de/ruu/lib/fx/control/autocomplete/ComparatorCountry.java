package de.ruu.lib.fx.control.autocomplete;

import java.util.Comparator;

public abstract class ComparatorCountry
{
	public final static Comparator<Country> COMPARATOR_COUNTRY =
			(c1, c2) -> compare(c1, c2);

	private static int compare(Country c1, Country c2)
	{
		if (c1 == null && c2 == null) return 0;
		if (c1 == null) return -1;
		if (c2 == null) return 1;
		return c1.getName().compareToIgnoreCase(c2.getName());
	}
}