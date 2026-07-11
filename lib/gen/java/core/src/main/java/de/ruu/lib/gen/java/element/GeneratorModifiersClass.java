package de.ruu.lib.gen.java.element;

import javax.lang.model.element.Element;

import de.ruu.lib.gen.java.Visibility;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** generator for modifiers of java class {@link Element}s (java classes may be abstract) */
public interface GeneratorModifiersClass extends GeneratorModifiersAbstractable
{
	@Override GeneratorModifiersClass visibility(@NonNull Visibility visibility);
	@Override GeneratorModifiersClass setAbstract(boolean isAbstract) throws IllegalArgumentException;

	abstract class GeneratorModifiersClassAbstract
	    extends GeneratorModifiersAbstractableAbstract implements GeneratorModifiersClass
	{
		protected GeneratorModifiersClassAbstract(@NonNull CompilationUnitContext context) { super(context); }

		@Override public GeneratorModifiersClass visibility(@NonNull Visibility visibility)
		{
			super.visibility(visibility);
			return this;
		}

		@Override public GeneratorModifiersClass setAbstract(boolean isAbstract) throws IllegalArgumentException
		{
			super.setAbstract(isAbstract);
			return this;
		}
	}

	class GeneratorModifiersClassSimple extends GeneratorModifiersClassAbstract
	{
		public GeneratorModifiersClassSimple(@NonNull CompilationUnitContext context) { super(context); }
	}

	static GeneratorModifiersClass create(        @NonNull CompilationUnitContext context)
			{ return new GeneratorModifiersClassSimple(context); }
	static GeneratorModifiersClass classModifiers(@NonNull CompilationUnitContext context)
			{ return create(context); }
}