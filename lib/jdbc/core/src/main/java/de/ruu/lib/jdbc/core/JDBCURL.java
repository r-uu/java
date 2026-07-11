package de.ruu.lib.jdbc.core;

public abstract class JDBCURL
{
	private final String host;
	private final int    port;
	private final String databaseName;

	protected JDBCURL(String host, int port, String databaseName)
	{
		this.host         = host;
		this.port         = port;
		this.databaseName = databaseName;
	}

	public String host()         { return host;         }
	public int    port()         { return port;         }
	public String databaseName() { return databaseName; }

	public abstract String protocol();

	public String asString() { return protocol() + "://" + host + ":" + port + "/" + databaseName; }
}
