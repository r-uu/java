package de.ruu.lib.gen.java.element;

import javax.lang.model.element.Element;

import de.ruu.lib.gen.java.Visibility;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** generator for modifiers of java method {@link Element}s (java methods may be abstract) */
public interface GeneratorModifiersMethod extends GeneratorModifiersAbstractable
{
	@Override GeneratorModifiersMethod visibility (@NonNull Visibility visibility);
	@Override GeneratorModifiersMethod setStatic  (boolean isStatic  ) throws IllegalArgumentException;
	@Override GeneratorModifiersMethod setFinal   (boolean isFinal   ) throws IllegalArgumentException;
	@Override GeneratorModifiersMethod setAbstract(boolean isAbstract) throws IllegalArgumentException;

	abstract class GeneratorModifiersMethodAbstract
			extends GeneratorModifiersAbstractableAbstract implements GeneratorModifiersMethod
	{
		protected GeneratorModifiersMethodAbstract(@NonNull CompilationUnitContext context) { super(context); }

		@Override public GeneratorModifiersMethod visibility(@NonNull Visibility visibility)
		{
			super.visibility(visibility);
			return this;
		}

		@Override public GeneratorModifiersMethod setAbstract(boolean isAbstract) throws IllegalArgumentException
		{
			super.setAbstract(isAbstract);
			return this;
		}

		@Override public GeneratorModifiersMethod setStatic(boolean isStatic) throws IllegalArgumentException
		{
			super.setStatic(isStatic);
			return this;
		}

		@Override public GeneratorModifiersMethod setFinal(boolean isFinal) throws IllegalArgumentException
		{
			super.setFinal(isFinal);
			return this;
		}
	}

	class GeneratorModifiersMethodSimple extends GeneratorModifiersMethodAbstract
	{
		public GeneratorModifiersMethodSimple(@NonNull CompilationUnitContext context) { super(context); }
	}

	static GeneratorModifiersMethod create(         @NonNull CompilationUnitContext context)
			{ return new GeneratorModifiersMethodSimple(context); }
	static GeneratorModifiersMethod methodModifiers(@NonNull CompilationUnitContext context)
			{ return create(context); }
}