package de.ruu.lib.fx.control.textfield.number;

import java.math.BigDecimal;
import java.util.function.Predicate;

import javafx.scene.control.TextField;

public abstract class NumberTextFieldConfigurator
{
	public static void configureIntegerTextField(
			TextField                        textField,
			Predicate<Integer>               predicate,
			NumberTextFieldPostConvertAction postConvertAction,
			TextFieldPostValidateAction      postValidateAction)
	{
		textField
				.textProperty()
				.addListener
				(
						createIntegerTextFieldValueChangedListener(
								textField, predicate, postConvertAction, postValidateAction)
				);
	}

	public static void configureIntegerTextField(
			TextField textField,
			NumberTextFieldPostConvertAction postConvertAction)
	{
		textField
				.textProperty()
				.addListener(new IntegerTextFieldValueChangeListener(textField, postConvertAction));
	}

	public static void configureIntegerTextField(TextField textField)
	{
		textField
				.textProperty()
				.addListener(new IntegerTextFieldValueChangeListener(textField));
	}

	public static void configureBigDecimalTextField(
			TextField                        textField,
			Predicate<BigDecimal>            predicate,
			NumberTextFieldPostConvertAction postConvertAction,
			TextFieldPostValidateAction      postValidateAction)
	{
		textField
				.textProperty()
				.addListener
				(
						createBigDecimalTextFieldValueChangedListener(
								textField, predicate, postConvertAction, postValidateAction)
				);
	}

	private static IntegerTextFieldValueChangeListener createIntegerTextFieldValueChangedListener(
			TextField                        textField,
			Predicate<Integer>               predicate,
			NumberTextFieldPostConvertAction postConvertAction,
			TextFieldPostValidateAction      postValidateAction)
	{
		return new IntegerTextFieldValueChangeListener
		(
				textField,
				postConvertAction,
				new IntegerValidation(
						predicate,
						postValidateAction)
		);
	}

	private static BigDecimalTextFieldValueChangeListener createBigDecimalTextFieldValueChangedListener(
			TextField                        textField,
			Predicate<BigDecimal>            predicate,
			NumberTextFieldPostConvertAction postConvertAction,
			TextFieldPostValidateAction      postValidateAction)
	{
		return new BigDecimalTextFieldValueChangeListener
		(
				textField,
				postConvertAction,
				new BigDecimalValidation(
						predicate,
						postValidateAction)
		);
	}
}