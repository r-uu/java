package de.ruu.lib.util.classpath;

import java.io.File;

public class ClasspathResourceFile extends ClasspathResource
{
	private File file;

	public ClasspathResourceFile(ResourceContainer resourcecontainer, File file)
	{
		super(resourcecontainer, file.getPath());
		this.file = file;
	}

	@Override public boolean directory() { return file.isDirectory(); }
}