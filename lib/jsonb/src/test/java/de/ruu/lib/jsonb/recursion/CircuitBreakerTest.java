package de.ruu.lib.jsonb.recursion;

import de.ruu.lib.jsonb.JsonbConfigurator;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbConfig;
import org.junit.jupiter.api.Test;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

class CircuitBreakerTest
{
	private static final Logger log = LogManager.getLogger(CircuitBreakerTest.class);

	@Test
	void toJson()
	{
		Parent parent = new Parent("parent");
		Child child = new Child("child", parent);

		Jsonb  jsonb = getContext();
		String json  = jsonb.toJson(parent);
		log.debug("json\n{}", json);
	}

	private Jsonb getContext()
	{
		JsonbConfig config = JsonbConfigurator.config();
		config.withAdapters
		(
				new CircuitBreaker(Parent.class)
		);
		return new JsonbConfigurator().context(config);
	}
}
