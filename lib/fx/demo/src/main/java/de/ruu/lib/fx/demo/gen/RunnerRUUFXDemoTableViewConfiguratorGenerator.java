package de.ruu.lib.fx.demo.gen;

import java.io.IOException;

import com.tngtech.archunit.core.importer.ClassFileImporter;

import de.ruu.lib.fx.demo.bean.FXBean;
import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.fx.tableview.GeneratorFXTableViewConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RunnerRUUFXDemoTableViewConfiguratorGenerator
{
	private static final Logger log = LoggerFactory.getLogger(RunnerRUUFXDemoTableViewConfiguratorGenerator.class);

	public static void main(String[] args) throws GeneratorException, IOException
	{
		Class<?>                         fxBeanModelClass = FXBean.class;
		GeneratorFXTableViewConfigurator generator;

		log.debug("creating java fx table view configurator for java fx bean {}", fxBeanModelClass.getName());
		generator =
				new GeneratorFXTableViewConfigurator
				(
						fxBeanModelClass.getPackageName(),
						"FXTableViewConfigurator",
						new ClassFileImporter().importClass(fxBeanModelClass)
				);
		log.debug("created  java fx table view configurator for java fx bean {}", fxBeanModelClass.getName());

		generator.run();
	}
}