package de.ruu.lib.fx.control.autocomplete.textfield.v2;

import de.ruu.lib.fx.control.autocomplete.Country;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import static de.ruu.lib.fx.control.autocomplete.BiPredicateCountryConverter.BIPREDICATE_COUNTRY_CONVERTER;
import static de.ruu.lib.fx.control.autocomplete.BiPredicateCountrySuggestion.BIPREDICATE_COUNTRY_SUGGESTION;
import static de.ruu.lib.fx.control.autocomplete.FunctionCountryGraphicsProvider.FUNCTION_COUNTRY_GRAPHICS_PROVIDER;
import static de.ruu.lib.fx.control.autocomplete.FunctionCountryTextProvider.FUNCTION_COUNTRY_TEXT_PROVIDER;
import static de.ruu.lib.fx.control.autocomplete.FunctionCountryToolTipProvider.FUNCTION_TOOL_TIP_PROVIDER;

public class AutoCompleteTextFieldCountryDemo extends Application
{
    @Override public void start(Stage primaryStage)
    {
        AutoCompleteTextField textField =
            AutoCompleteTextFieldBuilder
                .<Country>create()
                .items(Country.countries())
                .suggestionFilter(BIPREDICATE_COUNTRY_SUGGESTION)
                .converterTest   (BIPREDICATE_COUNTRY_CONVERTER)
                .graphicsProvider(FUNCTION_COUNTRY_GRAPHICS_PROVIDER)
                .textProvider    (FUNCTION_COUNTRY_TEXT_PROVIDER)
                .toolTipProvider (FUNCTION_TOOL_TIP_PROVIDER)
                .prompt          ("select country")
                .build();

        VBox root = new VBox(10, textField);
        root.setStyle("-fx-padding: 20;");

        Scene scene = new Scene(root, 350, 120);
        primaryStage.setScene(scene);
        primaryStage.setTitle("TextFieldAutoCompleteClearable Demo");
        primaryStage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}