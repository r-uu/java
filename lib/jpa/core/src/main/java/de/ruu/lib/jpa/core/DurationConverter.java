package de.ruu.lib.jpa.core;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Duration;

import static java.time.temporal.ChronoUnit.NANOS;

@Converter(autoApply = true)
public class DurationConverter implements AttributeConverter<Duration, Long>
{
	@Override public Long     convertToDatabaseColumn( Duration duration) { return duration.toNanos(); }
	@Override public Duration convertToEntityAttribute(Long     duration) { return Duration.of(duration, NANOS); }
}