package de.ruu.lib.gen.java.element;

import static de.ruu.lib.util.Constants.LS;
import static de.ruu.lib.util.StringBuilders.sb;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ElementKind;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.Generator;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import de.ruu.lib.gen.java.doc.GeneratorJavaDoc;
import org.jspecify.annotations.NonNull;

/**
 * {@link GeneratorElement} for Java {@link javax.lang.model.element.Element}s like {@code package de.ruu.lib.gen.java} or {@code class
 * GeneratorElementAbstract ...}. {@link GeneratorElement}s are Java code fragments that are of a determined
 * {@link #elementKind()} (e.g. class, field, method, ...) and have a determined name.
 */
public interface GeneratorElement extends Generator
{
	/** @return kind of element produced by {@link #generate()} */
	ElementKind elementKind();

	/** narrowing method from super */
	@Override GeneratorElement childNodesSeparator(@NonNull String separator);

	GeneratorJavaDoc javaDoc();
	GeneratorElement javaDoc(@NonNull GeneratorJavaDoc javaDoc);

	GeneratorAnnotations annotations();
	GeneratorElement     annotations(@NonNull GeneratorAnnotations annotations);

	GeneratorModifiers modifiers();
	GeneratorElement   modifiers(@NonNull GeneratorModifiers modifiers);

	/**
	 * @return name of element produced by {@link #generate()}, e.g. in a field declaration {@code String description;}
	 *         "description" is the name
	 */
	String           name();
	GeneratorElement name(@NonNull String name);

	abstract class GeneratorElementAbstract extends GeneratorAbstract implements GeneratorElement
	{
		private String               name;

		private GeneratorJavaDoc     javaDoc;
		private GeneratorAnnotations annotations;
		private GeneratorModifiers   modifiers;

		public GeneratorElementAbstract(@NonNull CompilationUnitContext context, @NonNull String name)
		{
			super(context);

			if (name.isEmpty()) throw new IllegalArgumentException("name must not be empty");

			this.name   = name;

			javaDoc     = GeneratorJavaDoc    .javaDoc(context);
			annotations = GeneratorAnnotations.annotations(context);
			modifiers   = GeneratorModifiers  .modifiers(context);
		}

		@Override public String             name()        { return name; }
		@Override public GeneratorJavaDoc   javaDoc()     { return javaDoc; }
		@Override public GeneratorAnnotations annotations() { return annotations; }
		@Override public GeneratorModifiers modifiers()   { return modifiers; }

		@Override public GeneratorElement childNodesSeparator(@NonNull String separator)
		{
			super.childNodesSeparator(separator);
			return this;
		}

		@Override public GeneratorElement name(@NonNull String name)
		{
			if (name.isEmpty()) throw new IllegalArgumentException("name must not be empty");
			this.name = name;
			return this;
		}

		@Override public GeneratorElement javaDoc(@NonNull GeneratorJavaDoc javaDoc)
		{
			this.javaDoc = javaDoc;
			return this;
		}

		@Override public GeneratorElement annotations(@NonNull GeneratorAnnotations annotations)
		{
			this.annotations = annotations;
			return this;
		}

		@Override public GeneratorElement modifiers(@NonNull GeneratorModifiers modifiers)
		{
			this.modifiers = modifiers;
			return this;
		}

		/**
		 * Generates javadoc, annotations, modifiers, kind and name of an element declaration.
		 * Does <b>not</b> append {@code ;} to end of generator output.
		 */
		@Override public StringBuilder generate() throws GeneratorException
		{
			StringBuilder result = sb(generateJavaDocAnnotationsAndModifiers());

			if (result.isEmpty() == false) result.append(" ");

			return
					result
							.append(elementKind().toString().toLowerCase())
							.append(" ")
							.append(name)
							.append(super.generate());
		}

		protected String generateJavaDocAnnotationsAndModifiers() throws GeneratorException
		{
			List<String> list = new ArrayList<>();

			StringBuilder generatedJavaDoc     = javaDoc    .generate();
			StringBuilder generatedAnnotations = annotations.generate();
			StringBuilder generatedModifiers   = modifiers  .generate();

			if (generatedJavaDoc    .isEmpty() == false) list.add(generatedJavaDoc    .toString());
			if (generatedAnnotations.isEmpty() == false) list.add(generatedAnnotations.toString());
			if (generatedModifiers  .isEmpty() == false) list.add(generatedModifiers  .toString());

			StringBuilder childNodesSeparatorLocal = childNodesSeparator();
			if (childNodesSeparatorLocal.isEmpty())
					childNodesSeparatorLocal = sb(LS);

			return String.join(childNodesSeparatorLocal, list);
		}
	}
}
