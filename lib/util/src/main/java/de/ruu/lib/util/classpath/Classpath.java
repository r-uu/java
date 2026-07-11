package de.ruu.lib.util.classpath;

import static de.ruu.lib.util.BooleanFunctions.not;
import static de.ruu.lib.util.StringBuilders.sb;
import static java.lang.System.getProperty;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Classpath
{
	private static final Logger log = LoggerFactory.getLogger(Classpath.class);

	public static final String RAM_COMPILER_TYPENAME = "__dyna_type__";

	public static final FileFilter FILTER_ACCEPT_FILES_ALL              = file -> true;
	public static final FileFilter FILTER_ACCEPT_FILES_ALL_BUT_JAR      = file -> not(file.getName().endsWith(".jar"));
	public static final FileFilter FILTER_ACCEPT_FILES_EXISTING_ONLY    = File::exists;
	public static final FileFilter FILTER_ACCEPT_FILES_DIRECTORIES_ONLY = File::isDirectory;

	public static final ZipEntryFilter FILTER_ACCEPT_ZIP_ENTRIES_ALL              = zipEntry -> true;
	public static final ZipEntryFilter FILTER_ACCEPT_ZIP_ENTRIES_DIRECTORIES_ONLY = ZipEntry::isDirectory;

	private Classpath() { throw new IllegalStateException("utility class must not be instantiated"); }

	public static Map<String, List<ClasspathResource>> classpathResources()
			throws IOException
	{
		return
				classpathResources(
						FILTER_ACCEPT_FILES_ALL,
						FILTER_ACCEPT_FILES_ALL,
						FILTER_ACCEPT_ZIP_ENTRIES_ALL);
	}

	/**
	 * @param resourceContainerFilter filter for resource containers listed in <code>System.getProperty("java.class.path")
	 *                                </code>, use this filter to narrow the entries in the java class path
	 * @param directoryResourceFilter filter for resource files in <code>System.getProperty("java.class.path")</code>
	 *                                directories, use this filter to narrow the resources of the file system directories
	 *                                in the java class path
	 * @param zipResourceFilter filter for jar resource files in <code>System.getProperty("java.class.path")</code> .jar
	 *                          files, use this filter to narrow the entries of a .jar file
	 * @return resources in <code>System.getProperty("java.class.path")</code> if
	 *         they are accepted by all filter parameters
	 * @throws IOException
	 */
	public static Map<String, List<ClasspathResource>> classpathResources(
			FileFilter resourceContainerFilter,
			FileFilter directoryResourceFilter,
			ZipEntryFilter zipResourceFilter)
			throws IOException
	{
		Map<String, List<ClasspathResource>> result = new HashMap<>();

		// list of all ResourceContainers which's files are accepted by resourceContainerFilter
		List<ResourceContainer> resourceContainers = new ArrayList<>();

		String[] resourceContainerNames = getProperty("java.class.path").split(getProperty("path.separator"));

		for (String resourceContainerName : resourceContainerNames)
		{
			File resourceContainerFile = new File(resourceContainerName);

			if (resourceContainerFilter.accept(resourceContainerFile))
			{
				Optional<ResourceContainer> optional = createResourceContainer(resourceContainerFile);

				optional.ifPresent(resourceContainers::add);
			}
		}

		// collect resources from all ResourceContainers
		for (ResourceContainer resourceContainer : resourceContainers)
		{
			if (resourceContainer instanceof ResourceContainerDirectory resourceContainerDirectory)
			{
				// fetch resources in directory
				Map<String, ClasspathResource> resourcesInDirectory =
						resourceContainerDirectory.classPathResources(directoryResourceFilter);

				for (String resourceName : resourcesInDirectory.keySet())
				{
					// try to fetch list of class path resources from result
					List<ClasspathResource> classpathResources = result.get(resourceName);

					if (classpathResources == null)
					{
						// create new list of class path resources and put that list into result
						classpathResources = new ArrayList<>();
						result.put(resourceName, classpathResources);
					}

					// add resource from directory into class path resources
					classpathResources.add(resourcesInDirectory.get(resourceName));
				}
			}
			else if (resourceContainer instanceof ResourceContainerJarFile resourceContainerJarFile)
			{
				// fetch resources in .jar file
				Map<String, ClasspathResource> resourcesInJarFile =
						resourceContainerJarFile.classPathResources(zipResourceFilter);

				for (String resourceName : resourcesInJarFile.keySet())
				{
					// try to fetch list of class path resources from result
					List<ClasspathResource> classpathResources = result.get(resourceName);

					if (classpathResources == null)
					{
						// create new list of class path resources and put that list into result
						classpathResources = new ArrayList<>();
						result.put(resourceName, classpathResources);
					}

					// add resource from .jar file into class path resources
					classpathResources.add(resourcesInJarFile.get(resourceName));
				}
			}
			else
			{
				log.error("unexpected resource container type {}", resourceContainer.getClass().getName());
			}
		}

		return result;
	}

	public static Map<String, List<ClasspathResource>> packagesAsClasspathResources() throws IOException
	{
		return
				classpathResources(
						FILTER_ACCEPT_FILES_ALL,
						FILTER_ACCEPT_FILES_DIRECTORIES_ONLY,
						FILTER_ACCEPT_ZIP_ENTRIES_DIRECTORIES_ONLY);
	}

	public static String[] classpathAsArray()
	{
		return getProperty("java.class.path").split(getProperty("path.separator"));
	}

	public static StringBuilder reportClasspathResourcesByName(
			Map<String, List<ClasspathResource>> classpathResourcesByName)
	{
		// collect reports for all resources
		List<String> resourceReports = new ArrayList<>();

		Set<String> resourceNames = new TreeSet<>(classpathResourcesByName.keySet());

		for (String resourceName : resourceNames)
		{
			StringBuilder resourceReport = sb(resourceName);

			List<ClasspathResource> classpathResources = classpathResourcesByName.get(resourceName);

			// collect classpath containers for current resource
			List<String> containersForResource = new ArrayList<>();

			for (ClasspathResource classpathResource : classpathResources)
			{
				containersForResource.add(
						"\t" + classpathResource.resourceContainer().resourceContainerFile().getAbsolutePath());
			}

			if (not(containersForResource.isEmpty())) resourceReport.append("\n");

			resourceReport.append(String.join("\n", containersForResource));

			resourceReports.add(resourceReport.toString());
		}

		return sb(String.join("\n", resourceReports));
	}

	private static Optional<ResourceContainer> createResourceContainer(File resourceContainerFile)
	{
		ResourceContainer resourceContainer = null;

		if (resourceContainerFile.isDirectory())
		{
			resourceContainer = new ResourceContainerDirectory(resourceContainerFile);
		}
		else
		{
			resourceContainer = new ResourceContainerJarFile(resourceContainerFile);
		}

		return Optional.ofNullable(resourceContainer);
	}
}