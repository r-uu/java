package de.ruu.lib.gen.java.element;

import static de.ruu.lib.gen.java.context.CompilationUnitContext.context;
import static de.ruu.lib.gen.java.element.GeneratorModifiers.modifiers;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.context.CompilationUnitContext;

class GeneratorModifiersTest
{
	private CompilationUnitContext context;

	@BeforeEach void beforeEach() { context = context("package.name", "SimpleFileName"); }

	@Test void defaultGenerator() throws GeneratorException
	{
		GeneratorModifiers generator = modifiers(context);
		
		assertThat(generator).isNotNull();
		assertThat(generator.generate().toString()).isEqualTo("");
	}
}