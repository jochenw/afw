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
package com.github.jochenw.afw.core.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Provider;

import com.github.jochenw.afw.core.inject.Types.Type;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Objects;



/**
 * Abstract base class for builders of {@link IComponentFactory}.
 * @param <T> Type of the componentfactory, that is being built.
 */
public abstract class ComponentFactoryBuilder<T extends ComponentFactoryBuilder<T>> {
	/** Interface of a binding builder without scope.
	 */
	public interface ScopedBindingBuilder {
		/**
		 * Sets the bindings scope.
		 * @param pScope Sets the bindings scope.
		 */
	    void in(Scope pScope);
		/**
		 * Sets the bindings scope to {@link Scopes#EAGER_SINGLETON}.
		 */
	    void asEagerSingleton();
	}
	/**
	 * Interface of a binding builder without scope, and {@link Supplier}.
	 * @param <T> Type of the binding, that is being built.
	 */
	public interface LinkedBindingBuilder<T extends Object> extends ScopedBindingBuilder {
		/** Sets the bindings supplier to instantiating the given class.
		 * @param pImplementation The class, which is being instantiated by the binding.
		 * @return A binding builder without scope, but with supplier.
		 */
	    ScopedBindingBuilder to(Class<? extends T> pImplementation);
		/** Sets the bindings supplier to instantiating the given class.
		 * @param pImplementation The class, which is being instantiated by the binding.
		 * @return A binding builder without scope, but with supplier.
		 */
	    ScopedBindingBuilder toClass(Class<? extends T> pImplementation);
		/** Sets the bindings supplier to referencing the given {@link Key}.
		 * @param pKey The key of the binding, that is being referenced.
		 * @return A binding builder without scope, but with supplier.
		 */
	    ScopedBindingBuilder to(Key<? extends T> pKey);
		/** Sets the bindings supplier to returning the given instance, and
		 * the bindings scope to {@link Scopes#SINGLETON}.
		 * @param pInstance The instance, which is being returned by the binding.
		 */
	    void toInstance(T pInstance);
		/** Sets the bindings supplier to an invocation of the given provider.
		 * @param pProvider The provider, that is being invoked by the binding.
		 * @return A binding builder without scope, but with supplier.
		 */
	    ScopedBindingBuilder toProvider(Provider<? extends T> pProvider);
		/** Sets the bindings supplier to the given supplier.
		 * @param pSupplier The supplier, that is being invoked by the binding.
		 * @return A binding builder without scope, but with supplier.
		 */
	    ScopedBindingBuilder toSupplier(Supplier<? extends T> pSupplier);
		/** Sets the bindings supplier to an invocation of the given constructor
		 * (without parameters).
		 * @param pConstructor The supplier, that is being invoked by the binding.
		 * @param <S> Type, that is created by the constructor.
		 * @return A binding builder without scope, but with supplier.
		 */
	    <S extends T> ScopedBindingBuilder toConstructor(Constructor<S> pConstructor);
	}
	/**
	 * Interface of a binding builder without scope, and {@link Supplier},
	 * and the ability to restriction by annotations.
	 * @param <T> Type of the binding, that is being built.
	 */
	public interface AnnotatedBindingBuilder<T extends Object> extends LinkedBindingBuilder<T> {
		/**
		 * Restricts the binding to a context, where an annotation of the given type is
		 * present.
		 * @param pAnnotationType Type of the annotation, that must be present in the
		 *   context upon application of the binding.
		 * @return A binding builder without scope, and {@link Supplier}.
		 */
	    LinkedBindingBuilder<T> annotatedWith(Class<? extends Annotation> pAnnotationType);
		/**
		 * Restricts the binding to a context, where the given annotation is present.
		 * The method {@link Object#equals(Object)} is used on the given parameter
		 * to compare annotations.
		 * @param pAnnotation The annotation, that must be present in the
		 *   context upon application of the binding.
		 * @return A binding builder without scope, and {@link Supplier}.
		 */
	    LinkedBindingBuilder<T> annotatedWith(Annotation pAnnotation);
		/**
		 * Restricts the binding to a context, where a {@link Named} annotation
		 * is present with the given value.
		 * @param pName The expected value of the {@link Named} annotation.
		 * @return A binding builder without scope, and {@link Supplier}.
		 */
	    LinkedBindingBuilder<T> named(String pName);
	}
	/** A {@link Binder} is an object, that can be used to create binding
	 * builders.
	 */
	public interface Binder {
		/** Creates a binding builder, that will be applicable in a context
		 * with the given {@link Key}.
		 * @param <T> The created binding builders type.
		 * @param pKey The key, in which the created binding will be applicable.
		 * @return The created binding builder.
		 */
	    <T> LinkedBindingBuilder<T> bind(Key<T> pKey);
		/** Creates a binding builder, that will be applicable in a context
		 * with the given {@link Type type}.
		 * @param <T> The created binding builders type.
		 * @param pType The type, in which the created binding will be applicable.
		 * @return The created binding builder.
		 */
	    <T> AnnotatedBindingBuilder<T> bind(Types.Type<T> pType);
		/** Creates a binding builder, that will be applicable in a context
		 * with the given {@link Type type}, and the given name.
		 * @param <T> The created binding builders type.
		 * @param pType The type, in which the created binding will be applicable.
		 * @param pName The expected value of the {@link Named} annotation.
		 * @return The created binding builder.
		 */
	    <T> LinkedBindingBuilder<T> bind(Types.Type<T> pType, String pName);
		/** Creates a binding builder, that will be applicable in a context
		 * with the given {@link Class type}.
		 * @param <T> The created binding builders type.
		 * @param pType The type, in which the created binding will be applicable.
		 * @return The created binding builder.
		 */
	    <T> AnnotatedBindingBuilder<T> bind(Class<T> pType);
		/** Creates a binding builder, that will be applicable in a context
		 * with the given {@link Class type}, and the given name.
		 * @param <T> The created binding builders type.
		 * @param pType The type, in which the created binding will be applicable.
		 * @param pName The expected value of the {@link Named} annotation.
		 * @return The created binding builder.
		 */
	    <T> LinkedBindingBuilder<T> bind(Class<T> pType, String pName);
	    /** Requests, that the binder should configure static fields of the given classes.
	     * @param pTypes The classes, on which static fields are being configured.
	     */
	    void requestStaticInjection(Class<?>... pTypes);
	    /** Configures a consumer, that will be invoked after creation of the
	     * {@link IComponentFactory component factory}.
	     * @param pComponentFactory The component factory, that has been created.
	     */
	    void addFinalizer(Consumer<IComponentFactory> pComponentFactory);
	}
	/**
	 * Interface of a module, that participates in the creation of bindings by
	 * consuming, and using, the given {@link Binder binder}.
	 */
	public interface Module {
		/**
		 * Called to participate in the creation of bindings by
		 * consuming, and using, the given {@link Binder binder}.
		 * @param pBinder The binder, which may be called to create
		 * new binding builders.
		 */
		void configure(Binder pBinder);
	}

