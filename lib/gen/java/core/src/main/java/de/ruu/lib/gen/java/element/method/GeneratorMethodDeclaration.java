package de.ruu.lib.gen.java.element.method;

import static de.ruu.lib.util.Constants.LS;
import static de.ruu.lib.util.StringBuilders.sb;
import static javax.lang.model.element.ElementKind.FIELD;

import javax.lang.model.element.ElementKind;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.GeneratorCodeBlock;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import de.ruu.lib.gen.java.doc.GeneratorJavaDoc;
import de.ruu.lib.gen.java.element.GeneratorAnnotations;
import de.ruu.lib.gen.java.element.GeneratorElement;
import de.ruu.lib.gen.java.element.GeneratorModifiersMethod;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** generates method declaration consisting of javadoc, annotations, modifiers, type, name, parameters and throws clause */
public interface GeneratorMethodDeclaration extends GeneratorElement
{
	// narrowing methods from super
	@Override GeneratorMethodDeclaration childNodesSeparator(@NonNull String                   separator);
	@Override GeneratorMethodDeclaration javaDoc(            @NonNull GeneratorJavaDoc         javaDoc);
	@Override GeneratorMethodDeclaration annotations(        @NonNull GeneratorAnnotations     annotations);
	          GeneratorMethodDeclaration modifiers(          @NonNull GeneratorModifiersMethod methodModifiers);
	          GeneratorMethodDeclaration type(               @NonNull String                   type);
	          GeneratorMethodDeclaration parameters(         @NonNull GeneratorParameters      parameters);
	          GeneratorMethodDeclaration throwsClause(       @NonNull GeneratorThrowsClause    throwsClause);
	          GeneratorMethodDeclaration codeBlock(          @NonNull GeneratorCodeBlock       codeBlockContent);

	@Override default ElementKind elementKind() { return FIELD; }

	abstract class GeneratorDeclarationAbstract
			extends GeneratorElementAbstract implements GeneratorMethodDeclaration
	{
		private String                type;
		private GeneratorParameters   parameters;
		private GeneratorThrowsClause throwsClause;
		private GeneratorCodeBlock    codeBlock;

		protected GeneratorDeclarationAbstract(@NonNull CompilationUnitContext context, @NonNull String type, @NonNull String name)
		{
			super(context, name);
			type(type);

			parameters  (GeneratorParameters  .parameters(  context));
			throwsClause(GeneratorThrowsClause.throwsClause(context));
			codeBlock(   GeneratorCodeBlock   .codeBlokk(   context));
		}

		@Override public GeneratorMethodDeclaration childNodesSeparator(@NonNull String separator)
		{
			super.childNodesSeparator(separator);
			return this;
		}

		@Override public GeneratorMethodDeclaration javaDoc(@NonNull GeneratorJavaDoc javaDoc)
		{
			super.javaDoc(javaDoc);
			return this;
		}

		@Override public GeneratorMethodDeclaration annotations(@NonNull GeneratorAnnotations annotations)
		{
			super.annotations(annotations);
			return this;
		}

		@Override public GeneratorMethodDeclaration modifiers(@NonNull GeneratorModifiersMethod methodModifiers)
		{
			super.modifiers(methodModifiers);
			return this;
		}

		@Override public GeneratorMethodDeclaration type(@NonNull String type)
		{
			// constructor methods have empty type
//			if (type.isEmpty()) throw new IllegalArgumentException("type must not be empty");
			this.type = type;
			return this;
		}

		@Override public GeneratorMethodDeclaration parameters(@NonNull GeneratorParameters parameters)
		{
			this.parameters = parameters;
			return this;
		}

		@Override public GeneratorMethodDeclaration throwsClause(@NonNull GeneratorThrowsClause throwsClause)
		{
			this.throwsClause = throwsClause;
			return this;
		}

		@Override public GeneratorMethodDeclaration codeBlock(@NonNull GeneratorCodeBlock codeBlock)
		{
			this.codeBlock = codeBlock;
			return this;
		}

		protected GeneratorCodeBlock codeBlock() { return codeBlock; }

		/**
		 * appends
		 * <p> {@link #javaDoc()}   (if present),
		 * <p> {@link #modifiers()} (if present),
		 * <p> {@link #type}        (if present)
		 * <p> {@link #name()}
		 * <p> {@link #parameters} and
		 * <p> {@link #throwsClause}
		 */
		@Override public StringBuilder generate() throws GeneratorException
		{
			StringBuilder result = sb(generateJavaDocAnnotationsAndModifiers());

			StringBuilder modifiers = modifiers().generate();
			if (modifiers.isEmpty() == false)
			{
				// separate modifiers from field type
				result.append(" ");
			}
			else
			{
				StringBuilder javaDoc     = javaDoc().generate();
				StringBuilder annotations = annotations().generate();
				if (javaDoc.isEmpty() == false || annotations.isEmpty() == false)
				{
					// separate javadoc / annotations from method type
					result.append(LS);
				}
			}

			// constructor declarations have no type
			if (type.isEmpty() == false)
					result.append(type).append(" ");

			result
					.append(name())
					.append(parameters.generate());
					
			StringBuilder localThrowsClause = throwsClause.generate();

			if (localThrowsClause.isEmpty() == false)
					result.append(" ").append(throwsClause.generate());

			return result;
		}
	}

	class GeneratorDeclarationSimple extends GeneratorDeclarationAbstract
	{
		public GeneratorDeclarationSimple(
				@NonNull CompilationUnitContext context, @NonNull String type, @NonNull String name)
		{ super(context, type, name); }
	}

	static GeneratorMethodDeclaration create(
			@NonNull CompilationUnitContext context, @NonNull String type, @NonNull String name)
	{ return new GeneratorDeclarationSimple(context, type, name); }

	static GeneratorMethodDeclaration declaration(
			@NonNull CompilationUnitContext context, @NonNull String type, @NonNull String name)
	{ return create(context, type, name); }
}