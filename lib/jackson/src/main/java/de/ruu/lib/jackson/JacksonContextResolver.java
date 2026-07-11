package de.ruu.lib.jackson;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * As a {@link ContextResolver} parameterised with {@link ObjectMapper} and being
 * a {@link Provider} instances will be created and used when an {@link ObjectMapper}
 * needs to be provided. The implementation instantiates and customises a new {@link ObjectMapper}.
 */
@Provider
@Produces(APPLICATION_JSON) // TODO find out if this is necessary
public class JacksonContextResolver implements ContextResolver<ObjectMapper>
{
	private final static ObjectMapper MAPPER = new ObjectMapper();

	public JacksonContextResolver()
	{
		MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		MAPPER.registerModule(new JavaTimeModule());
		MAPPER.setVisibility(PropertyAccessor.FIELD, ANY);
		MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
//		MAPPER
//				.getSerializationConfig()
//				.getDefaultVisibilityChecker()
//				.withFieldVisibility(ANY)
//				.withGetterVisibility(NONE);
	}

	@Override public ObjectMapper getContext(Class<?> type) { return MAPPER; }

	public ObjectMapper context() { return getContext(null); }
	public ObjectMapper mapper () { return context(); }
}
