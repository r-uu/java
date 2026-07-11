package de.ruu.lib.fx.control.autocomplete;

import java.util.function.Function;

import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.util.Callback;

public class AutoCompleteCellFactory<T> implements Callback<ListView<T>, ListCell<T>>
{
	private Function<T, Node   > graphicsProvider;
	private Function<T, String > textProvider;
	private Function<T, Tooltip> toolTipProvider;

	public AutoCompleteCellFactory
			(Function<T, Node> graphicsProvider, Function<T, String> textProvider, Function<T, Tooltip> toolTipProvider)
	{
		this.graphicsProvider = graphicsProvider;
		this.textProvider     = textProvider;
		this.toolTipProvider  = toolTipProvider;
	}

	public AutoCompleteCellFactory(Function<T, String> textProvider)
	{
		this(null, textProvider, null);
	}

	public AutoCompleteCellFactory(Function<T, String> textProvider, Function<T, Tooltip> toolTipProvider)
	{
		this(null, textProvider, toolTipProvider);
	}

	@Override public ListCell<T> call(ListView<T> param)
	{
		return new AutoCompleteListCell<T>(graphicsProvider, textProvider, toolTipProvider);
	}
}