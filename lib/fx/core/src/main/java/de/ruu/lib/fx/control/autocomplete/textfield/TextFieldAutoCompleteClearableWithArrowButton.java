package de.ruu.lib.fx.control.autocomplete.textfield;

import static de.ruu.lib.util.BooleanFunctions.not;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

import de.ruu.lib.util.Strings;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextFieldAutoCompleteClearableWithArrowButton<T> extends TextFieldAutoCompleteClearable<T>
{
	private static final Logger log = LoggerFactory.getLogger(TextFieldAutoCompleteClearableWithArrowButton.class);

	private final Button button = new Button("▼");

	public TextFieldAutoCompleteClearableWithArrowButton(List<T> items, @NonNull BiPredicate<T, String> suggestionFilter,
			Comparator<T> comparator, Function<T, Node> graphicsProvider, Function<T, String> textProvider,
			Function<T, Tooltip> toolTipProvider, String promptText) {
		super(items, suggestionFilter, comparator/* , converterTest */, graphicsProvider, textProvider, toolTipProvider,
				promptText);

		/**
		 * Arrow button (▼) configuration: Styled identically to the clear button (✕) for visual consistency. Shown when
		 * text field is empty and suggestions are available, allowing users to see all available options by clicking the
		 * arrow.
		 */
		button.setFocusTraversable(false);
		button.setBackground(Background.EMPTY);
		button.setTextFill(Color.GRAY);
		button.setFont(Font.font(null, FontWeight.NORMAL, Font.getDefault().getSize() * 0.8));
		button.setOnAction(e -> onArrowButtonClicked());

		/**
		 * Position arrow button at exact same location as clear button. Both buttons occupy the same space (CENTER_RIGHT
		 * with 2px margin), but only one is visible at a time based on text field content.
		 */
		StackPane.setAlignment(button, Pos.CENTER_RIGHT);
		StackPane.setMargin(button, new Insets(0, 2, 0, 0));

		/**
		 * Mutual exclusion: arrow button visible ⟷ text field empty AND items available. Binds to both textProperty and
		 * itemsProperty to update visibility dynamically.
		 */
		button.visibleProperty().bind(Bindings.createBooleanBinding(() -> textFieldIsEmptyButSuggestionsAreAvailable(),
				textField().textProperty(), itemsProperty()));

		/**
		 * Clear button (✕) visible ⟷ text field NOT empty. Original binding in ClearableTextField is overridden here to
		 * ensure mutual exclusion with arrow button.
		 */
		clearableTextField().button().visibleProperty().bind(Bindings.isNotEmpty(textField().textProperty()));

		/**
		 * Override clear button behavior: 1. Explicitly hide popup first (because autoHide is disabled) 2. Then clear text
		 * field This ensures popup closes on first click even when it has focus, preventing the "click twice" problem where
		 * first click only closes popup.
		 */
		clearableTextField().button().setOnAction(e -> {
			popup.hide();
			textField().clear();
		});

		/**
		 * Add arrow button to ClearableTextField's StackPane (not to parent HBox). This ensures it overlays the text field
		 * at the same layer as clear button, rather than appearing beside the text field.
		 */
		clearableTextField().getChildren().add(button);
	}

	/**
	 * Determines whether arrow button should be visible: - Text field is empty (nothing typed yet or just cleared) -
	 * Items list is not empty (there are suggestions to show)
	 */
	private boolean textFieldIsEmptyButSuggestionsAreAvailable()
	{
		return Strings.isEmptyOrBlank(textField().getText()) && not(items().isEmpty());
	}

	/**
	 * Handles arrow button click: 1. Refresh popup content via populatePopup() to clear any stale filters from previous
	 * text 2. Show popup with all available items (since text field is empty) Without step 1, popup would still show
	 * filtered results from before the clear button was clicked.
	 */
	private void onArrowButtonClicked()
	{
		populatePopup(); // Refresh list based on current text (should be empty when arrow is visible)
		showPopup();
	}
}