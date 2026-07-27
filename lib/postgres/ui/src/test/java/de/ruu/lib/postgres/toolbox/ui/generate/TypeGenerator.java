package de.ruu.lib.postgres.toolbox.ui.generate;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.fx.comp.GeneratorFXCompBundle;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;

public class TypeGenerator
{
	private static final Logger log = LogManager.getLogger(TypeGenerator.class);

	public static void main(String[] args) throws IOException, GeneratorException
	{
		generateJavaFXComponentPostgresUtilUI();
	}

	private static void generateJavaFXComponentPostgresUtilUI() throws GeneratorException, IOException
	{
		log.debug("creating java fx component bundle for postgres util ui component of r-uu library");
		GeneratorFXCompBundle postgresUtilUIComponentBundleGenerator =
				new GeneratorFXCompBundle
				(
						"de.ruu.lib.postgres.util.ui.generate",
						"PostgresUtilUI"
				);

		postgresUtilUIComponentBundleGenerator.run();
		log.debug("created  java fx component bundle for postgres util ui component of r-uu library");
	}
}
