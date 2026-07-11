package de.ruu.lib.fx.control.autocomplete.textfield.v2;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

import static java.util.Objects.isNull;

public class AutoCompleteTextFieldBuilder<T>
{
	private final ObservableList<T         > DEFAULT_ITEMS             = FXCollections.observableArrayList(new ArrayList<>());
	private final BiPredicate   <T, String > DEFAULT_SUGGESTION_FILTER = (t, string) -> t.toString().equals(string);
	private final BiPredicate   <T, String > DEFAULT_CONVERTER_TEST    = (t, string) -> t.toString().equals(string);
	private final Function      <T, Node   > DEFAULT_GRAPHICS_PROVIDER = t -> null;
	private final Function      <T, String > DEFAULT_TEXT_PROVIDER     = t -> t.toString();
	private final Function      <T, Tooltip> DEFAULT_TOOLTIP_PROVIDER  = t -> new Tooltip(t.toString());
	private final String                     DEFAULT_PROMPT            = "";

	private ObservableList<T         > items;
	private BiPredicate   <T, String > suggestionFilter;
	private BiPredicate   <T, String > converterTest;
	private Function      <T, Node   > graphicsProvider;
	private Function      <T, String > textProvider;
	private Function      <T, Tooltip> toolTipProvider;
	private String                     prompt;

	public AutoCompleteTextFieldBuilder()
	{
		items            = DEFAULT_ITEMS;
		suggestionFilter = DEFAULT_SUGGESTION_FILTER;
		converterTest    = DEFAULT_CONVERTER_TEST;
		graphicsProvider = DEFAULT_GRAPHICS_PROVIDER;
		textProvider     = DEFAULT_TEXT_PROVIDER;
		toolTipProvider  = DEFAULT_TOOLTIP_PROVIDER;
		prompt           = DEFAULT_PROMPT;
	}

	public static <T> AutoCompleteTextFieldBuilder<T> create() { return new AutoCompleteTextFieldBuilder<>(); }

	public AutoCompleteTextFieldBuilder<T> items(final List<T> items)
	{
		if (isNull(items))            this.items            = DEFAULT_ITEMS;
		else                          this.items            = FXCollections.observableArrayList(items);
		return this;
	}

	public AutoCompleteTextFieldBuilder<T> suggestionFilter(final BiPredicate<T, String> suggestionFilter)
	{
		if (isNull(suggestionFilter)) this.suggestionFilter = DEFAULT_SUGGESTION_FILTER;
		else                          this.suggestionFilter = suggestionFilter;
		return this;
	}

	public AutoCompleteTextFieldBuilder<T> converterTest   (final BiPredicate<T, String> converterTest)
	{
		if (isNull(converterTest))    this.converterTest    = DEFAULT_CONVERTER_TEST;
		else                          this.converterTest    = converterTest;
		return this;
	}

	public AutoCompleteTextFieldBuilder<T> graphicsProvider(final Function<T, Node> graphicsProvider)
	{
		if (isNull(graphicsProvider)) this.graphicsProvider = DEFAULT_GRAPHICS_PROVIDER;
		else                          this.graphicsProvider = graphicsProvider;
		return this;
	}

	public AutoCompleteTextFieldBuilder<T> textProvider(final Function<T, String> textProvider)
	{
		if (isNull(textProvider))     this.textProvider     = DEFAULT_TEXT_PROVIDER;
		else                          this.textProvider     = textProvider;
		return this;
	}

	public AutoCompleteTextFieldBuilder<T> toolTipProvider(final Function<T, Tooltip> toolTipProvider)
	{
		if (isNull(toolTipProvider)) this.toolTipProvider   = DEFAULT_TOOLTIP_PROVIDER;
		else                         this.toolTipProvider   = toolTipProvider;
		return this;
	}

	public AutoCompleteTextFieldBuilder<T> prompt(final String prompt)
	{
		if (isNull(prompt))          this.prompt            = DEFAULT_PROMPT;
		else                         this.prompt            = prompt;
		return this;
	}

	public AutoCompleteTextField<T> build()
	{
		return
				new AutoCompleteTextField<>
				(
						items,
						suggestionFilter,
						converterTest,
						graphicsProvider,
						textProvider,
						toolTipProvider,
						prompt
				);
	}
}