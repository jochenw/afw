package com.github.jochenw.afw.di.impl.simple;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.inject.Inject;

import com.github.jochenw.afw.di.api.Key;


/** A binding set is basically a list of bindings, that have been registered for one
 * common type.
 */
public class BindingSet {
	/** A tupel of a key, that will be searched for, and a binding, that is
	 * requested by the search.
	 */
	public static class KeyAndBinding {
		private final Key<Object> key;
		private final Binding binding;

		/** Creates a new instance, with the given key, and binding.
		 * @param pKey
		 * @param pBinding
		 */
		public KeyAndBinding(Key<Object> pKey, Binding pBinding) {
			this.key = pKey;
			this.binding = pBinding;
		}

		/** Returns the key.
		 * @return The key.
		 */
		public Key<Object> getKey() { return key; }
		/** Returns the binding.
		 * @return The binding.
		 */
		public Binding getBinding() { return binding; }
	}

	private final List<KeyAndBinding> bindings = new ArrayList<>();
	private final Type type;

	/** Creates a new instance with the given type.
	 * @param pType The bindings sets type. In other words: The common type,
	 *   that all bindings in the set share.
	 */
	public BindingSet(Type pType) {
		type = pType;
	}

	/** Finds a binding with the given key. As the keys type is supposed to match
	 * the binding set's type, this is basically a search for the keys annotation,
	 * or annotation class.
	 * @param pKey The key, that is being searched for.
	 * @return The matching binding, if any, or null.
	 */
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

	/** Registers a new binding with the given key.
	 * @param pKey The bindings key.
	 * @param pBinding The registered binding.
	 */
	public synchronized void register(Key<Object> pKey, Binding pBinding) {
		bindings.add(new KeyAndBinding(pKey, pBinding));
	}

	/** Returns the binding sets type.
	 * @return The bindings sets type.
	 */
	public Type getType() {
		return type;
	}

	/** Iterates over all the bindings in the set.
	 * @param pConsumer The consumer, which is being invoked for all the bindings.
	 */
	public void forEach(Consumer<Binding> pConsumer) {
		for (KeyAndBinding kab : bindings) {
			pConsumer.accept(kab.getBinding());
		};
	}

	/** Finds a binding for the given annotations.
	 * @param pAnnotations The annotations, that must be matched.
	 * @return The matching binding, if any, or null.
	 */
	public Binding find(Annotation[] pAnnotations) {
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
						if (!(annotation instanceof Inject)) {
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
