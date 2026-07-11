package de.ruu.lib.util.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Marker annotation for constructors that can be used to tell tools like mapstruct which constructor to use when
 * there are ambiguous constructors eligible.
 *
 * @author r-uu
 */
@Target(CONSTRUCTOR)
@Retention(CLASS)
@Documented
public @interface Default { }