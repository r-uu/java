package de.ruu.lib.jsonb;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.config.BinaryDataStrategy;
import jakarta.json.bind.config.PropertyVisibilityStrategy;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

/**
 * {@link ContextResolver} that creates {@link Jsonb} contextes with
 * <ul>
 *   <li>{@code formatting == true}</li>
 *   <li>{@code nullValues == true}</li>
 *   <li>{@link PropertyVisibilityStrategy} == {@link PrivateElementsVisibleStrategy}</li>
 * </ul>
 * @author ruu
 */
@Provider
public class JsonbConfigurator implements ContextResolver<Jsonb>
{
	protected final static JsonbConfig CONFIG =
			new JsonbConfig()
					.withFormatting(true)
					.withNullValues(true)
					.withPropertyVisibilityStrategy(new PrivateElementsVisibleStrategy())
					.withBinaryDataStrategy(BinaryDataStrategy.BASE_64);

	@Override public Jsonb getContext(Class<?> type)
	{
		return JsonbBuilder.newBuilder().withConfig(CONFIG).build();
	}

	public static Jsonb context() { return new JsonbConfigurator().getContext(null); }

	public static Jsonb context(JsonbConfig config)
	{
		return JsonbBuilder.newBuilder().withConfig(config).build();
	}

	public static JsonbConfig config() { return CONFIG; }
}