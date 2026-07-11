package de.ruu.lib.fx.control.autocomplete.textfield.v1;

import static de.ruu.lib.util.BooleanFunctions.not;
import static java.util.Objects.isNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;

import de.ruu.lib.fx.FXUtil;
import de.ruu.lib.fx.control.autocomplete.AutoCompleteCellFactory;
import de.ruu.lib.fx.control.autocomplete.AutoCompleteStringConverter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Popup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a {@link #textField} that suggests {@link #items} for auto completion in a {@link #popup}. {@link #items}
 * are lookup to suggestions by {@link #suggestionFilter}. If defined {@link #graphicsProvider} provides a graphical
 * representation of an item in the {@link #popup}. Analogously, if defined, {@link #textProvider} and
 * {@link #toolTipProvider} provide a textual representation or a tool tip for an item in the {@link #popup}.
 *
 * @param <T>
 * @author r-uu
 */
public class AutoCompleteTextField<T> extends HBox
{
	private static final Logger log = LoggerFactory.getLogger(AutoCompleteTextField.class);

	public enum Position {
		ABOVE, BELOW
	}

	/** stores the current value of an instance at all time, accessible for clients via {@link #value()} */
	private final ObjectProperty<T> value;

	private final TextField textField;
	private ListView<T> listView;
	private Popup popup;

	private ObservableList<T> items;
	private final BiPredicate<T, String> suggestionFilter;
	private final BiPredicate<T, String> converterTest;
	private final Function<T, Node> graphicsProvider;
	private final Function<T, String> textProvider;
	private final Function<T, Tooltip> toolTipProvider;
	private final Position position;

	private AutoCompleteStringConverter<T> converter;

	/**
	 * @param suggestionFilter returns <code>true</code> if an item is a valid suggestion for the value of the combo box,
	 * <code>false</code> otherwise
	 * @param converterTest returns <code>true</code> if an item corresponds to the text, <code>false</code> otherwise
	 * @param graphicsProvider
	 * @param textProvider
	 * @param toolTipProvider
	 * @param promptText
	 * @param position
	 */
	public AutoCompleteTextField(List<T> items, BiPredicate<T, String> suggestionFilter,
			BiPredicate<T, String> converterTest, Function<T, Node> graphicsProvider, Function<T, String> textProvider,
			Function<T, Tooltip> toolTipProvider, String promptText, Position position) {
		if (items == null)
			items = new ArrayList<>();
		if (suggestionFilter == null)
			throw new IllegalArgumentException("suggestion filter must not be null");
		if (converterTest == null)
			throw new IllegalArgumentException("converter test must not be null");
		if (graphicsProvider == null)
			graphicsProvider = item -> null;
		if (textProvider == null)
			textProvider = item -> isNull(item) ? "" : item.toString();
		if (toolTipProvider == null)
			toolTipProvider = item -> null;
		if (promptText == null)
			promptText = "";
		if (position == null)
			position = Position.BELOW;

		this.items = FXCollections.observableArrayList(items);
		this.suggestionFilter = suggestionFilter;
		this.converterTest = converterTest;
		this.graphicsProvider = graphicsProvider;
		this.textProvider = textProvider;
		this.toolTipProvider = toolTipProvider;
		this.position = position;

		value = new SimpleObjectProperty<>();
		value.addListener((obs, old, newValue) -> onValueChanged(old, newValue));

		textField = new TextField();
		textField.setPromptText(promptText);
		textField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background,-30%); }");
		textField.setOnKeyReleased(e -> onKeyReleasedTextField(e));
		textField.textProperty().addListener((obs, old, newValue) -> onTextFieldTextChanged(old, newValue));

		// initPopup();
		setItems(items);

		// focus change events
		textField.focusedProperty().addListener((obs, old, newValue) -> onTextFieldFocusChanged(old, newValue));
		popup.focusedProperty().addListener((obs, old, newValue) -> onPopupFocusChanged(old, newValue));

		HBox.setHgrow(textField, Priority.ALWAYS);
		getChildren().add(textField);

		setAlignment(Pos.CENTER_LEFT);
	}

	public T value()
	{
		return value.get();
	}

	public Optional<T> optionalValue()
	{
		return Optional.ofNullable(value.get());
	}

	public void setValue(final T value)
	{
		if (value == null)
		{
			this.value.set(null);
		}
		else if (items.contains(value))
		{
			this.value.set(value);
		}
		else
		{
			log.warn("value not contained in items, did not set value to " + value);
		}
	}

	public ObjectProperty<T> valueProperty()
	{
		return value;
	}

	public void setItems(final List<T> items)
	{
		this.items = FXCollections.observableArrayList(items);
		initPopup();
		converter = new AutoCompleteStringConverter<>(items, converterTest, textProvider);

		if (items.size() == 1)
		{
			listView.getSelectionModel().selectFirst();
			commitListViewSelection();
			// commitTextField();
		}
	}

	public List<T> items()
	{
		return items;
	}

	public ReadOnlyObjectProperty<T> selectedItemInListProperty()
	{
		return listView.getSelectionModel().selectedItemProperty();
	}

	public StringProperty textProperty()
	{
		return textField.textProperty();
	}

	/** initialises popup after {@link #items} has been changed */
	private void initPopup()
	{
		final AutoCompleteCellFactory<T> autoCompleteCellFactory = new AutoCompleteCellFactory<>(graphicsProvider,
				textProvider, toolTipProvider);

		listView = new ListView<>(FXCollections.observableArrayList(items));

		// Bind ListView width to TextField width for consistent popup sizing
		listView.minWidthProperty().bind(textField.widthProperty());
		listView.maxWidthProperty().bind(textField.widthProperty());

		listView.setCellFactory(autoCompleteCellFactory);
		listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		listView.getSelectionModel().selectFirst();
		listView.setOnMouseClicked(e -> onMouseClickedListView(e));
		listView.setOnKeyReleased(e -> onKeyReleasedListView(e));

		popup = new Popup();
		popup.getContent().add(listView);
	}

	private void onValueChanged(final T old, final T newValue)
	{
		textField.setText(converter.toString(newValue));
	}

	private void onKeyReleasedTextField(final KeyEvent e)
	{
		log.debug("code of released key {}", e.getCode());
		if (e.getCode() == KeyCode.ENTER)
			commit();
		else if (e.getCode() == KeyCode.DOWN)
			showPopup();
		else if (e.getCode() == KeyCode.ESCAPE)
			popup.hide();
	}

	private void onTextFieldTextChanged(final String old, final String newValue)
	{
		updateSuggestions();

		// update (resize) popup with suggestions, first hide popup
		popup.hide();

		if (listView.getItems().size() > 1)
		{
			// if there is more than one suggestion, show (and implicitly resize) the popup again
			showPopup();
		}
		else if (listView.getItems().size() == 1)
		{
			// if there is just one suggestion, do _not_ show the popup but select the first suggestion
			listView.getSelectionModel().selectFirst();
			// and commit this selection
			commitListViewSelection();
		}
	}

	private void onTextFieldFocusChanged(final Boolean old, final Boolean newValue)
	{
		if (newValue)
		{
			if (listView.getItems().size() < 2)
			{
				popup.hide(); // hide popup if there are not more suggestions than one
			}
			else
			{
				showPopup();
			}
		}
		else
		{
			popup.hide();
		}
	}

	private void onPopupFocusChanged(final Boolean old, final Boolean newValue)
	{
		if (not(newValue))
			popup.hide();
	}

	private void onMouseClickedListView(final MouseEvent e)
	{
		if (e.getClickCount() == 2)
			commitListViewSelection();
	}

	private void onKeyReleasedListView(KeyEvent e)
	{
		log.debug("code of released key {}", e.getCode());
		if (e.getCode() == KeyCode.ENTER)
			commitListViewSelection();
	}

	private void commit()
	{
		// if (popup.isShowing()) commitListViewSelection();
		// else commitTextField();
		commitTextField();
	}

	private void commitTextField()
	{
		T converted = converter.fromString(textField.getText());
		if (not(isNull(converted)))
			value.set(converted);
	}

	private void commitListViewSelection()
	{
		value.set(listView.getSelectionModel().getSelectedItem());
		textField.requestFocus();
		popup.hide();
	}

	private void updateSuggestions()
	{
		// ternary op because otherwise text is not effectively final and can not be used in lambdas
		final String text = isNull(textField.getText()) ? "" : textField.getText();

		final List<T> filteredSuggestions = new ArrayList<>();

		// populate filteredSuggestions from items
		items.forEach(item -> {
			if (suggestionFilter.test(item, text))
				filteredSuggestions.add(item);
		});

		listView.setItems(FXCollections.observableArrayList(filteredSuggestions));
		listView.getSelectionModel().clearSelection();
	}

	private void showPopup()
	{
		if (position == Position.BELOW)
			FXUtil.showPopupBelowNode(textField, popup);
		else
			FXUtil.showPopupAboveNode(textField, popup);
	}
}