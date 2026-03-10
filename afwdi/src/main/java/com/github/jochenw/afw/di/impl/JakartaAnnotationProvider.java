package com.github.jochenw.afw.di.impl;

import java.lang.reflect.AccessibleObject;

import com.github.jochenw.afw.di.api.IAnnotationProvider;

import jakarta.inject.Inject;
import jakarta.inject.Named;


/** Implementation of {@link IAnnotationProvider} for Jakarta
 * annotations.
 */
public class JakartaAnnotationProvider implements IAnnotationProvider {
	private static final JakartaAnnotationProvider INSTANCE = new JakartaAnnotationProvider();
	/** Returns the singleton instance.
	 * @return The singleton instance
	 */
	public static final JakartaAnnotationProvider getInstance() { return INSTANCE; }

	@Override
	public boolean isInjectable(AccessibleObject pObject) {
		return pObject.isAnnotationPresent(Inject.class);
	}

	@Override
	public String getNamedValue(AccessibleObject pObject) {
		final Named named = pObject.getAnnotation(Named.class);
		if (named == null) {
			return null;
		} else {
			return named.value();
		}
	}
}
