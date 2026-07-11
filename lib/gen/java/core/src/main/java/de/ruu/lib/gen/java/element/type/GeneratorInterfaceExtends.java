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
 * {@link GeneratorInterfaceExtends} for interface extends clause like <pre>extends Generator, Serializable</pre>
 */
public interface GeneratorInterfaceExtends extends Generator
{
	@Override GeneratorInterfaceExtends childNodesSeparator(@NonNull String separator);
        
	GeneratorInterfaceExtends add(@NonNull Class<?> interfaceType);
	GeneratorInterfaceExtends add(@NonNull String   interfaceType);

	abstract class GeneratorInterfaceExtendsAbstract extends GeneratorAbstract implements GeneratorInterfaceExtends
	{
		private List<String> extensions = new ArrayList<>();

		protected GeneratorInterfaceExtendsAbstract(@NonNull CompilationUnitContext context)
		{
			super(context);
			childNodesSeparator(", ");
		}

		@Override public GeneratorInterfaceExtends childNodesSeparator(@NonNull String separator)
		{
			super.childNodesSeparator(separator);
			return this;
		}

		@Override public GeneratorInterfaceExtends add(@NonNull Class<?> extension) throws UnsupportedOperationException
		{
			if (extension.isInterface() == false)
					throw new UnsupportedOperationException(extension.getName() + " is not an interface");
			extensions.add(extension.getName());
			return this;
		}

		@Override public GeneratorInterfaceExtends add(@NonNull String   extension) throws UnsupportedOperationException
		{
			extensions.add(extension);
			return this;
		}

		@Override public StringBuilder generate() throws GeneratorException
		{
			if (extensions.isEmpty()) return sb();
			return sb(" extends ").append(String.join(childNodesSeparator(), extensions));
		}
	}

	class GeneratorInterfaceExtendsSimple extends GeneratorInterfaceExtendsAbstract
	{
		protected GeneratorInterfaceExtendsSimple(@NonNull CompilationUnitContext context) { super(context); }
	}

	static GeneratorInterfaceExtends create(    @NonNull CompilationUnitContext context)
			{ return new GeneratorInterfaceExtendsSimple(context); }
	static GeneratorInterfaceExtends extensions(@NonNull CompilationUnitContext context)
			{ return create(context); }
}