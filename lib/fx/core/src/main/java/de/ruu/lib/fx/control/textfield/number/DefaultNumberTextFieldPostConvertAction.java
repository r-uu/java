package de.ruu.lib.fx.control.textfield.number;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.ruu.lib.util.BooleanFunctions.not;
import static java.util.Objects.isNull;

public class DefaultNumberTextFieldPostConvertAction implements NumberTextFieldPostConvertAction
{
	private static final Logger log = LoggerFactory.getLogger(DefaultNumberTextFieldPostConvertAction.class);

	private Border  errorBorder;
	private Tooltip tooltip;

	@Override public void accept(TextField textField, NumberFormatException numberFormatException)
	{
		if (isNull(numberFormatException))
		{
			try
			{
				// hide tooltip (if present) from text field
				if (not(isNull(textField.getTooltip()))) textField.getTooltip().hide();
			}
			catch (Exception e)
			{
				String msg = "Not on FX application thread; currentThread = main";
				if (e.getMessage().contains(msg))
				{
					log.warn(msg);
					// swallow
				}
				else
				{
					throw e;
				}
			}

			// remove "special" border and tooltip
			textField.setBorder(null);
			textField.setTooltip(null);
		}
		else
		{
			// create tooltip for text field
			textField.setTooltip(provideTooltip(textField.getText()));

			// show tooltip for text field
			Bounds bounds;
			bounds = textField.getBoundsInLocal();
			bounds = textField.localToScreen(bounds);

			try
			{
				textField
						.getTooltip()
						.show(
								textField,
								bounds.getMaxX(),
								bounds.getMaxY());
			}
			catch (Exception e)
			{
				String msg = "Cannot invoke \"javafx.geometry.Bounds.getMaxX()\" because \"bounds\" is null";

				if (e.getMessage().contains(msg))
				{
					log.warn(msg);
					// swallow
				}
				else
				{
					throw e;
				}
			}

			// set "special border" for text field
			textField.setBorder(provideErrorBorder());
		}
	}

	private Tooltip provideTooltip(String textFieldText)
	{
		if (isNull(tooltip))
		{
			tooltip = new Tooltip("number format error, [" + textFieldText + "] can not be converted to number");
			tooltip.setShowDelay(Duration.millis(200));
			tooltip.setHideDelay(Duration.seconds(2));
		}

		return tooltip;
	}

	private Border provideErrorBorder()
	{
		if (isNull(errorBorder))
		{
			errorBorder =
					new Border(
							new BorderStroke(
									Color.RED,
									BorderStrokeStyle.SOLID,
									new CornerRadii(3),
									new BorderWidths(2),
									new Insets(-2)));
		}

		return errorBorder;
	}
}