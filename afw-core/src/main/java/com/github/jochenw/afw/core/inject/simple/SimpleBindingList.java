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
package com.github.jochenw.afw.core.inject.simple;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.github.jochenw.afw.core.inject.Key;
import com.github.jochenw.afw.core.inject.simple.SimpleComponentFactory.Binding;


/**
 * A binding list is a collection of bindings, that have the
 * same type, but not the same key. The idea is, that we use
 * the type to identify a binding list, and then search for
 * an actually matching key in the list. Roughly like a hash
 * map.
 */
public class SimpleBindingList {
	private static class KeyAndBinding {
		final Key<?> key;
		Binding<?> binding;

		KeyAndBinding(Key<?> pKey, Binding<?> pBinding) {
			key = pKey;
			binding = pBinding;
		}
	}
	private final List<KeyAndBinding> bindings = new ArrayList<>();

	/**
	 * Adds a new, or replaces an existing binding with the given.
	 * @param pKey The bindings key.
	 * @param pBinding The binding
	 * @param pReplace Whether to replace an existing binding, or
	 * add a new one.
	 */
	public void add(Key<?> pKey, Binding<?> pBinding, boolean pReplace) {
		for (KeyAndBinding kab : bindings) {
			if (pKey.equals(kab.key)) {
				if (pReplace) {
					kab.binding = pBinding;
					return;
				} else {
					break;
				}
			}
		}
		bindings.add(new KeyAndBinding(pKey, pBinding));
	}

	/** Given the supplied list of annotations, and the given predicate, searches
	 * for a binding, that matches.
	 * @param pAnnotations An annotated elements (a field, or a method parameter) list of annotations,
	 *   possibly empty.
	 * @param pFilter A predicate, that is supposed to match either of the annotations in the
	 *   list.
	 * @return A binding, which is applicable to either of the given annotations, that matches
	 *   the given predicate.
	 */
	public Binding<?> find(Annotation[] pAnnotations, Predicate<Annotation> pFilter) {
		Binding<?> bindingMatchedByAnnotationType = null;
		boolean annotationFound = false;
		for (Annotation annotation : pAnnotations) {
			if (pFilter.test(annotation)) {
				annotationFound = true;
				for (KeyAndBinding kab : bindings) {
					final int res = isMatching(annotation, kab);
					switch(res) {
					case 0: break;
					case 1: return kab.binding;
					case 2: if (bindingMatchedByAnnotationType == null) {
								bindingMatchedByAnnotationType = kab.binding;
							}
							break;
					default:
						throw new IllegalStateException("Invalid match type: " + res);
					}
				}
			}
		}
		if (annotationFound) {
			return bindingMatchedByAnnotationType;
		} else {
			for (KeyAndBinding kab : bindings) {
				if (isMatching(null, kab) != 0) {
					return kab.binding;
				}
			}
			return null;
		}
	}

	protected boolean isMatching(Key<?> pKey, Annotation pAnnotation) {
		final Annotation keyAnnotation = pKey.getAnnotation();
		if (keyAnnotation == null) {
			return false;
		} else {
			return keyAnnotation.equals(pAnnotation);
		}
	}

	protected boolean isMatching(Class<? extends Annotation> pKeyAnnotationType, Annotation pAnnotation) {
		if (pKeyAnnotationType == null) {
			return false;
		} else {
			return pKeyAnnotationType.isAssignableFrom(pAnnotation.annotationType());
		}
	}

	/** Searches the bindings in the list for an exact match of the given key.
	 * @param pKey A key with the signature of an injectable element (a field,
	 *   or a method parameter)
	 * @return A binding, which is associated with the given key.
	 */
	public Binding<?> find(Key<?> pKey) {
		final Annotation annotation = pKey.getAnnotation();
		for (KeyAndBinding kab : bindings) {
			if (isMatching(annotation, kab) != 0) {
				return kab.binding;
			}
		}
		return null;
	}

	private int isMatching(Annotation pAnnotation, KeyAndBinding pBinding) {
		final Key<?> key = pBinding.key;
		if (pAnnotation == null) {
			if (key.getAnnotation() == null  &&  key.getAnnotationClass() == null) {
				return 1;
			} else {
				return 0;
			}
		} else {
			final Annotation annotation = key.getAnnotation();
			if (annotation == null) {
				final Class<? extends Annotation> annotationType = key.getAnnotationClass();
				if (annotationType != null) {
					if (annotationType.isAssignableFrom(pAnnotation.annotationType())) {
						return 2;
					}
				}
			} else {
				if (pAnnotation.annotationType().equals(annotation.annotationType())) {
					if (pAnnotation.equals(annotation)) {
						return 1;
					}
				}
			}
			return 0;
		}
	}
}