	private boolean immutable;
	private OnTheFlyBinder onTheFlyBinder;
	private @Nullable IComponentFactory instance;
	private Class<? extends IComponentFactory> componentFactoryClass;
	private final List<Module> modules = new ArrayList<>();

	/**
	 * Sets an {@link OnTheFlyBinder}, which participates in the
	 * component creation by performing dynamic bindings.
	 * @param pBinder An {@link OnTheFlyBinder}, which participates in the
	 * component creation by performing dynamic bindings.
	 * @return This builder.
	 */
	public T onTheFlyBinder(@Nonnull OnTheFlyBinder pBinder) {
		assertMutable();
		onTheFlyBinder = pBinder;
		return self();
	}

	/**
	 * Returns the {@link OnTheFlyBinder}, that has been configured,
	 * if any, or null.
	 * @return An {@link OnTheFlyBinder}, which participates in the
	 * component creation by performing dynamic bindings. May be null,
	 * if no such binder has been configured.
	 */
	public OnTheFlyBinder getOnTheFlyBinder() {
		return onTheFlyBinder;
	}

	protected void assertMutable() {
		if (immutable) {
			throw new IllegalStateException("This object is no longer mutable.");
		}
	}

	/**
	 * Configures a module, that will participate in the creation of the
	 * component factory by creating binding builders. If more than one
	 * module is registered, then they will be invoked in the order of
	 * registration.
	 * @param pModule A module, that will participate in the creation of the
	 * component factory by creating binding builders.
	 * @return This builder.
	 */
	public T module(@Nonnull Module pModule) {
		final Module module = Objects.requireNonNull(pModule);
		assertMutable();
		modules.add(module);
		return self();
	}

