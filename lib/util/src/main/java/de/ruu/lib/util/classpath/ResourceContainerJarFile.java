package de.ruu.lib.util.classpath;

import de.ruu.lib.util.Strings;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class ResourceContainerJarFile
		extends ResourceContainerBase
{
	ResourceContainerJarFile(File containerFile)
  {
    super(containerFile);
    
    if (containerFile.getName().endsWith(".jar") == false)
    {
    	throw new ExceptionInInitializerError(
    			containerFile.getName() + " is not a .jar file");
    }
  }

	@Override
  public Map<String, ClasspathResource> classPathResources()
  		throws IOException
  {
	  return
	  		classPathResources(Classpath.FILTER_ACCEPT_ZIP_ENTRIES_ALL);
  }

	public Map<String, ClasspathResource> classPathResources(
	    ZipEntryFilter zipEntryFilter) throws IOException
	{
		Map<String, ClasspathResource> result = new HashMap<>();

		ZipFile zipFile = null;

		try
		{
			zipFile = new ZipFile(containerFile.getAbsolutePath());

			Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();

			while (zipEntries.hasMoreElements())
			{
				ZipEntry zipEntry = zipEntries.nextElement();

				if (zipEntryFilter.accept(zipEntry))
				{
					ClasspathResourceZipEntry value =
							new ClasspathResourceZipEntry(
										new ResourceContainerJarFile(containerFile), zipEntry);

					result.put(Strings.rTrimChars(value.resourceName(), "\\/"), value);
				}
			}
		}
		finally
		{
			if (zipFile != null)
			{
				zipFile.close();
			}
		}

		return result;
	}
}