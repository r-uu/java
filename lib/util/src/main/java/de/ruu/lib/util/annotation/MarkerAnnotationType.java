package de.ruu.lib.util.annotation;

import java.lang.annotation.*;

/**
 * Marker annotation for elements of kind {@link ElementType#TYPE}.
 *
 * @author r-uu
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Documented
public @interface MarkerAnnotationType { }