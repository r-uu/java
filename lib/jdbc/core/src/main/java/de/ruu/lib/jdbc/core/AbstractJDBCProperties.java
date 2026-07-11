package de.ruu.lib.jdbc.core;

import java.util.Properties;

public abstract class AbstractJDBCProperties
{
	public static final String PROPERTY_KEY_JAKARTA_PERSISTENCE_JDBC_DRIVER   = "jakarta.persistence.jdbc.driver";
	public static final String PROPERTY_KEY_JAKARTA_PERSISTENCE_JDBC_URL      = "jakarta.persistence.jdbc.url";
	public static final String PROPERTY_KEY_JAKARTA_PERSISTENCE_JDBC_USER     = "jakarta.persistence.jdbc.user";
	public static final String PROPERTY_KEY_JAKARTA_PERSISTENCE_JDBC_PASSWORD = "jakarta.persistence.jdbc.password";

	private String driver;
	private String url;
	private String user;
	private String password;

	protected AbstractJDBCProperties(String driver, String url, String user, String password)
	{
		this.driver   = driver;
		this.url      = url;
		this.user     = user;
		this.password = password;
	}

	public String driver()   { return driver;   }
	public String url()      { return url;      }
	public String user()     { return user;     }
	public String password() { return password; }

	public Properties properties()
	{
		Properties result = new Properties();

		result.put(PROPERTY_KEY_JAKARTA_PERSISTENCE_JDBC_DRIVER  , driver);
		result.put(PROPERTY_KEY_JAKARTA_PERSISTENCE_JDBC_URL     , url);
		result.put(PROPERTY_KEY_JAKARTA_PERSISTENCE_JDBC_USER    , user);
		result.put(PROPERTY_KEY_JAKARTA_PERSISTENCE_JDBC_PASSWORD, password);

		return result;
	}
}
