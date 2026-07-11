package de.ruu.lib.gen.java.element.method;

import static de.ruu.lib.util.StringBuilders.sb;

import java.util.Optional;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.Generator;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import de.ruu.lib.gen.java.element.GeneratorAnnotations;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface GeneratorParameter extends Generator
{
	/** narrowing and disallowing method from super */
	@Override default GeneratorAbstract add(@NonNull Generator other) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException(
				"adding " + other.getClass().getName() + " is not allowed for parameter");
	}

	GeneratorParameter annotations(GeneratorAnnotations annotations);

	GeneratorParameter type(@NonNull String type);
	GeneratorParameter name(@NonNull String name);


	abstract class GeneratorParameterAbstract
			extends GeneratorAbstract implements GeneratorParameter
	{
		private Optional<GeneratorAnnotations> annotations = Optional.empty();
		private StringBuilder type;
		private StringBuilder name;

		protected GeneratorParameterAbstract(
				@NonNull CompilationUnitContext context, @NonNull String type, @NonNull String name)
		{
			super(context);
			type(type);
			name(name);
		}

		@Override public GeneratorAbstract add(@NonNull Generator other)
		{
			return GeneratorParameter.super.add(other);
		}

		@Override public GeneratorParameter type(@NonNull String type)
		{
			if (type.isEmpty() || type.isBlank()) throw new IllegalArgumentException("invalid type parameter value [" + type + "]");
			this.type = sb(type);
			return this;
		}

		@Override public GeneratorParameter name(@NonNull String name)
		{
			if (name .isEmpty() || name .isBlank()) throw new IllegalArgumentException("invalid name parameter value ["  + name  + "]");
			this.name  = sb(name);
			return this;
		}

		@Override public GeneratorParameter annotations(GeneratorAnnotations annotations)
		{
			this.annotations = Optional.ofNullable(annotations);
			return this;
		}

		@Override public StringBuilder generate() throws GeneratorException
		{
			StringBuilder result = sb();
			if (annotations.isPresent()) result.append(annotations.get().generate()).append(" ");
			return result.append(type).append(" ").append(name);
		}
	}

	class GeneratorAnnotationParameterSimple extends GeneratorParameterAbstract
	{
		public GeneratorAnnotationParameterSimple(
				@NonNull CompilationUnitContext context, @NonNull String name, @NonNull String value) { super(context, name, value); }
	}

	static GeneratorParameter create(
			@NonNull CompilationUnitContext context, @NonNull String type, @NonNull String name)
	{ return new GeneratorAnnotationParameterSimple(context, type, name); }

	static GeneratorParameter parameter(
			@NonNull CompilationUnitContext context, @NonNull String type, @NonNull String name)
	{ return create(context, type, name); }
}