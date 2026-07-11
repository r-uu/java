package de.ruu.lib.jdbc.core;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static de.ruu.lib.jdbc.core.AbstractJDBCProperties.PROPERTY_KEY_JAKARTA_PERSISTENCE_JDBC_DRIVER;
import static de.ruu.lib.jdbc.core.AbstractJDBCProperties.PROPERTY_KEY_JAKARTA_PERSISTENCE_JDBC_PASSWORD;
import static de.ruu.lib.jdbc.core.AbstractJDBCProperties.PROPERTY_KEY_JAKARTA_PERSISTENCE_JDBC_URL;
import static de.ruu.lib.jdbc.core.AbstractJDBCProperties.PROPERTY_KEY_JAKARTA_PERSISTENCE_JDBC_USER;
import static org.assertj.core.api.Assertions.assertThat;

class AbstractJDBCPropertiesTest
{
  static class TestJDBCProperties extends AbstractJDBCProperties
  {
    TestJDBCProperties(String driver, String url, String user, String password)
    {
      super(driver, url, user, password);
    }
  }

  @Test
  void propertiesContainsAllFourKeys()
  {
    AbstractJDBCProperties props = new TestJDBCProperties(
        "org.postgresql.Driver",
        "jdbc:postgresql://localhost:5432/db",
        "user",
        "secret"
    );
    Properties p = props.properties();

    assertThat(p).containsKey(PROPERTY_KEY_JAKARTA_PERSISTENCE_JDBC_DRIVER);
    assertThat(p).containsKey(PROPERTY_KEY_JAKARTA_PERSISTENCE_JDBC_URL);
    assertThat(p).containsKey(PROPERTY_KEY_JAKARTA_PERSISTENCE_JDBC_USER);
    assertThat(p).containsKey(PROPERTY_KEY_JAKARTA_PERSISTENCE_JDBC_PASSWORD);
  }

  @Test
  void propertiesValuesMatchConstructorArgs()
  {
    AbstractJDBCProperties props = new TestJDBCProperties("drv", "url", "usr", "pwd");

    assertThat(props.properties().getProperty(PROPERTY_KEY_JAKARTA_PERSISTENCE_JDBC_DRIVER)).isEqualTo("drv");
    assertThat(props.properties().getProperty(PROPERTY_KEY_JAKARTA_PERSISTENCE_JDBC_URL)).isEqualTo("url");
    assertThat(props.properties().getProperty(PROPERTY_KEY_JAKARTA_PERSISTENCE_JDBC_USER)).isEqualTo("usr");
    assertThat(props.properties().getProperty(PROPERTY_KEY_JAKARTA_PERSISTENCE_JDBC_PASSWORD)).isEqualTo("pwd");
  }

  @Test
  void gettersReturnConstructorValues()
  {
    AbstractJDBCProperties props = new TestJDBCProperties("drv", "url", "usr", "pwd");

    assertThat(props.driver()).isEqualTo("drv");
    assertThat(props.url()).isEqualTo("url");
    assertThat(props.user()).isEqualTo("usr");
    assertThat(props.password()).isEqualTo("pwd");
  }
}
