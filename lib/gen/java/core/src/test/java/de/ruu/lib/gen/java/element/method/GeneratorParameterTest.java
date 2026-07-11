package de.ruu.lib.gen.java.element.method;

import static de.ruu.lib.gen.java.context.CompilationUnitContext.context;
import static de.ruu.lib.gen.java.element.method.GeneratorParameter.parameter;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import de.ruu.lib.gen.java.element.GeneratorAnnotation;
import de.ruu.lib.gen.java.element.GeneratorAnnotations;

class GeneratorParameterTest
{
	private CompilationUnitContext context;

	@BeforeEach void beforeEach() { context = context("package.name", "SimpleFileName"); }

	@Test void parameterisedGenerator() throws GeneratorException
	{
		String type = "type";
		String name = "name";

		GeneratorParameter generator = parameter(context, type, name);
		
		assertThat(generator).isNotNull();
		assertThat(generator.generate().toString()).isEqualTo(type + " " + name);
	}

	@Test void parameterisedGeneratorWithAnnotation() throws GeneratorException
	{
		String annotation1 = "annotation1";
		String annotation2 = "annotation2";
		String annotation3 = "annotation3";

		String type = "type";
		String name = "name";

		GeneratorParameter generator = parameter(context, type, name);
		generator.annotations
		(
				GeneratorAnnotations.annotations(context)
						.childNodesSeparator(" ")
						.add(GeneratorAnnotation.annotation(context, annotation1))
						.add(GeneratorAnnotation.annotation(context, annotation2))
						.add(GeneratorAnnotation.annotation(context, annotation3))
		);
		
		assertThat(generator).isNotNull();
		assertThat(
				generator.generate().toString()).isEqualTo("@" + annotation1 + " " +
						"@" + annotation2 + " " +
						"@" + annotation3 + " " + type + " " + name);
	}
}