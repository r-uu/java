package de.ruu.lib.ws.rs.filter.logging;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static de.ruu.lib.util.BooleanFunctions.not;
import static de.ruu.lib.util.StringBuilders.sb;

@Provider
public class ClientLoggingFilter implements ClientRequestFilter, ClientResponseFilter
{
	private static final Logger log = LoggerFactory.getLogger(ClientLoggingFilter.class);

	@Override public void filter(ClientRequestContext requestContext) throws IOException
	{
		StringBuilder result = sb("\nrequest URL=" + requestContext.getUri().toURL().toString());

		MultivaluedMap<String, Object> headers = requestContext.getHeaders();
		if (not(headers.isEmpty()))     result.append("\n" + Util.toString(headers));

		if (requestContext.hasEntity()) result.append("\nentity=" + requestContext.getEntity().toString());

		log.debug(result.toString());
	}

	@Override public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext)
			throws IOException
	{
		StringBuilder result = sb("\nresponse");

		MultivaluedMap<String, String> headers = responseContext.getHeaders();
		if (not(headers.isEmpty())) result.append("\n" + Util.toString(headers));

		log.debug(result.toString());
	}
}