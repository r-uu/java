package de.ruu.lib.cdi.common;

import jakarta.enterprise.inject.spi.CDI;

import java.util.Objects;

public abstract class CDIUtil
{
	public static <T> void fire (T event) { Objects.requireNonNull(event, "event"); CDI.current().getBeanManager().getEvent().fire(event); }
	public static <T> T select(Class<T> type) { Objects.requireNonNull(type, "type"); return CDI.current().select(type).get(); }
}
