package de.ruu.lib.gen.java.context;

import static de.ruu.lib.gen.java.Generator.generator;
import static de.ruu.lib.gen.java.context.CompilationUnitContext.context;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


import org.junit.jupiter.api.Test;

import java.util.List;

import de.ruu.lib.gen.java.Generator;

class CompilationUnitContextTest
{
	@Test void parameterisedContext()
	{
		String packageName    = "package.name";
		String simpleFileName = "SimpleFileName";

		CompilationUnitContext context = context(packageName, simpleFileName);

		assertThat(context).isNotNull();

		assertThat(context.packageName()   .toString()).isEqualTo(packageName);
		assertThat(context.simpleFileName().toString()).isEqualTo(simpleFileName);

		assertThat(context.registeredGenerators()).isNotNull();
		assertThat(context.registeredGenerators()).isEqualTo(List.of());

		assertThat(context.importManager().targetCompilationUnitPackageName()).isEqualTo(packageName);
		assertThat(context.importManager().targetCompilationUnitSimpleName() ).isEqualTo(simpleFileName);

		Generator generator = generator(context);
		assertThat(context.registeredGenerators().size()).isEqualTo(0); // generator was not registered

		context.register(generator);
		assertThat(context.registeredGenerators().size()).isEqualTo(1); // generator was     registered once

		assertThatThrownBy(() -> context.register(generator))
				.isInstanceOf(UnsupportedOperationException.class)
				.hasMessageContaining("generator can not be registered more than once");
	}
}