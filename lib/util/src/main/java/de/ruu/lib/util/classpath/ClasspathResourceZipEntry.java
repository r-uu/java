package de.ruu.lib.util.classpath;

import java.util.zip.ZipEntry;

public class ClasspathResourceZipEntry extends ClasspathResource
{
	private ZipEntry zipEntry;

	public ClasspathResourceZipEntry(ResourceContainer resourcecontainer, ZipEntry zipEntry)
	{
		super(resourcecontainer, zipEntry.getName());
		this.zipEntry = zipEntry;
	}

	@Override
	public boolean directory()
	{
		return zipEntry.isDirectory();
	}
}