package de.ruu.lib.cdi.se.demo;

import de.ruu.lib.cdi.se.CDIContainer;
import jakarta.enterprise.inject.spi.CDI;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

class DemoCDIContainer
{
	private static final Logger log = LogManager.getLogger(DemoCDIContainer.class);

//	@Test void test()
//	{
//		CDIContainer.bootstrap(TestCDIContainer.class.getClassLoader());
//		log.debug(CDI.current().select(Context.class).get().injectable().ping());
//	}

	public static void main(String[] args)
	{
		CDIContainer.bootstrap(Context.class.getClassLoader());
//		log.debug(CDI.current().getBeanManager().toString());
//		log.debug(CDI.current().select(InjectableImpl.class).get().ping());
//		log.debug(CDI.current().select(Injectable.class).get().ping());
		log.debug(CDI.current().select(Context.class).get().injectable().ping());
	}
}
