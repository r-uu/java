package de.ruu.lib.gen.java.element.type;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.GeneratorCodeBlock;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import de.ruu.lib.gen.java.doc.GeneratorJavaDoc;
import de.ruu.lib.gen.java.element.GeneratorAnnotations;
import de.ruu.lib.gen.java.element.GeneratorModifiers;
import org.jspecify.annotations.NonNull;

import javax.lang.model.element.ElementKind;

import static de.ruu.lib.util.BooleanFunctions.not;
import static de.ruu.lib.util.Constants.LS;
import static javax.lang.model.element.ElementKind.CLASS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface GeneratorClass extends GeneratorType
{
	@Override GeneratorClass childNodesSeparator(@NonNull String                   separator);
	@Override GeneratorClass javaDoc(            @NonNull GeneratorJavaDoc         javaDoc);
	@Override GeneratorClass annotations(        @NonNull GeneratorAnnotations     annotations);
	@Override GeneratorClass modifiers(          @NonNull GeneratorModifiers       modifiers);

	          GeneratorClass extendsClause(      @NonNull GeneratorClassExtends    extendsClause);
	          GeneratorClass implementsClause(   @NonNull GeneratorClassImplements implementsClause);

	@Override GeneratorClass codeBlock(          @NonNull GeneratorCodeBlock       codeBlock);

	abstract class GeneratorClassAbstract
			extends GeneratorTypeAbstract implements GeneratorClass
	{
		private GeneratorClassExtends    extendsClause;
		private GeneratorClassImplements implementsClause;
		private GeneratorCodeBlock       codeBlock;

		public GeneratorClassAbstract(@NonNull CompilationUnitContext context, @NonNull String name)
		{
			super(context, name);
			
			extendsClause    = GeneratorClassExtends   .extendsClause(   context);
			implementsClause = GeneratorClassImplements.implementsClause(context);
			codeBlock        = GeneratorCodeBlock      .codeBlokk(       context);
		}

		@Override public ElementKind elementKind()
		{ return CLASS; }

		@Override public GeneratorClass childNodesSeparator(@NonNull String separator)
		{
			super.childNodesSeparator(separator);
			return this;
		}

		@Override public GeneratorClass javaDoc(@NonNull GeneratorJavaDoc javaDoc)
		{
			super.javaDoc(javaDoc);
			return this;
		}

		@Override public GeneratorClass annotations(@NonNull GeneratorAnnotations annotations)
		{
			super.annotations(annotations);
			return this;
		}

		@Override public GeneratorClass modifiers(@NonNull GeneratorModifiers modifiers)
		{
			super.modifiers(modifiers);
			return this;
		}

		@Override public GeneratorClass extendsClause(@NonNull GeneratorClassExtends extendsClause)
		{
			this.extendsClause = extendsClause;
			return this;
		}

		@Override public GeneratorClass implementsClause(@NonNull GeneratorClassImplements implementsClause)
		{
			this.implementsClause = implementsClause;
			return this;
		}

		@Override public GeneratorClass codeBlock(@NonNull GeneratorCodeBlock codeBlock)
		{
			this.codeBlock = codeBlock;
			return this;
		}

		@Override public StringBuilder generate() throws GeneratorException
		{
			StringBuilder importStatements = context().importManager().generateImportStatements();
			
			if (not(importStatements.isEmpty())) importStatements.append(LS).append(LS);

			return
					packageStatement.generate()
							.append(LS)
							.append(LS)
							.append(importStatements)
							.append(super.generate())
							.append(extendsClause.generate())
							.append(implementsClause.generate())
							.append(LS)
							.append(codeBlock.generate())
							;
		}
	}

	class GeneratorClassSimple extends GeneratorClassAbstract
	{
		public GeneratorClassSimple(@NonNull CompilationUnitContext context, @NonNull String name)
		{ super(context, name); }
	}

	static GeneratorClass create(   @NonNull CompilationUnitContext context, @NonNull String name)
	{ return new GeneratorClassSimple(context, name); }

	static GeneratorClass classType(@NonNull CompilationUnitContext context, @NonNull String name)
	{ return create(context, name); }
}