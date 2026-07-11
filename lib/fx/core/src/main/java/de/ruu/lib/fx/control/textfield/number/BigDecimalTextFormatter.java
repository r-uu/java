package de.ruu.lib.fx.control.textfield.number;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;

public class BigDecimalTextFormatter
{
//	private final static Pattern PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");
	private final static Pattern PATTERN = Pattern.compile("-?\\d+(,\\d+)?");
	private final static UnaryOperator<Change> FILTER =
			change ->
			{
				String text = change.getText().replace('.', ',');

				if (PATTERN.matcher(text).matches())
				{
					return change;
				}
				else
				{
					return null;
				}
			};

	/**
	 * @return formatter that allows
	 * <p>10.0 and
	 * <p>-10.0 but disallows
	 * <p>10.000,0
	 */
	public final static TextFormatter<String> formatter()
	{
		return new TextFormatter<>(FILTER);
	}
}