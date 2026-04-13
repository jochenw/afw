package com.github.jochenw.afw.di.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Supplier;

import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.Key;
import com.github.jochenw.afw.di.api.Scopes;
import com.github.jochenw.afw.di.api.Scopes.Scope;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.name.Names;


/** Implementation of {@link IComponentFactory}, based
 * on {@code Google Guice}.
 */
public class GuiceComponentFactory extends AbstractComponentFactory {
	private Injector injector;
	private Map<Key<Object>,IBinding<Object>> bindings;

	@Override
	public void init(IConfiguration pConfiguration) {
		super.init(pConfiguration);
		injector = newInjector(pConfiguration);
		bindings = pConfiguration.getBindings();
	}

	@Override
	public <T> IBinding<T> getBinding(Key<T> pKey) {
		@SuppressWarnings("unchecked")
		final IBinding<T> binding = (IBinding<T>) bindings.get(pKey);
		return binding;
	}

	@Override
	public void init(Object pObject) {
		injector.injectMembers(pObject);
	}

	@Override
	public <T> Supplier<T> getInstantiator(Class<? extends T> pImplType) {
		return () -> injector.getInstance(pImplType);
	}

	@Override
	public <T> Supplier<T> getInstantiator(Constructor<? extends T> pConstructor) {
		return getInstantiator(pConstructor.getDeclaringClass());
	}

	@Override
	public Map<Key<Object>, IBinding<Object>> getBindings() {
		return bindings;
	}

	/** Creaes the Guice injector, as specified by the given configuration.
	 * @param pConfiguration The injectors configuration.
	 * @return The created injector.
	 */
	protected Injector newInjector(IConfiguration pConfiguration) {
		final com.google.inject.Module module = (b) -> {
			for (Map.Entry<Key<Object>,IBinding<Object>> en : pConfiguration.getBindings().entrySet()) {
				final Key<Object> key = en.getKey();
				final IBinding<Object> binding = en.getValue();
				final com.google.inject.Key<Object> gKey = asGuiceKey(key);
				final LinkedBindingBuilder<Object> lbb = b.bind(gKey);
				final Provider<Object> provider = new Provider<>() {
					@Override
					public Object get() {
						return binding.apply(GuiceComponentFactory.this);
					}
				};
				lbb.toProvider(provider);
				final Scope scope = binding.getScope();
				if (scope == Scopes.EAGER_SINGLETON) {
					lbb.asEagerSingleton();
				} else if (scope == Scopes.SINGLETON) {
					lbb.in(com.google.inject.Scopes.SINGLETON);
				} else if (scope == Scopes.NO_SCOPE) {
					lbb.in(com.google.inject.Scopes.NO_SCOPE);
				} else {
					throw new IllegalStateException("Invalid scope: " + scope.name());
				}
			}
		};
		return Guice.createInjector(module);
	}

	@SuppressWarnings("unchecked")
	private com.google.inject.Key<Object> asGuiceKey(Key<Object> pKey) {
		final Type type = pKey.getType();
		Annotation annotation = pKey.getAnnotation();
		final Class<? extends Annotation> annotationType = pKey.getAnnotationType();
		if (pKey.getName() != null) {
			if (annotation != null) {
				throw new IllegalStateException("Guice doesn't support annotated bindings"
						+ " with a name.");
			} else {
				annotation = Names.named(pKey.getName());
			}
		}
		if (annotation == null) {
			return (com.google.inject.Key<Object>) com.google.inject.Key.get(type);
		} else {
			if (annotationType == null) {
				return (com.google.inject.Key<Object>) com.google.inject.Key.get(type, annotation);
			} else {
				return (com.google.inject.Key<Object>) com.google.inject.Key.get(type, annotationType);
			}
		}
	}
}
