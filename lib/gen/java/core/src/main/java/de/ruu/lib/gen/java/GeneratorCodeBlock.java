package de.ruu.lib.gen.java;

import static de.ruu.lib.util.Constants.LS;
import static de.ruu.lib.util.StringBuilders.sb;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.LineIndenter;
import de.ruu.lib.gen.java.context.CompilationUnitContext;
import de.ruu.lib.gen.java.element.field.GeneratorField;
import de.ruu.lib.gen.java.element.method.GeneratorMethod;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** code block will be indented and wrapped in curly braces */
public interface GeneratorCodeBlock extends Generator
{
	@Override GeneratorCodeBlock childNodesSeparator(@NonNull String separator);

	GeneratorCodeBlock add(@NonNull GeneratorField  field);
	GeneratorCodeBlock add(@NonNull GeneratorMethod method);
	GeneratorCodeBlock add(@NonNull String          output);

	abstract class GeneratorCodeBlockAbstract
			extends GeneratorAbstract implements GeneratorCodeBlock
	{
		public GeneratorCodeBlockAbstract(@NonNull CompilationUnitContext context)
				{ super(context); }

		@Override public GeneratorCodeBlock childNodesSeparator(@NonNull String separator)
		{
			super.childNodesSeparator(separator);
			return this;
		}

		@Override public GeneratorCodeBlock add(@NonNull GeneratorField field)
		{
			super.add(field);
			return this;
		}

		@Override public GeneratorCodeBlock add(@NonNull GeneratorMethod method)
		{
			super.add(method);
			return this;
		}

		@Override public GeneratorCodeBlock add(@NonNull String output)
		{
			super.add(Generator.GeneratorSimple.create(context()).output(output));
			return this;
		}

		@Override public StringBuilder generate() throws GeneratorException
		{
			LineIndenter indenter = new LineIndenter("\t", 1);
			String result = indenter.indent(super.generate().toString());
			if (result.isEmpty() == false) result += LS;
			return sb("{").append(LS).append(result).append("}");
		}
	}

	class GeneratorCodeBlockSimple extends GeneratorCodeBlockAbstract
	{
		public GeneratorCodeBlockSimple(@NonNull CompilationUnitContext context)
				{ super(context); }
	}

	static GeneratorCodeBlock create(   @NonNull CompilationUnitContext context)
			{ return new GeneratorCodeBlockSimple(context); }

	// strange spelling in "blokk" to avoid name clashes
	static GeneratorCodeBlock codeBlokk(@NonNull CompilationUnitContext context)
			{ return create(context); }
}