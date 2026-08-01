package de.ruu.app.pragma.client;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

final class RestClientAuthConfig
{
  private final String serverUrl;
  private final String realm;
  private final String clientId;
  private final String username;
  private final String password;

  private RestClientAuthConfig(String serverUrl, String realm, String clientId, String username, String password)
  {
    this.serverUrl = serverUrl;
    this.realm = realm;
    this.clientId = clientId;
    this.username = username;
    this.password = password;
  }

  static RestClientAuthConfig read()
  {
    Config config = ConfigProvider.getConfig();
    boolean testMode = config.getOptionalValue("pragma.auth.test-mode", Boolean.class).orElse(true);
    String serverUrl = config.getOptionalValue("pragma.keycloak.server-url", String.class).orElse("http://localhost:8080");
    String realm = config.getOptionalValue("pragma.keycloak.realm", String.class).orElse("pragma-realm");
    String clientId = config.getOptionalValue("pragma.keycloak.client-id", String.class).orElse("pragma-frontend");

    if (testMode) {
      String username = config.getOptionalValue("pragma.keycloak.username", String.class).orElse("r-uu");
      String password = config.getOptionalValue("pragma.keycloak.password", String.class).orElse(username);
      return new RestClientAuthConfig(serverUrl, realm, clientId, username, password);
    }

    String username = required(config, "pragma.keycloak.username");
    String password = required(config, "pragma.keycloak.password");
    return new RestClientAuthConfig(serverUrl, realm, clientId, username, password);
  }

  private static String required(Config config, String key)
  {
    return config.getOptionalValue(key, String.class)
        .filter(value -> !value.isBlank())
        .orElseThrow(() -> new IllegalStateException(
            "Missing required property '" + key + "' (set pragma.auth.test-mode=true for dev shortcut mode)"));
  }

  String serverUrl() { return serverUrl; }
  String realm() { return realm; }
  String clientId() { return clientId; }
  String username() { return username; }
  String password() { return password; }
}
