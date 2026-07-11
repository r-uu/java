package de.ruu.lib.jpa.se;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@InterceptorBinding
@Target( { METHOD, TYPE } )
@Inherited
@Retention(RUNTIME)
public @interface Transactional { }