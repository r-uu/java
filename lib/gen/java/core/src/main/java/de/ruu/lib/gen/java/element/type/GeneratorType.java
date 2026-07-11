package de.ruu.lib.gen.java.element.type;

import static de.ruu.lib.gen.java.GeneratorCodeBlock.codeBlokk;
import static de.ruu.lib.gen.java.element.pckg.GeneratorPackageStatement.pckgStatement;

import javax.lang.model.element.ElementKind;

import de.ruu.lib.gen.java.GeneratorCodeBlock;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import de.ruu.lib.gen.java.element.GeneratorElement;
import de.ruu.lib.gen.java.element.GeneratorModifiers;
import de.ruu.lib.gen.java.element.pckg.GeneratorPackageStatement;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface GeneratorType extends GeneratorElement
{
	@Override GeneratorType modifiers(@NonNull GeneratorModifiers modifiers);

	GeneratorType packageStatement(@NonNull GeneratorPackageStatement statement);

	GeneratorType codeBlock(@NonNull GeneratorCodeBlock codeBlock);
	
	class GeneratorElementDelegate extends GeneratorElementAbstract
	{
		public GeneratorElementDelegate(@NonNull CompilationUnitContext context, @NonNull String name)
				{ super(context, name); }

		@Override public ElementKind elementKind() { return ElementKind.CLASS; }
	}

	abstract class GeneratorTypeAbstract
			extends GeneratorElementAbstract
			implements GeneratorType
	{
		protected GeneratorPackageStatement packageStatement;
		protected GeneratorCodeBlock        codeBlock;

		public GeneratorTypeAbstract(@NonNull CompilationUnitContext context, @NonNull String name)
		{
			super(context, name);

			packageStatement(pckgStatement(context, context.packageName().toString()));
			codeBlock(codeBlokk(context));
		}

		@Override public GeneratorType modifiers(@NonNull GeneratorModifiers modifiers)
		{
			super.modifiers(modifiers);
			return this;
		}

		@Override public GeneratorType packageStatement(@NonNull GeneratorPackageStatement packageStatement)
		{
			this.packageStatement = packageStatement;
			return this;
		}

		@Override public GeneratorType codeBlock(@NonNull GeneratorCodeBlock codeBlock)
		{
			this.codeBlock = codeBlock;
			return this;
		}
	}
}