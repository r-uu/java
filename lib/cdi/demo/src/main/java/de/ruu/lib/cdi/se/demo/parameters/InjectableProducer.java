package de.ruu.lib.cdi.se.demo.parameters;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Qualifier;
import jakarta.inject.Singleton;

@Singleton
public class InjectableProducer
{
	@Produces @Ping public String ping;
	@Produces @Pong public String pong;

	@Qualifier @Retention(RUNTIME) @Target({PARAMETER, FIELD}) public @interface Ping { }
	@Qualifier @Retention(RUNTIME) @Target({PARAMETER, FIELD}) public @interface Pong { }

	@Produces public Injectable produce() { return CDI.current().select(InjectableImpl.class).get(); }
}