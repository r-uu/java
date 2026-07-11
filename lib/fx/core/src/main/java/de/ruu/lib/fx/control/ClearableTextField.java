package de.ruu.lib.fx.control;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static javafx.scene.layout.Priority.ALWAYS;

public class ClearableTextField extends StackPane
{
	private final TextField textField = new TextField();
	private final Button    button    = new Button("✕");

	public ClearableTextField()
	{
		button.setFocusTraversable(false);
		button.setBackground(Background.EMPTY);
		button.setTextFill(Color.GRAY);
		button.setFont(Font.font(null, FontWeight.BOLD, Font.getDefault().getSize()));
		button.visibleProperty().bind(Bindings.isNotEmpty(textField.textProperty()));
		button.setOnAction(e -> textField.clear());

		StackPane.setAlignment(button, Pos.CENTER_RIGHT);
		StackPane.setMargin   (button, new Insets(0, 2, 0, 0));

		// textField properties
		textField.setMaxWidth(Double.MAX_VALUE);

		setMaxWidth(Double.MAX_VALUE);
		setPadding(new Insets(0, 2, 0, 2));

		HBox.setHgrow(this, ALWAYS);
		HBox.setHgrow(textField, ALWAYS);

		// layout
		getChildren().addAll(textField, button);
	}

	public TextField textField()           { return textField;                       }
	/** Provides access to clear button for subclasses to override behavior or bind visibility */
	public Button    button()              { return button;                          }
	public String    text()                { return textField.getText();             }
	public void      text  (String text)   {        textField.setText(text);         }
	public void      prompt(String prompt) {        textField.setPromptText(prompt); }
}