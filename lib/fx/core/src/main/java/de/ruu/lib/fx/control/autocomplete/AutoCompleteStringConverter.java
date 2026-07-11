package de.ruu.lib.fx.control.autocomplete;

import javafx.util.StringConverter;

import java.util.Collection;
import java.util.function.BiPredicate;
import java.util.function.Function;

import static de.ruu.lib.util.BooleanFunctions.not;
import static java.util.Objects.isNull;

public class AutoCompleteStringConverter<T> extends StringConverter<T>
{
	private Collection<T> items;
	private BiPredicate<T, String> conversionTestPredicate;
	private Function<T, String> stringProvider;

	/**
	 * @param items list of <code>item</code>s
	 * @param conversionTestPredicate returns <code>true</code> if <code>string</code> identifies an <code>item</code>
	 * @param stringProvider provides <code>string</code> for <code>item</code>
	 */
	public AutoCompleteStringConverter(
			final Collection<T> items,
			final BiPredicate<T, String> conversionTestPredicate,
			Function<T, String> stringProvider)
	{
		this.items = items;
		this.conversionTestPredicate = conversionTestPredicate;
		if (not(isNull(stringProvider))) this.stringProvider = stringProvider;
		else stringProvider = item -> item == null ? null : item.toString();
	}

	AutoCompleteStringConverter(final Collection<T> items,final BiPredicate<T, String> conversionTestPredicate)
	{
		this(items, conversionTestPredicate, null);
	}

	@Override public String toString(final T item) { return stringProvider.apply(item); }

	@Override public T fromString(final String string)
	{
		for (final T item : items)
		{
			if (conversionTestPredicate.test(item, string)) return item;
		}
		return null;
	}
}