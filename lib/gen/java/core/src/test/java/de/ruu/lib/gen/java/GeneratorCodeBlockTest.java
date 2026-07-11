package de.ruu.lib.gen.java;

import static de.ruu.lib.gen.java.Generator.GeneratorSimple.generator;
import static de.ruu.lib.gen.java.GeneratorCodeBlock.codeBlokk;
import static de.ruu.lib.gen.java.context.CompilationUnitContext.context;
import static de.ruu.lib.util.Constants.LS;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.context.CompilationUnitContext;

class GeneratorCodeBlockTest
{
	private CompilationUnitContext context;

	@BeforeEach void beforeEach() { context = context("package.name", "SimpleFileName"); }

	@Test void defaultCompilationUnit() throws GeneratorException
	{
		GeneratorCodeBlock codeBlock = codeBlokk(context);
		
		assertThat(codeBlock                      ).isNotNull();
		assertThat(codeBlock.generate().toString()).isEqualTo("{" + LS + "}");
	}

	@Test void defaultCompilationUnitWithSimpleGenerator() throws GeneratorException
	{
		String content = "content";
		GeneratorCodeBlock codeBlock = codeBlokk(context);
		codeBlock.add(generator(context).output(content));

		assertThat(
				codeBlokk(context)
						.add
						(
								generator(context)
										.output(content)
						)
						.generate().toString()).isEqualTo("{" + LS + "\t" + content + LS + "}");
	}
}