package com.github.jochenw.afw.di.api;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.jochenw.afw.di.api.Types.Type;
import com.github.jochenw.afw.di.impl.AbstractComponentFactory;
import com.github.jochenw.afw.di.impl.BindingBuilder;
import com.github.jochenw.afw.di.impl.DefaultOnTheFlyBinder;
import com.github.jochenw.afw.di.impl.simple.SimpleComponentFactory;
import com.github.jochenw.afw.di.util.Exceptions;

/** A builder for component factories. This is the officially recommended way to
 * obtain a component factory. In other words. to obtain a component factory, you
 * are supposed to do something like
 * <pre>
 *   IComponentFactory.builder().module(module).build();
 * </pre>
 */
public class ComponentFactoryBuilder {
	private @Nonnull List<Module> modules = new ArrayList<>();
	private @Nonnull Supplier<AbstractComponentFactory> supplier = newSupplier(SimpleComponentFactory.class);
	private @Nullable IComponentFactory instance;
	private @Nonnull IAnnotationProvider annotationProvider = Annotations.getDefaultProvider();
	private IOnTheFlyBinder onTheFlyBinder = new DefaultOnTheFlyBinder();

	/** Returns the {@link IAnnotationProvider}, which is being used by
	 * this builder, and the component factory, that it creates.
	 * @return This builders {@link IAnnotationProvider}.
	 */
	public IAnnotationProvider getAnnotations() {
		return annotationProvider;
	}

	/** Sets the {@link IAnnotationProvider}, which is being used by
	 * this builder, and the component factory, that it creates.
	 * @param pAnnotations This builders {@link IAnnotationProvider}.
	 * @return This builder.
	 */
	public ComponentFactoryBuilder annotations(IAnnotationProvider pAnnotations) {
		annotationProvider = Objects.requireNonNull(pAnnotations, "Annotations");
		return this;
	}

	/** Sets the {@link IAnnotationProvider}, which is being used by
	 * this builder, and the component factory, that it creates, to
	 * the JavaxAnnotationProvider.
	 * @return This builder.
	 * @see #jakarta()
	 * @see #guice()
	 */
	public ComponentFactoryBuilder javax() {
		final IAnnotationProvider annotations = Annotations.getProvider("javax.inject");
		if (annotations == null) {
			throw new IllegalStateException("Annotation provider javax.inject"
					+ " is not available. Perhaps you meant jakarta.inject,"
					+ " or com.google.inject, or you need to fix your class path.");
		}
		return annotations(annotations);
	}

	/** Sets the {@link IAnnotationProvider}, which is being used by
	 * this builder, and the component factory, that it creates, to
	 * the JakartaAnnotationProvider.
	 * @return This builder.
	 * @see #javax()
	 * @see #guice()
	 */
	public ComponentFactoryBuilder jakarta() {
		final IAnnotationProvider annotations = Annotations.getProvider("jakarta.inject");
		if (annotations == null) {
			throw new IllegalStateException("Annotation provider jakarta.inject"
					+ " is not available. Perhaps you meant javax.inject,"
					+ " or com.google.inject, or you need to fix your class path.");
		}
		return annotations(annotations);
	}
	
	/** Sets the {@link IAnnotationProvider}, which is being used by
	 * this builder, and the component factory, that it creates, to
	 * the GoogleAnnotationProvider.
	 * @return This builder.
	 * @see #javax()
	 * @see #jakarta()
	 */
	public ComponentFactoryBuilder guice() {
		final IAnnotationProvider annotations = Annotations.getProvider("com.google.inject");
		if (annotations == null) {
			throw new IllegalStateException("Annotation provider com.google.inject"
					+ " is not available. Perhaps you meant javax.inject,"
					+ " or jakarta.inject, or you need to fix your class path.");
		}
		return annotations(annotations);
	}
	
