package com.github.jochenw.afw.di.api;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;

/** Interface of an object, that recognizes inject-related annotations of
 * a given type.
 */
public interface IAnnotationProvider {
	/** Returns true, if the given method, constructor, or
	 * field is annotated with an @Inject annotation.
	 * @param pObject The method, constructor, or field,
	 * which is being tested.
	 * @return True, if the given object has an
	 *   @Inject annotation.
	 */
	public boolean isInjectable(AccessibleObject pObject);

	/** If the given method, method parameter, constructor,
	 * or field, is annotated with an @Named annotation:
	 * Returns the value of the annotation.
	 * Otherwise, returns null.
	 * @param pObject The method, constructor, or field,
	 * which is being tested.
	 * @return The value of the given objects @Named
	 *   annotation, if any, or null.
	 */
	public String getNamedValue(AnnotatedElement pObject);
}
