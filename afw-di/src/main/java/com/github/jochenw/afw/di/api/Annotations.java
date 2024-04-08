/*
 * Copyright 2018 Jochen Wiedmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jochenw.afw.di.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.di.impl.GoogleAnnotationProvider;
import com.github.jochenw.afw.di.impl.JakartaAnnotationProvider;
import com.github.jochenw.afw.di.impl.JavaxAnnotationProvider;

/** This class is the anchor point for working with annotation
 * providers.
 */
public class Annotations {
	private static final @NonNull IAnnotationProvider @NonNull[] annotationProviders = initAnnotationProviders();

	private static @NonNull IAnnotationProvider @NonNull[] initAnnotationProviders() {
		final List<@NonNull IAnnotationProvider> list = new ArrayList<>();
		try {
			final @NonNull IAnnotationProvider ap = new JavaxAnnotationProvider();
			// Check, whether the provider works.
			ap.getInjectClass();
			list.add(ap);
		} catch (Throwable t) {
			// Ignore this,
		}
		try {
			final @NonNull IAnnotationProvider ap = new JakartaAnnotationProvider();
			// Check, whether the provider works.
			ap.getInjectClass();
			list.add(ap);
		} catch (Throwable t) {
			// Ignore this,
		}
		try {
			final @NonNull IAnnotationProvider ap = new GoogleAnnotationProvider();
			// Check, whether the provider works.
			ap.getInjectClass();
			list.add(ap);
		} catch (Throwable t) {
			// Ignore this,
		}
		@SuppressWarnings("null")
		final @NonNull IAnnotationProvider @NonNull[] array = list.toArray(new IAnnotationProvider[list.size()]);
		return array;
	}

	/** If the given annotation is in instance of the frameworks
	 * Named annotation class: Returns the Named annotations value.
	 * Otherwise, returns null.
	 * @param pAnnotation The annotation, which is tested to be a
	 *   Named annotation.
	 * @return The Named annotations value, or null.
	 */
	public static String getNamedValue(@NonNull Annotation pAnnotation) {
		for (IAnnotationProvider annotationProvider : annotationProviders) {
			final String value = annotationProvider.getNamedValue(pAnnotation);
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	/** Returns the annotation provider with the given id.
	 * @param pId The requested annotation providers id.
	 * @return the annotation provider with the given id, or null.
	 */
	public static IAnnotationProvider getProvider(String pId) {
		final String id = Objects.requireNonNull(pId, "Id");
		for (IAnnotationProvider annotationProvider : annotationProviders) {
			if (id.equals(annotationProvider.getId())) {
				return annotationProvider;
			}
		}
		return null;
	}

	/** Returns the default annotation provider: This will be a
	 * {@link JavaxAnnotationProvider}, if available, otherwise a
	 * {@link JakartaAnnotationProvider}.
	 * @return The default annotation provider
	 */
	public static @NonNull IAnnotationProvider getDefaultProvider() {
		return annotationProviders[0];
	}

	/** Returns, whether the given annotation is an Inject annotation.
	 * @param pAnnotation The annotation, which is being tested.
	 * @return True, if the given annotation is an Inject annotation.
	 *   Otherwise false.
	 */
	public static boolean isInjectAnnotation(Annotation pAnnotation) {
		for (IAnnotationProvider ap : annotationProviders) {
			if (ap.getInjectClass().isAssignableFrom(pAnnotation.getClass())) {
				return true;
			}
		}
		return false;
	}

	/** Returns, whether the given method, or field, is annotated with
	 * an {@code Inject} annotation.
	 * @param pAccessible The method, or field, which is being tested.
	 * @return True, if the given method, or field has an {@code Inject}
	 *   annotation.
	 */
	public static boolean isInjectPresent(AccessibleObject pAccessible) {
		for (IAnnotationProvider ap : annotationProviders) {
			if (pAccessible.isAnnotationPresent(ap.getInjectClass())) {
				return true;
			}
		}
		return false;
	}

	/** Returns, whether the given object, is a
	 * {@code Provider}.
	 * @param pType The class, which is being tested.
	 * @return True, if the given method, or field has an {@code Inject}
	 *   annotation.
	 */
	public static boolean isProviderClass(Type pType) {
		for (IAnnotationProvider ap : annotationProviders) {
			if (ap.getProviderClass() == pType) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the list of available annotation providers.
	 * @return the list of available annotation providers.
	 */
	public static IAnnotationProvider[] getProviders() {
		return annotationProviders;
	}

}
