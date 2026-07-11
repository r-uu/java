package de.ruu.lib.fx.control;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A decorator for a {@link ListView} that allows items to be checked or unchecked.
 * <p>
 * This class provides a way to manage checkable items in a ListView, where each item can be checked or unchecked
 * independently. It uses a map to associate each item with its checked state.
 * <p>
 * Checked items can be retrieved using the {@link #selectedItems()} method.
 *
 * @param <T> the type of items in the ListView
 */
public class ListViewWithCheckableItemsDecorator<T>
{
	private final @NonNull ListView<T>             listView;
	private final @NonNull Map<T, BooleanProperty> checkedItems = new HashMap<>();

	public ListViewWithCheckableItemsDecorator(@NonNull ListView<T> listView, @NonNull Function<T, String> stringProvider)
	{
		this.listView = listView;

		StringConverter<T> stringConverter = new StringConverter<>()
		{
			@Override public String toString(T item) { return stringProvider.apply(item); }
			@Override public T fromString(String string) { throw new UnsupportedOperationException("fromString not supported"); }
		};

		listView.getItems().forEach(item -> checkedItems.put(item, new SimpleBooleanProperty(false)));
		listView.setCellFactory(CheckBoxListCell.forListView(item -> checkedItems.get(item), stringConverter));
	}

	public List<T> selectedItems()
	{
		return listView.getItems().stream().filter(item -> checkedItems.get(item).get()).toList();
	}
}