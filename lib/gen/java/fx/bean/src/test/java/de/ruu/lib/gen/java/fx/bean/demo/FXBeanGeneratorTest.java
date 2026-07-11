package de.ruu.lib.gen.java.fx.bean.demo;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.fx.bean.FXBeanGenerator;
import de.ruu.lib.gen.java.fx.bean.FXProperty;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

class FXBeanGeneratorTest
{
	interface FXModelDemo
	{
		@FXProperty boolean      aBoolean();
		@FXProperty int          anInteger();
		@FXProperty String       aString();
		@FXProperty BigDecimal   aBigDecimal();
		@FXProperty List<String> stringList();
	}

	class FXModelDemoDTO
	{
		FXModelDemoDTO(boolean aBoolean, int anInteger, String aString, BigDecimal aBigDecimal, java.util.List<String> stringList)
		{
			this.aBoolean    = aBoolean;
			this.anInteger   = anInteger;
			this.aString     = aString;
			this.aBigDecimal = aBigDecimal;
			this.stringList  = stringList;
		}
		boolean      aBoolean;
		int          anInteger;
		String       aString;
		BigDecimal   aBigDecimal;
		List<String> stringList;
	}

	@Test void test() throws GeneratorException, IOException
	{
		FXBeanGenerator generator =
				new FXBeanGenerator
				(
						FXModelDemo.class.getPackageName(),
						"FXBean",
						new ClassFileImporter().importClass(FXModelDemo.class),
						new ClassFileImporter().importClass(FXModelDemoDTO.class)
				);

		generator.run();
	}
}