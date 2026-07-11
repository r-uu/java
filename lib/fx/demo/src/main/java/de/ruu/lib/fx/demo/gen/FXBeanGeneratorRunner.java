package de.ruu.lib.fx.demo.gen;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import de.ruu.lib.fx.demo.gen.input.FXBeanModel;
import de.ruu.lib.fx.demo.gen.input.FXBeanModelDTO;
import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.fx.bean.FXBeanGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

class FXBeanGeneratorRunner
{
	private static final Logger log = LoggerFactory.getLogger(FXBeanGeneratorRunner.class);

	public static void main(String[] args) throws GeneratorException, IOException
	{
		Class<?>        fxBeanModelClass       = FXBeanModel.class;
		Class<?>        fxBeanModelSourceClass = FXBeanModelDTO.class;
		FXBeanGenerator generator;

		log.debug("creating java fx bean for java fx bean model {}", fxBeanModelClass.getName());
		generator =
				new FXBeanGenerator
				(
						fxBeanModelClass.getPackageName(),
						"FXBean",
						new ClassFileImporter().importClass(fxBeanModelClass),
						new ClassFileImporter().importClass(fxBeanModelSourceClass)
				);
		log.debug("created  java fx bean for java fx bean model {}", fxBeanModelClass.getName());

		generator.run();
	}
}