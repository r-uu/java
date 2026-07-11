package de.ruu.lib.gen.java.element;

import static de.ruu.lib.gen.java.context.CompilationUnitContext.context;
import static de.ruu.lib.gen.java.element.GeneratorAnnotation.annotation;
import static de.ruu.lib.gen.java.element.GeneratorAnnotationParameter.parameter;
import static de.ruu.lib.gen.java.element.GeneratorAnnotationParameters.parameters;
import static de.ruu.lib.gen.java.element.GeneratorAnnotations.annotations;
import static de.ruu.lib.util.Constants.LS;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import de.ruu.lib.util.Strings;

class GeneratorAnnotationsTest
{
	private CompilationUnitContext context;

	@BeforeEach void beforeEach() { context = context("package.name", "SimpleFileName"); }

	@Test void defaultGenerator() throws GeneratorException
	{
		GeneratorAnnotations generator = annotations(context);
		
		assertThat(generator).isNotNull();
		assertThat(generator.generate().toString()).isEqualTo("");
	}

	@Test void parameterisedGenerator() throws GeneratorException
	{
		String type1 = "type1";
		String type2 = "type2";
		String type3 = "type3";

		GeneratorAnnotations generator =
				annotations(context)
						.childNodesSeparator("\n")
						.add(annotation(context, type1))
						.add(annotation(context, type2))
						.add(annotation(context, type3))
						;
		
		assertThat(
				Strings.normaliseLineSeparator(
						generator.generate().toString())).isEqualTo(
						Strings.normaliseLineSeparator("@" + type1 + LS + "@" + type2 + LS + "@" + type3));
	}

	@Test void parameterisedGenerator2() throws GeneratorException
	{
		String type = "type";

		String name1 = "name1";
		String name2 = "name2";
		String name3 = "name3";

		String value1 = "value1";
		String value2 = "value2";
		String value3 = "value3";

		GeneratorAnnotations generator =
				annotations(context)
						.childNodesSeparator("LS")
						.add(
								annotation(context, type)
										.add(
												parameters(context)
														.childNodesSeparator(", ")
														.add(parameter(context, name1, value1))
														.add(parameter(context, name2, value2))
														.add(parameter(context, name3, value3))
										    ))
						;
		
		assertThat(
				generator.generate().toString()).isEqualTo("@type(name1 = value1, name2 = value2, name3 = value3)");
	}
}