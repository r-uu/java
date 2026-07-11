package de.ruu.lib.fx.control.autocomplete.textfield;

import de.ruu.lib.fx.control.ClearableTextField;
import de.ruu.lib.fx.control.autocomplete.AutoCompleteCellFactory;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.ruu.lib.util.BooleanFunctions.not;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

// public class TextFieldAutoCompleteClearable<T> extends ClearableTextField
public class TextFieldAutoCompleteClearable<T> extends HBox
{
	private static final Logger log = LoggerFactory.getLogger(TextFieldAutoCompleteClearable.class);

	private final ClearableTextField clearableTextField = new ClearableTextField();
	protected final ListView<T> listView = new ListView<>();
	protected final Popup popup = new Popup();

	/** stores the current value of an instance at all time, accessible for clients via {@link #valueProperty()} */
	private final ObjectProperty<T> value = new SimpleObjectProperty<>();

	private final ObservableList<T>      items;
	private final BiPredicate<T, String> suggestionFilter;
	private final Comparator<T>          comparator;
	private final Function<T, Node>      graphicsProvider;
	private final Function<T, String>    textProvider;
	private final Function<T, Tooltip>   toolTipProvider;

	// convenience reference (shortcut) to the text field of the clearable text field
	private final TextField textField = clearableTextField.textField();

	/**
	 * @param suggestionFilter returns <code>true</code> if an item is a valid suggestion for the value of the combo box,
	 * <code>false</code> otherwise
	 * @param graphicsProvider returns a <code>Node</code> to be used as graphic for the item in the list view
	 * @param textProvider returns a <code>String</code> to be used as text for the item in the list view
	 * @param toolTipProvider returns a <code>Tooltip</code> to be used for the item in the list view
	 * @param promptText the text to be shown in the text field when it is empty
	 */
	public TextFieldAutoCompleteClearable
	(
			List<T>                         items           ,
			@NonNull BiPredicate<T, String> suggestionFilter,
			Comparator<T>                   comparator      ,
			Function<T, Node>               graphicsProvider,
			Function<T, String>             textProvider    ,
			Function<T, Tooltip>            toolTipProvider ,
			String                          promptText
	)
	{
		if (isNull(items           )) items = new ArrayList<>();
		if (isNull(comparator      )) comparator = (t1, t2) -> 0; // default comparator that does nothing
		if (isNull(graphicsProvider)) graphicsProvider = item -> null;
		if (isNull(textProvider    )) textProvider = item -> isNull(item) ? "" : item.toString();
		if (isNull(toolTipProvider )) toolTipProvider = item -> null;
		if (isNull(promptText      )) promptText = "";

		this.items            = FXCollections.observableArrayList(items);
		this.suggestionFilter = suggestionFilter;
		this.comparator       = comparator;
		this.graphicsProvider = graphicsProvider;
		this.textProvider     = textProvider;
		this.toolTipProvider  = toolTipProvider;

		value.addListener((obs, old, cur) -> onValueChanged(cur));

		textField.setPromptText(promptText);

		setupPopup();
		setupListeners();

		getChildren().add(clearableTextField);
	}

	/**
	 * Replaces an element in the items list if an element is found that compares to 0 with the new element
	 * using the provided comparator. If no such element is found, the new element is added to the list.
	 *
	 * @param newItem the element to replace with or add to the items list
	 * @param comparator the comparator to be used for comparison of existing and new item
	 * @return true if an existing element was replaced, false otherwise
	 */
	public boolean replaceInItems(@NonNull T newItem, @NonNull Comparator<T> comparator)
	{
		for (int i = 0; i < items.size(); i++)
		{
			T itemsElement = items.get(i);
			if (comparator.compare(itemsElement, newItem) == 0)
			{
				// itemsElement is the element to replace, do replacement
				items.set(i, newItem);

				// if itemsElement is the currently selected element (value.get()), update the text
				// field directly without triggering value change listeners (it's the same item, just
				// updated)
				if (value.get() != null && comparator.compare(value.get(), itemsElement) == 0)
				{
					textField.setText(textProvider.apply(newItem));
					popup.hide();
				}

				return true;
			}
		}
		items.add(newItem);
		return false;
	}

	public void value(final T value)
	{
		     if (value == null)         this.value.set(null);
		else if (items.contains(value)) this.value.set(value);
		else                            log.warn("value not contained in items, did not set value to " + value);
	}

	public ObjectProperty<T> valueProperty()
	{
		return value;
	}

	public void items(List<T> items)
	{
		this.items.setAll(items);
	}

