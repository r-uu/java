package de.ruu.lib.gen.java.element.type;

import static de.ruu.lib.util.Constants.LS;
import static javax.lang.model.element.ElementKind.INTERFACE;

import javax.lang.model.element.ElementKind;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.GeneratorCodeBlock;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import de.ruu.lib.gen.java.doc.GeneratorJavaDoc;
import de.ruu.lib.gen.java.element.GeneratorAnnotations;
import de.ruu.lib.gen.java.element.GeneratorModifiers;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface GeneratorInterface extends GeneratorType
{
	@Override GeneratorInterface childNodesSeparator(@NonNull String               separator);
	@Override GeneratorInterface javaDoc(            @NonNull GeneratorJavaDoc     javaDoc);
	@Override GeneratorInterface annotations(        @NonNull GeneratorAnnotations annotations);
	@Override GeneratorInterface modifiers(          @NonNull GeneratorModifiers   modifiers);

	          GeneratorInterface extendsClause(@NonNull GeneratorInterfaceExtends extendsClause);

	@Override GeneratorInterface codeBlock(          @NonNull GeneratorCodeBlock   codeBlock);

	abstract class GeneratorInterfaceAbstract
			extends GeneratorTypeAbstract implements GeneratorInterface
	{
		private GeneratorInterfaceExtends extendsClause;
		private GeneratorCodeBlock        codeBlock;

		public GeneratorInterfaceAbstract(@NonNull CompilationUnitContext context, @NonNull String name)
		{
			super(context, name);
			
			extendsClause = GeneratorInterfaceExtends.extensions(context);
			codeBlock     = GeneratorCodeBlock       .codeBlokk( context);
		}

		@Override public ElementKind elementKind()
		{ return INTERFACE; }

		@Override public GeneratorInterface childNodesSeparator(@NonNull String separator)
		{
			super.childNodesSeparator(separator);
			return this;
		}

		@Override public GeneratorInterface javaDoc(@NonNull GeneratorJavaDoc javaDoc)
		{
			super.javaDoc(javaDoc);
			return this;
		}

		@Override public GeneratorInterface annotations(@NonNull GeneratorAnnotations annotations)
		{
			super.annotations(annotations);
			return this;
		}

		@Override public GeneratorInterface modifiers(@NonNull GeneratorModifiers modifiers)
		{
			super.modifiers(modifiers);
			return this;
		}

		@Override public GeneratorInterface extendsClause(@NonNull GeneratorInterfaceExtends extendsClause)
		{
			this.extendsClause = extendsClause;
			return this;
		}

		@Override public GeneratorInterface codeBlock(@NonNull GeneratorCodeBlock codeBlock)
		{
			super.codeBlock(codeBlock);
			return this;
		}

		@Override public StringBuilder generate() throws GeneratorException
		{
			StringBuilder importStatements = context().importManager().generateImportStatements();
			
			if (importStatements.isEmpty() == false) importStatements.append(LS + LS);

			return
					packageStatement.generate()
							.append(LS)
							.append(LS)
							.append(importStatements)
							.append(super.generate())
							.append(extendsClause.generate())
							.append(LS)
							.append(codeBlock.generate())
							;
		}
	}

	class GeneratorInterfaceSimple extends GeneratorInterfaceAbstract
	{
		public GeneratorInterfaceSimple(@NonNull CompilationUnitContext context, @NonNull String name)
		{ super(context, name); }
	}

	static GeneratorInterface create(@NonNull CompilationUnitContext context, @NonNull String name)
	{
		return new GeneratorInterfaceSimple(context, name);
	}
	
	static GeneratorInterface interfaceType(@NonNull CompilationUnitContext context, @NonNull String name)
	{
		return create(context, name);
	}
}