package de.ruu.lib.gen.java.fx.bean;


import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.FIELD })
public @interface FXProperty
{
	boolean readOnly()       default false;
	int     parameterIndex() default 0; // position of property in all-args-constructor
}