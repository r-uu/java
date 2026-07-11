package de.ruu.lib.gen.java.element;

import static de.ruu.lib.gen.java.Visibility.DEFAULT;
import static de.ruu.lib.util.BooleanFunctions.not;
import static de.ruu.lib.util.StringBuilders.sb;
import static java.lang.String.join;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.Generator;
import de.ruu.lib.gen.java.Visibility;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import org.jspecify.annotations.NonNull;

/** common (base) generator for java {@link Element} modifiers */
public interface GeneratorModifiers extends Generator
{
	GeneratorModifiers visibility (@NonNull Visibility visibility);
	Visibility visibility();

	/** @throws IllegalArgumentException if param value is true and isAbstract is true */
	GeneratorModifiers setStatic  (boolean isStatic  ) throws IllegalArgumentException;
	boolean isStatic();

	/** @throws IllegalArgumentException if param value is true and isAbstract is true */
	GeneratorModifiers setFinal   (boolean isFinal   ) throws IllegalArgumentException;
	boolean isFinal();

	abstract class GeneratorModifiersAbstract extends GeneratorAbstract implements GeneratorModifiers
	{
		private Visibility visibility = DEFAULT;
		private boolean    isStatic;
		private boolean    isFinal;
		private boolean    isAbstract;

		protected GeneratorModifiersAbstract(@NonNull CompilationUnitContext context) { super(context); }

		@Override public Visibility visibility() { return visibility; }
		@Override public boolean    isStatic()   { return isStatic; }
		@Override public boolean    isFinal()    { return isFinal; }
		public    boolean isAbstract()           { return isAbstract; }

		@Override public GeneratorModifiers visibility(@NonNull Visibility visibility)
		{
			this.visibility = visibility;
			return this;
		}

		@Override public GeneratorModifiers setStatic(boolean isStatic) throws IllegalArgumentException
		{
			if (isStatic && isAbstract) throw new IllegalArgumentException("illegal combination of static and abstract");
			this.isStatic = isStatic;
			return this;
		}

		@Override public GeneratorModifiers setFinal(boolean isFinal) throws IllegalArgumentException
		{
			if (isFinal && isAbstract) throw new IllegalArgumentException("illegal combination of final and abstract");
			this.isFinal = isFinal;
			return this;
		}

		@Override public StringBuilder generate() throws GeneratorException
		{
			List<String> modifiers = new ArrayList<>();

			if (not(visibility().equals(DEFAULT))) modifiers.add(visibility().asString());
			if (isFinal())                         modifiers.add(Modifier.FINAL.toString());
			if (isAbstract())                      modifiers.add(Modifier.ABSTRACT.toString());
			if (isStatic())                        modifiers.add(Modifier.STATIC.toString());

			return sb(join(" ", modifiers));
		}

		@Override public String toString()
		{
			return "GeneratorModifiersAbstract(visibility=" + visibility
					+ ", isStatic=" + isStatic + ", isFinal=" + isFinal + ", isAbstract=" + isAbstract + ")";
		}
	}

	class GeneratorModifiersSimple extends GeneratorModifiersAbstract
	{
		public GeneratorModifiersSimple(@NonNull CompilationUnitContext context) { super(context); }
	}

	static GeneratorModifiers create(@NonNull CompilationUnitContext context)
			{ return new GeneratorModifiersSimple(context); }
	static GeneratorModifiers modifiers(@NonNull CompilationUnitContext context)
			{ return create(context); }
}
