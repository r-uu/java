package de.ruu.lib.cdi.common;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;

public class CDIExtension implements Extension
{
	private static final Logger log = LogManager.getLogger(CDIExtension.class);

	void beforeBeanDiscovery(@Observes BeforeBeanDiscovery bbd)
	{
		log.debug("beginning the scanning process");
	}

	<T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat)
	{
		log.debug("scanning type: " + pat.getAnnotatedType().getJavaClass().getName());
	}

	void afterDeploymentValidation(@Observes AfterDeploymentValidation adv, BeanManager beanManager)
	{
		List<String> beanClasses =
				beanManager
						.getBeans(Object.class)
						.stream()
		          .map(bean -> bean.getBeanClass().getName())
		          .sorted()
		          .toList();

    String logOutput = "finished the deployment validation process, managed beans:\n" + String.join("\n", beanClasses);

    log.debug(logOutput);
	}

	void afterBeanDiscovery(@Observes AfterBeanDiscovery abd)
	{
		log.debug("finished the scanning process");
	}
}
