package de.ruu.lib.gen.java.fx.comp;

import java.io.IOException;
import java.nio.file.Path;

import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.util.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeneratorFXCViewFXML
{
	private static final Logger log = LoggerFactory.getLogger(GeneratorFXCViewFXML.class);
	private String packageName;
	private String simpleFileName;

	public GeneratorFXCViewFXML(String packageName, String simpleFileName)
	{
		this.packageName    = packageName;
		this.simpleFileName = simpleFileName;
	}

	public void run() throws GeneratorException, IOException
	{
		Path path =
				Path.of(
						"src/gen/resources",
						Files.toDirectoryName(packageName),
						simpleFileName + ".fxml");
		Files.writeToFile
		(
"""
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.layout.AnchorPane?>

<AnchorPane fx:id="root" maxHeight="-Infinity" maxWidth="-Infinity" minHeight="-Infinity" minWidth="-Infinity" prefHeight="400.0" prefWidth="600.0" xmlns="http://javafx.com/javafx/19" xmlns:fx="http://javafx.com/fxml/1" />
		    
""",
path);
		log.debug("wrote {}", path.toAbsolutePath());
	}
}