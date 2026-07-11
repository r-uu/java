package de.ruu.lib.gen.java;

import static de.ruu.lib.util.StringBuilders.rTrimChars;
import static de.ruu.lib.util.StringBuilders.sb;

import java.util.ArrayList;
import java.util.List;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.LineIndenter;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import de.ruu.lib.gen.java.element.GeneratorAnnotations;
import org.jspecify.annotations.NonNull;

public interface Generator extends de.ruu.lib.gen.Generator
{
	/** @return non {@code null}, line indenter for {@link #generate()} output */
	LineIndenter lineIndenter();

	/** configures line indenter for {@link #generate()} output */
	Generator lineIndenter(LineIndenter lineIndenter);
	/** configures line indenter for {@link #generate()} output */
	Generator lineIndenter(String indentation, int level);

	/** @return non {@code null}, separator for {@link #generate()} output of children */
	StringBuilder childNodesSeparator();

	/** sets child nodes separator */
	Generator childNodesSeparator(@NonNull String separator);

	/**
	 * Adds {@code other} to {@link CompilationUnitContext#registeredGenerators()}
	 * if it is not already contained. If that succeeds without exception {@code other}
	 * will be added to this generator's children (see {@link GeneratorAbstract#children}).
	 *
	 * @param other generator
	 * @return {@code this}
	 * @throws UnsupportedOperationException per default, override this method to support adding
	 *         particular generators
	 */
	default Generator add(@NonNull Generator other) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException(
				"adding " + other.getClass().getName() + " generator is not supported");
	}

	abstract class GeneratorAbstract implements Generator
	{
		private final CompilationUnitContext  context;
		private final List<Generator>         children            = new ArrayList<>();
		private       LineIndenter            lineIndenter        = new LineIndenter();
		private       StringBuilder           childNodesSeparator = sb();

		protected GeneratorAbstract(@NonNull CompilationUnitContext context) { this.context = context; }

		public CompilationUnitContext context()           { return context; }
		public List<Generator>        children()          { return children; }
		public LineIndenter           lineIndenter()      { return lineIndenter; }
		public StringBuilder          childNodesSeparator() { return childNodesSeparator; }

		// TODO can not be final because otherwise it would not be possible to disallow adding unwanted child
		//      generators in sub classes???
		@Override public GeneratorAbstract add(@NonNull Generator other) throws UnsupportedOperationException
		{
			if (other == this) throw new UnsupportedOperationException("generator can not be registered at itself");

			// register other (fails if already registered)
			context().register(other);

			// if registration did not fail, add to children
			children.add(other);

			return this;
		}

		@Override public StringBuilder generate() throws GeneratorException
		{
			final StringBuilder collector = sb();

			for (Generator child : children)
			{
				// recursive call!!!
				StringBuilder childOutput = child.generate();

				if (childOutput.toString().equals(childNodesSeparator.toString()))
				{
					// do not append another child nodes separator to a child nodes separator

					// indent child output
					childOutput = child.lineIndenter().indent(childOutput);

					// append indented output to result
					collector.append(childOutput);
				}
				else
				{
					// indent child output
					childOutput = child.lineIndenter().indent(childOutput);

					// append indented output and child node separator to result
					collector.append(childOutput).append(childNodesSeparator);
				}
			}

			// remove trailing child node separator
			StringBuilder result = rTrimChars(collector, childNodesSeparator.toString());

			// indent result
			result = lineIndenter.indent(result);

			return result;
		}

		@Override public Generator childNodesSeparator(@NonNull String separator)
		{
			childNodesSeparator = sb(separator);
			return this;
		}

		@Override public Generator lineIndenter(LineIndenter indenter)
		{
			lineIndenter = indenter;
			return this;
		}

		@Override public Generator lineIndenter(String indentation, int level)
		{
			lineIndenter = new LineIndenter(indentation, level);
			return this;
		}
	}

	/**
	 * produces generator output and finally disallows adding children ({@link #add(GeneratorAnnotations)})
	 */
	class GeneratorSimple extends GeneratorAbstract
	{
		private String output = "";

		public GeneratorSimple(@NonNull CompilationUnitContext context) { super(context); }
		public GeneratorSimple(@NonNull CompilationUnitContext context, @NonNull String input)
		{
			this(context);
			this.output = input;
		}

		public String output() { return output; }

		public GeneratorSimple output(@NonNull String input)
		{
			this.output = input;
			return this;
		}

		@Override public final StringBuilder generate() throws GeneratorException { return sb(output); }

		public static GeneratorSimple create(   @NonNull CompilationUnitContext context) { return new GeneratorSimple(context); }
		public static GeneratorSimple generator(@NonNull CompilationUnitContext context) { return create(context); }
	}

	static Generator create(   @NonNull CompilationUnitContext context) { return GeneratorSimple.generator(context); }
	static Generator generator(@NonNull CompilationUnitContext context) { return create(context); }
}
