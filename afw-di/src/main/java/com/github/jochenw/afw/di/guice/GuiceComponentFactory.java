package com.github.jochenw.afw.di.guice;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Supplier;

import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IComponentFactory.IConfiguration;
import com.github.jochenw.afw.di.api.Key;
import com.github.jochenw.afw.di.api.Scopes;
import com.github.jochenw.afw.di.impl.AbstractComponentFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.name.Names;

/** Implementation of {@link IComponentFactory}, based on
 * <a href="https://github.com/google/guice">Google Guice</a>.
 */
public class GuiceComponentFactory extends AbstractComponentFactory {
	private Injector injector;
	private Map<Key<Object>,IBinding<Object>> bindings;

	/** Creates a new instance.
	 */
	public GuiceComponentFactory() {
	}

	@Override
	public void init(IConfiguration pConfiguration) {
		bindings = pConfiguration.getBindings();
		injector = newInjector(pConfiguration);
	}



	/** Creates a Guice injector with the given configuration,
	 * @param pConfiguration The injectors configuration.
	 * @return The created Guice injector.
	 */
	protected Injector newInjector(IConfiguration pConfiguration) {
		final Module module = (b) -> {
			pConfiguration.getBindings().forEach((k,bnd) -> {
				final Type type = k.getType();
				final String name = k.getName();
				Annotation annotation = k.getAnnotation();
				if (name.length() > 0) {
					if (annotation == null) {
						annotation = Names.named(name);
					} else {
						throw new IllegalStateException("Use of Annotation, and name, are mutually exclusive in Google Guice.");
					}
				}
				final Class<? extends Annotation> annotationType = k.getAnnotationType();
				com.google.inject.Key<Object> guiceKey;
				if (annotation == null) {
					if (annotationType == null) {
						@SuppressWarnings("unchecked")
						final com.google.inject.Key<Object> gKey = (com.google.inject.Key<Object>) com.google.inject.Key.get(type);
						guiceKey = gKey;
					} else {
						@SuppressWarnings("unchecked")
						final com.google.inject.Key<Object> gKey = (com.google.inject.Key<Object>) com.google.inject.Key.get(type, annotationType);
						guiceKey = gKey;
					}
				} else {
					if (annotationType == null) {
						@SuppressWarnings("unchecked")
						final com.google.inject.Key<Object> gKey = (com.google.inject.Key<Object>) com.google.inject.Key.get(type, annotation);
						guiceKey = gKey;
					} else {
						throw new IllegalStateException("Use of Annotation, and annotation type are mutually exclusive in Google Guice.");
					}
				}
				final LinkedBindingBuilder<Object> lbb = b.bind(guiceKey);
				final ScopedBindingBuilder sbb = lbb.toProvider(() -> bnd.apply(GuiceComponentFactory.this));
				if (bnd.getScope() == Scopes.EAGER_SINGLETON) {
					sbb.asEagerSingleton();
				} else if (bnd.getScope() == Scopes.SINGLETON) {
					sbb.in(com.google.inject.Scopes.SINGLETON);
				} else if (bnd.getScope() == Scopes.NO_SCOPE) {
					sbb.in(com.google.inject.Scopes.NO_SCOPE);
				}
			});
			pConfiguration.getStaticInjectionClasses().forEach((c) -> {
				b.requestStaticInjection(c);
			});
		};
		return Guice.createInjector(module);
	}

	@Override
	public <T> IBinding<T> getBinding(com.github.jochenw.afw.di.api.Key<T> pKey) {
		final IBinding<Object> binding = bindings.get(pKey);
		@SuppressWarnings("unchecked")
		final IBinding<T> result = (IBinding<T>) binding;
		return result;
	}

	@Override
	public void init(Object pObject) {
		injector.injectMembers(pObject);
	}

	@Override
	public <T> Supplier<T> getInstantiator(Class<? extends T> pImplType) {
		return () -> {
			final T t = (T) injector.getInstance(pImplType);
			return t;
		};
	}

	@Override
	public <T> Supplier<T> getInstantiator(Constructor<? extends T> pConstructor) {
		throw new IllegalStateException("Not implemented.");
	}

	@Override
	public Map<com.github.jochenw.afw.di.api.Key<Object>, IBinding<Object>> getBindings() {
		return bindings;
	}
}
