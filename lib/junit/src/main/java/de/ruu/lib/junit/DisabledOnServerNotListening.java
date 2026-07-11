package de.ruu.lib.junit;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DisabledOnServerNotListeningCondition.class)
public @interface DisabledOnServerNotListening
{
	String propertyNameHost() default "host";
	String propertyNamePort() default "port";
}