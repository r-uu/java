package de.ruu.lib.gen.java;

import static de.ruu.lib.gen.java.Generator.generator;
import static de.ruu.lib.gen.java.context.CompilationUnitContext.context;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.context.CompilationUnitContext;

class GeneratorTest
{
	private CompilationUnitContext context;

	@BeforeEach void beforeEach() { context = context("package.name", "SimpleFileName"); }

	@Test void defaultGenerator() throws GeneratorException
	{
		Generator generator = generator(context);
		
		assertThat(generator).isNotNull();
		assertThat(generator.generate().toString()).isEqualTo("");
		
		assertThatThrownBy(() -> generator.add(generator))
				.isInstanceOf(UnsupportedOperationException.class)
				.hasMessageContaining("generator can not be registered at itself");
	}
}