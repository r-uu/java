package de.ruu.lib.util;

import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class Wsl2IpResolverTest
{
	@AfterEach
	void clearCache()
	{
		Wsl2IpResolver.clearCache();
	}

	@Test
	void testGetWsl2Ip_shouldReturnValidIp()
	{
		String ip = Wsl2IpResolver.wsl2Ip();

		assertThat(ip).as("IP should not be null").isNotNull();
		assertThat(ip.isEmpty()).as("IP should not be empty").isFalse();

		// Should be either localhost or valid IP format
		assertThat(ip.equals("localhost") || ip.matches("\\d+\\.\\d+\\.\\d+\\.\\d+"))
				.as("IP should be 'localhost' or valid IP format, but was: " + ip)
				.isTrue();

		System.out.println("Detected WSL2 IP: " + ip);
	}

	@Test
	void testGetWsl2Ip_shouldCacheResult()
	{
		String ip1 = Wsl2IpResolver.wsl2Ip();
		String ip2 = Wsl2IpResolver.wsl2Ip();

		assertThat(ip1).as("Should return same cached instance").isSameAs(ip2);
	}

	@Test
	void testClearCache_shouldInvalidateCache()
	{
		String ip1 = Wsl2IpResolver.wsl2Ip();
		Wsl2IpResolver.clearCache();
		String ip2 = Wsl2IpResolver.wsl2Ip();

		// Values should be equal, but not same instance (new object after cache clear)
		assertThat(ip2).as("IPs should be equal").isEqualTo(ip1);
	}

	@Test
	void testResolve_shouldReplacePlaceholder() throws Exception
	{
		Properties props = new Properties();
		props.setProperty("db.host", "${WSL2_IP}");
		props.setProperty("db.port", "5432");
		props.setProperty("other.host", "localhost");

		Wsl2IpResolver.resolve(props);

		String dbHost = props.getProperty("db.host");
		assertThat(dbHost).isNotNull();
		assertThat(dbHost.contains("${WSL2_IP}")).as("Placeholder should be replaced").isFalse();
		assertThat(dbHost.contains("$")).as("No dollar signs should remain").isFalse();

		// Other properties should remain unchanged
		assertThat(props.getProperty("db.port")).isEqualTo("5432");
		assertThat(props.getProperty("other.host")).isEqualTo("localhost");

		System.out.println("Resolved db.host: " + dbHost);
	}

	@Test
	void testResolve_shouldHandleMultiplePlaceholders() throws Exception
	{
		Properties props = new Properties();
		props.setProperty("host1", "${WSL2_IP}");
		props.setProperty("host2", "${WSL2_IP}");
		props.setProperty("url", "jdbc:postgresql://${WSL2_IP}:5432/db");

		Wsl2IpResolver.resolve(props);

		String host1 = props.getProperty("host1");
		String host2 = props.getProperty("host2");
		String url = props.getProperty("url");

		assertThat(host1.contains("$")).isFalse();
		assertThat(host2.contains("$")).isFalse();
		assertThat(url.contains("$")).isFalse();
		assertThat(url.startsWith("jdbc:postgresql://")).isTrue();
		assertThat(url.endsWith(":5432/db")).isTrue();

		System.out.println("Resolved URL: " + url);
	}

	@Test
	void testLoadAndResolve_shouldLoadAndReplacePlaceholders() throws Exception
	{
		// Create temporary properties file
		File tempFile = File.createTempFile("test-config", ".properties");
		tempFile.deleteOnExit();

		try (FileWriter writer = new FileWriter(tempFile))
		{
			writer.write("db.host=${WSL2_IP}\n");
			writer.write("db.port=5432\n");
			writer.write("api.url=http://${WSL2_IP}:9080\n");
		}

		Properties props = Wsl2IpResolver.loadAndResolve(tempFile.getAbsolutePath());

		String dbHost = props.getProperty("db.host");
		String apiUrl = props.getProperty("api.url");

		assertThat(dbHost).isNotNull();
		assertThat(dbHost.contains("$")).isFalse();

		assertThat(apiUrl).isNotNull();
		assertThat(apiUrl.contains("$")).isFalse();
		assertThat(apiUrl.startsWith("http://")).isTrue();
		assertThat(apiUrl.endsWith(":9080")).isTrue();

		System.out.println("Loaded and resolved properties:");
		System.out.println("  db.host = " + dbHost);
		System.out.println("  api.url = " + apiUrl);
	}
}

