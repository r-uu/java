package de.ruu.lib.gen.java.element.field;

import static de.ruu.lib.gen.java.Visibility.PUBLIC;
import static de.ruu.lib.gen.java.context.CompilationUnitContext.context;
import static de.ruu.lib.gen.java.doc.GeneratorJavaDoc.javaDoc;
import static de.ruu.lib.gen.java.element.GeneratorAnnotation.annotation;
import static de.ruu.lib.gen.java.element.GeneratorAnnotations.annotations;
import static de.ruu.lib.gen.java.element.GeneratorModifiers.modifiers;
import static de.ruu.lib.gen.java.element.field.GeneratorField.field;
import static de.ruu.lib.util.Constants.LS;
import static de.ruu.lib.util.Strings.normaliseLineSeparator;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.context.CompilationUnitContext;

class GeneratorFieldTest
{
	private CompilationUnitContext context;

	@BeforeEach void beforeEach() { context = context("package.name", "SimpleFileName"); }

	@Test void generator() throws GeneratorException
	{
		String type = "type";
		String name = "name";

		GeneratorField generator = field(context, type, name);

		assertThat(generator).isNotNull();
		assertThat(generator.generate().toString()).isEqualTo(type + " " + name + ";");
	}

	@Test void generatorAnnotations() throws GeneratorException
	{
		String type       = "type";
		String name       = "name";
		String annotation = "annotation";

		GeneratorField generator =
				field(context, type, name)
						.annotations(
								annotations(context)
										.add(annotation(context, annotation)));

		assertThat(generator).isNotNull();
		assertThat(
				generator.generate().toString()).isEqualTo("@" + annotation + LS + type + " " + name + ";");
	}

	@Test void generatorModifiers() throws GeneratorException
	{
		String type = "type";
		String name = "name";

		GeneratorField generator =
				field(context, type, name)
						.modifiers
						(
								modifiers(context)
										.visibility(PUBLIC)
										.setFinal(true)
										.setStatic(true)
						);

		assertThat(generator).isNotNull();
		assertThat(
				generator.generate().toString()).isEqualTo("public final static " + type + " " + name + ";");
	}

	@Test void generatorModifiersAndAssignment() throws GeneratorException
	{
		String type  = "type";
		String name  = "name";
		String value = "value";

		GeneratorField generator =
				field(context, type, name)
						.modifiers
						(
								modifiers(context)
										.visibility(PUBLIC)
										.setFinal(true)
										.setStatic(true)
						)
						.assignment(value);

		assertThat(generator).isNotNull();
		assertThat(
				generator.generate().toString()).isEqualTo("public final static " + type + " " + name + " = " + value + ";");
	}

	@Test void generatorJavaDocModifiersAndAssignment() throws GeneratorException
	{
		String javaDoc = "javaDoc";
		String type    = "type";
		String name    = "name";
		String value   = "value";

		GeneratorField generator =
				field(context, type, name)
						.childNodesSeparator(LS)
						.javaDoc
						(
								javaDoc(context)
										.add(javaDoc)
						)
						.modifiers
						(
								modifiers(context)
										.visibility(PUBLIC)
										.setFinal(true)
										.setStatic(true)
						)
						.assignment(value);

		assertThat(generator).isNotNull();

		String expected =
				"""
		    /**
		     * javaDoc
		     */
		    public final static type name = value;""";

//		log.debug(LS + generator.generate().toString());
//		log.debug(LS + "/**" + LS + " * javaDoc" + LS + " */" + LS + "public final static " + type + " " + name + " = " + value + ";");

		assertThat(
				normaliseLineSeparator(generator.generate().toString())).isEqualTo(normaliseLineSeparator(expected));
	}
}