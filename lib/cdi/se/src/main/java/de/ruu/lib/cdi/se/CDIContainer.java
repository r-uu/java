package de.ruu.lib.cdi.se;

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.interceptor.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Objects;

public abstract class CDIContainer
{
	private static final Logger log = LoggerFactory.getLogger(CDIContainer.class);

	private CDIContainer()
	{
		throw new IllegalStateException("utility class with static methods only must not be instantiated");
	}

	public static void bootstrap() { bootstrap(ClassLoader.getSystemClassLoader()); }

	/** @param classLoader used for loading {@code META-INF/beans.xml} */
	public static void bootstrap(ClassLoader classLoader)
	{
		Objects.requireNonNull(classLoader, "classLoader");
		bootstrap(classLoader, List.of());
	}

	/**
	 * @param classLoader used for loading {@code META-INF/beans.xml}}
	 * @param interceptorClasses list of {@link Interceptor} classes to enable
	 */
	public static void bootstrap(ClassLoader classLoader, List<Class<?>> interceptorClasses)
	{
		Objects.requireNonNull(classLoader,       "classLoader");
		Objects.requireNonNull(interceptorClasses, "interceptorClasses");
		bootstrap(classLoader, interceptorClasses, List.of());
	}

	public static void bootstrap(
			ClassLoader                      classLoader,
			List<Class<?>>                   interceptorClasses,
			List<Class<? extends Extension>> extensionClasses)
	{
		Objects.requireNonNull(classLoader,       "classLoader");
		Objects.requireNonNull(interceptorClasses, "interceptorClasses");
		Objects.requireNonNull(extensionClasses,   "extensionClasses");
		bootstrap(classLoader, interceptorClasses, extensionClasses, List.of());
	}

	/**
	 * @param classLoader used for loading {@code META-INF/beans.xml}}
	 * @param interceptorClasses list of {@link Interceptor} classes to enable
	 * @param extensionClasses list of {@link Extension} classes to enable
	 */
	public static void bootstrap(
			ClassLoader                      classLoader,
			List<Class<?>>                   interceptorClasses,
			List<Class<? extends Extension>> extensionClasses,
			List<Class<?>>                   beanClasses)
	{
		Objects.requireNonNull(classLoader,       "classLoader");
		Objects.requireNonNull(interceptorClasses, "interceptorClasses");
		Objects.requireNonNull(extensionClasses,   "extensionClasses");
		Objects.requireNonNull(beanClasses,        "beanClasses");

		try
		{
			CDI.current();
			// container available, do not bootstrap another one
			return;
		}
		catch (IllegalStateException e)
		{
			// no container available, fine, let's bootstrap a new one
		}

		log.debug("initialising CDI");

		checkIfBeansXMLIsPresent(classLoader);

		try
		{
			final SeContainerInitializer initializer =
					SeContainerInitializer
							.newInstance()
							.enableInterceptors(interceptorClasses.toArray(Class[]::new))
							.addExtensions     (extensionClasses  .toArray(Class[]::new))
							.addBeanClasses    (beanClasses       .toArray(Class[]::new));

			final SeContainer container = initializer.initialize();

			log.debug("initialised CDI successfully: {}", container != null);
		}
		catch (Exception e)
		{
			log.error("failure initialising CDI", e);
			throw e;
		}
	}

	private static final String META_INF_BEANS_XML = "META-INF/beans.xml";

	private static void checkIfBeansXMLIsPresent(ClassLoader classLoader)
	{
		URL url = classLoader.getResource(META_INF_BEANS_XML);

		if (url == null)
		{
			log.warn("could not find {}", META_INF_BEANS_XML);
		}
		else
		{
			log.debug("found {} in {}", META_INF_BEANS_XML, url.getPath());
		}
	}
}
