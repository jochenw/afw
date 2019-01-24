package com.github.jochenw.afw.core.inject.simple;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.github.jochenw.afw.core.inject.Key;
import com.github.jochenw.afw.core.inject.simple.SimpleComponentFactory.Binding;


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

	public void add(Key<?> pKey, Binding<?> pBinding, boolean pReplace) {
		for (KeyAndBinding kab : bindings) {
			if (pKey.equals(pBinding)) {
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
