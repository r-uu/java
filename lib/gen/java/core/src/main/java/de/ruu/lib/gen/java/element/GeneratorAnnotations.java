package de.ruu.lib.gen.java.element;

import de.ruu.lib.gen.java.Generator;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link GeneratorAnnotations} for annotations like <pre>@Entity(name="Person")</pre>
 */
public interface GeneratorAnnotations extends Generator
{
	@Override GeneratorAnnotations childNodesSeparator(@NonNull String separator);

	/** narrowing method from super */
	GeneratorAnnotations add(@NonNull GeneratorAnnotation annotation) throws UnsupportedOperationException;

	abstract class GeneratorAnnotationsAbstract extends GeneratorAbstract implements GeneratorAnnotations
	{
		protected GeneratorAnnotationsAbstract(@NonNull CompilationUnitContext context) { super(context); }

		@Override public GeneratorAnnotations childNodesSeparator(@NonNull String separator)
		{
			super.childNodesSeparator(separator);
			return this;
		}

		@Override public GeneratorAnnotations add(@NonNull GeneratorAnnotation annotation) throws UnsupportedOperationException
		{
			super.add(annotation);
			return this;
		}
	}

	class GeneratorAnnotationsSimple extends GeneratorAnnotationsAbstract
	{
		protected GeneratorAnnotationsSimple(@NonNull CompilationUnitContext context) { super(context); }
	}

	static GeneratorAnnotations create(
			@NonNull CompilationUnitContext context) { return new GeneratorAnnotationsSimple(context); }
	static GeneratorAnnotations annotations(
			@NonNull CompilationUnitContext context) { return create(context); }
}