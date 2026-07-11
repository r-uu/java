package de.ruu.lib.fx.control.autocomplete.textfield.v2;

import de.ruu.lib.fx.control.ClearableTextField;
import de.ruu.lib.fx.control.autocomplete.AutoCompleteStringConverter;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;

import static de.ruu.lib.util.BooleanFunctions.not;
import static java.util.Objects.isNull;

public class AutoCompleteTextField<T> extends StackPane
{
	private static final Logger log = LoggerFactory.getLogger(AutoCompleteTextField.class);

	/** stores the current value of an instance at all time, accessible for clients via {@link #valueProperty()} */
	private final ObjectProperty<T> value = new SimpleObjectProperty<>();

	private final ClearableTextField textField = new ClearableTextField();
	private final ContextMenu        popup     = new ContextMenu();

 	private final ObservableList<T>      items;
 	private final BiPredicate<T, String > suggestionFilter;
	private final BiPredicate<T, String > converterTest;
	private final Function   <T, Node   > graphicsProvider;
	private final Function   <T, String > textProvider;
	private final Function   <T, Tooltip> toolTipProvider;

	private final AutoCompleteStringConverter<T> converter;

	public AutoCompleteTextField(
                 List       <T         > items,
        @NonNull BiPredicate<T, String > suggestionFilter,
        @NonNull BiPredicate<T, String > converterTest,
                 Function   <T, Node   > graphicsProvider,
                 Function   <T, String > textProvider,
                 Function   <T, Tooltip> toolTipProvider,
                 String                  promptText)
  {
		if (isNull(items           )) items            = new ArrayList<>();
		if (isNull(graphicsProvider)) graphicsProvider = item -> null;
		if (isNull(textProvider    )) textProvider     = item -> isNull(item) ? "" : item.toString();
		if (isNull(toolTipProvider )) toolTipProvider  = item -> null;
		if (isNull(promptText      )) promptText       = "";


		this.items            = FXCollections.observableArrayList(items);
		this.suggestionFilter = suggestionFilter;
		this.converterTest    = converterTest;
		this.graphicsProvider = graphicsProvider;
		this.textProvider     = textProvider;
		this.toolTipProvider  = toolTipProvider;

		converter = new AutoCompleteStringConverter<>(items, converterTest, textProvider);

		textField.textField().setPromptText(promptText);

		// ensure textField and suggestionsPopup resize with the control
		textField.prefWidthProperty().bind(widthProperty());
		popup    .prefWidthProperty().bind(widthProperty());

		setupListeners();

		getChildren().add(textField);
	}

	public ObjectProperty<T> valueProperty() { return value; }
	public void              value(final T value)
	{
		if (value == null) this.value.set(null);
		else if (items.contains(value)) this.value.set(value);
		else log.warn("value not contained in items, did not set value to " + value);
	}

	public List<T> items() { return items; }
	public void    items(final List<T> items)
	{
		this.items.clear();
		this.items.addAll(items);
		populatePopup();

		if (items.size() == 1)
		{
//            listView.getSelectionModel().selectFirst();
//            commitListViewSelection();
//			commitTextField();
		}
	}

	public ClearableTextField      textField() { return textField; }
	public          TextField innerTextField() { return textField.textField(); }

	private void setupListeners()
	{
		innerTextField().textProperty().addListener((obs, old, act) -> innerTextFieldListener(act));

		textField.focusedProperty().addListener
		(
				(obs, wasFocused, isNowFocused) ->
				{
					if (not(isNowFocused)) popup.hide();
				}
		);
	}

	private void innerTextFieldListener(String act)
	{
		if (isNull(act) || act.isEmpty())
		{
			popup.hide();
			value(null);
			return;
		}

		// there is some text in the text field
		populatePopup();
		if (!popup.isShowing())
		{
			popup.show(textField, javafx.geometry.Side.BOTTOM, 0, 0);
		}
		else
				popup.hide();
	}

	private void populatePopup()
	{
		Set<T> filteredItems = new HashSet<>();

		for (T item : items)
		{
			if (suggestionFilter.test(item, textField.text()))
					filteredItems.add(item);
		}

		List<CustomMenuItem> menuItems = new ArrayList<>();

		for (T filteredItem : filteredItems)
		{
			String         text       = textProvider.apply(filteredItem);
			Label          entryLabel = new Label(text);
			CustomMenuItem item       = new CustomMenuItem(entryLabel, true);
			item.setOnAction(e ->
			{
				textField.text(converter.toString(filteredItem));
				value(filteredItem); // set value
				popup.hide();
			});
			menuItems.add(item);
		}

		popup.getItems().setAll(menuItems);

		if (not(menuItems.isEmpty()))
		{
			Platform.runLater(() ->
			{
//				Node node = menuItems.get(0).getContent();
				Node node = popup.getSkin().getNode().lookup(".menu-item");
				node.requestFocus();  // Focuses the Label inside the CustomMenuItem
			});
		}
//		popup.setOnShowing(event -> {
//			Platform.runLater(() -> {
//				Node firstItem = popup.getSkin().getNode().lookup(".menu-item");
//				if (firstItem != null) {
//					firstItem.requestFocus();
//				}
//			});
//		});

	}
}