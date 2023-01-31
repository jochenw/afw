package com.github.jochenw.afw.di.api;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.Objects;
import java.util.function.Supplier;

import javax.inject.Provider;

import com.github.jochenw.afw.di.impl.DefaultLifecycleController;
import com.github.jochenw.afw.di.util.Exceptions;

/** Base class for deriving application singleton objects, that main control
 * over the application in general.
 * <em>Note:</em> This class is supposed to be thread safe.
 */
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

	/** Returns the applications component factory.
	 * @return The applications component factory.
	 */
	public synchronized IComponentFactory getComponentFactory() {
		if (componentFactory == null) {
			componentFactory = newComponentFactory();
		}
		return componentFactory;
	}

	protected IOnTheFlyBinder getOnTheFlyBinder() {
		return null;
	}
	protected IComponentFactory newComponentFactory() {
		if (componentFactoryProvider == null) {
			final Module mInner = Objects.requireNonNull(moduleSupplier.get(),
					"Module supplier returned a null object.");
			final Module mOuter = (b) -> {
				if (mInner != null) {
					mInner.configure(b);
				}
				b.bind(ILifecycleController.class).to(DefaultLifecycleController.class).in(Scopes.SINGLETON);
				b.bind(Application.class).toInstance(Application.this);
				b.addFinalizer((cf) -> {
					cf.requireInstance(ILifecycleController.class).start();
				});
			};
			final ComponentFactoryBuilder componentFactoryBuilder = new ComponentFactoryBuilder();
			IOnTheFlyBinder iotfb = getOnTheFlyBinder();
			if (iotfb != null) {
				componentFactoryBuilder.onTheFlyBinder(iotfb);
			}
			return componentFactoryBuilder.module(mOuter).build();
		} else {
			return componentFactoryProvider.get();
		}
	}

	/** Creates a new instance with the given component factory.
	 * @param pComponentFactory The component factory, that the created
	 *   application should use.
	 * @return The created instance.
	 */
	public static Application of(IComponentFactory pComponentFactory) {
		final Provider<IComponentFactory> provider = () -> pComponentFactory;
		return of(provider);
	}

	/** Creates a new instance with the given component factory, that's
	 * returned by the given provider.
	 * @param pComponentFactoryProvider Provider of the component factory,
	 *   that the created application should use.
	 * @return The created instance.
	 */
	public static Application of(Provider<IComponentFactory> pComponentFactoryProvider) {
		final Provider<IComponentFactory> provider = Objects.requireNonNull(pComponentFactoryProvider, "Provider");
		return new Application(provider);
	}

	/** Creates a new instance with the given component factory, that's
	 * returned by the given provider.
	 * @param pType Type of the created instance, a subclass of {@link Application}.
	 * @param pComponentFactoryProvider Provider of the component factory,
	 *   that the created application should use.
	 * @param <App> Type of the created instance, a subclass of {@link Application}.
	 * @return The created instance.
	 */
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

	/** Creates a new instance with a component factory, that's
	 * created using the given module.
	 * @param pModule A module, tha will be used to create the aplications
	 *   component factory.
	 * @return The created instance.
	 */
	public static Application of(Module pModule) {
		final Module m = Objects.requireNonNull(pModule, "Module");
		return new Application(() -> m);
	}

	/** Creates a new instance of the given type, with a component factory,
	 * that's created using the given module.
	 * @param pType Type of the created instance, a subclass of {@link Application}.
	 * @param pModule A module, that will be used to create the aplications
	 *   component factory.
	 * @return The created instance.
	 */
	public static <App extends Application> App of(Class<App> pType, Module pModule) {
		final Module m = Objects.requireNonNull(pModule, "Module");
		final Supplier<Module> supplier = () -> m;
		return of(pType, supplier);
	}

	/** Creates a new instance of the given type, with a component factory,
	 * that's created using a module, thats returned by the given supplier.
	 * @param pType Type of the created instance, a subclass of {@link Application}.
	 * @param pSupplier A supplier for the module, that will be used to create the aplications
	 *   component factory.
	 * @return The created instance.
	 */
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
