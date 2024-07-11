package com.github.jochenw.afw.di.api;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.Objects;
import java.util.function.Supplier;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.di.impl.DefaultLifecycleController;
import com.github.jochenw.afw.di.util.Exceptions;

/** Base class for deriving application singleton objects, that main control
 * over the application in general.
 * <em>Note:</em> This class is supposed to be thread safe.
 */
public class Application {
	/** Interface of an object, that provides the applications component factory.
	 */
	public interface ComponentFactorySupplier extends Supplier<IComponentFactory> {}
	private final String annotationType;
	private final IOnTheFlyBinder onTheFlyBinder;
	private final Supplier<@NonNull Module> moduleSupplier;
	private final Supplier<IComponentFactory> componentFactoryProvider;
	private IComponentFactory componentFactory;

	/** Creates a new instance. The instances component factory is
	 * configured by a module, that is returned by an invocation of
	 * the given supplier.
	 * @param pModuleSupplier The modules supplier.
	 * @param pAnnotationType The (optional) annotation style, either of "javax" (default), "jakarta", or "guice".
	 * @param pOnTheFlyBinder The (optional) provider of dynamic bindings.
	 */
	protected Application(Supplier<@NonNull Module> pModuleSupplier, String pAnnotationType, IOnTheFlyBinder pOnTheFlyBinder) {
		moduleSupplier = Objects.requireNonNull(pModuleSupplier, "Supplier");
		componentFactoryProvider = null;
		annotationType = pAnnotationType;
		onTheFlyBinder = pOnTheFlyBinder;
	}

