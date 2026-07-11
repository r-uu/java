package de.ruu.lib.util.config.mp;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigValue;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.Converter;

import java.util.*;

/**
 * Test configuration for unit tests.
 * Creates a Config instance with custom properties.
 */
public class TestConfig implements Config
{
	private final Map<String, String> properties;

	public TestConfig(Map<String, String> properties)
	{
		this.properties = new HashMap<>(properties);
	}

	@Override
	public <T> T getValue(String propertyName, Class<T> propertyType)
	{
		Optional<T> value = getOptionalValue(propertyName, propertyType);
		if (value.isPresent())
		{
			return value.get();
		}
		throw new NoSuchElementException("Property " + propertyName + " not found");
	}

	@Override
	public ConfigValue getConfigValue(String propertyName)
	{
		String value = properties.get(propertyName);
		return new ConfigValue()
		{
			@Override
			public String getName() { return propertyName; }

			@Override
			public String getValue() { return value; }

			@Override
			public String getRawValue() { return value; }

			@Override
			public String getSourceName() { return "TestConfig"; }

			@Override
			public int getSourceOrdinal() { return 100; }
		};
	}

	@Override
	public <T> List<T> getValues(String propertyName, Class<T> propertyType)
	{
		return getOptionalValues(propertyName, propertyType).orElse(List.of());
	}

	@Override
	public <T> Optional<List<T>> getOptionalValues(String propertyName, Class<T> propertyType)
	{
		String value = properties.get(propertyName);
		if (value == null)
		{
			return Optional.empty();
		}

		// Simple implementation: split by comma
		String[] parts = value.split(",");
		List<T> result = new ArrayList<>();
		for (String part : parts)
		{
			@SuppressWarnings("unchecked")
			T convertedValue = (T) convert(part.trim(), propertyType);
			result.add(convertedValue);
		}
		return Optional.of(result);
	}

	@Override
	public <T> Optional<T> getOptionalValue(String propertyName, Class<T> propertyType)
	{
		String value = properties.get(propertyName);
		if (value == null)
		{
			return Optional.empty();
		}

		@SuppressWarnings("unchecked")
		T convertedValue = (T) convert(value, propertyType);
		return Optional.ofNullable(convertedValue);
	}

	@Override
	public Iterable<String> getPropertyNames()
	{
		return properties.keySet();
	}

	@Override
	public Iterable<ConfigSource> getConfigSources()
	{
		return List.of();
	}

	@Override
	public <T> T unwrap(Class<T> type)
	{
		throw new UnsupportedOperationException("unwrap not supported in TestConfig");
	}

	@Override
	public <T> Optional<Converter<T>> getConverter(Class<T> forType)
	{
		return Optional.empty();
	}

	private Object convert(String value, Class<?> targetType)
	{
		if (targetType == String.class)
		{
			return value;
		}
		if (targetType == Integer.class || targetType == int.class)
		{
			return Integer.parseInt(value);
		}
		if (targetType == Boolean.class || targetType == boolean.class)
		{
			return Boolean.parseBoolean(value);
		}
		if (targetType == Long.class || targetType == long.class)
		{
			return Long.parseLong(value);
		}
		throw new IllegalArgumentException("Unsupported type: " + targetType);
	}
}
