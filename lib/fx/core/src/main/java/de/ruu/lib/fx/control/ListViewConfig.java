package de.ruu.lib.fx.control;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.Predicate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

/** @param <E> list element type */
public class ListViewConfig<E>
{
	private ObservableList<E> data = FXCollections.observableArrayList();

	private Comparator<E> comparator = (e1, e2) -> e1.toString().compareTo(e2.toString());
	private Predicate<E>  predicate  = null;

	public ListViewConfig(Collection<E> data) { this.data.addAll(data); }

	public ListViewConfig<E> comparator(Comparator<E> comparator)
	{
		this.comparator = comparator;
		return this;
	}

	public ListViewConfig<E> predicate(Predicate<E> predicate)
	{
		this.predicate = predicate;
		return this;
	}

	public ObservableList<E> data()               { return                    data;                      }
	public SortedList    <E> dataSorted()         { return new SortedList<E> (data        , comparator); }
	public FilteredList  <E> dataFiltered()       { return new FilteredList<>(data        , predicate ); }
	public FilteredList  <E> dataSortedFiltered() { return new FilteredList<>(dataSorted(), predicate ); }
}