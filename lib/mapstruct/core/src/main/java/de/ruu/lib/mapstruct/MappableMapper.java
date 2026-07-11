package de.ruu.lib.mapstruct;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Context;

import java.util.Objects;

public interface MappableMapper<IN extends Mappable<IN, OUT>, OUT extends Mappable<OUT, IN>>
{
	@NonNull OUT map(@NonNull IN in);

	default void beforeMapping(@NonNull IN in, @NonNull @Context OUT out)
	{
		Objects.requireNonNull(in,  "in");
		Objects.requireNonNull(out, "out");
		out.beforeMapping(out, in);
	}

	default void afterMapping(@NonNull IN in, @NonNull @Context OUT out)
	{
		Objects.requireNonNull(in,  "in");
		Objects.requireNonNull(out, "out");
		out.afterMapping(out, in);
	}
}
