package de.ruu.lib.gen.java.doc;

import static de.ruu.lib.gen.java.context.CompilationUnitContext.context;
import static de.ruu.lib.gen.java.doc.GeneratorJavaDoc.javaDoc;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.context.CompilationUnitContext;

class GeneratorJavaDocTest
{
	private CompilationUnitContext context;

	@BeforeEach void beforeEach() { context = context("package.name", "SimpleFileName"); }

	@Test void defaultGenerator() throws GeneratorException
	{
		GeneratorJavaDoc generator = javaDoc(context);

		assertThat(generator).isNotNull();
		assertThat(generator.generate().toString()).isEqualTo("");
	}

	@Test void defaultGeneratorWithSingleLine() throws GeneratorException
	{
		String line = "line";

		assertThat(
				javaDoc(context)
						.add(line)
						.generate().toString()).isEqualTo("/**" + System.lineSeparator() + " * " + line + System.lineSeparator() + " */");
	}
}