	public T selectedItem()
	{
		return listView.getSelectionModel().getSelectedItem();
	}

	public TextField textField()
	{
		return textField;
	}

	/** Provides access to ClearableTextField for subclasses to add buttons or override behavior */
	public ClearableTextField clearableTextField()
	{
		return clearableTextField;
	}

	/** @return unmodifiable copy of the list of items that are used to populate the list view */
	protected List<T> items()
	{
		return Collections.unmodifiableList(items);
	}

	protected ObservableList<T> itemsProperty()
	{
		return items;
	}

	// private void onValueChanged(final T newValue) { textField().setText(converter.toString(newValue)); }
	/**
	 * Updates the text field when the selected value changes. When value is null (cleared), the text field must be
	 * explicitly cleared instead of calling textProvider.apply(null) which might return unexpected text.
	 */
	private void onValueChanged(final T newValue)
	{
		if (newValue == null)
		{
			clearableTextField.textField().clear();
		}
		else
		{
			clearableTextField.textField().setText(textProvider.apply(newValue));
		}
	}

	private void setupPopup()
	{
		listView.setMaxHeight(150);
		// Bind ListView width to TextField width for consistent popup sizing
		listView.minWidthProperty().bind(textField.widthProperty());
		listView.maxWidthProperty().bind(textField.widthProperty());
		listView.setFocusTraversable(false);
		listView.setCellFactory(new AutoCompleteCellFactory<>(graphicsProvider, textProvider, toolTipProvider));

		/**
		 * Popup configuration: autoHide is disabled to prevent click event consumption. With autoHide(true), the first
		 * click outside the popup would close it but consume the click, preventing buttons like the clear button from
		 * receiving their click events. Instead, we manage popup hiding manually via: - textFieldListener when text becomes
		 * empty - focusedProperty listener when focus is lost - clear button action (explicitly calls hidePopup) - ESC key
		 * (via setHideOnEscape)
		 */
		popup.setAutoHide(false);
		popup.setAutoFix(true);
		popup.setHideOnEscape(true);
		popup.getContent().add(listView);
	}

	private void setupListeners()
	{
		textField.textProperty().addListener((obs, old, act) -> textFieldListener(act));

		textField.setOnKeyPressed(event -> {
			if (popup.isShowing())
			{
				if (event.getCode() == KeyCode.DOWN)
				{
					listView.requestFocus();
					listView.getSelectionModel().selectFirst();
					event.consume();
				}
			}
			else
			{
				// log.debug("text field key pressed when popup is hidden: {}", event.getCode());
				if (event.getCode() == KeyCode.DOWN)
				{
					populatePopup();
					showPopup();
					// listView.requestFocus();
					// listView.getSelectionModel().selectFirst();
					event.consume();
				}
			}
		});

		listView.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER)
			{
				applySelection();
			}
			else if (event.getCode() == KeyCode.ESCAPE)
			{
				popup.hide();
			}
		});

		listView.setOnMouseClicked(event -> {
			if (event.getClickCount() == 1)
			{
				applySelection();
			}
		});

		focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
			if (not(isNowFocused))
				popup.hide();
		});
	}

	private void textFieldListener(String act)
	{
		if (isNull(act) || act.isEmpty())
		{
			popup.hide();
			value(null);
			return;
		}

		// there is some text in the text field
		populatePopup();
		showPopup();
	}

	/**
	 * Populates the popup list with filtered items based on current text field content. Changed from private to protected
	 * to allow subclasses (e.g., TextFieldAutoCompleteClearableWithArrowButton) to refresh the popup content when needed,
	 * such as when reopening after clearing.
	 */
	protected void populatePopup()
	{
		Set<T> filteredItems = items.stream().filter(item -> suggestionFilter.test(item, textField.getText()))
				.collect(Collectors.toSet());
		listView.getItems().setAll(filteredItems.stream().sorted(comparator).toList());
	}

	private void applySelection()
	{
		T selected = listView.getSelectionModel().getSelectedItem();
		if (nonNull(selected))
		{
			String value = textProvider.apply(selected);
			textField.setText(value);
			textField.positionCaret(value.length());
			popup.hide();
			value(selected);
		}
	}

	protected void showPopup()
	{
		Bounds bounds = textField.localToScreen(textField.getBoundsInLocal());
		if (bounds == null) return; // control not yet attached to a window
		// Preselect first item
		Platform.runLater(() -> {
			listView.getSelectionModel().selectFirst();
			listView.scrollTo(0);
			listView.requestFocus();
		});
		popup.show(textField, bounds.getMinX(), bounds.getMaxY());
	}
}
