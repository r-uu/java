package de.ruu.app.pragma.client;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;

import java.io.IOException;

final class BearerTokenFilter implements ClientRequestFilter
{
  private final KeycloakTokenProvider tokenProvider;

  BearerTokenFilter(KeycloakTokenProvider tokenProvider)
  {
    this.tokenProvider = tokenProvider;
  }

  @Override
  public void filter(ClientRequestContext requestContext) throws IOException
  {
    requestContext.getHeaders().putSingle("Authorization", "Bearer " + tokenProvider.accessToken());
  }
}