	/**
	 * Configures zero, or more modules, that will participate in the
	 * creation of the component factory by creating binding builders.
	 * If more than one module is registered, then they will be
	 * invoked in the order of registration.
	 * @param pModules Zero, or more, modules, that will participate in
	 * the creation of the component factory by creating binding builders.
	 * @return This builder.
	 */
	public T modules(Module... pModules) {
		final Module[] modules = Objects.requireAllNonNull(pModules, "Module");
		assertMutable();
		for (Module m : modules) {
			module(m);
		}
		return self();
	}

	/**
	 * Configures zero, or more modules, that will participate in the
	 * creation of the component factory by creating binding builders.
	 * If more than one module is registered, then they will be
	 * invoked in the order of registration.
	 * @param pModules Zero, or more, modules, that will participate in
	 * the creation of the component factory by creating binding builders.
	 * @return This builder.
	 */
	public T modules(Iterable<Module> pModules) {
		final Iterable<Module> modules = Objects.requireAllNonNull(pModules, "Module");
		assertMutable();
		for (Module m : modules) {
			this.modules.add(m);
		}
		return self();
	}

	/**
	 * Returns the modules, that will participate in the
	 * creation of the component factory by creating binding builders.
	 * If more than one module is registered, then they will be
	 * invoked in the given order.
	 * @return The modules, that will participate in the
	 * creation of the component factory by creating binding builders.
	 */
	public List<Module> getModules() {
		return modules;
	}

	/**
	 * Sets the type of the component factory, that is being created.
	 * @param pType The type of the component factory, that is being created.
	 * @return This builder.
	 */
	public @Nonnull T componentFactoryClass(@Nonnull Class<? extends IComponentFactory> pType) {
		final Class<? extends IComponentFactory> cfClass = Objects.requireNonNull(pType, "Type");
		assertMutable();
		componentFactoryClass = cfClass;
		return self();
	}

	/**
	 * Returns the type of the component factory, that is being created.
	 * @return The type of the component factory, that is being created.
	 */
	public @Nonnull Class<? extends IComponentFactory> getComponentFactoryClass() {
		return Objects.requireNonNull(componentFactoryClass);
	}

