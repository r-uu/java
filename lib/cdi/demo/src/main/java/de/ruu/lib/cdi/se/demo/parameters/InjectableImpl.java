package de.ruu.lib.cdi.se.demo.parameters;

import de.ruu.lib.cdi.se.demo.parameters.InjectableProducer.Ping;
import de.ruu.lib.cdi.se.demo.parameters.InjectableProducer.Pong;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InjectableImpl implements Injectable
{
	private static final Logger log = LoggerFactory.getLogger(InjectableImpl.class);

	private String ping;
	private String pong;

	@Inject
	public InjectableImpl(@Ping String ping, @Pong String pong)
	{
		this.ping = ping;
		this.pong = pong;
	}

	@Override public String ping()
	{
		log.debug(ping);
		return ping;
	}

	@Override
	public String pong()
	{
		log.debug(pong);
		return pong;
	}
}
