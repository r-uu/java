package de.ruu.lib.gen.java.naming;

import static de.ruu.lib.util.StringBuilders.sb;

import de.ruu.lib.gen.Generator;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

/**
 * Import statements look like {@code import x.y.z.T;} where x.y.z.T is the fully qualified name of type T. Import
 * statements can also define imports for static fields or static methods.
 */
public interface GeneratorImportStatement extends Generator
{
	class GeneratorImportStatementAbstract implements GeneratorImportStatement
	{
		private final String fullyQualifiedName;

		public GeneratorImportStatementAbstract(String fullyQualifiedName)
		{
			this.fullyQualifiedName = fullyQualifiedName;
		}

		public String fullyQualifiedName() { return fullyQualifiedName; }

		@Override public StringBuilder generate() { return sb("import " + fullyQualifiedName + ";"); }

		@Override public boolean equals(Object o)
		{
			if (this == o) return true;
			if (!(o instanceof GeneratorImportStatementAbstract other)) return false;
			return Objects.equals(fullyQualifiedName, other.fullyQualifiedName);
		}

		@Override public int hashCode() { return Objects.hash(fullyQualifiedName); }

		@Override public String toString()
		{
			return "GeneratorImportStatementAbstract(fullyQualifiedName=" + fullyQualifiedName + ")";
		}
	}

	class GeneratorImportStatementSimple extends GeneratorImportStatementAbstract
	{
		public GeneratorImportStatementSimple(String fullyQualifiedName) { super(fullyQualifiedName); }
	}

	static GeneratorImportStatement create(@NonNull String fullyQualifiedName)
	{
		return new GeneratorImportStatementSimple(fullyQualifiedName);
	}

	static GeneratorImportStatement importStatement(@NonNull String fullyQualifiedName) { return create(fullyQualifiedName); }
}
