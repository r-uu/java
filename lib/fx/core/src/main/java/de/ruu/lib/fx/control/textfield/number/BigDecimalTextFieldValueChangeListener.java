package de.ruu.lib.fx.control.textfield.number;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.BigDecimalStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Optional;

import static de.ruu.lib.util.BooleanFunctions.not;
import static java.util.Objects.isNull;

/**
 * {@link ChangeListener} that attempts to convert the new value in {@link #changed(ObservableValue, String, String)} to
 * {@code BigDecimal} for every invocation. If present it additionally executes a {@link
 * NumberTextFieldPostConvertAction} after the attempt to convert the new value to an integer. If present the post
 * convert action will be executed <b>regardless</b> if conversion succeeded or not.
 * <p>
 * Furthermore, if present <b>and</b> the conversion succeeded, it executes a {@link BigDecimalValidation}.
 */
public class BigDecimalTextFieldValueChangeListener implements ChangeListener<String>
{
	private static final Logger log = LoggerFactory.getLogger(BigDecimalTextFieldValueChangeListener.class);

	private final BigDecimalStringConverter converter = new BigDecimalStringConverter();
	private final TextFormatter<BigDecimal> formatter = new TextFormatter<>(converter);

	private TextField textField;

	private Optional<NumberTextFieldPostConvertAction> postConvertActionOptional = Optional.empty();
	private Optional<BigDecimalValidation>             validationOptional        = Optional.empty();

	public BigDecimalTextFieldValueChangeListener(
			TextField                        textField,
			NumberTextFieldPostConvertAction postConvertAction,
			BigDecimalValidation             validation)
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

		textField.setTextFormatter(formatter);
		textField.textProperty().addListener(this);
	}

	public BigDecimalTextFieldValueChangeListener(
			TextField textField,
			NumberTextFieldPostConvertAction postConverterAction)
	{
		this(textField, postConverterAction, null);
	}

	public BigDecimalTextFieldValueChangeListener(
			TextField textField,
			BigDecimalValidation validation)
	{
		this(textField, null, validation);
	}

	public BigDecimalTextFieldValueChangeListener(TextField textField)
	{
		this(textField, null, null);
	}

	@Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
	{
		BigDecimal bigDecimal = null;
		NumberFormatException numberFormatException = null;

		// try conversion of newValue
		try
		{
			bigDecimal = converter.fromString(newValue);
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
				validationOptional.get().accept(textField, bigDecimal);
			}
		}
	}

	public BigDecimalStringConverter converter() { return converter; }

	public Optional<BigDecimalValidation> validation() { return validationOptional; }

	public boolean currentTextFieldValueValid()
	{
		try
		{
			BigDecimal bigDecimal = converter.fromString(textField.getText());

			if (validation().isPresent())
			{
				return validation().get().predicate().test(bigDecimal);
			}

			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	public Optional<BigDecimal> currentTextFieldValueAsBigDecimal()
	{
		BigDecimal result;
		try
		{
			result = converter.fromString(textField.getText());
		}
		catch (Exception e)
		{
			return Optional.empty();
		}
		return Optional.ofNullable(result);
	}
}