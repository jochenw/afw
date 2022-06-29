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

public class ComponentFactoryBuilder {
	private @Nonnull List<Module> modules = new ArrayList<>();
	private @Nonnull Supplier<AbstractComponentFactory> supplier = newSupplier(SimpleComponentFactory.class);
	private @Nullable IComponentFactory instance;
	private IOnTheFlyBinder onTheFlyBinder = new DefaultOnTheFlyBinder();

	public List<Module> getModules() {
		return modules;
	}

	public ComponentFactoryBuilder module(@Nonnull Module pModule) {
		modules.add(pModule);
		return this;
	}

	public ComponentFactoryBuilder modules(@Nullable Module... pModules) {
		if (pModules != null) {
			for (Module m : pModules) {
				module(m);
			}
		}
		return this;
	}

	public ComponentFactoryBuilder modules(@Nullable Iterable<Module> pModules) {
		if (pModules != null) {
			for (Module m : pModules) {
				module(m);
			}
		}
		return this;
	}

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
				final BindingBuilder<Object> bb = new BindingBuilder<Object>(key);
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
						&&  currentBb.getTargetProvider() == null
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
				@SuppressWarnings("unchecked")
				final Key<Object> key = (Key<Object>) new Key<T>(reflectType);
				return register(key);
			}

			@Override
			public <T> LinkableBindingBuilder<T> bind(Type<T> pType, String pName) {
				return bind(pType).named(pName);
			}

			@Override
			public <T> AnnotatableBindingBuilder<T> bind(Class<T> pType) {
				final java.lang.reflect.Type reflectType = pType;
				@SuppressWarnings("unchecked")
				final Key<Object> key = (Key<Object>) new Key<T>(reflectType);
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
		pComponentFactory.configure(onTheFlyBinder, builders, staticInjectionClasses);
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

	public ComponentFactoryBuilder type(Class<? extends AbstractComponentFactory> pType) {
		supplier = newSupplier(pType);
		return this;
	}

	public ComponentFactoryBuilder type(Supplier<AbstractComponentFactory> pSupplier) {
		supplier = pSupplier;
		return this;
	}
}
