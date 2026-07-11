package de.ruu.lib.util.classpath;

import java.io.File;

abstract class ResourceContainerBase
		implements ResourceContainer
{
	protected File containerFile;

	/**
	 * @param containerFile
	 * @throws IllegalArgumentException if <code>containerFile</code> does not exist
	 */
	ResourceContainerBase(File containerFile)
			throws IllegalArgumentException
	{
		super();
	
		if (containerFile.exists() == false)
		{
			throw new IllegalArgumentException(containerFile.getAbsolutePath() + " not found");
		}
	
		this.containerFile = containerFile;
	}

	@Override
	public File resourceContainerFile()
	{
		return containerFile;
	}
}