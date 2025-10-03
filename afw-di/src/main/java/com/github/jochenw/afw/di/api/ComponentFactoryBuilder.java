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
	private Consumer<String> logger;

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

	/** Registers a logger for this builder.
	 * @param pLogger The builders logger. If the builder already has a logger, then a
	 *   new logger is being created, which invokes the old logger first, and then the
	 *   given.
	 *
	 *   This value may be null, in which case an existing logger will be discarded.
	 * @return This builder.
	 */
	public ComponentFactoryBuilder logger(Consumer<String> pLogger) {
		if (pLogger == null) {
			logger = null;
		} else {
			@SuppressWarnings("null")
			final @NonNull Consumer<String> log = Objects.requireNonNull(pLogger, "Logger");
			if (logger == null) {
				logger = log;
			} else {
				@SuppressWarnings("null")
				final @NonNull Consumer<String> oldLogger = Objects.requireNonNull(logger, "Old Logger");
				logger = (s) -> {
					oldLogger.accept(s);
					log.accept(s);
				};
			}
		}
		return this;
	}

	/** Returns the current logger, if any, or null.
	 * @return The current logger, if any, or null.
	 */
	public Consumer<String> getLogger() { return logger; }

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
	 * modules in the given set, the order is determined by the iterables
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

	/** Logs a message.
	 * @param pMsg The message, which is being logged.
	 */
	protected void log(String pMsg) {
		if (pMsg != null  &&  logger != null) {
			logger.accept(pMsg);
		}
	}

	/** Upon the first invocation, creates a new component factory, and configures
	 * it by applying the registered modules. For all following invocations, returns
	 * the same instance.
	 * @return The created, and configured component factory, ready to use.
	 */
	public @NonNull IComponentFactory build() {
		final IComponentFactory inst = instance;
		if (inst == null) {
			log("build: Creating a new instance of IComponentFactory.");
			@SuppressWarnings("null")
			final @NonNull AbstractComponentFactory ins = Objects.requireNonNull(supplier.get(),
					"The instance supplier returned null.");
			log("build: Created a new instance of " + ins.getClass().getName() + " as an implementation of IComponentFactory.");
			configure(ins);
			instance = ins;
			return ins;
		} else {
			return inst;
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
		log("configure: Creating a new Binder.");
		final FinalizableBinder binder = new FinalizableBinder() {
			private BindingBuilder<Object> currentBb;

			@Override
			public <T> LinkableBindingBuilder<T> bind(@NonNull Key<T> pKey) {
				log("configure.bind: Key=" + pKey);
				@SuppressWarnings("unchecked")
				final Key<Object> key = (Key<Object>) pKey;
				return register(key);
			}

			protected <T> BindingBuilder<T> register(final @NonNull Key<Object> pKey) {
				log("configure.register: Key=" + pKey);
				final BindingBuilder<Object> bb = new BindingBuilder<Object>(pKey) {
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
				@SuppressWarnings("null")
				final @NonNull Consumer<@NonNull IComponentFactory> finalizer = Objects.requireNonNull(pFinalizer, "Finalizer");
				finalizers.add(finalizer);
			}
		};
		for (Module module : getModules()) {
			log("configure: Invoking module " + module);
			module.configure(binder);
		}
		final IComponentFactory icf = pComponentFactory;
		binder.bind(IComponentFactory.class).toInstance(icf);
		binder.finished();
		log("configure: Bindings created, configuring the IComponentFactory instance");
		pComponentFactory.configure(getAnnotations(), onTheFlyBinder, builders, staticInjectionClasses, logger);
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
		@SuppressWarnings("null")
		final @NonNull IOnTheFlyBinder binder = Objects.requireNonNull(pBinder);
		onTheFlyBinder = binder;
		return this;
	}

	/** Returns the {@link IOnTheFlyBinder}, that is being used.
	 * @return The {@link IOnTheFlyBinder}, that is being used
	 */
	public @NonNull IOnTheFlyBinder getOnTheFlyBinder() {
		return onTheFlyBinder;
	}
}
