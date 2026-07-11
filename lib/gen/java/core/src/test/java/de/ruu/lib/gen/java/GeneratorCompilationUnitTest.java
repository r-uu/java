package de.ruu.lib.gen.java;

import static de.ruu.lib.gen.java.Generator.GeneratorSimple.generator;
import static de.ruu.lib.gen.java.GeneratorCompilationUnit.compilationUnit;
import static de.ruu.lib.gen.java.context.CompilationUnitContext.context;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.context.CompilationUnitContext;

class GeneratorCompilationUnitTest
{
	private CompilationUnitContext context;

	@BeforeEach void beforeEach() { context = context("package.name", "SimpleFileName"); }

	@Test void defaultCompilationUnit() throws GeneratorException
	{
		GeneratorCompilationUnit compilationUnit = compilationUnit(context, generator(context));
		
		assertThat(compilationUnit                      ).isNotNull();
		assertThat(compilationUnit.generate().toString()).isEqualTo("");
	}

	@Test void defaultCompilationUnitWithSimpleGenerator() throws GeneratorException
	{
		String output = "test";

		assertThat(
				compilationUnit(
						context,
						generator(context)
								.output(output))
								.generate().toString()).isEqualTo(output);
	}
}