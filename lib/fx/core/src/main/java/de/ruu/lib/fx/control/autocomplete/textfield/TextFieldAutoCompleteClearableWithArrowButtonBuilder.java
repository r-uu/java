package de.ruu.lib.fx.control.autocomplete.textfield;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

import javafx.scene.Node;
import javafx.scene.control.Tooltip;

public class TextFieldAutoCompleteClearableWithArrowButtonBuilder<T> extends TextFieldAutoCompleteClearableBuilder<T>
{
	public static <T> TextFieldAutoCompleteClearableWithArrowButtonBuilder<T> create()
	{
		return new TextFieldAutoCompleteClearableWithArrowButtonBuilder<>();
	}

	@Override
	public TextFieldAutoCompleteClearableWithArrowButtonBuilder<T> items(final List<T> items)
	{
		super.items(items);
		return this;
	}

	@Override
	public TextFieldAutoCompleteClearableWithArrowButtonBuilder<T> suggestionFilter(
			final BiPredicate<T, String> suggestionFilter)
	{
		super.suggestionFilter(suggestionFilter);
		return this;
	}

	@Override
	public TextFieldAutoCompleteClearableWithArrowButtonBuilder<T> comparator(final Comparator<T> comparator)
	{
		super.comparator(comparator);
		return this;
	}

	@Override
	public TextFieldAutoCompleteClearableWithArrowButtonBuilder<T> graphicsProvider(
			final Function<T, Node> graphicsProvider)
	{
		super.graphicsProvider(graphicsProvider);
		return this;
	}

	@Override
	public TextFieldAutoCompleteClearableWithArrowButtonBuilder<T> textProvider(final Function<T, String> textProvider)
	{
		super.textProvider(textProvider);
		return this;
	}

	@Override
	public TextFieldAutoCompleteClearableWithArrowButtonBuilder<T> toolTipProvider(
			final Function<T, Tooltip> toolTipProvider)
	{
		super.toolTipProvider(toolTipProvider);
		return this;
	}

	@Override
	public TextFieldAutoCompleteClearableWithArrowButtonBuilder<T> prompt(final String prompt)
	{
		super.prompt(prompt);
		return this;
	}

	@Override
	public TextFieldAutoCompleteClearableWithArrowButton<T> build()
	{
		return new TextFieldAutoCompleteClearableWithArrowButton<>(items, suggestionFilter, comparator, graphicsProvider,
				textProvider, toolTipProvider, prompt);
	}
}