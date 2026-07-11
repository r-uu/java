package de.ruu.lib.gen;

import de.ruu.lib.util.Strings;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LineIndenter
{
	private static final Logger log = LoggerFactory.getLogger(LineIndenter.class);
	/** String used for indentation of lines in {@link #indent(String)} and {@link #indent(StringBuffer)}. */
	private String indentation;

	/** Number of times {@link #indentation} is used for indenting. */
	private int indentationLevel;

	/**
	 * @param indentation
	 * @param indentationLevel values &lt; 0 will be handled as 0
	 *
	 * @see #indentation
	 * @see #indentationLevel
	 */
	public LineIndenter(@NonNull String indentation, int indentationLevel)
	{
		setIndentation(indentation);
		setIndentationLevel(indentationLevel);
	}

	/**
	 * Shortcut to {@link LineIndenter#LineIndenter(String, int)} with the parameter values <code>""</code> and
	 * <code>0</code>. The line indenter will not indent lines.
	 */
	public LineIndenter() { this("", 0); }

	public String indentation() { return indentation; }

	/** @param indentation */
	public LineIndenter setIndentation(@NonNull String indentation)
	{
		this.indentation = indentation;
		return this;
	}

	public int indentationLevel() { return indentationLevel; }

	/** @param indentationLevel values &lt; 0 are handled as 0 */
	public LineIndenter setIndentationLevel(int indentationLevel)
	{
		if (indentationLevel < 0)
		{
			this.indentationLevel = 0;
		}
		else
		{
			this.indentationLevel = indentationLevel;
		}
		return this;
	}

	public String indent(@NonNull String string)
	{
		return Strings.indent(string, indentation, indentationLevel);
	}

	public StringBuffer indent(@NonNull StringBuffer sb) { return new StringBuffer(indent(sb.toString())); }

	public StringBuilder indent(@NonNull StringBuilder sb) { return new StringBuilder(indent(sb.toString())); }

	public static LineIndenter create(String indentation, int level) { return new LineIndenter(indentation, level); }
	public static LineIndenter lineIndenter(String indentation, int level) { return create(indentation, level); }

	/**
	 * @param lineIndenter
	 * @param by
	 * @return new line indenter based on <code>lineIndenter</code> with indentation increased by <code>by</code>
	 */
	public static LineIndenter newIncreasedBy(@NonNull LineIndenter lineIndenter, int by)
	{
		return new LineIndenter(lineIndenter.indentation, lineIndenter.indentationLevel + by);
	}
}