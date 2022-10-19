package com.github.jochenw.afw.di.impl.guice;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import javax.inject.Provider;

import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IOnTheFlyBinder;
import com.github.jochenw.afw.di.api.Key;
import com.github.jochenw.afw.di.api.Scope;
import com.github.jochenw.afw.di.api.Scopes;
import com.github.jochenw.afw.di.impl.AbstractComponentFactory;
import com.github.jochenw.afw.di.impl.BindingBuilder;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.matcher.Matchers;


/** Implementation of {@link IComponentFactory}, that uses Google Guice
 * internally.
 */
public class GuiceComponentFactory extends AbstractComponentFactory {
	private Injector injector;

	@Override
	public void init(Object pObject) {
		final Object object = Objects.requireNonNull(pObject, "Object");
		injector.injectMembers(object);
	}

	@Override
	public <O> O getInstance(Key<O> pKey) {
		final Key<O> key = Objects.requireNonNull(pKey, "Key");
		final O o = (O) injector.getInstance(asGuiceKey(key));
		return o;
	}

	@Override
	public <O> O newInstance(Class<? extends O> pImplClass) {
		final Class<?> implClass = Objects.requireNonNull(pImplClass, "Class");
		@SuppressWarnings("unchecked")
		final O o = (O) injector.getInstance(implClass);
		return o;
	}

	@Override
	public <O> O newInstance(Constructor<? extends O> pConstructor) {
		throw new IllegalStateException("Not implemented");
	}

	@Override
	public void configure(IOnTheFlyBinder pOnTheFlyBinder,
			              List<BindingBuilder<Object>> pBuilders,
			              Set<Class<?>> pStaticInjectionClasses) {
		// Eliminate duplicate bindings.
		final Map<Key<Object>,BindingBuilder<Object>> bindingsByKey = new HashMap<>();
		for (BindingBuilder<Object> bb : pBuilders) {
			final Key<Object> key = bb.getKey();
			bindingsByKey.put(key, bb);
		}
		com.google.inject.Module module = new com.google.inject.Module() {
			@Override
			public void configure(Binder pBinder) {
				if (pOnTheFlyBinder != null) {
					pBinder.bindListener(Matchers.any(), new GuiceComponentFactoryTypeListener(GuiceComponentFactory.this, pOnTheFlyBinder));
				}
				for (Class<?> cl : pStaticInjectionClasses) {
					pBinder.requestStaticInjection(cl);
				}
				for (BindingBuilder<Object> bib : pBuilders) {
					final Key<Object> key = bib.getKey();
					final BindingBuilder<Object> bb = bindingsByKey.remove(key);
					if (bb == null) {
						continue;
					}
					final com.google.inject.Key<Object> gkey = asGuiceKey(key, bb.getAnnotation(), bb.getAnnotationType());
					final LinkedBindingBuilder<Object> lbb = pBinder.bind(gkey);
					final ScopedBindingBuilder sbb;
					@SuppressWarnings("unchecked")
					final Class<Object> targetClass = (Class<Object>) bb.getTargetClass();
					if (targetClass == null) {
						@SuppressWarnings("unchecked")
						final Constructor<Object> targetConstructor = (Constructor<Object>) bb.getTargetConstructor();
						if (targetConstructor == null) {
							final Object targetInstance = bb.getTargetInstance();
							if (targetInstance == null) {
								@SuppressWarnings("unchecked")
								final Key<Object> targetKey = (Key<Object>) bb.getTargetKey();
								if (targetKey == null) {
									@SuppressWarnings("unchecked")
									final Provider<Object> targetProvider = (Provider<Object>) bb.getTargetProvider();
									if (targetProvider == null) {
										@SuppressWarnings("unchecked")
										final Supplier<Object> targetSupplier = (Supplier<Object>) bb.getTargetSupplier();
										if (targetSupplier == null) {
											final Type type = key.getType();
											if (type instanceof Class) {
												final Class<?> cl = (Class<?>) type;
												if (cl.isAnnotation()) {
													throw new IllegalStateException("No target was specified on the BindingBuilder"
															+ " for key " + key + ", and self-binding is not possible for an annotation class.");
												} else if (cl.isArray()) {
													throw new IllegalStateException("No target was specified on the BindingBuilder"
															+ " for key " + key + ", and self-binding is not possible for an array class.");
												} else if (cl.isEnum()) {
													throw new IllegalStateException("No target was specified on the BindingBuilder"
															+ " for key " + key + ", and self-binding is not possible for an enum class.");
												} else if (cl.isInterface()) {
													throw new IllegalStateException("No target was specified on the BindingBuilder"
															+ " for key " + key + ", and self-binding is not possible for an interface class.");
												} else {
													// Self-Binding, do nothing.
													sbb = (ScopedBindingBuilder) lbb;
												}
											} else {
												throw new IllegalStateException("No target was specified on the BindingBuilder"
														+ " for key " + key + ", and self-binding is not possible for a generic type.");
											}
										} else {
											sbb = lbb.toProvider(() -> targetSupplier.get());
										}
									} else {
										sbb = lbb.toProvider(targetProvider);
									}
								} else {
									sbb = lbb.to(asGuiceKey(targetKey));
								}
							} else {
								lbb.toInstance(targetInstance);
								sbb = null;
							}
						} else {
							sbb = lbb.toConstructor(targetConstructor);
						}
					} else {
						sbb = lbb.to(targetClass);
					}
					if (sbb != null) {
						final Scope scope = bb.getScope();
						if (scope == Scopes.SINGLETON) {
							sbb.in(com.google.inject.Scopes.SINGLETON);
						} else if (scope == Scopes.NO_SCOPE  ||  scope == null) {
							sbb.in(com.google.inject.Scopes.NO_SCOPE);
						} else if (scope == Scopes.NO_SCOPE) {
							sbb.asEagerSingleton();
						}
					}
				}
			}
		};
		injector = Guice.createInjector(module);
	}

	protected <O> com.google.inject.Key<O> asGuiceKey(final Key<O> pKey) {
		return asGuiceKey(pKey, null, null);
	}

	protected <O> com.google.inject.Key<O> asGuiceKey(final Key<O> pKey, Annotation pAnnotation, Class<? extends Annotation> pAnnotationType) {
		final com.google.inject.Key<?> gkey;
		final Annotation annotation = pKey.getAnnotation() == null ? pAnnotation : pKey.getAnnotation();
		final Class<? extends Annotation> annotationType = pKey.getAnnotationClass() == null ? pAnnotationType : pKey.getAnnotationClass();
		if (annotation == null) {
			if (annotationType == null) {
				gkey = com.google.inject.Key.get(pKey.getType());
			} else {
				gkey = com.google.inject.Key.get(pKey.getType(), annotationType);
			}
		} else {
			gkey = com.google.inject.Key.get(pKey.getType(), annotation);
		}
		@SuppressWarnings("unchecked")
		final com.google.inject.Key<O> k = (com.google.inject.Key<O>) gkey;
		return k;
	}

	@Override
	public boolean hasKey(Key<?> pKey) {
		@SuppressWarnings("unchecked")
		final Key<Object> key = (Key<Object>) pKey;
		final com.google.inject.Key<Object> gkey = asGuiceKey(key);
		return injector.getBinding(gkey) != null;
	}

}
