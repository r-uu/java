package de.ruu.lib.util.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for all kind of elements (no particular {@link Target} specification).
 *
 * @author r-uu
 */
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface MarkerAnnotation {}