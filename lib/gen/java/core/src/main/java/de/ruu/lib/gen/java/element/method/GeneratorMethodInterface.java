package de.ruu.lib.gen.java.element.method;

import static de.ruu.lib.util.StringBuilders.sb;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.GeneratorCodeBlock;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import de.ruu.lib.gen.java.doc.GeneratorJavaDoc;
import de.ruu.lib.gen.java.element.GeneratorAnnotations;
import de.ruu.lib.gen.java.element.GeneratorModifiers;
import org.jspecify.annotations.NonNull;

public interface GeneratorMethodInterface extends GeneratorMethodDeclaration
{
	GeneratorMethodInterface childNodesSeparator(@NonNull String separator);
	GeneratorMethodInterface javaDoc(            @NonNull GeneratorJavaDoc javaDoc);
	GeneratorMethodInterface annotations(        @NonNull GeneratorAnnotations annotations);
	GeneratorMethodInterface modifiers(          @NonNull GeneratorModifiers methodModifiers);
	GeneratorMethodInterface type(               @NonNull String type);
	GeneratorMethodInterface parameters(         @NonNull GeneratorParameters parameters);
	GeneratorMethodInterface throwsClause(       @NonNull GeneratorThrowsClause throwsClause);
	GeneratorMethodInterface codeBlock(          @NonNull GeneratorCodeBlock codeBlockContent);
	GeneratorMethodInterface isDefault(          boolean isDefault);

	abstract class GeneratorMethodInterfaceAbstract extends GeneratorDeclarationAbstract implements GeneratorMethodInterface
	{
		private boolean isDefault = false;

		protected GeneratorMethodInterfaceAbstract(
				@NonNull CompilationUnitContext context, @NonNull String type, @NonNull String name)
		{ super(context, type, name); }

		public boolean isDefault() { return isDefault; }

		@Override public GeneratorMethodInterfaceAbstract isDefault(boolean v)
		{
			this.isDefault = v;
			return this;
		}

		@Override public GeneratorMethodInterface childNodesSeparator(@NonNull String separator)
		{
			super.childNodesSeparator(separator);
			return this;
		}

		@Override public GeneratorMethodInterface javaDoc(@NonNull GeneratorJavaDoc javaDoc)
		{
			super.javaDoc(javaDoc);
			return this;
		}

		@Override public GeneratorMethodInterface annotations(@NonNull GeneratorAnnotations annotations)
		{
			super.annotations(annotations);
			return this;
		}

		@Override public GeneratorMethodInterface modifiers(@NonNull GeneratorModifiers methodModifiers)
		{
			super.modifiers(methodModifiers);
			return this;
		}

		@Override public GeneratorMethodInterface type(@NonNull String type)
		{
			super.type(type);
			return this;
		}

		@Override public GeneratorMethodInterface parameters(@NonNull GeneratorParameters parameters)
		{
			super.parameters(parameters);
			return this;
		}

		@Override public GeneratorMethodInterface throwsClause(@NonNull GeneratorThrowsClause throwsClause)
		{
			super.throwsClause(throwsClause);
			return this;
		}

		@Override public GeneratorMethodInterface codeBlock(@NonNull GeneratorCodeBlock codeBlockContent)
		{
			super.codeBlock(codeBlockContent);
			return this;
		}

		@Override public StringBuilder generate() throws GeneratorException
		{
			if (isDefault == false)
					return super.generate().append(";");
			return
					sb("default ")
							.append(super.generate())
							.append(childNodesSeparator())
							.append(codeBlock().generate());
		}
	}

	class GeneratorMethodInterfaceSimple extends GeneratorMethodInterfaceAbstract
	{
		protected GeneratorMethodInterfaceSimple(
				@NonNull CompilationUnitContext context, @NonNull String type, @NonNull String name)
		{
			super(context, type, name);
		}
	}

	static GeneratorMethodInterface create(
			@NonNull CompilationUnitContext context, @NonNull String type, @NonNull String name)
	{
		return new GeneratorMethodInterfaceSimple(context, type, name);
	}

	static GeneratorMethodInterface interfaceMethod(
			@NonNull CompilationUnitContext context, @NonNull String type, @NonNull String name)
	{
		return create(context, type, name);
	}
}
