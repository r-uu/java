package de.ruu.lib.gen.java.element;

import static de.ruu.lib.util.StringBuilders.sb;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.Generator;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link GeneratorAnnotationParameters} for annotation parameters
 */
public interface GeneratorAnnotationParameters extends Generator
{
	GeneratorAnnotationParameters childNodesSeparator(@NonNull String separator);

	/** narrowing method from super */
	GeneratorAnnotationParameters add(@NonNull GeneratorAnnotationParameter parameter)
			throws UnsupportedOperationException;

	abstract class GeneratorAnnotationParametersAbstract
			extends GeneratorAbstract implements GeneratorAnnotationParameters
	{
		protected GeneratorAnnotationParametersAbstract(@NonNull CompilationUnitContext context)
		{ super(context); }

		@Override public GeneratorAnnotationParametersAbstract childNodesSeparator(
				@NonNull String separator)
		{
			super.childNodesSeparator(separator);
			return this;
		}

		@Override public GeneratorAnnotationParameters add(
				@NonNull GeneratorAnnotationParameter parameter) throws UnsupportedOperationException
		{
			super.add(parameter);
			return this;
		}

		@Override public StringBuilder generate() throws GeneratorException
		{
			return
					sb(     "(")
					.append(super.generate())
					.append(")");
		}
	}

	class GeneratorAnnotationParamtersSimple extends GeneratorAnnotationParametersAbstract
	{
		protected GeneratorAnnotationParamtersSimple(@NonNull CompilationUnitContext context) { super(context); }
	}

	static GeneratorAnnotationParameters create(
			@NonNull CompilationUnitContext context) { return new GeneratorAnnotationParamtersSimple(context); }
	static GeneratorAnnotationParameters parameters(
			@NonNull CompilationUnitContext context) { return create(context); }
}