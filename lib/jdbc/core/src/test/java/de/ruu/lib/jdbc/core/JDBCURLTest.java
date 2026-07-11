package de.ruu.lib.jdbc.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JDBCURLTest
{
  static class TestJDBCURL extends JDBCURL
  {
    TestJDBCURL(String host, int port, String database)
    {
      super(host, port, database);
    }

    @Override
    public String protocol() { return "jdbc:postgresql"; }
  }

  @Test
  void asStringFormatsCorrectly()
  {
    JDBCURL url = new TestJDBCURL("localhost", 5432, "mydb");
    assertThat(url.asString()).isEqualTo("jdbc:postgresql://localhost:5432/mydb");
  }

  @Test
  void gettersReturnConstructorValues()
  {
    JDBCURL url = new TestJDBCURL("db.example.com", 5433, "testdb");
    assertThat(url.host()).isEqualTo("db.example.com");
    assertThat(url.port()).isEqualTo(5433);
    assertThat(url.databaseName()).isEqualTo("testdb");
  }

  @Test
  void protocolAppearsAtStartOfUrl()
  {
    JDBCURL url = new TestJDBCURL("host", 1234, "db");
    assertThat(url.asString()).startsWith("jdbc:postgresql://");
  }
}