	/** Default implementation of the various binding builder classes.
	 * @param <O> Type of the object, that is being created.
	 */
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
			final Class<? extends O> implClass = Objects.requireNonNull(pImplClass, "ImplClass");
			assertNotTargeted();
			haveTarget = true;
			targetClass = implClass;
			return this;
		}

		@Override
		public ScopedBindingBuilder toClass(Class<? extends O> pImplClass) {
			final Class<? extends O> implClass = Objects.requireNonNull(pImplClass, "ImplClass");
			assertNotTargeted();
			haveTarget = true;
			final Class<? extends O> cl = (Class<? extends O>) implClass;
			targetClass = cl;
			return this;
		}

		@Override
		public ScopedBindingBuilder to(Key<? extends O> pKey) {
			final Key<? extends O> key = Objects.requireNonNull(pKey, "Key");
			assertNotTargeted();
			haveTarget = true;
			targetKey = key;
			return this;
		}

		@Override
		public void toInstance(O pInstance) {
			final O instance = Objects.requireNonNull(pInstance, "Instance");
			assertNotTargeted();
			haveTarget = true;
			targetInstance = instance;
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

		/**
		 * Returns the key, that has been configured by invoking
		 * the corresponding constructor.
		 * @return The key, that has been configured by invoking
		 * the corresponding constructor.
		 */
		public Key<O> getKey() {
			return key;
		}

		/** Returns the bindings scope, that has been configured
		 * by invoking {@link #in(Scope)}, {@link #asEagerSingleton()},
		 * {@link #toInstance(Object)}.
		 * @return The bindings scope.
		 */
		public Scope getScope() {
			return scope;
		}

		/** Returns the annotation, that has been configured
		 * by invoking {@link #annotatedWith(Annotation)},
		 * if any, or null.
		 * @return The annotation, that has been configured
		 * by invoking {@link #annotatedWith(Annotation)},
		 * if any, or null.
		 */
		public Annotation getAnnotation() {
			return annotation;
		}

		/** Returns the annotation type, that has been configured
		 * by invoking {@link #annotatedWith(Class)},
		 * if any, or null.
		 * @return The annotation, that has been configured
		 * by invoking {@link #annotatedWith(Class)},
		 * if any, or null.
		 */
		public Class<? extends Annotation> getAnnotationClass() {
			return annotationClass;
		}

		/** Returns, whether either of the methods {@link #to(Class)},
		 * {@link #to(Key)}, {@link #toInstance(Object)},
		 * {@link #toConstructor(Constructor)}
		 * {@link #toSupplier(Supplier)}, or {@link #toProvider(Provider)}
		 * has been invoked.
		 * @return True, if either of the methods {@link #to(Class)},
		 * {@link #to(Key)}, {@link #toInstance(Object)},
		 * {@link #toConstructor(Constructor)},
		 * {@link #toSupplier(Supplier)}, or {@link #toProvider(Provider)}
		 * has been invoked.
		 */
		public boolean hasTarget() {
			return haveTarget;
		}

		/** Returns the type, that has been configured by invoking
		 * {@link #to(Class)}, if any, or null.
		 * @return The type, that has been configured by invoking
		 * {@link #to(Class)}, if any, or null.
		 */
		public Class<? extends O> getTargetClass() {
			return targetClass;
		}

		/** Returns the key, that has been configured by invoking
		 * {@link #to(Key)}, if any, or null.
		 * @return The key, that has been configured by invoking
		 * {@link #to(Key)}, if any, or null.
		 */
		public Key<? extends O> getTargetKey() {
			return targetKey;
		}

		/** Returns the provider, that has been configured by invoking
		 * {@link #toProvider(Provider)}, if any, or null.
		 * @return The provider, that has been configured by invoking
		 * {@link #toProvider(Provider)}, if any, or null.
		 */
		public Provider<? extends O> getTargetProvider() {
			return targetProvider;
		}

		/** Returns the supplier, that has been configured by invoking
		 * {@link #toSupplier(Supplier)}, if any, or null.
		 * @return The supplier, that has been configured by invoking
		 * {@link #toSupplier(Supplier)}, if any, or null.
		 */
		public Supplier<? extends O> getTargetSupplier() {
			return targetSupplier;
		}

		/** Returns the constructor, that has been configured by invoking
		 * {@link #toConstructor(Constructor)}, if any, or null.
		 * @return The constructor, that has been configured by invoking
		 * {@link #toConstructor(Constructor)}, if any, or null.
		 */
		public Constructor<? extends O> getTargetConstructor() {
			return targetConstructor;
		}

		/** Returns the instance, that has been configured by invoking
		 * {@link #toInstance(Object)}, if any, or null.
		 * @return The instance, that has been configured by invoking
		 * {@link #toConstructor(Constructor)}, if any, or null.
		 */
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

	/**
	 * Returns the component factory, that has been created.
	 * @return The component factory, that has been created.
	 * @throws IllegalStateException The method {@link #build()} has
	 *   not yet been invoked, and no such instance is available (yet).
	 */
	public @Nonnull IComponentFactory getInstance() {
		if (!immutable   ||  instance == null) {
			throw new IllegalStateException("This object is not yet mutable.");
		}
		return instance;
	}

	/**
	 * Creates, and returns a component factory, by applying the collected
	 * binding builders.
	 * @return The component factory, that has been created.
	 */
	public @Nonnull IComponentFactory build() {
		if (instance == null) {
			immutable = true;
			final IComponentFactory componentFctory = newInstance();
			final Set<Class<?>> staticInjectionClasses = new HashSet<>();
			final List<Consumer<IComponentFactory>> finalizers = new ArrayList<>();
			final List<BindingBuilder<?>> builders = createBindingBuilders(componentFctory, finalizers, staticInjectionClasses);
			createBindings(componentFctory, builders, staticInjectionClasses);
			instance = componentFctory;
			finalizers.forEach((fin) -> fin.accept(componentFctory));
		}
		return instance;
	}

	protected List<BindingBuilder<?>> createBindingBuilders(IComponentFactory pCf, List<Consumer<IComponentFactory>> pFinalizers, Set<Class<?>> pStaticInjectionClasses) {
		final Map<Key<?>,BindingBuilder<?>> builders = new HashMap<>();
		final Binder binder = new Binder() {
			@Override
			public <O> LinkedBindingBuilder<O> bind(Key<O> pKey) {
				final BindingBuilder<O> builder = new BindingBuilder<O>(pKey);
				final BindingBuilder<?> b = builder;
				add(b);
				return builder;
			}

			protected void add(BindingBuilder<?> pBuilder) {
				final Key<?> key = pBuilder.getKey();
				builders.put(key, pBuilder);
			}

			@Override
			public <O> AnnotatedBindingBuilder<O> bind(Type<O> pType) {
				final java.lang.reflect.Type type = pType.getRawType();
				final Key<O> key = new Key<O>(type);
				final BindingBuilder<O> builder = new BindingBuilder<O>(key);
				final BindingBuilder<?> b = builder;
				add(b);
				return builder;
			}

			@Override
			public <LT> LinkedBindingBuilder<LT> bind(Type<LT> pType, String pName) {
				final java.lang.reflect.Type type = Objects.requireNonNull(pType, "Type").getRawType();
				final Named named = Names.named(Objects.requireNonNull(pName, "Name"));
				final Key<LT> key = new Key<LT>(type, named);
				final BindingBuilder<LT> builder = new BindingBuilder<LT>(key);
				final BindingBuilder<?> b = builder;
				add(b);
				return builder;
			}

			@Override
			public <AT> AnnotatedBindingBuilder<AT> bind(Class<AT> pType) {
				final Key<AT> key = new Key<AT>(pType);
				final BindingBuilder<AT> builder = new BindingBuilder<AT>(key);
				final BindingBuilder<?> b = builder;
				add(b);
				return builder;
			}

			@Override
			public <LT> LinkedBindingBuilder<LT> bind(Class<LT> pType, String pName) {
				final java.lang.reflect.Type type = Objects.requireNonNull(pType, "Type");
				final Named named = Names.named(Objects.requireNonNull(pName, "Name"));
				final Key<LT> key = new Key<LT>(type, named);
				final BindingBuilder<LT> builder = new BindingBuilder<LT>(key);
				final BindingBuilder<?> b = builder;
				add(b);
				return builder;
			}

			@Override
			public void requestStaticInjection(@Nonnull Class<?>... pTypes) {
				for (Class<?> type : Objects.requireAllNonNull(pTypes, "Type")) {
					pStaticInjectionClasses.add(type);
				}
			}

			@Override
			public void addFinalizer(Consumer<IComponentFactory> pFinalizer) {
				pFinalizers.add(pFinalizer);
			}
		};
		binder.bind(IComponentFactory.class).toInstance(pCf);
		for (Module module : getModules()) {
			module.configure(binder);
		}
		return new ArrayList<>(builders.values());
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
							throw new IllegalStateException("Neither of the methods to(Class), to(Key), toInstance(), toProvider(), or toConstructor() have been invoked on the BindingBuilder: " + pBb.key);
						} else {
							return () -> {
								return supplier.get();
							};
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
