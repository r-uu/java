package de.ruu.lib.gen.java.element.method;

import static de.ruu.lib.gen.java.context.CompilationUnitContext.context;
import static de.ruu.lib.gen.java.element.method.GeneratorThrowsClause.throwsClause;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.context.CompilationUnitContext;

class GeneratorThrowsClauseTest
{
	private CompilationUnitContext context;

	@BeforeEach void beforeEach() { context = context("package.name", "SimpleFileName"); }

	@Test void defaultGenerator() throws GeneratorException
	{
		GeneratorThrowsClause generator = throwsClause(context);
		
//		log.debug(generator.generate().toString());
		
		assertThat(generator).isNotNull();
		assertThat(generator.generate().toString()).isEqualTo("");
	}

	@Test void parameterisedGenerator() throws GeneratorException
	{
		String exception1 = "Throwable";
		String exception2 = "Exception";

		GeneratorThrowsClause generator = throwsClause(context);

		generator.add(exception1);
		generator.add(exception2);
		
//		log.debug(generator.generate().toString());

		assertThat(
				generator.generate().toString()).isEqualTo("throws " + exception1 + ", " + exception2);
	}
}