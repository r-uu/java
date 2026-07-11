package de.ruu.lib.gen.java;

import static de.ruu.lib.gen.java.Generator.generator;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO extend this type with functionality for writing generator output correctly to file
 *      system in convenient manner
 */
public interface GeneratorCompilationUnit extends de.ruu.lib.gen.Generator
{
	abstract class GeneratorCompilationUnitAbstract implements GeneratorCompilationUnit
	{
		private Generator generator;

		protected GeneratorCompilationUnitAbstract(@NonNull Generator generator)
		{
			this.generator = generator;
		}

		@Override public StringBuilder generate() throws GeneratorException
		{
			return generator.generate();
		}
	}
	
	class GeneratorCompilationUnitSimple extends GeneratorCompilationUnitAbstract
	{
		public GeneratorCompilationUnitSimple(@NonNull Generator generator) { super(generator); }
	}

	static GeneratorCompilationUnit create(@NonNull CompilationUnitContext context)
	{
		return new GeneratorCompilationUnitSimple(generator(context));
	}

	static GeneratorCompilationUnit create(
			@NonNull CompilationUnitContext context, @NonNull Generator generator)
	{
		return new GeneratorCompilationUnitSimple(generator);
	}

	static GeneratorCompilationUnit compilationUnit(
			@NonNull CompilationUnitContext context, @NonNull Generator generator)
	{ return create(context, generator); }
}