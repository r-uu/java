package de.ruu.lib.gen.java.element.field;

import static de.ruu.lib.util.Constants.LS;
import static de.ruu.lib.util.StringBuilders.sb;
import static javax.lang.model.element.ElementKind.FIELD;

import java.util.Optional;

import javax.lang.model.element.ElementKind;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import de.ruu.lib.gen.java.doc.GeneratorJavaDoc;
import de.ruu.lib.gen.java.element.GeneratorAnnotations;
import de.ruu.lib.gen.java.element.GeneratorElement;
import de.ruu.lib.gen.java.element.GeneratorModifiers;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** generates field declaration consisting of javadoc, annotations, modifiers, type, name and assignment */
public interface GeneratorField extends GeneratorElement
{
	/** narrowing method from super */
	@Override GeneratorField childNodesSeparator(@NonNull String separator);

	/** narrowing method from super */
	@Override GeneratorField javaDoc(@NonNull GeneratorJavaDoc javaDoc);

	/** narrowing method from super */
	@Override GeneratorField annotations(@NonNull GeneratorAnnotations annotations);

	/** narrowing method from super */
	@Override GeneratorField modifiers(@NonNull GeneratorModifiers methodModifiers);

	@Override default ElementKind elementKind() { return FIELD; }

	GeneratorField type(@NonNull String type);
	String         type();

	GeneratorField   assignment(@NonNull String assignment);
	Optional<String> assignment();

	abstract class GeneratorFieldAbstract
			extends GeneratorElementAbstract implements GeneratorField
	{
		private String           type;
		private Optional<String> assignment = Optional.empty();

		protected GeneratorFieldAbstract(@NonNull CompilationUnitContext context, @NonNull String type, @NonNull String name)
		{
			super(context, name);
			type(type);
		}

		@Override public GeneratorField childNodesSeparator(@NonNull String separator)
		{
			super.childNodesSeparator(separator);
			return this;
		}

		@Override public GeneratorField javaDoc(@NonNull GeneratorJavaDoc javaDoc)
		{
			super.javaDoc(javaDoc);
			return this;
		}

		@Override public GeneratorField annotations(@NonNull GeneratorAnnotations annotations)
		{
			super.annotations(annotations);
			return this;
		}

		@Override public GeneratorField modifiers(@NonNull GeneratorModifiers methodModifiers)
		{
			super.modifiers(methodModifiers);
			return this;
		}

		@Override public GeneratorField type(@NonNull String type)
		{
			if (type.isEmpty()) throw new IllegalArgumentException("type must not be empty");
			this.type = type;
			return this;
		}

		@Override public GeneratorField assignment(@NonNull String assignment)
		{
			if (assignment.isEmpty()) throw new IllegalArgumentException("assignment must not be empty");
			this.assignment = Optional.of(assignment);
			return this;
		}

		@Override public String type() { return type; }

		@Override public Optional<String> assignment() { return assignment; }

		/**
		 * appends
		 * <p> {@link #javaDoc()}   (if present),
		 * <p> {@link #modifiers()} (if present),
		 * <p> {@link #type()}
		 * <p> {@link #name()}
		 * <p> {@link #assignment}  (if present) separated by " = "
		 * <p> and {@code ";"}
		 */
		@Override public StringBuilder generate() throws GeneratorException
		{
			StringBuilder result = sb(generateJavaDocAnnotationsAndModifiers());

			StringBuilder modifiers = modifiers().generate();
			if (modifiers.isEmpty() == false)
			{
				// separate modifiers from field type
				result.append(" ");
			}
			else
			{
				StringBuilder javaDoc     = javaDoc().generate();
				StringBuilder annotations = annotations().generate();
				if (javaDoc.isEmpty() == false || annotations.isEmpty() == false)
				{
					// separate javadoc / annotations from method type
					result.append(LS);
				}
			}

			result.append(type).append(" ").append(name());

			if (assignment().isPresent())
					result.append(" = ").append(assignment().get());

			return result.append(";");
		}
	}

	class GeneratorFieldSimple extends GeneratorFieldAbstract
	{
		public GeneratorFieldSimple(
				@NonNull CompilationUnitContext context, @NonNull String type, @NonNull String name)
		{ super(context, type, name); }
	}

	static GeneratorField create(
			@NonNull CompilationUnitContext context, @NonNull String type, @NonNull String name)
	{ return new GeneratorFieldSimple(context, type, name); }

	static GeneratorField field(
			@NonNull CompilationUnitContext context, @NonNull String type, @NonNull String name)
	{ return create(context, type, name); }
}