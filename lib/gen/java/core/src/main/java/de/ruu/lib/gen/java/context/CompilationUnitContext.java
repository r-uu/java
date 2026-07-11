package de.ruu.lib.gen.java.context;

import static de.ruu.lib.util.StringBuilders.sb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import de.ruu.lib.gen.java.Generator;
import de.ruu.lib.gen.java.naming.ImportManager;
import org.jspecify.annotations.NonNull;

public interface CompilationUnitContext
{
	/** @return the import manager of a Java compilation unit */
	ImportManager importManager();

	/** @return the package name of a Java compilation unit */
	default StringBuilder packageName() { return sb(importManager().targetCompilationUnitPackageName()); }

	/** @return the simple file name of a Java compilation unit */
	default StringBuilder simpleFileName() { return sb(importManager().targetCompilationUnitSimpleName()); }

	/**
	 * Do not use {@link Set} because {@link Generator} is not immutable, so {@link Object#equals(Object)}
	 * and {@link Object#hashCode()} may not work properly and break the unique element constraint in {@link Set}.
	 *
	 * @return unmodifiable list of all registered generators
	 */
	List<Generator> registeredGenerators();

	/** @throws UnsupportedOperationException if {@code generator} is attempted to be registered more than once */
	List<Generator> register(Generator generator) throws UnsupportedOperationException;

	abstract class CompilationUnitContextAbstract implements CompilationUnitContext
	{
		private ImportManager importManager;

		/**
		 * Contains each generator that has been registered since the last call to {@link #reset(String, String)}.
		 */
		private final List<Generator> registeredGenerators = new ArrayList<>();

		@Override public ImportManager importManager() { return importManager; }

		@Override public List<Generator> register(Generator generator) throws UnsupportedOperationException
		{
//			log.debug("registration request for type {}, hash {}", generator.getClass().getName(), generator.hashCode());
			if (isRegistered(generator))
					throw new UnsupportedOperationException("generator can not be registered more than once");

			registeredGenerators.add(generator);

			return Collections.unmodifiableList(registeredGenerators);
		}

		@Override public List<Generator> registeredGenerators() { return Collections.unmodifiableList(registeredGenerators); }

		protected CompilationUnitContextAbstract(@NonNull String packageName, @NonNull String simpleFileName)
		{
			if (packageName   .isEmpty()) throw new IllegalArgumentException(    "package name may not be empty");
			if (simpleFileName.isEmpty()) throw new IllegalArgumentException("simple file name may not be empty");

			importManager = new ImportManager(packageName, simpleFileName);
		}

		private boolean isRegistered(Generator generator)
		{
			for (Generator registeredGenerator : registeredGenerators)
			{
				if (registeredGenerator == generator) return true;
			}

			return false;
		}
	}

	class CompilationUnitContextSimple extends CompilationUnitContextAbstract
	{
		/** {@code private} modifier makes sure that instances can not be created outside. */
		private CompilationUnitContextSimple(@NonNull String packageName, @NonNull String simpleFileName)
		{
			super(packageName, simpleFileName);
		}
	}

	static CompilationUnitContext context(@NonNull String packageName, @NonNull String simpleFileName)
	{ return new CompilationUnitContextSimple(packageName, simpleFileName); }
}
