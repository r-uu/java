package de.ruu.lib.gen.java.fx.tableview;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.fx.bean.FXBeanGenerator;
import de.ruu.lib.gen.java.fx.tableview.demo.FXModelDemo;
import de.ruu.lib.util.Files;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

class GeneratorFXTableViewConfiguratorTest
{
	@BeforeAll
	static void beforeAll() throws GeneratorException, IOException
	{
		String packageName    = "de.ruu.lib.gen.java.fx.tableview.demo";
		String simpleFileName = FXModelDemo.class.getSimpleName();

		FXBeanGenerator generator =
				new FXBeanGenerator
				(
						"de.ruu.lib.gen.java.fx.tableview.demo",
						"FXModelDemo",
						new ClassFileImporter().importClass(JavaModelDemo.class),
						new ClassFileImporter().importClass(JavaModelDemoDTO.class)
				);

		generator.run();
		
		Path path =
				Path.of(
						"./src/gen/resources",
						Files.toDirectoryName(packageName),
						simpleFileName + ".fxml");
		Files.writeToFile("", path);
	}

	@Test void test() throws GeneratorException, IOException
	{
		GeneratorFXTableViewConfigurator generator =
				new GeneratorFXTableViewConfigurator
				(
						"de.ruu.lib.gen.java.fx.tableview.demo",
						"FXTableViewConfigurator",
						new ClassFileImporter().importClass(FXModelDemo.class)
				);

		generator.run();
	}
}