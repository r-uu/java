package de.ruu.lib.util.classpath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static de.ruu.lib.util.StringBuilders.sb;
import static de.ruu.lib.util.classpath.Classpath.*;
import static org.assertj.core.api.Assertions.assertThat;

//@Disabled // log level for this class is set to info in log4j2.xml
class ClasspathTest
{
	private static final Logger log = LoggerFactory.getLogger(ClasspathTest.class);

	@Test void testGetClasspathResourcesByName() throws IOException
	{
		final Map<String, List<ClasspathResource>> classpathResourceLists = classpathResources();

		assertThat(classpathResourceLists.size() > 0).isTrue();

		final Set<String> resourceNamesSorted = new TreeSet<>(classpathResourceLists.keySet());

		final StringBuilder stringBuilder = sb();

		for (final String resourceName : resourceNamesSorted)
		{
			stringBuilder.append("\nresource name " + resourceName);

			for (final ClasspathResource classpathResource : classpathResourceLists.get(resourceName))
			{
				stringBuilder.append
				(
						"\n\tresource container " +
						classpathResource.resourceContainer().resourceContainerFile().getAbsolutePath()
				);
			}
		}

		stringBuilder.append("\nnumber of resources: " + classpathResourceLists.size());

		log.debug(stringBuilder.toString());
	}

	private final static String FILTERED_RESOURCE_CONTAINER_NAME =
			System.getProperty("java.class.path").split(System.getProperty("path.separator"))[0];

	@Test void testGetClasspathResourcesWithResourceContainerFilter() throws IOException
	{
		final FileFilter resourceContainerFilter =
				new FileFilter()
				{
					@Override
					public boolean accept(final File file)
					{
						if (file.getPath().equals(FILTERED_RESOURCE_CONTAINER_NAME))
						{
							log.debug("filtering resource container: " + FILTERED_RESOURCE_CONTAINER_NAME);
							return false;
						}
						return true;
					}
				};

		final Map<String, List<ClasspathResource>> classpathResourcesWithoutFilter = classpathResources();
		final Map<String, List<ClasspathResource>> classpathResourcesWithFilter    =
				classpathResources(resourceContainerFilter, FILTER_ACCEPT_FILES_ALL, FILTER_ACCEPT_ZIP_ENTRIES_ALL);

		assertThat(classpathResourcesWithoutFilter.size() > classpathResourcesWithFilter.size()).isTrue();

		log.debug("""
				number of resources with filter: {}
				number of resources without filter: {}
				classpath:
				{}""",
				classpathResourcesWithFilter.size(),
				classpathResourcesWithoutFilter.size(),
				System.getProperty("java.class.path"));
	}

//	@Disabled("TODO find out why this test fails under maven from command line")
	@Test void testGetClasspathResourcesWithDirectoryResourceFilter() throws IOException
	{
		final FileFilter resourceContainerFilter = FILTER_ACCEPT_FILES_ALL;
		final FileFilter directoryResourceFilter = FILTER_ACCEPT_FILES_DIRECTORIES_ONLY;

		final Map<String, List<ClasspathResource>> classpathResourceListsWithoutFilter = classpathResources();
		final Map<String, List<ClasspathResource>> classpathResourceListsWithFilter    =
				classpathResources(resourceContainerFilter, directoryResourceFilter, FILTER_ACCEPT_ZIP_ENTRIES_ALL);

		assertThat(classpathResourceListsWithoutFilter.size() > classpathResourceListsWithFilter.size()).isTrue();

		log.debug(
				"\nnumber of resources with filter: " + classpathResourceListsWithFilter.size() +
				"\nnumber of resources without filter: " + classpathResourceListsWithoutFilter.size());
	}

	@Test void testGetClasspathPackages() throws IOException
	{
		final Map<String, List<ClasspathResource>> classpathResourceListsWithoutFilter = classpathResources();
		final Map<String, List<ClasspathResource>> classpathResourceListsWithFilter    = packagesAsClasspathResources();

		assertThat(classpathResourceListsWithoutFilter.size() > classpathResourceListsWithFilter.size()).isTrue();

		log.debug("""
				number of resources with filter: {}
				number of resources without filter: {}""",
				classpathResourceListsWithFilter.size(),
				classpathResourceListsWithoutFilter.size());
	}

	@Test void testReportClasspathResourcesByResourceContainerName() throws IOException
	{
		log.debug(
				"class path resources by resource container name\n{}",
				Classpath.reportClasspathResourcesByName(classpathResources()));
	}
}