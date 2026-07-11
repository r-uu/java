package de.ruu.lib.jsonb;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import jakarta.json.bind.config.PropertyVisibilityStrategy;

/**
 * use this class to configure {@code ContextResolver<Jsonb>} objects to handle private elements (fields <b>and</b>
 * methods) as visible
 *
 * @see JsonbConfigurator for an example
 */
public class PrivateElementsVisibleStrategy implements PropertyVisibilityStrategy
{
	@Override public boolean isVisible(Field field  ) { return true; }
	@Override public boolean isVisible(Method method) { return true; }
}