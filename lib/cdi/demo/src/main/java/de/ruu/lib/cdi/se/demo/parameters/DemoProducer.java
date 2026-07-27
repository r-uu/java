package de.ruu.lib.cdi.se.demo.parameters;

import de.ruu.lib.cdi.se.CDIContainer;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

class DemoProducer
{
	private static final Logger log = LogManager.getLogger(DemoProducer.class);

	@Inject private InjectableProducer producer;

	private void run()
	{
		producer.ping = "ping";
		producer.pong = "pong";

		Injectable injectable = producer.produce();

		log.debug(injectable.ping());
		log.debug(injectable.ping());
	}

	public static void main(String[] args)
	{
		CDIContainer.bootstrap();
		DemoProducer demo = CDI.current().select(DemoProducer.class).get();
		demo.run();
	}
}
