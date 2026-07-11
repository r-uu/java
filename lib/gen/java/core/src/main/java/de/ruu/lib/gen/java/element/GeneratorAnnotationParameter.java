package de.ruu.lib.gen.java.element;

import static de.ruu.lib.util.StringBuilders.sb;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.Generator;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import org.jspecify.annotations.NonNull;

public interface GeneratorAnnotationParameter extends Generator
{
	GeneratorAnnotationParameter name( @NonNull String name);
	GeneratorAnnotationParameter value(@NonNull String value);

	StringBuilder name();
	StringBuilder value();

	abstract class GeneratorAnnotationParameterAbstract
			extends GeneratorAbstract implements GeneratorAnnotationParameter
	{
		private StringBuilder name;
		private StringBuilder value;

		protected GeneratorAnnotationParameterAbstract(
				@NonNull CompilationUnitContext context, @NonNull String name, @NonNull String value)
		{
			super(context);
			name(name);
			value(value);
		}

		@Override public StringBuilder name()  { return name; }
		@Override public StringBuilder value() { return value; }

		@Override public GeneratorAnnotationParameter name(@NonNull String name)
		{
			if (name .isEmpty() || name .isBlank()) throw new IllegalArgumentException("invalid name parameter value ["  + name  + "]");
			this.name  = sb(name);
			return this;
		}

		@Override public GeneratorAnnotationParameter value(@NonNull String value)
		{
			if (value.isEmpty() || value.isBlank()) throw new IllegalArgumentException("invalid value parameter value [" + value + "]");
			this.value = sb(value);
			return this;
		}

		@Override public StringBuilder generate() throws GeneratorException
		{
			return name.append(" = ").append(value);
		}
	}

	class GeneratorAnnotationParameterSimple extends GeneratorAnnotationParameterAbstract
	{
		public GeneratorAnnotationParameterSimple(
				@NonNull CompilationUnitContext context, @NonNull String name, @NonNull String value) { super(context, name, value); }
	}

	static GeneratorAnnotationParameter create(
			@NonNull CompilationUnitContext context, @NonNull String name, @NonNull String value)
	{ return new GeneratorAnnotationParameterSimple(context, name, value); }

	static GeneratorAnnotationParameter parameter(
			@NonNull CompilationUnitContext context, @NonNull String name, @NonNull String value)
	{ return create(context, name, value); }
}
