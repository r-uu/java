package de.ruu.lib.gen.java.element.method;

import static de.ruu.lib.util.StringBuilders.sb;

import java.util.ArrayList;
import java.util.List;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.Generator;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Generator} for throws clauses like {@code throws IllegalArgumentException,
 * NullPointerException}
 */
public interface GeneratorThrowsClause extends Generator
{
	GeneratorThrowsClause add(String exception);

	abstract class GeneratorThrowsClauseAbstract
			extends GeneratorAbstract implements GeneratorThrowsClause
	{
		protected GeneratorThrowsClauseAbstract(@NonNull CompilationUnitContext context) { super(context); }

		private List<String> throwables = new ArrayList<>();

		@Override public GeneratorThrowsClause add(String line)
		{
			throwables.add(line);
			return this;
		}

		@Override public StringBuilder generate() throws GeneratorException
		{
			if (throwables.isEmpty()) return sb("");
			return sb("throws ").append(String.join(", ", throwables));
		}
	}

	class GeneratorJavaDocSimple extends GeneratorThrowsClauseAbstract
	{
		protected GeneratorJavaDocSimple(@NonNull CompilationUnitContext context) { super(context); }
	}

	static GeneratorThrowsClause create(      @NonNull CompilationUnitContext context) { return new GeneratorJavaDocSimple(context); }
	static GeneratorThrowsClause throwsClause(@NonNull CompilationUnitContext context) { return create(context); }
}