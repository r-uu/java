package de.ruu.lib.gen.java.element;

import static de.ruu.lib.gen.java.context.CompilationUnitContext.context;
import static de.ruu.lib.gen.java.element.GeneratorAnnotationParameter.parameter;
import static de.ruu.lib.gen.java.element.GeneratorAnnotationParameters.parameters;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.context.CompilationUnitContext;

class GeneratorAnnotationParametersTest
{
	private CompilationUnitContext context;

	@BeforeEach void beforeEach() { context = context("package.name", "SimpleFileName"); }

	@Test void defaultGenerator() throws GeneratorException
	{
		GeneratorAnnotationParameters generator = parameters(context);
		
		assertThat(generator).isNotNull();
		assertThat(generator.generate().toString()).isEqualTo("()");
	}

	@Test void parameterisedGenerator() throws GeneratorException
	{
		String name1 = "name1";
		String name2 = "name2";
		String name3 = "name3";

		String value1 = "value1";
		String value2 = "value2";
		String value3 = "value3";

		GeneratorAnnotationParameters generator =
				parameters(context)
						.childNodesSeparator(", ")
						.add(parameter(context, name1, value1))
						.add(parameter(context, name2, value2))
						.add(parameter(context, name3, value3))
						;
		
		assertThat(generator.generate().toString()).isEqualTo("(name1 = value1, name2 = value2, name3 = value3)");
	}
}