/**
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
package com.github.jochenw.afw.core.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Provider;

import com.github.jochenw.afw.core.inject.Types.Type;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Objects;


public abstract class ComponentFactoryBuilder<T extends ComponentFactoryBuilder<T>> {
	public interface ScopedBindingBuilder {
	    void in(Scope scope);
	    void asEagerSingleton();
	}
	public interface LinkedBindingBuilder<T extends Object> extends ScopedBindingBuilder {
	    ScopedBindingBuilder to(Class<? extends T> pImplementation);
	    ScopedBindingBuilder toClass(Class<?> pImplementation);
	    ScopedBindingBuilder to(Key<? extends T> targetKey);
	    void toInstance(T pInstance);
	    ScopedBindingBuilder toProvider(Provider<? extends T> pProvider);
	    ScopedBindingBuilder toSupplier(Supplier<? extends T> pSupplier);
	    ScopedBindingBuilder toProvider(Class<? extends Provider<? extends T>> pProviderType);
	    <P extends Provider<? extends T>> ScopedBindingBuilder toProvider(Key<P> providerKey);
	    <S extends T> ScopedBindingBuilder toConstructor(Constructor<S> constructor);
	}
	public interface AnnotatedBindingBuilder<T extends Object> extends LinkedBindingBuilder<T> {
	    LinkedBindingBuilder<T> annotatedWith(Class<? extends Annotation> pAnnotationType);
	    LinkedBindingBuilder<T> annotatedWith(Annotation pAnnotation);
	    LinkedBindingBuilder<T> named(String pName);
	}
	public interface Binder {
	    <T> LinkedBindingBuilder<T> bind(Key<T> pKey);
	    <T> AnnotatedBindingBuilder<T> bind(Types.Type<T> pType);
	    <T> LinkedBindingBuilder<T> bind(Types.Type<T> pType, String pName);
	    <T> AnnotatedBindingBuilder<T> bind(Class<T> pType);
	    <T> LinkedBindingBuilder<T> bind(Class<T> pType, String pName);
	    void requestStaticInjection(Class<?>... types);
	}
	public interface Module {
		void configure(Binder pBinder);
	}

	private boolean immutable;
	private OnTheFlyBinder onTheFlyBinder;
	private @Nullable IComponentFactory instance;
	private Class<? extends IComponentFactory> componentFactoryClass;
	private final List<Module> modules = new ArrayList<>();

	public T onTheFlyBinder(OnTheFlyBinder pBinder) {
		assertMutable();
		onTheFlyBinder = pBinder;
		return self();
	}

	public OnTheFlyBinder getOnTheFlyBinder() {
		return onTheFlyBinder;
	}

	protected void assertMutable() {
		if (immutable) {
			throw new IllegalStateException("This object is no longer mutable.");
		}
	}

	public T module(@Nonnull Module pModule) {
		final Module module = Objects.requireNonNull(pModule);
		assertMutable();
		modules.add(module);
		return self();
	}

	public T modules(Module... pModules) {
		final Module[] modules = Objects.requireAllNonNull(pModules, "Module");
		assertMutable();
		for (Module m : modules) {
			this.modules.add(m);
		}
		return self();
	}

	public T modules(Iterable<Module> pModules) {
		final Iterable<Module> modules = Objects.requireAllNonNull(pModules, "Module");
		assertMutable();
		for (Module m : modules) {
			this.modules.add(m);
		}
		return self();
	}

	public List<Module> getModules() {
		return modules;
	}

	public @Nonnull T componentFactoryClass(@Nonnull Class<? extends IComponentFactory> pType) {
		final Class<? extends IComponentFactory> cfClass = Objects.requireNonNull(pType, "Type");
		assertMutable();
		componentFactoryClass = cfClass;
		return self();
	}

	public @Nonnull Class<? extends IComponentFactory> getComponentFactoryClass() {
		return Objects.requireNonNull(componentFactoryClass);
	}

	public static class BindingBuilder<O extends Object> implements AnnotatedBindingBuilder<O>, LinkedBindingBuilder<O>, ScopedBindingBuilder {
		private Key<O> key;
		private Scope scope;
		private Annotation annotation;
		private Class<? extends Annotation> annotationClass;
		private boolean haveTarget;
		private Class<? extends O> targetClass;
		private Key<? extends O> targetKey;
		private Provider<? extends O> targetProvider;
		private Supplier<? extends O> targetSupplier;
		private Class<? extends Provider<? extends O>> targetProviderClass;
		private Key<? extends Provider<? extends O>> targetProviderKey;
		private Constructor<? extends O> targetConstructor;
		private O targetInstance;

		BindingBuilder(Key<O> pKey) {
			key = pKey;
		}

		protected void assertNotScoped() {
			if (scope != null) {
				throw new IllegalStateException("The methods in(Scope), and asEagerSingleton(), are mutually exclusive, and may be used only once.");
			}
		}

		protected void assertNotAnnotated() {
			if (annotation != null  ||  annotationClass != null) {
				throw new IllegalStateException("The methods annotatedWith(Annotation), and annotatedWith(Class), and named(), are mutually exclusive, and may be used only once.");
			}
		}

		protected void assertNotTargeted() {
			if (haveTarget) {
				throw new IllegalStateException("The methods to(Class), to(Key), toInstance(), toProvider(*), and toConstructor(*), "
						+ " are mutually exclusive, and may be invoked only once.");
			}
		}

		@Override
		public void in(Scope pScope) {
			final @Nonnull Scope scp = Objects.requireNonNull(pScope);
			assertNotScoped();
			scope = scp;
		}

		@Override
		public void asEagerSingleton() {
			in(Scopes.EAGER_SINGLETON);
		}

		@Override
		public ScopedBindingBuilder to(Class<? extends O> pImplClass) {
			assertNotTargeted();
			haveTarget = true;
			targetClass = pImplClass;
			return this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public ScopedBindingBuilder toClass(Class<?> pImplClass) {
			assertNotTargeted();
			haveTarget = true;
			final Class<? extends O> cl = (Class<? extends O>) pImplClass;
			targetClass = cl;
			return this;
		}

		@Override
		public ScopedBindingBuilder to(Key<? extends O> pKey) {
			assertNotTargeted();
			haveTarget = true;
			targetKey = pKey;
			return this;
		}

		@Override
		public void toInstance(O pInstance) {
			assertNotTargeted();
			haveTarget = true;
			targetInstance = pInstance;
			if (scope == null) {
				scope = Scopes.SINGLETON;
			}
		}

		@Override
		public ScopedBindingBuilder toProvider(Provider<? extends O> pProvider) {
			assertNotTargeted();
			haveTarget = true;
			targetProvider = pProvider;
			return this;
		}

		@Override
		public ScopedBindingBuilder toSupplier(Supplier<? extends O> pSupplier) {
			assertNotTargeted();
			haveTarget = true;
			targetSupplier = pSupplier;
			return this;
		}

		@Override
		public ScopedBindingBuilder toProvider(Class<? extends Provider<? extends O>> pProviderType) {
			assertNotTargeted();
			haveTarget = true;
			targetProviderClass = pProviderType;
			return this;
		}

		@Override
		public <P extends Provider<? extends O>> ScopedBindingBuilder toProvider(Key<P> pProviderKey) {
			assertNotTargeted();
			haveTarget = true;
			targetProviderKey = pProviderKey;
			return this;
		}

		@Override
		public <S extends O> ScopedBindingBuilder toConstructor(Constructor<S> pConstructor) {
			assertNotTargeted();
			haveTarget = true;
			targetConstructor = pConstructor;
			return this;
		}

		@Override
		public LinkedBindingBuilder<O> annotatedWith(Class<? extends Annotation> pAnnotationType) {
			final Class<? extends Annotation> annoClass = Objects.requireNonNull(pAnnotationType);
			assertNotAnnotated();
			annotationClass = annoClass;
			key = new Key<O>(key.getType(), annotationClass);
			return this;
		}

		@Override
		public LinkedBindingBuilder<O> annotatedWith(Annotation pAnnotation) {
			final Annotation anno = Objects.requireNonNull(pAnnotation);
			assertNotAnnotated();
			annotation = anno;
			key = new Key<O>(key.getType(), annotation);
			return this;
		}

		@Override
		public LinkedBindingBuilder<O> named(String pName) {
			final Named named = Names.named(pName);
			return annotatedWith(named);
		}

		public Key<O> getKey() {
			return key;
		}

		public Scope getScope() {
			return scope;
		}

		public Annotation getAnnotation() {
			return annotation;
		}

		public Class<? extends Annotation> getAnnotationClass() {
			return annotationClass;
		}

		public boolean hasTarget() {
			return haveTarget;
		}

		public Class<? extends O> getTargetClass() {
			return targetClass;
		}

		public Key<? extends O> getTargetKey() {
			return targetKey;
		}

		public Provider<? extends O> getTargetProvider() {
			return targetProvider;
		}

		public Supplier<? extends O> getTargetSupplier() {
			return targetSupplier;
		}

		public Class<? extends Provider<? extends O>> getTargetProviderClass() {
			return targetProviderClass;
		}

		public Key<? extends Provider<? extends O>> getTargetProviderKey() {
			return targetProviderKey;
		}

		public Constructor<? extends O> getTargetConstructor() {
			return targetConstructor;
		}

		public O getTargetInstance() {
			return targetInstance;
		}
	}
	
	protected T self() {
		@SuppressWarnings("unchecked")
		final T t = (T) this;
		return t;
	}

	protected IComponentFactory newInstance() {
		final @Nonnull Class<? extends IComponentFactory> cfClass = getComponentFactoryClass();
		try {
			final Constructor<? extends IComponentFactory> constructor = cfClass.getDeclaredConstructor();
			if (!constructor.isAccessible()) {
				constructor.setAccessible(true);
			}
			return constructor.newInstance();
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	public @Nonnull IComponentFactory getInstance() {
		if (!immutable   ||  instance == null) {
			throw new IllegalStateException("This object is not yet mutable.");
		}
		return instance;
	}

	public @Nonnull IComponentFactory build() {
		if (instance == null) {
			immutable = true;
			final IComponentFactory cf = newInstance();
			final Set<Class<?>> staticInjectionClasses = new HashSet<>();
			final List<BindingBuilder<?>> builders = createBindingBuilders(cf, staticInjectionClasses);
			createBindings(cf, builders, staticInjectionClasses);
			instance = cf;
		}
		return instance;
	}

	protected List<BindingBuilder<?>> createBindingBuilders(IComponentFactory pCf, Set<Class<?>> pStaticInjectionClasses) {
		final List<BindingBuilder<?>> builders = new ArrayList<>();
		final Binder binder = new Binder() {
			@Override
			public <O> LinkedBindingBuilder<O> bind(Key<O> pKey) {
				final BindingBuilder<O> builder = new BindingBuilder<O>(pKey);
				final BindingBuilder<?> b = builder;
				builders.add(b);
				return builder;
			}

			@Override
			public <O> AnnotatedBindingBuilder<O> bind(Type<O> pType) {
				final java.lang.reflect.Type type = pType.getRawType();
				final Key<O> key = new Key<O>(type);
				final BindingBuilder<O> builder = new BindingBuilder<O>(key);
				final BindingBuilder<?> b = builder;
				builders.add(b);
				return builder;
			}

			@Override
			public <LT> LinkedBindingBuilder<LT> bind(Type<LT> pType, String pName) {
				final java.lang.reflect.Type type = Objects.requireNonNull(pType, "Type").getRawType();
				final Named named = Names.named(Objects.requireNonNull(pName, "Name"));
				final Key<LT> key = new Key<LT>(type, named);
				final BindingBuilder<LT> builder = new BindingBuilder<LT>(key);
				final BindingBuilder<?> b = builder;
				builders.add(b);
				return builder;
			}

			@Override
			public <AT> AnnotatedBindingBuilder<AT> bind(Class<AT> pType) {
				final Key<AT> key = new Key<AT>(pType);
				final BindingBuilder<AT> builder = new BindingBuilder<AT>(key);
				final BindingBuilder<?> b = builder;
				builders.add(b);
				return builder;
			}

			@Override
			public <LT> LinkedBindingBuilder<LT> bind(Class<LT> pType, String pName) {
				final java.lang.reflect.Type type = Objects.requireNonNull(pType, "Type");
				final Named named = Names.named(Objects.requireNonNull(pName, "Name"));
				final Key<LT> key = new Key<LT>(type, named);
				final BindingBuilder<LT> builder = new BindingBuilder<LT>(key);
				final BindingBuilder<?> b = builder;
				builders.add(b);
				return builder;
			}

			@Override
			public void requestStaticInjection(@Nonnull Class<?>... pTypes) {
				for (Class<?> type : Objects.requireAllNonNull(pTypes, "Type")) {
					pStaticInjectionClasses.add(type);
				}
			}
		};
		binder.bind(IComponentFactory.class).toInstance(pCf);
		for (Module module : getModules()) {
			module.configure(binder);
		}
		return builders;
	}

	protected abstract void createBindings(@Nonnull IComponentFactory pComponentFactory, @Nonnull List<BindingBuilder<?>> pBindings, @Nonnull Set<Class<?>> pStaticInjectionClasses);

	protected <O extends Object> Provider<O> asProvider(BindingBuilder<O> pBb) {
		if (pBb.haveTarget) {
			final O instance = pBb.targetInstance;
			if (instance == null) {
				final Class<? extends O> implClass = pBb.targetClass;
				if (implClass == null) {
					final Provider<? extends O> provider = pBb.targetProvider;
					if (provider == null) {
						final Supplier<? extends O> supplier = pBb.targetSupplier;
						if (supplier == null) {
							final Class<? extends Provider<? extends O>> providerClass = pBb.targetProviderClass;
							if (providerClass == null) {
								final Key<? extends O> key = pBb.targetKey;
								if (key == null) {
									final Key<? extends Provider<? extends O>> providerKey = pBb.targetProviderKey;
									if (providerKey == null) {
										throw new IllegalStateException("Neither of the methods to(Class), to(Key), toInstance(), toProvider(*), and toConstructor(*) have been invoked on the BindingBuilder.");
									} else {
										return () -> {
											final Provider<? extends O> o = getInstance().getInstance(providerKey);
											return o.get();
										};
									}
								} else {
									return () -> {
										final O o = getInstance().getInstance(key);
										return o;
									};
								}
							} else {
								return () -> {
									final Provider<? extends O> prov = getInstance().newInstance(providerClass);
									return prov.get();
								};
							}
						} else {
							return () -> supplier.get();
						}
					} else {
						@SuppressWarnings("unchecked")
						final Provider<O> prov = (Provider<O>) provider;
						return prov;
					}
				} else {
					return () -> {
						final O o;
						try {
							o = (O) getInstance().newInstance(implClass);
							return o;
						} catch (Throwable t) {
							throw Exceptions.show(t);
						}
					};
				}
			} else {
				return () -> instance;
			}
		} else {
			final java.lang.reflect.Type type = pBb.key.getType();
			if (type instanceof Class) {
				@SuppressWarnings("unchecked")
				final Class<? extends O> cl = (Class<? extends O>) type;
				return () -> {
					return getInstance().newInstance(cl);
				};
			} else if (type instanceof ParameterizedType) {
				final ParameterizedType ptype = (ParameterizedType) type;
				if (ptype.getRawType() instanceof Class) {
					@SuppressWarnings("unchecked")
					final Class<? extends O> cl = (Class<? extends O>) ptype.getRawType();
					return () -> {
						return getInstance().newInstance(cl);
					};
				}
			}
			throw new IllegalStateException("Unable to instantiate type: " + type);
		}
	}
}
