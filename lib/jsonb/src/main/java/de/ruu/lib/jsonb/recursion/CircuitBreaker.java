package de.ruu.lib.jsonb.recursion;

import de.ruu.lib.util.bimapped.BiMap;
import de.ruu.lib.jsonb.JsonbConfigurator;
import jakarta.json.Json;
import jakarta.json.JsonValue;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.adapter.JsonbAdapter;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

public class CircuitBreaker<ORIGINAL> implements JsonbAdapter<ORIGINAL, JsonValue>
{
	private static final Logger log  = LoggerFactory.getLogger(CircuitBreaker.class);
	private static final Jsonb  JSONB = JsonbConfigurator.context();

	private BiMap                biMap = new BiMap();
	private @NonNull Class<ORIGINAL> type;

	public CircuitBreaker(@NonNull Class<ORIGINAL> type)
	{
		this.type = Objects.requireNonNull(type, "type");
	}

	@Override public JsonValue adaptToJson(@NonNull ORIGINAL original) throws Exception
	{
		log.debug("marshalling {}", original.getClass().getName());
		Object adapted = null;

		Optional<?> optionalAdapted = biMap.lookup(original, original.getClass());

		if (optionalAdapted.isPresent())
		{
			adapted = optionalAdapted.get();
		}
		else
		{
			adapted = original;
			biMap.put(original, adapted);
		}

		return Json.createValue(JSONB.toJson(adapted));
	}

	@Override public ORIGINAL adaptFromJson(@NonNull JsonValue adapted) throws Exception
	{
		log.debug("unmarshalling {}", type.getName());
		return JSONB.fromJson(adapted.toString(), type);
	}
}
