package com.github.jochenw.afw.di.impl.simple;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.github.jochenw.afw.di.api.IComponentFactoryAware;
import com.github.jochenw.afw.di.api.Key;

public class BindingSet {
	public static class KeyAndBinding {
		private final Key<Object> key;
		private final Binding binding;

		public KeyAndBinding(Key<Object> key, Binding binding) {
			this.key = key;
			this.binding = binding;
		}

		public Key<Object> getKey() { return key; }
		public Binding getBinding() { return binding; }
	}

	private final List<KeyAndBinding> bindings = new ArrayList<>();
	private final Type type;

	public BindingSet(Type pType) {
		type = pType;
	}

	public synchronized Binding find(Key<Object> pKey) {
		final Predicate<Key<Object>> predicate;
		if (pKey.getAnnotation() == null) {
			if (pKey.getAnnotationClass() == null) {
				predicate = (key) -> {
					return key.getAnnotation() == null  &&  key.getAnnotationClass() == null;
				};
			} else {
				predicate = (key) -> {
					return key.getAnnotation() == null  &&  pKey.getAnnotationClass() == key.getAnnotationClass();
				};
			}
		} else {
			predicate = (key) -> {
				return key.getAnnotation() != null  &&  key.getAnnotation().equals(pKey.getAnnotation());
			};
		}
		for (KeyAndBinding kab : bindings) {
			if (predicate.test(kab.getKey())) {
				return kab.getBinding();
			}
		}
		return null;
	}

	public synchronized void register(Key<Object> pKey, Binding pBinding) {
		bindings.add(new KeyAndBinding(pKey, pBinding));
	}

	public Type getType() {
		return type;
	}

	public void forEach(Consumer<Binding> pConsumer) {
		for (KeyAndBinding kab : bindings) {
			pConsumer.accept(kab.getBinding());
		};
	}

	public Binding find(Annotation[] pAnnotations, Predicate<Annotation> pAnnotationPredicate) {
		Binding defaultBinding = null;
		Binding classAnnotatedBinding = null;
		for (KeyAndBinding kab : bindings) {
			final Key<Object> key = kab.getKey();
			if (key.getAnnotation() == null) {
				final Class<? extends Annotation> annotationClass = key.getAnnotationClass();
				if (annotationClass == null) {
					if (defaultBinding == null) {
						defaultBinding = kab.getBinding();
					}
				} else {
					for (Annotation annotation : pAnnotations) {
						if (pAnnotationPredicate.test(annotation)) {
							if (annotation.getClass() == annotationClass) {
								classAnnotatedBinding = kab.getBinding();
							}
						}
					}
				}
			} else {
				for (Annotation annotation : pAnnotations) {
					if (key.getAnnotation().equals(annotation)) {
						return kab.getBinding();
					}
				}
			}
		}
		if (classAnnotatedBinding != null) {
			return null;
		}
		return defaultBinding;
	}
}
