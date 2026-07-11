package de.ruu.lib.util.annotation;

import java.lang.annotation.*;

/**
 * Marker annotation for types and packages. {@link MarkerAnnotationPackageType#ignore()} optionally signals that this annotation should be ignored.
 *
 * @author r-uu
 */
@Retention(RetentionPolicy.SOURCE)
@Target
(
		{
			ElementType.TYPE,
			ElementType.PACKAGE
		}
)
@Documented
public @interface MarkerAnnotationPackageType
{
	/**
	 * @return Set this annotation attribute to <code>true</code> if you want to
	 *         signal to annotation processors that an annotated element and
	 *         existing nested elements are to be ignored.
	 *         <p>
	 *         This is especially useful if you want to ignore nested elements of
	 *         let's say a package without removing annotations for each nested
	 *         element (e.g. class in that package) manually.
	 */
	boolean ignore() default false;
}