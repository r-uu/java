package de.ruu.lib.gen.java.element;

import javax.lang.model.element.Element;

import de.ruu.lib.gen.java.context.CompilationUnitContext;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** generator for modifiers of java field {@link Element}s (java fields may not be abstract) */
public interface GeneratorModifiersField extends GeneratorModifiers
{
	abstract class GeneratorModifiersFieldAbstract
			extends GeneratorModifiersAbstract implements GeneratorModifiersField
	{
		protected GeneratorModifiersFieldAbstract(@NonNull CompilationUnitContext context) { super(context); }
	}

	class GeneratorModifiersFieldSimple extends GeneratorModifiersFieldAbstract
	{
		public GeneratorModifiersFieldSimple(@NonNull CompilationUnitContext context) { super(context); }
	}

	static GeneratorModifiersField create(        @NonNull CompilationUnitContext context)
			{ return new GeneratorModifiersFieldSimple(context); }
	static GeneratorModifiersField fieldModifiers(@NonNull CompilationUnitContext context)
			{ return create(context); }
}