package de.ruu.lib.fx.control.autocomplete;

import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;

import java.util.function.Function;

import static java.util.Objects.isNull;

public class AutoCompleteListCell<T> extends ListCell<T>
{
	private Function<T, Node   > graphicsProvider;
	private Function<T, String > textProvider;
	private Function<T, Tooltip> toolTipProvider;

	public AutoCompleteListCell
			(Function<T, Node> graphicsProvider, Function<T, String> textProvider, Function<T, Tooltip> toolTipProvider)
	{
		if (isNull(graphicsProvider)) graphicsProvider = t -> null;
		if (isNull(    textProvider))     textProvider = t -> null;
		if (isNull( toolTipProvider))  toolTipProvider = t -> null;

		this.graphicsProvider = graphicsProvider;
		this.textProvider     = textProvider;
		this.toolTipProvider  = toolTipProvider;
	}

	public AutoCompleteListCell(final Function<T, String> textProvider) { this(null, textProvider, null); }

	public AutoCompleteListCell(final Function<T, String> textProvider, final Function<T, Tooltip> toolTipProvider)
	{
		this(null, textProvider, toolTipProvider);
	}

	@Override protected void updateItem(final T item, final boolean empty)
	{
		super.updateItem(item, empty);

		if (item == null || empty)
		{
			setGraphic(null);
			setText   (null);
		}
		else
		{
			setGraphic(graphicsProvider.apply(item));
			setText   (textProvider    .apply(item));
			setTooltip(toolTipProvider .apply(item));
		}
	}
}