	/** Returns the modules, that have been registered for configuration of
	 * the created component factory.
	 * @return The modules, that have been registered for configuration of
	 * the created component factory
	 */
	public List<Module> getModules() {
		return modules;
	}

	/** Registers a new module for configuration of the created component
	 * factory. The modules will be used in the order of registration.
	 * @param pModule A new module for configuration of the created component
	 * factory.
	 * @return This builder.
	 */
	public ComponentFactoryBuilder module(@Nonnull Module pModule) {
		modules.add(pModule);
		return this;
	}

	/** Registers new modules for configuration of the created component
	 * factory. The modules will be used in the order of registration. For the
	 * modules in the given array, the order is determined by the arrays
	 * natural order.
	 * @param pModules A set of new modules for configuration of the created component
	 * factory.
	 * @return This builder.
	 */
	public ComponentFactoryBuilder modules(@Nullable Module... pModules) {
		if (pModules != null) {
			for (Module m : pModules) {
				module(m);
			}
		}
		return this;
	}

	/** Registers new modules for configuration of the created component
	 * factory. The modules will be used in the order of registration. For the
	 * modules in the given set, the order is determined bythe iterables
	 * natural order.
	 * @param pModules A set of new modules for configuration of the created component
	 * factory.
	 * @return This builder.
	 */
	public ComponentFactoryBuilder modules(@Nullable Iterable<Module> pModules) {
		if (pModules != null) {
			for (Module m : pModules) {
				module(m);
			}
		}
		return this;
	}

	/** Upon the first invocation, creates a new component factory, and configures
	 * it by applying the registered modules. For all following invocations, returns
	 * the same instance.
	 * @return The created, anf configured component factory, ready to use.
	 */
	public @Nonnull IComponentFactory build() {
		if (instance == null) {
			final AbstractComponentFactory inst = supplier.get();
			if (inst == null) {
				throw new NullPointerException("Supplier created a null object.");
			}
			configure(inst);
			instance = inst;
			return inst;
		} else {
			return instance;
		}
	}

	private interface FinalizableBinder extends Binder {
		void finished();
	}

