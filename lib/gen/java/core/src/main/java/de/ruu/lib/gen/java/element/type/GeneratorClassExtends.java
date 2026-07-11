package de.ruu.lib.gen.java.element.type;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.Generator;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import org.jspecify.annotations.NonNull;

import static de.ruu.lib.util.StringBuilders.sb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link GeneratorClassExtends} for interface extends clause like <pre>extends Generator, Serializable</pre>
 */
public interface GeneratorClassExtends extends Generator
{
	@Override GeneratorClassExtends childNodesSeparator(@NonNull String separator);

	GeneratorClassExtends extendsClause(@NonNull Class<?> classType);
	GeneratorClassExtends extendsClause(@NonNull String   classType);

	abstract class GeneratorClassExtendsAbstract extends GeneratorAbstract implements GeneratorClassExtends
	{
		private String extension = "";

		protected GeneratorClassExtendsAbstract(@NonNull CompilationUnitContext context)
		{
			super(context);
			childNodesSeparator(", ");
		}

		@Override public GeneratorClassExtends childNodesSeparator(@NonNull String separator)
		{
			super.childNodesSeparator(separator);
			return this;
		}

		@Override public GeneratorClassExtends extendsClause(@NonNull Class<?> classType) throws UnsupportedOperationException
		{
			if (classType.isAnnotation() || classType.isArray() || classType.isEnum() || classType.isInterface())
					throw new UnsupportedOperationException(classType.getName() + " is not a class");
			this.extension = context().importManager().useType(classType);
			return this;
		}

		@Override public GeneratorClassExtends extendsClause(@NonNull String classType) throws UnsupportedOperationException
		{
			this.extension = classType;
			return this;
		}

		@Override public StringBuilder generate() throws GeneratorException
		{
			if (extension.isEmpty()) return sb();
			return sb(" extends ").append(extension);
		}
	}

	class GeneratorClassExtendsSimple extends GeneratorClassExtendsAbstract
	{
		protected GeneratorClassExtendsSimple(@NonNull CompilationUnitContext context) { super(context); }
	}

	static GeneratorClassExtends create(@NonNull CompilationUnitContext context)
			{ return new GeneratorClassExtendsSimple(context); }
	static GeneratorClassExtends extendsClause(@NonNull CompilationUnitContext context)
			{ return create(context); }
}