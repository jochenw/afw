package com.github.jochenw.afw.di.api;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Objects;
import java.util.function.Supplier;

import javax.inject.Provider;

import org.checkerframework.checker.units.qual.A;

import com.github.jochenw.afw.di.util.Exceptions;

public class Application {
	private final Supplier<Module> moduleSupplier;
	private final Provider<IComponentFactory> componentFactoryProvider;
	private IComponentFactory componentFactory;

	protected Application(Supplier<Module> pModuleSupplier) {
		moduleSupplier = Objects.requireNonNull(pModuleSupplier, "Supplier");
		componentFactoryProvider = null;
	}

	protected Application(Provider<IComponentFactory> pComponentFctoryProvider) {
		componentFactoryProvider = Objects.requireNonNull(pComponentFctoryProvider, "Provider");
		moduleSupplier = null;
	}

	public synchronized IComponentFactory getComponentFactory() {
		if (componentFactory == null) {
			componentFactory = newComponentFactory();
		}
		return componentFactory;
	}

	protected IComponentFactory newComponentFactory() {
		if (componentFactoryProvider == null) {
			final Module mInner = Objects.requireNonNull(moduleSupplier.get(),
					"Module supplier returned a null object.");
			final Module mOuter = (b) -> {
				if (mInner != null) {
					mInner.configure(b);
				}
				b.bind(Application.class).toInstance(Application.this);
			};
			return new ComponentFactoryBuilder().module(mOuter).build();
		} else {
			return componentFactoryProvider.get();
		}
	}

	public static Application of(IComponentFactory pComponentFactory) {
		final Provider<IComponentFactory> provider = () -> pComponentFactory;
		return of(provider);
	}

	public static Application of(Provider<IComponentFactory> pComponentFactoryProvider) {
		final Provider<IComponentFactory> provider = Objects.requireNonNull(pComponentFactoryProvider, "Provider");
		return new Application(provider);
	}

	public static <App extends Application> App of(Class<App> pType, Provider<IComponentFactory> pComponentFactoryProvider) {
		final Provider<IComponentFactory> provider = Objects.requireNonNull(pComponentFactoryProvider, "Provider");
		final MethodHandles.Lookup lookup = MethodHandles.lookup();
		try {
			final Constructor<App> constructor = (Constructor<App>) pType.getDeclaredConstructor(Provider.class);
			final MethodHandle mh = lookup.unreflectConstructor(constructor);
			return (App) mh.invoke(provider);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}


	public static Application of(Module pModule) {
		final Module m = Objects.requireNonNull(pModule, "Module");
		return new Application(() -> m);
	}

	public static <App extends Application> App of(Class<App> pType, Module pModule) {
		final Module m = Objects.requireNonNull(pModule, "Module");
		final Supplier<Module> supplier = () -> m;
		return of(pType, supplier);
	}

	public static <App extends Application> App of (Class<App> pType, Supplier<Module> pSupplier) {
		final Supplier<Module> supplier = Objects.requireNonNull(pSupplier, "Supplier");
		final MethodHandles.Lookup lookup = MethodHandles.lookup();
		try {
			final Constructor<App> constructor = (Constructor<App>) pType.getDeclaredConstructor(Supplier.class);
			final MethodHandle mh = lookup.unreflectConstructor(constructor);
			return (App) mh.invoke(supplier);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}
}