	protected void configure(AbstractComponentFactory pComponentFactory) {
		final List<BindingBuilder<Object>> builders = new ArrayList<>();
		final Set<Class<?>> staticInjectionClasses = new HashSet<>();
		final List<Consumer<IComponentFactory>> finalizers = new ArrayList<>();
		final FinalizableBinder binder = new FinalizableBinder() {
			private BindingBuilder<Object> currentBb;

			@Override
			public <T> LinkableBindingBuilder<T> bind(Key<T> pKey) {
				@SuppressWarnings("unchecked")
				final Key<Object> key = (Key<Object>) pKey;
				return register(key);
			}

			protected <T> BindingBuilder<T> register(final Key<Object> key) {
				final BindingBuilder<Object> bb = new BindingBuilder<Object>(key) {
					@Override
					public LinkableBindingBuilder<Object> named(String pValue) {
						return annotatedWith(getAnnotations().newNamed(pValue));
					}
				};
				finished();
				currentBb = bb;
				builders.add(bb);
				@SuppressWarnings("unchecked")
				final BindingBuilder<T> tbb = (BindingBuilder<T>) bb;
				return tbb;
			}

			@Override
			public void finished() {
				if (currentBb != null) {
					if (currentBb.getTargetClass() == null
						&&  currentBb.getTargetConstructor() == null
						&&  currentBb.getTargetInstance() == null
						&&  currentBb.getTargetKey() == null
						&&  currentBb.getTargetSupplier() == null) {
						final java.lang.reflect.Type type = currentBb.getKey().getType();
						final boolean selfBindingPossible;
						if (type instanceof Class) {
							final Class<?> cl = (Class<?>) type;
							if (!cl.isAnnotation()
									&&  !cl.isInterface()
									&&  !cl.isEnum()
									&&  !cl.isPrimitive()) {
								selfBindingPossible = true;
							} else {
								selfBindingPossible = false;
							}
						} else {
							selfBindingPossible = false;
						}
						if (!selfBindingPossible) {
							throw new IllegalStateException("Neither of the methods to(Class), toClass(Class), "
									+ " to(Key), toInstance(Object), toProvider(Provider), toSupplier(Supplier), "
									+ " or toConstructor(Constructor) has been invoked on the previous BindingBuilder"
									+ " for key " + currentBb.getKey() + ", and self-binding is not possible.");
						}
					}
				}
			}

			@Override
			public <T> AnnotatableBindingBuilder<T> bind(Type<T> pType) {
				final java.lang.reflect.Type reflectType = pType.getRawType();
				final Key<Object> key = Key.of(reflectType);
				return register(key);
			}

			@Override
			public <T> LinkableBindingBuilder<T> bind(Type<T> pType, String pName) {
				return bind(pType).named(pName);
			}

			@Override
			public <T> AnnotatableBindingBuilder<T> bind(Class<T> pType) {
				final java.lang.reflect.Type reflectType = pType;
				final Key<Object> key = Key.of(reflectType);
				return register(key);
			}

			@Override
			public <T> LinkableBindingBuilder<T> bind(Class<T> pType, String pName) {
				return bind(pType).named(pName);
			}

			@Override
			public void requestStaticInjection(Class<?>... pTypes) {
				if (pTypes != null) {
					for (Class<?> cl : pTypes) {
						staticInjectionClasses.add(cl);
					}
				}
			}

			@Override
			public void addFinalizer(Consumer<IComponentFactory> pFinalizer) {
				final Consumer<IComponentFactory> finalizer = Objects.requireNonNull(pFinalizer, "Finalizer");
				finalizers.add(finalizer);
			}
		};
		for (Module module : getModules()) {
			module.configure(binder);
		}
		final IComponentFactory icf = pComponentFactory;
		binder.bind(IComponentFactory.class).toInstance(icf);
		binder.finished();
		pComponentFactory.configure(getAnnotations(), onTheFlyBinder, builders, staticInjectionClasses);
		for (Consumer<IComponentFactory> finalizer : finalizers) {
			finalizer.accept(pComponentFactory);
		}
	}

	protected Supplier<AbstractComponentFactory> newSupplier(Class<? extends AbstractComponentFactory> pType) {
		return () -> {
			try {
				@SuppressWarnings("unchecked")
				final Constructor<AbstractComponentFactory> constructor = (Constructor<AbstractComponentFactory>) pType.getConstructor();
				return constructor.newInstance();
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		};
	}

	/**
	 * Configures the created instances type.
	 * @param pType Type of the created instance, a subclass of {@link AbstractComponentFactory}.
	 * @return This builder.
	 */
	public ComponentFactoryBuilder type(Class<? extends AbstractComponentFactory> pType) {
		supplier = newSupplier(pType);
		return this;
	}

	/**
	 * Configures the created instance.
	 * @param pSupplier A supplier, that creates the instance of, that is being
	 *   built.
	 * @return This builder.
	 */
	public ComponentFactoryBuilder type(Supplier<AbstractComponentFactory> pSupplier) {
		supplier = pSupplier;
		return this;
	}

	/** Configures the {@link IOnTheFlyBinder}, that is being used.
	 * @param pBinder The {@link IOnTheFlyBinder}, that is being used.
	 * @return This builder.
	 */
	public ComponentFactoryBuilder onTheFlyBinder(IOnTheFlyBinder pBinder) {
		onTheFlyBinder = Objects.requireNonNull(pBinder);
		return this;
	}

	/** Returns the {@link IOnTheFlyBinder}, that is being used.
	 * @return The {@link IOnTheFlyBinder}, that is being used
	 */
	public IOnTheFlyBinder onTheFlyBinder() {
		return onTheFlyBinder;
	}
}
