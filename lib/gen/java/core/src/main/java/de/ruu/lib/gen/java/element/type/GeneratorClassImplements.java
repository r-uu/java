package de.ruu.lib.gen.java.element.type;

import static de.ruu.lib.util.StringBuilders.sb;

import java.util.ArrayList;
import java.util.List;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.Generator;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link GeneratorClassImplements} for interface extends clause like <pre>extends Generator, Serializable</pre>
 */
public interface GeneratorClassImplements extends Generator
{
	@Override GeneratorClassImplements childNodesSeparator(@NonNull String separator);
        
	GeneratorClassImplements add(@NonNull Class<?> interfaceType);
	GeneratorClassImplements add(@NonNull String   interfaceType);

	abstract class GeneratorClassImplementsAbstract extends GeneratorAbstract implements GeneratorClassImplements
	{
		private List<String> implementations = new ArrayList<>();

		protected GeneratorClassImplementsAbstract(@NonNull CompilationUnitContext context)
		{
			super(context);
			childNodesSeparator(", ");
		}

		@Override public GeneratorClassImplements childNodesSeparator(@NonNull String separator)
		{
			super.childNodesSeparator(separator);
			return this;
		}

		@Override public GeneratorClassImplements add(@NonNull Class<?> implementation) throws UnsupportedOperationException
		{
			if (implementation.isInterface() == false)
					throw new UnsupportedOperationException(implementation.getName() + " is not an interface");
			implementations.add(implementation.getName());
			return this;
		}

		@Override public GeneratorClassImplements add(@NonNull String   implementation) throws UnsupportedOperationException
		{
			implementations.add(implementation);
			return this;
		}

		@Override public StringBuilder generate() throws GeneratorException
		{
			if (implementations.isEmpty()) return sb();
			return sb(" implements ").append(String.join(childNodesSeparator(), implementations));
		}
	}

	class GeneratorClassImplementsSimple extends GeneratorClassImplementsAbstract
	{
		protected GeneratorClassImplementsSimple(@NonNull CompilationUnitContext context) { super(context); }
	}

	static GeneratorClassImplements create(    @NonNull CompilationUnitContext context)
			{ return new GeneratorClassImplementsSimple(context); }
	static GeneratorClassImplements implementsClause(@NonNull CompilationUnitContext context)
			{ return create(context); }
}