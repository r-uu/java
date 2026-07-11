package de.ruu.lib.jsonb;

import de.ruu.lib.util.json.Sanitiser;
import jakarta.json.Json;
import jakarta.json.JsonValue;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.adapter.JsonbAdapter;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;

import static de.ruu.lib.util.Strings.isNullOrEmptyOrBlank;

/**
 * Some JSONB implementations (e.g. <a href="https://github.com/eclipse-ee4j/yasson">yasson</a>) struggle to bind sets
 * with custom element types {@code T}. This adapter helps solving issues adapting from json by attempting to sanitise
 * the json string in {@link #adaptFromJson(JsonValue)}.
 *
 * @author r-uu
 *
 * @param <T>
 */
public abstract class AbstractOptionalSetAdapter<T> implements JsonbAdapter<Optional<Set<T>>, JsonValue>
{
	private static final Jsonb JSONB = JsonbConfigurator.context();

	protected abstract Type getType();

	@Override public JsonValue adaptToJson(Optional<Set<T>> param) throws Exception
	{
		if (param.isEmpty()) return null;
		return Json.createValue(Sanitiser.sanitise(JSONB.toJson(param.get(), getType())));
	}

	/** attempts to sanitise json data */
	@Override public Optional<Set<T>> adaptFromJson(JsonValue jsonValue) throws Exception
	{
		String jsonValueAsString = jsonValue.toString();
		if (isNullOrEmptyOrBlank(jsonValueAsString))
				return null; // NOSONAR
		return JSONB.fromJson(Sanitiser.sanitise(jsonValueAsString), getType());
	}
}