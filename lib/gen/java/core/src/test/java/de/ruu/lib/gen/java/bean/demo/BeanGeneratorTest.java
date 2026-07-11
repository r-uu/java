package de.ruu.lib.gen.java.bean.demo;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.importer.ClassFileImporter;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.bean.BeanGenerator;

class BeanGeneratorTest
{
	interface JavaModelDemo
	{
		boolean      aBoolean();
		int          anInteger();
		String       aString();
		BigDecimal   aBigDecimal();
		List<String> stringList();
	}

	@Test void test() throws GeneratorException, IOException
	{
		BeanGenerator generator =
				new BeanGenerator
				(
						JavaModelDemo.class.getPackageName(),
						"JavaBeanDemo",
						new ClassFileImporter().importClass(JavaModelDemo.class)
				);

		generator.run();
	}
}