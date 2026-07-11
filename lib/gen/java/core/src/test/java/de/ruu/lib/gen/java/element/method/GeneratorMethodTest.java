package de.ruu.lib.gen.java.element.method;

import static de.ruu.lib.gen.java.Visibility.PUBLIC;
import static de.ruu.lib.gen.java.context.CompilationUnitContext.context;
import static de.ruu.lib.gen.java.doc.GeneratorJavaDoc.javaDoc;
import static de.ruu.lib.gen.java.element.GeneratorAnnotation.annotation;
import static de.ruu.lib.gen.java.element.GeneratorAnnotations.annotations;
import static de.ruu.lib.gen.java.element.GeneratorModifiers.modifiers;
import static de.ruu.lib.gen.java.element.method.GeneratorMethod.method;
import static de.ruu.lib.gen.java.element.method.GeneratorMethodInterface.interfaceMethod;
import static de.ruu.lib.util.Constants.LS;
import static de.ruu.lib.util.Strings.normaliseLineSeparator;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.context.CompilationUnitContext;

class GeneratorMethodTest
{
	private CompilationUnitContext context;

	@BeforeEach void beforeEach() { context = context("package.name", "SimpleFileName"); }

	@Test void generator() throws GeneratorException
	{
		String type = "type";
		String name = "name";

		GeneratorMethod generator =
				method(context, type, name)
						.childNodesSeparator(LS);

		assertThat(generator).isNotNull();
		assertThat(generator.generate().toString()).isEqualTo(type + " " + name + "()" + LS + "{" + LS + "}");
	}

	@Test void generatorAnnotations() throws GeneratorException
	{
		String type       = "type";
		String name       = "name";
		String annotation = "annotation";

		GeneratorMethod generator =
				method(context, type, name)
						.childNodesSeparator(LS)
						.annotations(
								annotations(context)
										.add(annotation(context, annotation)));

		assertThat(generator).isNotNull();
		StringBuilder actual   = generator.generate();
		String        expected = "@" + annotation + LS + type + " " + name + "()" + LS + "{" + LS + "}";
		assertThat(actual.toString()).isEqualTo(expected);
	}

	@Test void generatorModifiers() throws GeneratorException
	{
		String type = "type";
		String name = "name";

		GeneratorMethodInterface generator =
				interfaceMethod(context, type, name)
						.modifiers
						(
								modifiers(context)
										.visibility(PUBLIC)
										.setFinal(true)
										.setStatic(true)
						);

		assertThat(generator).isNotNull();
		assertThat(
				generator.generate().toString()).isEqualTo("public final static " + type + " " + name + "();");
	}

	@Test void generatorJavaDocModifiers() throws GeneratorException
	{
		String javaDoc = "javaDoc";
		String type    = "type";
		String name    = "name";

		GeneratorMethodInterface generator =
				interfaceMethod(context, type, name)
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
						);

		assertThat(generator).isNotNull();

		String expected =
				"""
		    /**
		     * javaDoc
		     */
		    public final static type name();""";

//		log.debug(LS + generator.generate().toString());
//		log.debug(LS + "/**" + LS + " * javaDoc" + LS + " */" + LS + "public final static " + type + " " + name + " = " + value + ";");

		assertThat(
				normaliseLineSeparator(generator.generate().toString())).isEqualTo(normaliseLineSeparator(expected));
	}
}