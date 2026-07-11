package de.ruu.lib.fx.comp;

import static jakarta.enterprise.inject.se.SeContainerInitializer.newInstance;

import de.ruu.lib.cdi.common.CDIExtension;
import de.ruu.lib.util.classpath.ClasspathResource;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import javafx.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for classes that launch JavaFX {@link Application}s with CDI support ({@link FXCApp}s).
 *
 * @author r-uu
 */
public abstract class FXCAppRunner
{
	private static final Logger log = LoggerFactory.getLogger(FXCAppRunner.class);

	public static void run(Class<? extends FXCApp> appClass, String[] args, Runnable runBeforeAppLaunch)
	{
		if (appClass == null)
			throw new IllegalStateException("parameter appClass must not be null");

		log.debug("initialising CDI");

		checkIfBeansXMLIsPresent(appClass);

		final SeContainerInitializer initializer = newInstance();
		initializer.addExtensions(CDIExtension.class);

		try (SeContainer container = initializer.initialize())
		{
			if (runBeforeAppLaunch != null)
			{
				log.debug("calling runBeforeAppLaunch");
				runBeforeAppLaunch.run();
				log.debug("returned from runBeforeAppLaunch");
			}

			log.debug("starting application class: " + appClass.getName());
			Application.launch(appClass, args);
			log.debug("finished application class: " + appClass.getName());
		}

		log.debug("shut down CDI");
	}

	public static void run(Class<? extends FXCApp> appClass, String[] args)
	{
		run(appClass, args, null);
	}

	private final static String META_INF_BEANS_XML = "META-INF/beans.xml";

	/**
	 * logs a warning if META-INF/beans.xml can not be found in the same classpath location as <code>appClass</code>
	 *
	 * @param appClass
	 */
	private static void checkIfBeansXMLIsPresent(Class<?> appClass)
	{
		if (ClasspathResource.isResourceAvailableToClassLoaderOf(META_INF_BEANS_XML, appClass))
		{
			log.debug(appClass.getClassLoader().getResource(META_INF_BEANS_XML) + " found");
		}
		else
		{
			log.warn(appClass.getClassLoader().getResource(META_INF_BEANS_XML) + " not found");
		}
	}

	/**
	 * Configures JPMS module access for CDI event classes that need access to the unnamed module.
	 * <p>
	 * <b>Important:</b> The application module itself must be configured to read the unnamed module using the JVM
	 * parameter: {@code --add-reads module.name=ALL-UNNAMED}
	 * <p>
	 * This method only registers common FXC event classes that need module access. Application-specific event classes
	 * should call their own {@code addReadsUnnamedModule()} method after calling this method.
	 * <p>
	 * <b>Usage in AppRunner:</b>
	 *
	 * <pre>
	 * public static void main(String[] args)
	 * {
	 * 	FXCAppRunner.configureModuleAccessForCDI();
	 * 	MyCustomEvent.addReadsUnnamedModule(); // If needed
	 * 	FXCAppRunner.run(MyApp.class, args);
	 * }
	 * </pre>
	 *
	 * <b>Required JVM Parameter:</b> {@code --add-reads your.module.name=ALL-UNNAMED}
	 */
	public static void configureModuleAccessForCDI()
	{
		log.debug("configuring module access for FXC event classes");

		// Register common FXC event classes that Weld needs access to
		FXCAppStartedEvent   .addReadsUnnamedModule();
		FXComponentReadyEvent.addReadsUnnamedModule();
	}
}
