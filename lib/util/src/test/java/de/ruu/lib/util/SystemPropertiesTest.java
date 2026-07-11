package de.ruu.lib.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

class SystemPropertiesTest
{
	private static final Logger log = LoggerFactory.getLogger(SystemPropertiesTest.class);

	@Test void test()
	{
		log.debug("user name: {}", SystemProperties.userName());
		log.debug("user home: {}", SystemProperties.userHome());
		log.debug("work dir : {}", Paths.get(".").toAbsolutePath());
	}
}