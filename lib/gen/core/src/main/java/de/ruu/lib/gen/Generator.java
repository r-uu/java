package de.ruu.lib.gen;

import org.jspecify.annotations.NonNull;

import static de.ruu.lib.util.StringBuilders.sb;

/**
 * <code>Generator</code> objects provide the result of a generation process (see {@link #generate()}). They control the
 * complete process of building the return value of {@link #generate()}.
 */
public interface Generator
{
	/**
	 * @return the result of a generation process, the process itself is completely controlled by an implementation of
	 *         {@code GeneratorTreeNodeJava}
	 * @throws GeneratorException
	 */
	StringBuilder generate() throws GeneratorException;

	/** {@link GeneratorSimple} implementation that allows to define generators that produce {@link #output}. */
	class GeneratorSimple implements Generator
	{
		/** output of {@link #generate()}, must not be {@code null} */
		@NonNull private StringBuilder output;

		public GeneratorSimple() { this(""); }

		public GeneratorSimple(@NonNull String input) { output = sb(input); }

		/** @return non {@code null} {@link #output} */
		@Override public StringBuilder generate() throws GeneratorException { return output; }
	}

	static Generator create  (@NonNull String input) { return new GeneratorSimple(input); }
	static Generator generate(@NonNull String input) { return              create(input); }
}