package de.ruu.lib.junit;

import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

public class HostPortConfigExtension// implements BeforeAllCallback
{
	@Inject
	@ConfigProperty(name = "host", defaultValue = "localhost")
	private String host;
	@Inject
	@ConfigProperty(name = "port", defaultValue = "9080")
	private int    port;

	private HostPortConfigExtension() {}

	public String getHost() { return host; }
	public int    getPort() { return port; }

	public static Builder builder() { return new Builder(); }

	public static class Builder
	{
		private String host;
		private int    port;

		public Builder host(String host) { this.host = host; return this; }
		public Builder port(int    port) { this.port = port; return this; }

		public HostPortConfigExtension build()
		{
			HostPortConfigExtension result = new HostPortConfigExtension();
			result.host = host;
			result.port = port;
			return result;
		}
	}
}
