package de.ruu.lib.fx.control.textfield.number;

import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.util.Optional;

import static de.ruu.lib.util.BooleanFunctions.not;
import static java.util.Objects.isNull;

public abstract class BigDecimalTextFieldUtility
{
	public static Optional<BigDecimalTextFieldValueChangeListener> valueChangeListener(TextField textField)
	{
		Object object = textField.getProperties().get(BigDecimalTextFieldValueChangeListener.class.getName());

		if (not(isNull(object)))
		{
			if (object instanceof BigDecimalTextFieldValueChangeListener)
			{
				return Optional.of((BigDecimalTextFieldValueChangeListener) object);
			}
		}

		return Optional.empty();
	}

	public static Optional<BigDecimalValidation> validation(TextField textField)
	{
		Optional<BigDecimalTextFieldValueChangeListener> listenerOptional = valueChangeListener(textField);

		if (listenerOptional.isPresent())
		{
			return listenerOptional.get().validation();
		}

		return Optional.empty();
	}

	public static Optional<BigDecimal> currentTextFieldValueAsBigDecimal(TextField textField)
	{
		Optional<BigDecimalTextFieldValueChangeListener> listenerOptional = valueChangeListener(textField);

		if (listenerOptional.isPresent())
		{
			return listenerOptional.get().currentTextFieldValueAsBigDecimal();
		}

		return Optional.empty();
	}

	public static boolean isCurrentTextFieldValueValid(TextField textField)
	{
		Optional<BigDecimalTextFieldValueChangeListener> listenerOptional = valueChangeListener(textField);

		if (listenerOptional.isPresent())
		{
			return listenerOptional.get().currentTextFieldValueValid();
		}

		return true;
	}
}