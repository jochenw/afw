package com.github.jochenw.afw.di.impl;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;

import com.github.jochenw.afw.di.api.IAnnotationProvider;

import javax.inject.Inject;
import javax.inject.Named;

/** Implementation of {@link IAnnotationProvider} for Javax
 * annotations.
 */
public class JavaxAnnotationProvider implements IAnnotationProvider {
	private static final JavaxAnnotationProvider INSTANCE = new JavaxAnnotationProvider();
	/** Returns the singleton instance.
	 * @return The singleton instance
	 */
	public static final JavaxAnnotationProvider getInstance() { return INSTANCE; }

	@Override
	public boolean isInjectable(AccessibleObject pObject) {
		return pObject.isAnnotationPresent(Inject.class);
	}

	@Override
	public String getNamedValue(AnnotatedElement pObject) {
		final Named named = pObject.getAnnotation(Named.class);
		if (named == null) {
			return null;
		} else {
			return named.value();
		}
	}
}
