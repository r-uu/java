package de.ruu.lib.util.classpath;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public interface ResourceContainer
{
	Map<String, ClasspathResource> classPathResources() throws IOException;

	File resourceContainerFile();
}