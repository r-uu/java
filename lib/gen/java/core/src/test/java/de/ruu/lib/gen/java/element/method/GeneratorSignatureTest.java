package de.ruu.lib.gen.java.element.method;

import static de.ruu.lib.gen.java.context.CompilationUnitContext.context;
import static de.ruu.lib.gen.java.element.method.GeneratorParameter.parameter;
import static de.ruu.lib.gen.java.element.method.GeneratorSignature.signature;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import de.ruu.lib.gen.java.element.GeneratorAnnotation;
import de.ruu.lib.gen.java.element.GeneratorAnnotations;

class GeneratorSignatureTest
{
	private CompilationUnitContext context;

	@BeforeEach void beforeEach() { context = context("package.name", "SimpleFileName"); }

	@Test void parameterisedGenerator() throws GeneratorException
	{
		String name = "name";

		GeneratorSignature generator = signature(context, name, GeneratorParameters.parameters(context));
		
		assertThat(generator).isNotNull();
		assertThat(generator.generate().toString()).isEqualTo(name + "()");
	}

	@Test void parameterisedGeneratorWithAnnotations() throws GeneratorException
	{
		String parameter1annotation1 = "parameter1annotation1";
		String parameter1annotation2 = "parameter1annotation2";
		String parameter1annotation3 = "parameter1annotation3";

		String parameter2annotation1 = "parameter2annotation1";
		String parameter2annotation2 = "parameter2annotation2";
		String parameter2annotation3 = "parameter2annotation3";

		String parameter3annotation1 = "parameter3annotation1";
		String parameter3annotation2 = "parameter3annotation2";
		String parameter3annotation3 = "parameter3annotation3";

		String parameter1type = "parameter1type";
		String parameter2type = "parameter2type";
		String parameter3type = "parameter3type";
		
		String parameter1name = "parameter1name";
		String parameter2name = "parameter2name";
		String parameter3name = "parameter2name";
		
		GeneratorParameter generatorParameter1 = parameter(context, parameter1type, parameter1name);
		GeneratorParameter generatorParameter2 = parameter(context, parameter2type, parameter2name);
		GeneratorParameter generatorParameter3 = parameter(context, parameter3type, parameter3name);

		generatorParameter1.annotations
		(
				GeneratorAnnotations.annotations(context)
						.childNodesSeparator(" ")
						.add(GeneratorAnnotation.annotation(context, parameter1annotation1))
						.add(GeneratorAnnotation.annotation(context, parameter1annotation2))
						.add(GeneratorAnnotation.annotation(context, parameter1annotation3))
		);
		generatorParameter2.annotations
		(
				GeneratorAnnotations.annotations(context)
						.childNodesSeparator(" ")
						.add(GeneratorAnnotation.annotation(context, parameter2annotation1))
						.add(GeneratorAnnotation.annotation(context, parameter2annotation2))
						.add(GeneratorAnnotation.annotation(context, parameter2annotation3))
		);
		generatorParameter3.annotations
		(
				GeneratorAnnotations.annotations(context)
						.childNodesSeparator(" ")
						.add(GeneratorAnnotation.annotation(context, parameter3annotation1))
						.add(GeneratorAnnotation.annotation(context, parameter3annotation2))
						.add(GeneratorAnnotation.annotation(context, parameter3annotation3))
		);
		
		GeneratorParameters parameters = GeneratorParameters.parameters(context);
		
		parameters
				.childNodesSeparator(", ")
				.add(generatorParameter1)
				.add(generatorParameter2)
				.add(generatorParameter3);

		String methodname = "methodname";

		GeneratorSignature generatorSignature = signature(context, methodname, parameters);
		
		assertThat(
				generatorSignature.generate().toString()).isEqualTo(methodname
						+ "("
						+    "@" + parameter1annotation1 + " "
						+    "@" + parameter1annotation2 + " "
						+    "@" + parameter1annotation3 + " "
						+     parameter1type + " " + parameter1name + ", "
						+    "@" + parameter2annotation1 + " "
						+    "@" + parameter2annotation2 + " "
						+    "@" + parameter2annotation3 + " "
						+     parameter2type + " " + parameter2name + ", "
						+    "@" + parameter3annotation1 + " "
						+    "@" + parameter3annotation2 + " "
						+    "@" + parameter3annotation3 + " "
						+     parameter3type + " " + parameter3name
						+ ")");
	}
}