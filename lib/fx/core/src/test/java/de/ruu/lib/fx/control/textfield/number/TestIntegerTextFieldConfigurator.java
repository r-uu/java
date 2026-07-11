package de.ruu.lib.fx.control.textfield.number;

import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.condition.OS.LINUX;

//@DisabledOnOs(LINUX)
@Disabled("find out how to make these tests work")
class TestIntegerTextFieldConfigurator
{
	private TextField textField;

	@BeforeEach public void beforeEach()
	{
		textField = new TextField();
	}

	@Test void testDefaultConfiguratorWith0()
	{
		NumberTextFieldConfigurator.configureIntegerTextField(textField);
		textField.setText("0");
		Optional<Integer> optionalInteger = IntegerTextFieldUtility.currentTextFieldValueAsInteger(textField);
		assertThat(optionalInteger.isPresent()).isEqualTo(true);
		assertThat(optionalInteger.get()).isEqualTo(0);
	}

	@Test void testDefaultConfiguratorWithA()
	{
		NumberTextFieldConfigurator.configureIntegerTextField(textField);
		textField.setText("A");
		Optional<Integer> optionalInteger = IntegerTextFieldUtility.currentTextFieldValueAsInteger(textField);
		assertThat(optionalInteger.isPresent()).isEqualTo(false);
	}

	@Test void testConfiguratorWithConvertAction()
	{
		NumberTextFieldConfigurator.configureIntegerTextField(textField, new DefaultNumberTextFieldPostConvertAction());
		textField.setText("A");
		Optional<Integer> optionalInteger = IntegerTextFieldUtility.currentTextFieldValueAsInteger(textField);
		assertThat(optionalInteger.isPresent()).isEqualTo(false);
	}
}