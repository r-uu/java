package de.ruu.lib.gen.java.fx.comp;

import java.io.IOException;
import java.nio.file.Path;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.util.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeneratorFXCViewCSS
{
	private static final Logger log = LoggerFactory.getLogger(GeneratorFXCViewCSS.class);
	private String packageName;
	private String simpleFileName;

	public GeneratorFXCViewCSS(String packageName, String simpleFileName)
	{
		this.packageName = packageName;
		this.simpleFileName = simpleFileName;
	}

	public void run() throws GeneratorException, IOException
	{
		Path path =
				Path.of(
						"src/gen/resources", 
						Files.toDirectoryName(packageName),
						simpleFileName + ".css");
		Files.writeToFile("", path);
		log.debug("wrote {}", path.toAbsolutePath());
	}
}