	/** Creates a new instance. The instances component factory is
	 * created by the given component factory provider.
	 * @param pComponentFactoryProvider The component factories provider.
	 * @param pAnnotationType The (optional) annotation style, either of "javax" (default), "jakarta", or "guice".
	 * @param pOnTheFlyBinder The (optional) provider of dynamic bindings.
	 */
	protected Application(ComponentFactorySupplier pComponentFactoryProvider, String pAnnotationType, IOnTheFlyBinder pOnTheFlyBinder) {
		componentFactoryProvider = Objects.requireNonNull(pComponentFactoryProvider, "Provider");
		moduleSupplier = null;
		annotationType = pAnnotationType;
		onTheFlyBinder = pOnTheFlyBinder;
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

	/** Returns the applications {@link IOnTheFlyBinder}.
	 * @return The applications {@link IOnTheFlyBinder}.
	 */
	public IOnTheFlyBinder getOnTheFlyBinder() {
		return onTheFlyBinder;
	}

	/** Returns the applications annotation type, either of null, "javax", "jakarta", or "guice".
	 * Null indicates, that "javax" is being used as the default.
	 * @return The applications annotation type, either of null, "javax", "jakarta", or "guice".
	 */
	public String getAnnotationType() {
		return annotationType;
	}
	
	
	/** Called internally to create the applications component
	 * factory. If the application has been created by using the
	 * {@link Application#Application(ComponentFactorySupplier, String, IOnTheFlyBinder)
	 * component factory supplier constructor}, then that
	 * supplier will be invoked. Otherwise, the
	 * {@link #Application(Supplier, String, IOnTheFlyBinder) module supplier} will
	 * be invoked, to create the component factory.
	 * @return The created component factory.
	 */
	protected IComponentFactory newComponentFactory() {
		if (componentFactoryProvider == null) {
			final @NonNull Module mInner = Objects.requireNonNull(moduleSupplier.get(),
					"Module supplier returned a null object.");
			final Module mOuter = (b) -> {
				mInner.configure(b);
				b.bind(ILifecycleController.class).to(DefaultLifecycleController.class).in(Scopes.SINGLETON);
				b.bind(Application.class).toInstance(Application.this);
				b.addFinalizer((cf) -> {
					cf.requireInstance(ILifecycleController.class).start();
				});
			};
			final ComponentFactoryBuilder componentFactoryBuilder = new ComponentFactoryBuilder();
			IOnTheFlyBinder iotfb = onTheFlyBinder;
			if (iotfb != null) {
				componentFactoryBuilder.onTheFlyBinder(iotfb);
			}
			if (annotationType == null  ||  "javax".equals(annotationType)) {
				componentFactoryBuilder.javax();
			} else if ("jakarta".equals(annotationType)) {
				componentFactoryBuilder.jakarta();
			} else if ("guice".equals(annotationType)) {
				componentFactoryBuilder.guice();
			} else {
				throw new IllegalArgumentException("Invalid annotation style: Expected guice|javax|jakarta, got " + annotationType);
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
		final ComponentFactorySupplier provider = () -> pComponentFactory;
		return of(provider);
	}

	/** Creates a new instance with the given component factory, that's
	 * returned by the given provider.
	 * @param pComponentFactoryProvider Provider of the component factory,
	 *   that the created application should use.
	 * @return The created instance.
	 */
	public static Application of(ComponentFactorySupplier pComponentFactoryProvider) {
		final ComponentFactorySupplier provider = Objects.requireNonNull(pComponentFactoryProvider, "Provider");
		return new Application(provider, null, null);
	}

	/** Creates a new instance with the given component factory, that's
	 * returned by the given provider.
	 * @param pType Type of the created instance, a subclass of {@link Application}.
	 * @param pComponentFactoryProvider Provider of the component factory,
	 *   that the created application should use.
	 * @param <App> Type of the created instance, a subclass of {@link Application}.
	 * @return The created instance.
	 */
	public static <App extends Application> App of(Class<App> pType, ComponentFactorySupplier pComponentFactoryProvider) {
		final ComponentFactorySupplier provider = Objects.requireNonNull(pComponentFactoryProvider, "Provider");
		final MethodHandles.Lookup lookup = MethodHandles.lookup();
		Constructor<App> constructor1 = null;
		Constructor<App> constructor2 = null;
		try {
			constructor1 = (Constructor<App>) pType.getDeclaredConstructor(ComponentFactorySupplier.class);
		} catch (NoSuchMethodException e1) {
			try {
				constructor2 = (Constructor<App>) pType.getDeclaredConstructor(ComponentFactorySupplier.class,
						                                                       String.class, IOnTheFlyBinder.class);
			} catch (NoSuchMethodException e2) {
				throw new IllegalStateException("The application class " + pType.getName()
				    + " has neither of the following constructors: "
				    + pType.getSimpleName() + "(ComponentFactorySupplier), or "
				    + pType.getSimpleName() + "(ComponentFactorySuppier,String,IOnTheFlyBinder)");
			}
		}
		if (constructor1 != null) {
			try {
				final MethodHandle mh = lookup.unreflectConstructor(constructor1);
				return (App) mh.invoke(provider);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		} else {
			try {
				final MethodHandle mh = lookup.unreflectConstructor(constructor2);
				return (App) mh.invoke(provider, null, null);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
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
		return new Application(() -> m, null, null);
	}

	/** Creates a new instance with a component factory, that's
	 * created using the given module.
	 * @param pModule A module, tha will be used to create the aplications
	 *   component factory.
	 * @param pAnnotationType The (optional) annotation style, either of "javax" (default), "jakarta", or "guice".
	 * @param pOnTheFlyBinder The (optional) provider of dynamic bindings.
	 * @return The created instance.
	 */
	public static Application of(Module pModule, String pAnnotationType, IOnTheFlyBinder pOnTheFlyBinder) {
		final Module m = Objects.requireNonNull(pModule, "Module");
		return new Application(() -> m, pAnnotationType, pOnTheFlyBinder);
	}

	/** Creates a new instance of the given type, with a component factory,
	 * that's created using the given module.
	 * @param <App> Type of the created instance, a subclass of {@link Application}.
	 * @param pType Type of the created instance, a subclass of {@link Application}.
	 * @param pModule A module, that will be used to create the aplications
	 *   component factory.
	 * @return The created instance.
	 */
	public static <App extends Application> App of(Class<App> pType, Module pModule) {
		final Module m = Objects.requireNonNull(pModule, "Module");
		final Supplier<Module> supplier = () -> m;
		return of(pType, supplier, null, null);
	}

	/** Creates a new instance of the given type, with a component factory,
	 * that's created using the given module.
	 * @param <App> Type of the created instance, a subclass of {@link Application}.
	 * @param pType Type of the created instance, a subclass of {@link Application}.
	 * @param pModule A module, that will be used to create the aplications
	 *   component factory.
	 * @param pAnnotationType The (optional) annotation style, either of "javax" (default), "jakarta", or "guice".
	 * @param pOnTheFlyBinder The (optional) provider of dynamic bindings.
	 * @return The created instance.
	 */
	public static <App extends Application> App of(Class<App> pType, Module pModule, String pAnnotationType, IOnTheFlyBinder pOnTheFlyBinder) {
		final Module m = Objects.requireNonNull(pModule, "Module");
		final Supplier<Module> supplier = () -> m;
		return of(pType, supplier, pAnnotationType, pOnTheFlyBinder);
	}

	/** Creates a new instance of the given type, with a component factory,
	 * that's created using a module, thats returned by the given supplier.
	 * @param <App> Type of the created instance, a subclass of {@link Application}.
	 * @param pType Type of the created instance, a subclass of {@link Application}.
	 * @param pSupplier A supplier for the module, that will be used to create the aplications
	 *   component factory.
	 * @param pAnnotationType The (optional) annotation style, either of "javax" (default), "jakarta", or "guice".
	 * @param pOnTheFlyBinder The (optional) provider of dynamic bindings.
	 * @return The created instance.
	 */
	public static <App extends Application> App of(Class<App> pType, Supplier<Module> pSupplier, String pAnnotationType,
			                                       IOnTheFlyBinder pOnTheFlyBinder) {
		final Supplier<Module> supplier = Objects.requireNonNull(pSupplier, "Supplier");
		final MethodHandles.Lookup lookup = MethodHandles.lookup();
		try {
			final Constructor<App> constructor = (Constructor<App>) pType.getDeclaredConstructor(Supplier.class, String.class, IOnTheFlyBinder.class);
			final MethodHandle mh = lookup.unreflectConstructor(constructor);
			return (App) mh.invoke(supplier, pAnnotationType, pOnTheFlyBinder);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}
}
