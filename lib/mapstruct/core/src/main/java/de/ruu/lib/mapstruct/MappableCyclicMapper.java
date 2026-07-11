package de.ruu.lib.mapstruct;

import org.jspecify.annotations.NonNull;
import org.mapstruct.*;

import java.util.Objects;

public interface MappableCyclicMapper<IN extends MappableCyclic<OUT, IN>, OUT extends MappableCyclic<IN, OUT>>
{
	@NonNull OUT map(@NonNull IN in, @NonNull @Context ReferenceCycleTracking context);

	/**
	 * annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called as well as
	 * annotating parameter {@code context} with {@link Context}
	 */
	@BeforeMapping
	default void beforeMapping(
			@NonNull                IN                     in,
			@NonNull @MappingTarget OUT                    out,
			@NonNull @Context       ReferenceCycleTracking context)
	{
		Objects.requireNonNull(in,      "in");
		Objects.requireNonNull(out,     "out");
		Objects.requireNonNull(context, "context");
		out.beforeMapping(in, context);
	}

	/**
	 * annotating parameter {@code out} with {@link MappingTarget} is essential for this method being called as well as
	 * annotating parameter {@code context} with {@link Context}
	 */
	@AfterMapping
	default void afterMapping(
			@NonNull                IN                     in,
			@NonNull @MappingTarget OUT                    out,
			@NonNull @Context       ReferenceCycleTracking context)
	{
		Objects.requireNonNull(in,      "in");
		Objects.requireNonNull(out,     "out");
		Objects.requireNonNull(context, "context");
		out.afterMapping(in, context);
	}

	@NonNull Class<OUT> outType();
	@NonNull OUT        create(@NonNull IN in, @NonNull ReferenceCycleTracking context);

	/**
	 * object factory will be called by mapstruct during generated {@link #map(MappableCyclic, ReferenceCycleTracking)}
	 * implementation
	 */
	@ObjectFactory
	default @NonNull OUT lookupOrCreate(@NonNull IN in, @NonNull @Context ReferenceCycleTracking context)
	{
		Objects.requireNonNull(in,      "in");
		Objects.requireNonNull(context, "context");
		OUT out = context.get(in, outType());
		if (out == null)
		{
			out = create(in, context);
//			context.put(in, out); // mapstruct will put in and out into context directly after this method returns
		}
		return out;
	}
}
