package de.ruu.lib.gen.java.element.method;

import static de.ruu.lib.util.StringBuilders.sb;

import java.util.Optional;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.Generator;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** generates method signatures consisting of name and parameters */
public interface GeneratorSignature extends Generator
{
	/** narrowing and disallowing method from super */
	@Override default GeneratorSignature childNodesSeparator(@NonNull String separator)
			throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException(
				"configuration of child node separator is not allowed for method signatures");
	};

	GeneratorSignature name(@NonNull String name);
	
	GeneratorSignature parameters(GeneratorParameters parameters);

	abstract class GeneratorSignatureAbstract
			extends GeneratorAbstract implements GeneratorSignature
	{
		private String                        name;
		private Optional<GeneratorParameters> parameters = Optional.empty();

		protected GeneratorSignatureAbstract(
				@NonNull CompilationUnitContext context, @NonNull String name, @NonNull GeneratorParameters parameters)
		{
			super(context);
			name(name);
			parameters(parameters);
		}

		@Override public GeneratorSignature childNodesSeparator(@NonNull String separator)
		{
			return GeneratorSignature.super.childNodesSeparator(separator);
		}

		@Override public GeneratorSignature name(@NonNull String name)
		{
			if (name.isEmpty()) throw new IllegalArgumentException("name must not be empty");
			this.name = name;
			return this;
		}

		@Override public GeneratorSignature parameters(@NonNull GeneratorParameters parameters)
		{
			this.parameters = Optional.of(parameters);
			return this;
		}

		/** appends {@link #name} and {@link #parameters} (if present) separated by " " */
		@Override public StringBuilder generate() throws GeneratorException
		{
			StringBuilder result = sb(name);

			if (parameters.isPresent())
			{
				result.append(parameters.get().generate());
			}

			return result;
		}
	}

	class GeneratorSignatureSimple extends GeneratorSignatureAbstract
	{
		public GeneratorSignatureSimple(
				@NonNull CompilationUnitContext context, @NonNull String name, @NonNull GeneratorParameters parameters)
		{ super(context, name, parameters); }
	}

	static GeneratorSignature create(
			@NonNull CompilationUnitContext context, @NonNull String name, @NonNull GeneratorParameters parameters)
	{ return new GeneratorSignatureSimple(context, name, parameters); }

	static GeneratorSignature signature(
			@NonNull CompilationUnitContext context, @NonNull String name, @NonNull GeneratorParameters parameters)
	{ return create(context, name, parameters); }
}