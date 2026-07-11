package de.ruu.lib.fx.comp.buttons;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.fx.comp.GeneratorFXCompBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ButtonsGenerator
{
	private static final Logger log = LoggerFactory.getLogger(ButtonsGenerator.class);
	public static void main(String[] args) throws IOException, GeneratorException
	{
		generateButtonAdd();
	}

	private static void generateButtonAdd() throws GeneratorException, IOException

	{
		log.debug("generating add button");
		GeneratorFXCompBundle generator =
				new GeneratorFXCompBundle
						(
								"de.ruu.lib.fx.control.buttons",
								"Add"
						);

		generator.run();
	}
}