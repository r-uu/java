package de.ruu.lib.jdbc.postgres;

import de.ruu.lib.jdbc.core.JDBCURL;
import org.postgresql.ds.PGSimpleDataSource;
import org.postgresql.ds.common.BaseDataSource;

import javax.sql.DataSource;
import java.util.Objects;

public class DataSourceFactory
{
	private final JDBCURL url;
	private final String  user;
	private final String  password;

	public DataSourceFactory(JDBCURL url, String user, String password)
	{
		this.url      = Objects.requireNonNull(url,      "url");
		this.user     = Objects.requireNonNull(user,     "user");
		this.password = Objects.requireNonNull(password, "password");
	}

	public DataSource create()
	{
		BaseDataSource result = new PGSimpleDataSource();

		result.setURL(     url.asString());
		result.setUser(    user);
		result.setPassword(password);

		return (DataSource) result;
	}
}
