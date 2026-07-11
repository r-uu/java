package de.ruu.lib.gen.java.element.method;

import static de.ruu.lib.util.StringBuilders.sb;

import java.util.ArrayList;
import java.util.List;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.Generator;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link GeneratorParameters} for annotation parameters
 */
public interface GeneratorParameters extends Generator
{
	GeneratorParameters childNodesSeparator(@NonNull String separator);

	/** narrowing method from super */
	GeneratorParameters add(@NonNull GeneratorParameter parameter)
			throws UnsupportedOperationException;

	abstract class GeneratorParametersAbstract
			extends GeneratorAbstract implements GeneratorParameters
	{
		private List<GeneratorParameter> parameterGenerators = new ArrayList<>();

		protected GeneratorParametersAbstract(@NonNull CompilationUnitContext context)
		{ super(context); }

		@Override public GeneratorParametersAbstract childNodesSeparator(@NonNull String separator)
		{
			super.childNodesSeparator(separator);
			return this;
		}

		@Override public GeneratorParameters add(@NonNull GeneratorParameter parameter)
				throws UnsupportedOperationException
		{
			parameterGenerators.add(parameter);
			return this;
		}

		@Override public StringBuilder generate() throws GeneratorException
		{
			List<String> parameters = new ArrayList<>();
			
			for (GeneratorParameter generator : parameterGenerators)
			{
				parameters.add(generator.generate().toString());
			}

			return
					sb(     "(")
					.append(String.join(", ", parameters))
					.append(")");
		}
	}

	class GeneratorAnnotationParamtersSimple extends GeneratorParametersAbstract
	{
		protected GeneratorAnnotationParamtersSimple(@NonNull CompilationUnitContext context) { super(context); }
	}

	static GeneratorParameters create(
			@NonNull CompilationUnitContext context) { return new GeneratorAnnotationParamtersSimple(context); }
	static GeneratorParameters parameters(
			@NonNull CompilationUnitContext context) { return create(context); }
}