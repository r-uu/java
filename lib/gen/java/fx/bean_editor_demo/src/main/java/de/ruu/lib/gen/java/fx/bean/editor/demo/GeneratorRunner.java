package de.ruu.lib.gen.java.fx.bean.editor.demo;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.bean.BeanGenerator;
import de.ruu.lib.gen.java.fx.bean.FXBeanGenerator;
import de.ruu.lib.gen.java.fx.bean.FXProperty;
import de.ruu.lib.gen.java.fx.bean.editor.FXBeanViewFXMLGenerator;
import de.ruu.lib.gen.java.fx.comp.GeneratorFXCompBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Analyses a provided {@link JavaClass} that represents an existing java type (source). It
 * generates artifacts for a java fx editor control.
 */
class GeneratorRunner
{
	private static final Logger log = LoggerFactory.getLogger(GeneratorRunner.class);
	interface JavaModelDemo
	{
		@FXProperty boolean      aBoolean();
		@FXProperty int          anInteger();
		@FXProperty String       aString();
		@FXProperty BigDecimal   aBigDecimal();
		@FXProperty List<String> stringList();
	}

	class JavaModelDemoDTO
	{
		JavaModelDemoDTO(boolean aBoolean, int anInteger, String aString, BigDecimal aBigDecimal, java.util.List<String> stringList)
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

	public static void main(String[] args) throws GeneratorException, IOException
	{
		log.debug("create java bean class based on interface JavaModelDemo");
		BeanGenerator beanGenerator =
				new BeanGenerator
				(
						GeneratorRunner.class.getPackageName(),
						"JavaBeanDemo",
						new ClassFileImporter().importClass(JavaModelDemo.class)
				);

		beanGenerator.run();

		log.debug("create java fx bean class based on interface JavaModelDemo");
		FXBeanGenerator fxBeanGenerator =
				new FXBeanGenerator
				(
						GeneratorRunner.class.getPackageName(),
						"FXBeanDemo",
						new ClassFileImporter().importClass(JavaModelDemo.class),
						new ClassFileImporter().importClass(JavaModelDemoDTO.class)
				);

		fxBeanGenerator.run();

		log.debug("create java fxml for bean editor based on interface JavaModelDemo");
		FXBeanViewFXMLGenerator fxBeanEditorFXMLGenerator =
				new FXBeanViewFXMLGenerator
				(
						GeneratorRunner.class.getPackageName(),
						"FXBeanEditorDemo.grid.",
						new ClassFileImporter().importClass(JavaModelDemo.class)
				);

		fxBeanEditorFXMLGenerator.run();

		log.debug("create java fx component bundle");
		GeneratorFXCompBundle fxBeanEditorComponentBundleGenerator =
				new GeneratorFXCompBundle
				(
						GeneratorRunner.class.getPackageName(),
						"FXBeanEditorDemo"
				);

		fxBeanEditorComponentBundleGenerator.run();
	}
}