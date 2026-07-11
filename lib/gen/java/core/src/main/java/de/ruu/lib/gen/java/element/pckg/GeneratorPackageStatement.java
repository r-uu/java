package de.ruu.lib.gen.java.element.pckg;

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
 * Package statements look like {@code package x.y.z;} where x, y and z are the names
 * of packages in the package hierarchy.
 */
public interface GeneratorPackageStatement extends GeneratorElement
{
	/**
	 * delegates to {@link #name(String)}
	 * @param fullyQualifiedName
	 * @return {@code this}
	 */
	GeneratorPackageStatement fullyQualifiedName(@NonNull String fullyQualifiedName);

	/** narrowing and disallowing method from super */
	@Override default GeneratorPackageStatement javaDoc(@NonNull GeneratorJavaDoc javaDoc)
	{
		throw new UnsupportedOperationException(
				"adding " + javaDoc.getClass().getName() + " generator is not supported");
	}

	/** narrowing and disallowing method from super */
	@Override default GeneratorPackageStatement annotations(@NonNull GeneratorAnnotations annotations)
	{
		throw new UnsupportedOperationException(
				"adding " + annotations.getClass().getName() + " generator is not supported");
	}

	/** narrowing and disallowing method from super */
	@Override default GeneratorPackageStatement modifiers(@NonNull GeneratorModifiers modifiers)
	{
		throw new UnsupportedOperationException(
				"adding " + modifiers.getClass().getName() + " generator is not supported");
	}

	abstract class GeneratorPackageStatementAbstract
			extends GeneratorElementAbstract implements GeneratorPackageStatement
	{
		public GeneratorPackageStatementAbstract(
				@NonNull CompilationUnitContext context, @NonNull String fullyQualifiedName)
		{ super(context, fullyQualifiedName); }

		@Override public GeneratorPackageStatement fullyQualifiedName(@NonNull String fullyQualifiedName)
		{
			name(fullyQualifiedName);
			return this;
		}

		@Override public GeneratorPackageStatement javaDoc(@NonNull GeneratorJavaDoc javaDoc)
		{
			return GeneratorPackageStatement.super.javaDoc(javaDoc);
		}

		@Override public GeneratorPackageStatement annotations(@NonNull GeneratorAnnotations annotations)
		{
			return GeneratorPackageStatement.super.annotations(annotations);
		}

		@Override public GeneratorPackageStatement modifiers(@NonNull GeneratorModifiers modifiers)
		{
			return GeneratorPackageStatement.super.modifiers(modifiers);
		}

		@Override public ElementKind elementKind() { return PACKAGE; }

		@Override public StringBuilder generate() throws GeneratorException
		{ return sb(elementKind().toString().toLowerCase() + " " + name()).append(";"); }
	}

	class GeneratorPackageStatementSimple extends GeneratorPackageStatementAbstract
	{
		public GeneratorPackageStatementSimple(
				@NonNull CompilationUnitContext context, @NonNull String fullyQualifiedName)
		{ super(context, fullyQualifiedName); }
	}

	static GeneratorPackageStatement create(
			@NonNull CompilationUnitContext context, @NonNull String fullyQualifiedName)
	{ return new GeneratorPackageStatementSimple(context, fullyQualifiedName); }

	static GeneratorPackageStatement pckgStatement(
			@NonNull CompilationUnitContext context, @NonNull String fullyQualifiedName)
	{ return create(context, fullyQualifiedName); }
}