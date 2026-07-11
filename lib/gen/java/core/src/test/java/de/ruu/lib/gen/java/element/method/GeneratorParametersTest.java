package de.ruu.lib.gen.java.element.method;

import static de.ruu.lib.gen.java.context.CompilationUnitContext.context;
import static de.ruu.lib.gen.java.element.GeneratorAnnotation.annotation;
import static de.ruu.lib.gen.java.element.method.GeneratorParameter.parameter;
import static de.ruu.lib.gen.java.element.method.GeneratorParameters.parameters;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import de.ruu.lib.gen.java.element.GeneratorAnnotations;

class GeneratorParametersTest
{
	private CompilationUnitContext context;

	@BeforeEach void beforeEach() { context = context("package.name", "SimpleFileName"); }

	@Test void defaultGenerator() throws GeneratorException
	{
		GeneratorParameters generator = parameters(context);
		
//		log.debug(generator.generate().toString());
		
		assertThat(generator).isNotNull();
		assertThat(generator.generate().toString()).isEqualTo("()");
	}

	@Test void parameterisedGenerator() throws GeneratorException
	{
		String annotation = "annotation";
		String type       = "type";
		String name       = "name";

		GeneratorParameter generatorParameter = parameter(context, type, name);

		GeneratorAnnotations annotations = GeneratorAnnotations.annotations(context);
		annotations.add(annotation(context, annotation));
		
		generatorParameter.annotations(annotations);
		
		GeneratorParameters generator = parameters(context);
		generator.add(generatorParameter);
		
//		log.debug(generator.generate().toString());

		assertThat(
				generator.generate().toString()).isEqualTo("(@" + annotation + " " + type + " " + name + ")");
	}
}