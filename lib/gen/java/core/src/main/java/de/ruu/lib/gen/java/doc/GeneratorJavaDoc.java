package de.ruu.lib.gen.java.doc;

import static de.ruu.lib.util.Constants.LS;
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
 * {@link Generator} for JavaDoc comments like
 * 
 * <pre>
 * /**
 *  * sample java doc comment
 *  *{@code /}
 * </pre>
 */
public interface GeneratorJavaDoc extends Generator {
	GeneratorJavaDoc add(String line);

	abstract class GeneratorJavaDocAbstract extends GeneratorAbstract implements GeneratorJavaDoc {
		protected GeneratorJavaDocAbstract(@NonNull CompilationUnitContext context) {
			super(context);
		}

		private List<String> lines = new ArrayList<>();

		@Override
		public GeneratorJavaDoc add(String line) {
			lines.add(line);
			return this;
		}

		@Override
		public StringBuilder generate() throws GeneratorException {
			if (lines.isEmpty())
				return sb();

			List<String> localLines = new ArrayList<>();
			localLines.add("/**");
			lines.forEach(l -> localLines.add(" * " + l));
			localLines.add(" */");

			return sb(String.join(LS, localLines));
		}
	}

	class GeneratorJavaDocSimple extends GeneratorJavaDocAbstract {
		protected GeneratorJavaDocSimple(@NonNull CompilationUnitContext context) {
			super(context);
		}
	}

	static GeneratorJavaDoc create(@NonNull CompilationUnitContext context) {
		return new GeneratorJavaDocSimple(context);
	}

	static GeneratorJavaDoc javaDoc(@NonNull CompilationUnitContext context) {
		return create(context);
	}
}