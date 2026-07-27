package de.ruu.lib.fx;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.fx.comp.GeneratorFXCompBundle;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;

public class TypeGenerator
{
	private static final Logger log = LogManager.getLogger(TypeGenerator.class);

	public static void main(String[] args) throws IOException, GeneratorException { generate(); }

	private static void generate() throws IOException, GeneratorException
	{
		GeneratorFXCompBundle generator;
//		log.debug("create java fx component bundle for add button view");
//		generator =
//				new GeneratorFXCompBundle
//						(
//								"de.ruu.lib.fx.control.buttons",
//								"Add"
//						);
//		generator.run();
//		log.debug("create java fx component bundle for remove button view");
//		generator =
//				new GeneratorFXCompBundle
//						(
//								"de.ruu.lib.fx.control.buttons",
//								"Remove"
//						);
//		generator.run();
//		log.debug("create java fx component bundle for edit button view");
//		generator =
//				new GeneratorFXCompBundle
//						(
//								"de.ruu.lib.fx.control.buttons",
//								"Edit"
//						);
//		generator.run();
		log.debug("create java fx component bundle for edit button view");
		generator =
				new GeneratorFXCompBundle
						(
								"de.ruu.app.pragma.client.fx.task.view.hierarchy",
								"TaskHierarchyAbstract"
						);
		generator.run();
	}
}