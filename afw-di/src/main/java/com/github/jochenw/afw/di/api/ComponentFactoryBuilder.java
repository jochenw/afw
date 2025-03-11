package com.github.jochenw.afw.di.api;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
	/** Creates a new instance.
	 * This constructor might be removed, it is mainly present to avoid a Javadoc
	 * warning with JDK 21.
	 */
	public ComponentFactoryBuilder() {}

	private @NonNull List<Module> modules = new ArrayList<>();
	private @NonNull Supplier<@NonNull AbstractComponentFactory> supplier = newSupplier(SimpleComponentFactory.class);
	private @Nullable IComponentFactory instance;
	private @NonNull IAnnotationProvider annotationProvider = Annotations.getDefaultProvider();
	private @NonNull IOnTheFlyBinder onTheFlyBinder = new DefaultOnTheFlyBinder();

	/** Returns the {@link IAnnotationProvider}, which is being used by
	 * this builder, and the component factory, that it creates.
	 * @return This builders {@link IAnnotationProvider}.
	 */
	public @NonNull IAnnotationProvider getAnnotations() {
		return annotationProvider;
	}

	/** Sets the {@link IAnnotationProvider}, which is being used by
	 * this builder, and the component factory, that it creates.
	 * @param pAnnotations This builders {@link IAnnotationProvider}.
	 * @return This builder.
	 */
	public ComponentFactoryBuilder annotations(@NonNull IAnnotationProvider pAnnotations) {
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
	public ComponentFactoryBuilder module(@NonNull Module pModule) {
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
				if (m != null) {
					module(m);
				}
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
				if (m != null) {
					module(m);
				}
			}
		}
		return this;
	}

	/** Upon the first invocation, creates a new component factory, and configures
	 * it by applying the registered modules. For all following invocations, returns
	 * the same instance.
	 * @return The created, anf configured component factory, ready to use.
	 */
	public @NonNull IComponentFactory build() {
		if (instance == null) {
			final @NonNull AbstractComponentFactory inst = Objects.requireNonNull(supplier.get(),
					"The instance supplier returned null.");
			configure(inst);
			instance = inst;
			return inst;
		} else {
			return Objects.requireNonNull((IComponentFactory) instance);
		}
	}

	private interface FinalizableBinder extends Binder {
		void finished();
	}

	/** Called to configure the given instance by applying the bindings,
	 * that have been created by the {@link #getModules() modules}.
	 * @param pComponentFactory The configured instance, which is now
	 *   ready to use.
	 */
	protected void configure(@NonNull AbstractComponentFactory pComponentFactory) {
		final List<BindingBuilder<Object>> builders = new ArrayList<>();
		final Set<Class<?>> staticInjectionClasses = new HashSet<>();
		final List<@NonNull Consumer<@NonNull IComponentFactory>> finalizers = new ArrayList<>();
		final FinalizableBinder binder = new FinalizableBinder() {
			private BindingBuilder<Object> currentBb;

			@Override
			public <T> LinkableBindingBuilder<T> bind(@NonNull Key<T> pKey) {
				@SuppressWarnings("unchecked")
				final Key<Object> key = (Key<Object>) pKey;
				return register(key);
			}

			protected <T> BindingBuilder<T> register(final @NonNull Key<Object> key) {
				final BindingBuilder<Object> bb = new BindingBuilder<Object>(key) {
					@Override
					public LinkableBindingBuilder<Object> named(@NonNull String pValue) {
						return annotatedWith(getAnnotations().newNamed(pValue));
					}

					@Override
					public ScopableBindingBuilder toFunction(Function<IComponentFactory,Object> pFunction) {
						final Supplier<Object> supplier = () -> pFunction.apply(pComponentFactory);
						return toSupplier(supplier);
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
									+ " for key " + currentBb.getKey() + ", and self-binding is not possible"
									+ " for annotations, interfaces, enumerations, and primitives.");
						}
					}
				}
			}

			@Override
			public <T> AnnotatableBindingBuilder<T> bind(@NonNull Type<T> pType) {
				final java.lang.reflect.Type reflectType = pType.getRawType();
				final @NonNull Key<Object> key = Key.of(reflectType);
				return register(key);
			}

			@Override
			public <T> LinkableBindingBuilder<T> bind(@NonNull Type<T> pType, @NonNull String pName) {
				return bind(pType).named(pName);
			}

			@Override
			public <T> AnnotatableBindingBuilder<T> bind(@NonNull Class<T> pType) {
				final java.lang.reflect.Type reflectType = pType;
				final Key<Object> key = Key.of(reflectType);
				return register(key);
			}

			@Override
			public <T> LinkableBindingBuilder<T> bind(@NonNull Class<T> pType, @NonNull String pName) {
				return bind(pType).named(pName);
			}

			@Override
			public void requestStaticInjection(@NonNull Class<?>... pTypes) {
				if (pTypes != null) {
					for (Class<?> cl : pTypes) {
						staticInjectionClasses.add(cl);
					}
				}
			}

			@Override
			public void addFinalizer(@NonNull Consumer<@NonNull IComponentFactory> pFinalizer) {
				final @NonNull Consumer<@NonNull IComponentFactory> finalizer = Objects.requireNonNull(pFinalizer, "Finalizer");
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
		for (@NonNull Consumer<@NonNull IComponentFactory> finalizer : finalizers) {
			finalizer.accept(pComponentFactory);
		}
	}

	/** Creates a supplier for the component factory instance.
	 * @param pType The component factories type.
	 * @return The created supplier.
	 */
	protected @NonNull Supplier<@NonNull AbstractComponentFactory> newSupplier(Class<? extends AbstractComponentFactory> pType) {
		return () -> {
			try {
				@SuppressWarnings("unchecked")
				final Constructor<AbstractComponentFactory> constructor = (Constructor<AbstractComponentFactory>) pType.getConstructor();
				@SuppressWarnings("null")
				final @NonNull AbstractComponentFactory acf = constructor.newInstance();
				return acf;
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
	public ComponentFactoryBuilder type(@NonNull Supplier<@NonNull AbstractComponentFactory> pSupplier) {
		supplier = pSupplier;
		return this;
	}

	/** Configures the {@link IOnTheFlyBinder}, that is being used.
	 * @param pBinder The {@link IOnTheFlyBinder}, that is being used.
	 * @return This builder.
	 */
	public @NonNull ComponentFactoryBuilder onTheFlyBinder(@NonNull IOnTheFlyBinder pBinder) {
		onTheFlyBinder = Objects.requireNonNull(pBinder);
		return this;
	}

	/** Returns the {@link IOnTheFlyBinder}, that is being used.
	 * @return The {@link IOnTheFlyBinder}, that is being used
	 */
	public @NonNull IOnTheFlyBinder getOnTheFlyBinder() {
		return onTheFlyBinder;
	}
}
