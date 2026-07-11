package de.ruu.lib.fx.control.autocomplete.textfield;

import de.ruu.lib.fx.control.autocomplete.Country;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import static de.ruu.lib.fx.control.autocomplete.BiPredicateCountrySuggestion.BIPREDICATE_COUNTRY_SUGGESTION;
import static de.ruu.lib.fx.control.autocomplete.ComparatorCountry.COMPARATOR_COUNTRY;
import static de.ruu.lib.fx.control.autocomplete.FunctionCountryGraphicsProvider.FUNCTION_COUNTRY_GRAPHICS_PROVIDER;
import static de.ruu.lib.fx.control.autocomplete.FunctionCountryTextProvider.FUNCTION_COUNTRY_TEXT_PROVIDER;
import static de.ruu.lib.fx.control.autocomplete.FunctionCountryToolTipProvider.FUNCTION_TOOL_TIP_PROVIDER;
import static java.util.Objects.isNull;
import static javafx.scene.layout.Priority.ALWAYS;

public class TextFieldAutoCompleteClearableCountryDemo extends Application
{
	@Override	public void start(Stage stage)
	{
		TextFieldAutoCompleteClearable<Country> textField =
				TextFieldAutoCompleteClearableBuilder.<Country>create()
						.items           (Country.countries())
						.suggestionFilter(BIPREDICATE_COUNTRY_SUGGESTION)
						.comparator      (COMPARATOR_COUNTRY)
						.graphicsProvider(FUNCTION_COUNTRY_GRAPHICS_PROVIDER)
						.textProvider    (FUNCTION_COUNTRY_TEXT_PROVIDER)
						.toolTipProvider (FUNCTION_TOOL_TIP_PROVIDER)
						.prompt          ("select country")
						.build();

		textField.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(textField, ALWAYS);

		Button button = new Button("show text field value");

		Label label = new Label();
		label.setPrefHeight(25);
		label.setPrefWidth(200);

		HBox root = new HBox();
		root.getChildren().add(textField);
		root.getChildren().add(button);
		root.getChildren().add(label);
		root.setSpacing(10);

		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.show();

		HBox.setHgrow(root, Priority.ALWAYS);

		button.setOnAction
		(
				e -> buttonOnAction(textField, label)
		);

		button.requestFocus(); // focus anything outside the combo box
	}

	private void buttonOnAction(TextFieldAutoCompleteClearable<Country> textField, Label label)
	{
		Country country = textField.valueProperty().get();
		if (isNull(country)) label.setText("null");
		else                 label.setText(country.getName());
	}
}