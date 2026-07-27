package de.ruu.lib.util;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

class SystemPropertiesTest
{
	private static final Logger log = LogManager.getLogger(SystemPropertiesTest.class);

	@Test void test()
	{
		log.debug("user name: {}", SystemProperties.userName());
		log.debug("user home: {}", SystemProperties.userHome());
		log.debug("work dir : {}", Paths.get(".").toAbsolutePath());
	}
}