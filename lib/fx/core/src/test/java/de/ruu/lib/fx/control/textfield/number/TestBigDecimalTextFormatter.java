package de.ruu.lib.fx.control.textfield.number;

import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for BigDecimalTextFormatter.
 * Uses TestFX's ApplicationExtension to initialize JavaFX toolkit.
 */
@ExtendWith(ApplicationExtension.class)
class TestBigDecimalTextFormatter
{
    @BeforeAll
    static void initToolkit()
    {
        // ApplicationExtension handles JavaFX toolkit initialization
        // This method ensures toolkit is ready before any test runs
    }

    @Test void testInteger()
    {
        final Integer integer = 10;
        final TextField textField = new TextField();
        
        textField.setTextFormatter(BigDecimalTextFormatter.formatter());
        textField.setText(integer.toString());
        
        assertThat(Integer.valueOf(textField.getText())).isEqualTo(integer);
    }

    @Test void testIntegerNegative()
    {
        final Integer integer = -10;
        final TextField textField = new TextField();
        
        textField.setTextFormatter(BigDecimalTextFormatter.formatter());
        textField.setText(integer.toString());
        
        assertThat(Integer.valueOf(textField.getText())).isEqualTo(integer);
    }

    @Test void testBigDecimal()
    {
        final BigDecimal bigDecimal = BigDecimal.valueOf(10.0);
        final TextField textField = new TextField();
        
        textField.setTextFormatter(BigDecimalTextFormatter.formatter());
        textField.setText(bigDecimal.toString());
        
        assertThat(bigDecimal.compareTo(new BigDecimal(textField.getText()))).isEqualTo(0);
    }

    @Test void testBigDecimalNegative()
    {
        final BigDecimal bigDecimal = BigDecimal.valueOf(-10.0);
        final TextField textField = new TextField();
        
        textField.setTextFormatter(BigDecimalTextFormatter.formatter());
        textField.setText(bigDecimal.toString());
        
        assertThat(bigDecimal.compareTo(new BigDecimal(textField.getText()))).isEqualTo(0);
    }
}