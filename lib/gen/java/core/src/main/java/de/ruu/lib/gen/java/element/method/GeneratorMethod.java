package de.ruu.lib.gen.java.element.method;

import static de.ruu.lib.util.StringBuilders.sb;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.GeneratorCodeBlock;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import de.ruu.lib.gen.java.doc.GeneratorJavaDoc;
import de.ruu.lib.gen.java.element.GeneratorAnnotations;
import de.ruu.lib.gen.java.element.GeneratorModifiersAbstractable;
import de.ruu.lib.gen.java.element.GeneratorModifiersMethod;
import org.jspecify.annotations.NonNull;

/** method generator for all non-interface methods (see {@link GeneratorMethodInterface} */
public interface GeneratorMethod extends GeneratorMethodDeclaration
{
	// narrowing methods from super
	@Override GeneratorMethod childNodesSeparator(@NonNull String                    separator);
	@Override GeneratorMethod javaDoc            (@NonNull GeneratorJavaDoc          javaDoc);
	@Override GeneratorMethod annotations        (@NonNull GeneratorAnnotations      annotations);
	@Override GeneratorMethod modifiers          (@NonNull GeneratorModifiersMethod  methodModifiers);
	@Override GeneratorMethod type               (@NonNull String                    type);
	@Override GeneratorMethod parameters         (@NonNull GeneratorParameters       parameters);
	@Override GeneratorMethod throwsClause       (@NonNull GeneratorThrowsClause     throwsClause);
	@Override GeneratorMethod codeBlock          (@NonNull GeneratorCodeBlock codeBlockContent);

	default GeneratorMethod isDefault(boolean isDefault) throws UnsupportedOperationException
	{
		if (isDefault) throw new UnsupportedOperationException("default not allowed for non interface methods");
		return this;
	}

	abstract class GeneratorMethodAbstract extends GeneratorDeclarationAbstract implements GeneratorMethod
	{
		protected GeneratorMethodAbstract(
				@NonNull CompilationUnitContext context, @NonNull String type, @NonNull String name)
		{ super(context, type, name); }

		@Override public GeneratorMethod childNodesSeparator(@NonNull String separator)
		{
			super.childNodesSeparator(separator);
			return this;
		}

		@Override public GeneratorMethod javaDoc(@NonNull GeneratorJavaDoc javaDoc)
		{
			super.javaDoc(javaDoc);
			return this;
		}

		@Override public GeneratorMethod annotations(@NonNull GeneratorAnnotations annotations)
		{
			super.annotations(annotations);
			return this;
		}

		@Override public GeneratorMethod modifiers(@NonNull GeneratorModifiersMethod methodModifiers)
		{
			super.modifiers(methodModifiers);
			return this;
		}

		@Override public GeneratorMethod type(@NonNull String type)
		{
			super.type(type);
			return this;
		}

		@Override public GeneratorMethod parameters(@NonNull GeneratorParameters parameters)
		{
			super.parameters(parameters);
			return this;
		}

		@Override public GeneratorMethod throwsClause(@NonNull GeneratorThrowsClause throwsClause)
		{
			super.throwsClause(throwsClause);
			return this;
		}

		@Override public GeneratorMethod codeBlock(@NonNull GeneratorCodeBlock codeBlockContent)
		{
			super.codeBlock(codeBlockContent);
			return this;
		}

		@Override public StringBuilder generate() throws GeneratorException
		{
			StringBuilder result = sb();
			if (modifiers() instanceof GeneratorModifiersAbstractable)
			{
				GeneratorModifiersAbstractable abstractable = (GeneratorModifiersAbstractable) modifiers();
				if (abstractable.isAbstract())
				{
					// no code block for abstract methods, trailing semicolon instead
					result
							.append(super.generate())
							.append(";");
					return result;
				}
			}
			result
					.append(super.generate())
					.append(childNodesSeparator())
					.append(codeBlock().generate());
			return result;
		}
	}

	class GeneratorMethodSimple extends GeneratorMethodAbstract
	{
		protected GeneratorMethodSimple(
				@NonNull CompilationUnitContext context, @NonNull String type, @NonNull String name)
		{
			super(context, type, name);
		}
	}

	static GeneratorMethod create(
			@NonNull CompilationUnitContext context, @NonNull String type, @NonNull String name)
	{
		return new GeneratorMethodSimple(context, type, name);
	}

	static GeneratorMethod method(
			@NonNull CompilationUnitContext context, @NonNull String type, @NonNull String name)
	{
		return create(context, type, name);
	}
}
