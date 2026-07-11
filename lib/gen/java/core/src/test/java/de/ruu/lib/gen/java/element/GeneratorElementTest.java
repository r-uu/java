package de.ruu.lib.gen.java.element;

import static de.ruu.lib.gen.java.context.CompilationUnitContext.context;
import static javax.lang.model.element.ElementKind.CLASS;
import static org.assertj.core.api.Assertions.assertThat;

import javax.lang.model.element.ElementKind;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import de.ruu.lib.gen.java.element.GeneratorElement.GeneratorElementAbstract;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class GeneratorElementTest
{
	private static final Logger log = LoggerFactory.getLogger(GeneratorElementTest.class);
	private class GeneratorElementSimple extends GeneratorElementAbstract
	{
		public GeneratorElementSimple(
				@NonNull CompilationUnitContext context, @NonNull String name)
		{ super(context, name); }

		@Override public ElementKind elementKind() { return CLASS; }
	}

	private CompilationUnitContext context;

	@BeforeEach void beforeEach() { context = context("package.name", "SimpleFileName"); }

	@Test void generator() throws GeneratorException
	{
		String name = "name";
		GeneratorElement generator = new GeneratorElementSimple(context, name);
		
		assertThat(generator).isNotNull();
		assertThat(
				generator.generate().toString()).isEqualTo(generator.elementKind().toString().toLowerCase() + " " + name);
	}
}