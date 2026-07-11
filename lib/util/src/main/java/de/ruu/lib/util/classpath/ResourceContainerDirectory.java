package de.ruu.lib.util.classpath;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class ResourceContainerDirectory extends ResourceContainerBase
{
	private String containerFileName;

	/** used by {@link #getResourceFilesByName(File, FileFilter)} for an internal calculation, initialised in constructor */
	private int containerFileNameLength;

	ResourceContainerDirectory(File containerFile)
			throws IllegalArgumentException
	{
		super(containerFile);
	
		if (containerFile.isDirectory() == false)
		{
			throw new ExceptionInInitializerError(containerFile.getName() + " is not a directory");
		}
	
		containerFileName = containerFile.getAbsolutePath().replace(System.getProperty("file.separator"), "/");
		containerFileNameLength = containerFileName.length();
	}

	@Override public Map<String, ClasspathResource> classPathResources() throws IOException
	{
		return classPathResources(Classpath.FILTER_ACCEPT_FILES_ALL);
	}

	public Map<String, ClasspathResource> classPathResources(FileFilter fileFilter) throws IOException
	{
		Map<String, ClasspathResource> result = new HashMap<>();

		Map<String, File> resourceFiles = getResourceFilesByName(containerFile, fileFilter);

		for (String key : resourceFiles.keySet())
		{
			ClasspathResourceFile value =
					new ClasspathResourceFile(
							new ResourceContainerDirectory(containerFile),
							resourceFiles.get(key));

			result.put(value.resourceName(), value);
		}

		return result;
	}

	private Map<String, File> getResourceFilesByName(File currentDirectory, FileFilter fileFilter) throws IOException
	{
		Map<String, File> result = new HashMap<>();

		if (null == currentDirectory.listFiles())
		{
			return result;
		}

		for (File file : currentDirectory.listFiles())
		{
			// create new file without container file path
			String resourceFileName =
					file.getAbsolutePath()
					    .replace(System.getProperty("file.separator"), "/")
					    .substring(containerFileNameLength + 1);

			File resourceFile = new File(resourceFileName);

			if (file.isFile())
			{
				if (fileFilter.accept(resourceFile))
				{
					result.put(resourceFileName, resourceFile);
				}
			}
			else
			{
				if (fileFilter.accept(file))
				{
					result.put(resourceFileName, resourceFile);
					result.putAll(getResourceFilesByName(file, fileFilter));
				}
			}
		}

		return result;
	}
}