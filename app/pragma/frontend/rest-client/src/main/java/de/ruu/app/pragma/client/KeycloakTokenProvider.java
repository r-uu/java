package de.ruu.app.pragma.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

final class KeycloakTokenProvider
{
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final long CLOCK_SKEW_SECONDS = 20L;

  private final HttpClient httpClient = HttpClient.newHttpClient();
  private final String tokenEndpoint;
  private final String clientId;
  private final String username;
  private final String password;

  private String accessToken;
  private Instant expiresAt;

  KeycloakTokenProvider(String serverUrl, String realm, String clientId, String username, String password)
  {
    this.tokenEndpoint = trimTrailingSlash(serverUrl) + "/realms/" + realm + "/protocol/openid-connect/token";
    this.clientId = clientId;
    this.username = username;
    this.password = password;
  }

  synchronized String accessToken()
  {
    if (accessToken != null && expiresAt != null && Instant.now().isBefore(expiresAt.minusSeconds(CLOCK_SKEW_SECONDS)))
      return accessToken;
    refreshToken();
    return accessToken;
  }

  private void refreshToken()
  {
    String form = "grant_type=password"
        + "&client_id=" + encode(clientId)
        + "&username=" + encode(username)
        + "&password=" + encode(password);
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(tokenEndpoint))
        .header("Content-Type", "application/x-www-form-urlencoded")
        .POST(HttpRequest.BodyPublishers.ofString(form))
        .build();
    try {
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() / 100 != 2) {
        throw new IllegalStateException("Keycloak token request failed: HTTP " + response.statusCode() + " - " + response.body());
      }
      JsonNode node = OBJECT_MAPPER.readTree(response.body());
      JsonNode tokenNode = node.get("access_token");
      if (tokenNode == null || tokenNode.asText().isBlank()) {
        throw new IllegalStateException("Keycloak token response has no access_token");
      }
      accessToken = tokenNode.asText();
      long expiresIn = node.path("expires_in").asLong(60L);
      expiresAt = Instant.now().plusSeconds(expiresIn);
    }
    catch (IOException e) {
      throw new IllegalStateException("Failed to parse Keycloak token response", e);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while requesting Keycloak token", e);
    }
  }

  private static String encode(String value)
  {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  private static String trimTrailingSlash(String value)
  {
    return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
  }
}
