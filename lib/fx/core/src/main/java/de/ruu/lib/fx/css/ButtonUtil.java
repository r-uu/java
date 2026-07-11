package de.ruu.lib.fx.css;

import javafx.scene.control.Button;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public class ButtonUtil
{
	@NonNull private final Button button;

	public ButtonUtil(@NonNull Button button)
	{
		this.button = Objects.requireNonNull(button, "button");
	}

	public void backgroundColor(Color color)
	{
		button.setStyle(ButtonProperties.BACKGROUND_COLOR.value + ": " + color.value);
	}

	public static void backgroundColor(Button button, Color color)
	{
		button.setStyle(ButtonProperties.BACKGROUND_COLOR.value + ": " + color.value);
	}
}
