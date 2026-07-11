package de.ruu.lib.jdbc.postgres;

public class JDBCURL extends de.ruu.lib.jdbc.core.JDBCURL
{
	public static final String PROTOCOL = "jdbc:postgresql";

	public JDBCURL(String host, int port, String databaseName) { super(host, port, databaseName); }

	@Override public String protocol() { return PROTOCOL; }
}