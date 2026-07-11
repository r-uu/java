package de.ruu.lib.gen.java.element.pckg;

import static de.ruu.lib.gen.java.element.pckg.GeneratorPackageStatement.pckgStatement;
import static de.ruu.lib.util.Constants.LS;
import static de.ruu.lib.util.StringBuilders.sb;
import static javax.lang.model.element.ElementKind.PACKAGE;

import javax.lang.model.element.ElementKind;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import de.ruu.lib.gen.java.doc.GeneratorJavaDoc;
import de.ruu.lib.gen.java.element.GeneratorAnnotations;
import de.ruu.lib.gen.java.element.GeneratorElement;
import de.ruu.lib.gen.java.element.GeneratorModifiers;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Package declarations are contained in {@code package-info.java} files. In addition to simple
 * package statements (see {@link GeneratorPackageStatement}) they may have javadoc (see {@link
 * GeneratorJavaDoc}) and annotations (see {@link GeneratorAnnotations}.
 */
public interface GeneratorPackageDeclaration extends GeneratorElement
{
	/**
	 * delegates to {@link #name(String)}
	 * @param fullyQualifiedName
	 * @return {@code this}
	 */
	GeneratorPackageDeclaration fullyQualifiedName(@NonNull String fullyQualifiedName);

	/** narrowing and disallowing method from super */
	@Override default GeneratorPackageDeclaration modifiers(@NonNull GeneratorModifiers modifiers)
	{
		throw new UnsupportedOperationException(
				"adding " + modifiers.getClass().getName() + " generator is not supported for package declarations");
	}

	abstract class GeneratorPackageDeclarationAbstract
			extends GeneratorElementAbstract implements GeneratorPackageDeclaration
	{
		public GeneratorPackageDeclarationAbstract(
				@NonNull CompilationUnitContext context, @NonNull String fullyQualifiedName)
		{
			super(context, fullyQualifiedName);
		}

		@Override public GeneratorPackageDeclaration fullyQualifiedName(@NonNull String fullyQualifiedName)
		{
			name(fullyQualifiedName);
			return this;
		}

		@Override public GeneratorPackageDeclaration modifiers(@NonNull GeneratorModifiers modifiers)
		{
			return GeneratorPackageDeclaration.super.modifiers(modifiers);
		}

		@Override public GeneratorPackageDeclaration javaDoc(@NonNull GeneratorJavaDoc javaDoc)
		{
			super.javaDoc(javaDoc);
			return this;
		}

		@Override public GeneratorPackageDeclaration annotations(@NonNull GeneratorAnnotations annotations)
		{
			super.annotations(annotations);
			return this;
		}

		@Override public ElementKind elementKind() { return PACKAGE; }

		@Override public StringBuilder generate() throws GeneratorException
		{
			StringBuilder result = sb(); 

			StringBuilder javaDoc = javaDoc().generate();
			if (javaDoc.isEmpty() == false)
					result.append(javaDoc).append(LS);

			StringBuilder annotations = annotations().generate();
			if (annotations.isEmpty() == false)
					result.append(annotations).append(LS);

			return result.append(pckgStatement(context(), name()).generate().toString());
		}
	}
	
	class GeneratorPackageDeclarationSimple extends GeneratorPackageDeclarationAbstract
	{
		public GeneratorPackageDeclarationSimple(
				@NonNull CompilationUnitContext context, @NonNull String fullyQualifiedName)
		{ super(context, fullyQualifiedName); }
	}

	static GeneratorPackageDeclaration create(
			@NonNull CompilationUnitContext context, @NonNull String fullyQualifiedName)
	{ return new GeneratorPackageDeclarationSimple(context, fullyQualifiedName); }

	static GeneratorPackageDeclaration pckg(
			@NonNull CompilationUnitContext context, @NonNull String fullyQualifiedName)
	{ return create(context, fullyQualifiedName); }
}