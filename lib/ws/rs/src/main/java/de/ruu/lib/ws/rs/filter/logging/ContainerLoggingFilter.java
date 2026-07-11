package de.ruu.lib.ws.rs.filter.logging;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static de.ruu.lib.util.BooleanFunctions.not;
import static de.ruu.lib.util.StringBuilders.sb;
import static java.util.Objects.isNull;

// comment / uncomment @Provider ro deactivate / activate logging filter
@Provider
public class ContainerLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter
{
	private static final Logger log = LoggerFactory.getLogger(ContainerLoggingFilter.class);

	@Override public void filter(ContainerRequestContext requestContext) throws IOException
	{
		StringBuilder result =
				sb("request uriInfo.absolutePath=" + requestContext.getUriInfo().getAbsolutePath().toString());

		MultivaluedMap<String, String> headers = requestContext.getHeaders();
		if (not(headers.isEmpty())) result.append("\n").append(Util.toString(headers));

		if (requestContext.hasEntity())
		{
			InputStream stream = requestContext.getEntityStream();
			String json = new String(stream.readAllBytes());                           // read string payload from stream
			result.append("\nrequest entity payload as string\n").append(json);
			requestContext.setEntityStream(new ByteArrayInputStream(json.getBytes())); // restore stream
		}

		log.debug(result.toString());
	}

	@Override public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException
	{
		StringBuilder result = sb("\nresponse");

		MultivaluedMap<String, Object> headers = responseContext.getHeaders();
		if (not(headers.isEmpty())) result.append("\n").append(Util.toString(headers));

		Object entity = responseContext.getEntity();
		if (not(isNull(entity))) result.append("\nresponse entity payload as string\n").append(entity.toString());

		log.debug(result.toString());
	}
}