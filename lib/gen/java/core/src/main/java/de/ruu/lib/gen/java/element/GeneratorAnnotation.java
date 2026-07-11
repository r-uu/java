package de.ruu.lib.gen.java.element;

import static de.ruu.lib.util.StringBuilders.sb;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.Generator;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import org.jspecify.annotations.NonNull;

public interface GeneratorAnnotation extends Generator
{
	GeneratorAnnotation type(@NonNull Class<?> type);
	GeneratorAnnotation type(@NonNull String type);
	String              type();

	/** narrowing method from super */
	GeneratorAnnotation add(@NonNull GeneratorAnnotationParameters parameters)
			throws UnsupportedOperationException;

	abstract class GeneratorAnnotationAbstract extends GeneratorAbstract implements GeneratorAnnotation
	{
		private String type;

		protected GeneratorAnnotationAbstract(
				@NonNull CompilationUnitContext context, @NonNull String type)
		{
			super(context);
			if (type.isEmpty() || type.isBlank()) throw new IllegalArgumentException("invalid type parameter value [" + type + "]");
			this.type = context().importManager().useType(type);
		}

		protected GeneratorAnnotationAbstract(
				@NonNull CompilationUnitContext context, @NonNull Class<?> type)
		{
			super(context);
			this.type = context().importManager().useType(type);
		}

		@Override public String type() { return type; }

		@Override public GeneratorAnnotation type(@NonNull Class<?> type)
		{
			this.type = context().importManager().useType(type);
			return this;
		}

		@Override public GeneratorAnnotation type(@NonNull String type)
		{
			if (type.isEmpty() || type.isBlank()) throw new IllegalArgumentException("invalid type parameter value [" + type + "]");
			this.type = context().importManager().useType(type);
			return this;
		}

		@Override public GeneratorAnnotation add(@NonNull GeneratorAnnotationParameters parameters)
				throws UnsupportedOperationException
		{
			super.add(parameters);
			return this;
		}

		@Override public StringBuilder generate() throws GeneratorException
		{
			return sb("@").append(type).append(super.generate());
		}
	}

	class GeneratorAnnotationSimple extends GeneratorAnnotationAbstract
	{
		public GeneratorAnnotationSimple(@NonNull CompilationUnitContext context, @NonNull String   type)
		{ super(context, type); }
		public GeneratorAnnotationSimple(@NonNull CompilationUnitContext context, @NonNull Class<?> type)
		{ super(context, type); }
	}

	static GeneratorAnnotation create(@NonNull CompilationUnitContext context, @NonNull String   type)
	{ return new GeneratorAnnotationSimple(context, type); }
	static GeneratorAnnotation create(@NonNull CompilationUnitContext context, @NonNull Class<?> type)
	{ return new GeneratorAnnotationSimple(context, type); }

	static GeneratorAnnotation annotation(@NonNull CompilationUnitContext context, @NonNull String   type)
	{ return create(context, type); }
	static GeneratorAnnotation annotation(@NonNull CompilationUnitContext context, @NonNull Class<?> type)
	{ return create(context, type); }
}
