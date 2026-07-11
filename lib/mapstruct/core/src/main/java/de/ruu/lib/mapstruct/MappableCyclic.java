package de.ruu.lib.mapstruct;

import org.jspecify.annotations.NonNull;

public interface MappableCyclic<IN extends MappableCyclic<OUT, IN>, OUT extends MappableCyclic<IN, OUT>>
{
	void beforeMapping(@NonNull IN in, @NonNull ReferenceCycleTracking context);
	void  afterMapping(@NonNull IN in, @NonNull ReferenceCycleTracking context);
}
