package de.ruu.lib.jdbc.postgres;

import de.ruu.lib.jdbc.core.AbstractJDBCProperties;
import org.postgresql.Driver;

public class JDBCProperties extends AbstractJDBCProperties
{
	public JDBCProperties(JDBCURL url, String user, String password)
	{
		super(Driver.class.getName(), url.asString(), user, password);
	}
}