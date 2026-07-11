package de.ruu.lib.fx.control.textfield.number;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.IntegerStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static de.ruu.lib.util.BooleanFunctions.not;
import static java.util.Objects.isNull;

/**
 * {@link ChangeListener} that attempts to convert the new value in {@link #changed(ObservableValue, String, String)} to
 * {@code Integer} for every invocation. If present it additionally executes a {@link NumberTextFieldPostConvertAction}
 * after the attempt to convert the new value to an integer. If present the post convert action will be executed
 * <b>regardless</b> if conversion succeeded or not.
 * <p>
 * Furthermore, if present <b>and</b> the conversion succeeded, it executes an {@link IntegerValidation}.
 */
public class IntegerTextFieldValueChangeListener implements ChangeListener<String>
{
	private static final Logger log = LoggerFactory.getLogger(IntegerTextFieldValueChangeListener.class);

	private final IntegerStringConverter converter = new IntegerStringConverter();
	private final TextFormatter<Integer> formatter = new TextFormatter<>(converter);

	private TextField textField;

	private Optional<NumberTextFieldPostConvertAction> postConvertActionOptional = Optional.empty();
	private Optional<IntegerValidation>                validationOptional        = Optional.empty();

	public IntegerTextFieldValueChangeListener(
			TextField                        textField,
			NumberTextFieldPostConvertAction postConvertAction,
			IntegerValidation                validation)
	{
		if (isNull(textField)) throw new IllegalArgumentException("textField must not be null");

		this.textField                 = textField;
		this.postConvertActionOptional = Optional.ofNullable(postConvertAction);
		this.validationOptional        = Optional.ofNullable(validation);

			// put this instance in the properties of the text field
			Object previousValue = textField.getProperties().put(getClass().getName(), this);

			if (not(isNull(previousValue)))
			{
				log.warn("replacing previous value change listener property in text field");
			}
//		}

		textField.setTextFormatter(formatter);
		textField.textProperty().addListener(this);
	}

	public IntegerTextFieldValueChangeListener(
			TextField textField,
			NumberTextFieldPostConvertAction postConverterAction)
	{
		this(textField, postConverterAction, null);
	}

	public IntegerTextFieldValueChangeListener(
			TextField textField,
			IntegerValidation integerValidation)
	{
		this(textField, null, integerValidation);
	}

	public IntegerTextFieldValueChangeListener(TextField textField)
	{
		this(textField, null, null);
	}

	@Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
	{
		Integer integer = null;
		NumberFormatException numberFormatException = null;

		// try conversion of newValue
		try
		{
			integer = converter.fromString(newValue);
		}
		catch (NumberFormatException e)
		{
			numberFormatException = e;
		}

		if (postConvertActionOptional.isPresent())
		{
			// execute post converter action
			postConvertActionOptional.get().accept(textField, numberFormatException);
		}

		if (isNull(numberFormatException))
		{
			if (validationOptional.isPresent())
			{
				// execute validation and post validation action if the latter is available
				validationOptional.get().accept(textField, integer);
			}
		}
	}

	public IntegerStringConverter converter() { return converter; }

	public Optional<IntegerValidation> validation() { return validationOptional; }

	public boolean currentTextFieldValueValid()
	{
		try
		{
			Integer integer = converter.fromString(textField.getText());

			if (validation().isPresent())
			{
				return validation().get().predicate().test(integer);
			}

			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	public Optional<Integer> currentTextFieldValueAsInteger()
	{
		Integer integer;
		try
		{
			integer = converter.fromString(textField.getText());
		}
		catch (Exception e)
		{
			return Optional.empty();
		}
		return Optional.ofNullable(integer);
	}
}