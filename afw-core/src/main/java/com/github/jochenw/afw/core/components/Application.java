package com.github.jochenw.afw.core.components;

import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.jochenw.afw.core.ILifecycleController;
import com.github.jochenw.afw.core.impl.DefaultLifecycleController;
import com.github.jochenw.afw.core.inject.IComponentFactory;
import com.github.jochenw.afw.core.inject.ComponentFactoryBuilder;
import com.github.jochenw.afw.core.inject.ComponentFactoryBuilder.Module;
import com.github.jochenw.afw.core.inject.simple.SimpleComponentFactoryBuilder;
import com.github.jochenw.afw.core.log.ILog.Level;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.log.simple.SimpleLogFactory;
import com.github.jochenw.afw.core.props.DefaultPropertyFactory;
import com.github.jochenw.afw.core.props.IPropertyFactory;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Functions.FailableSupplier;


/** An abstract base class for deriving application singletons.
 */
public class Application {
	private final @Nullable Module module;
	private final @Nonnull FailableSupplier<ILogFactory,?> logFactorySupplier;
	private final @Nonnull FailableSupplier<IPropertyFactory,?> propertyFactorySupplier;
	private IComponentFactory componentFactory;
	private ILogFactory logFactory;
	private IPropertyFactory propertyFactory;

	/**
	 * Creates a new instance with the given secondary module, the given
	 * log factory supplier, and the given property factory supplier.
	 * @param pModule The secondary module, providing bindings that extend the
	 * {@link #newModule()} primary module.
	 * @param pLogFactorySupplier The log factory supplier.
	 * @param pPropertyFactorySupplier The property factory supplier.
	 */
	public Application(@Nullable Module pModule, @Nonnull FailableSupplier<ILogFactory,?> pLogFactorySupplier,
			           @Nonnull FailableSupplier<IPropertyFactory,?> pPropertyFactorySupplier) {
		module = pModule;
		logFactorySupplier = Objects.requireNonNull(pLogFactorySupplier, "Log factory supplier");
		propertyFactorySupplier = Objects.requireNonNull(pPropertyFactorySupplier, "Property factoty supplier");
	}

	/** Returns the applications {@link IComponentFactory component factory}.
	 * @return The applications {@link IComponentFactory component factory}.
	 */
	public synchronized @Nonnull IComponentFactory getComponentFactory() {
		IComponentFactory cf = componentFactory;
		if (cf == null) {
			cf = newComponentFactory();
			componentFactory = cf;
		}
		return cf;
	}

	protected @Nonnull IComponentFactory newComponentFactory() {
		final ComponentFactoryBuilder<?> cfb = newComponentFactoryBuilder();
		cfb.module(newModule());
		if (module != null) {
			cfb.module(module);
		}
		return cfb.build();
	}

	protected ComponentFactoryBuilder<?> newComponentFactoryBuilder() {
		return new SimpleComponentFactoryBuilder();
	}

	/**
	 * Creates the applications primary module. The primary module binds the following
	 * singletons: {@link ILifecycleController}, {@link IComponentFactory}, {@link ILogFactory},
	 * and {@link IPropertyFactory}.
	 * @return The applications primary module.
	 */
	protected Module newModule() {
		final ILifecycleController lc = new DefaultLifecycleController();
		final ILogFactory lf = getLogFactory();
		final IPropertyFactory pf = getPropertyFactory();
		return (b) -> {
			b.bind(ILifecycleController.class).toInstance(lc);
			b.bind(ILogFactory.class).toInstance(lf);
			b.bind(IPropertyFactory.class).toInstance(pf);
			b.bind(Application.class).toInstance(this);
		};
	}

	/** Returns the applications {@link ILogFactory log factory}.
	 * @return The applications {@link ILogFactory log factory}.
	 */
	public synchronized @Nonnull ILogFactory getLogFactory() {
		if (logFactory == null) {
			try {
				logFactory = Objects.requireNonNull(logFactorySupplier.get(), "ILogFactory");
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		}
		return logFactory;
	}

	/** Returns the applications {@link IPropertyFactory property factory}.
	 * @return The applications {@link IPropertyFactory property factory}.
	 */
	public synchronized @Nonnull IPropertyFactory getPropertyFactory() {
		if (propertyFactory == null) {
			try {
				propertyFactory = Objects.requireNonNull(propertyFactorySupplier.get(), "IPropertyFactory");
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		}
		return propertyFactory;
	}

	/** Creates a new {@link Application} with the given module, logger factory,
	 * and property factory.
	 * @param pModule A module for creating the component factory.
	 * @param pLogFile Path of the log file. May be null, in which case logging
	 *   occurs on {@link System#out}.
	 * @param pLogLevel Log level of the log file. May be null, in which case
	 *   {@link Level#INFO} is chosen as the default.
	 * @param pPropertyUris Paths of the Property files. At least one of these
	 *   files must exist. If multiple such files exist, then they are being
	 *   merged into a set of properties, with increasing priority.
	 * @return The created {@link Application}.
	 */
	public static Application of(Module pModule, @Nullable String pLogFile, String pLogLevel,
			                     String... pPropertyUris) {
		final Path logFile = pLogFile == null ? null : Paths.get(pLogFile);
		final Level logLevel = pLogLevel == null ? null : Level.valueOf(pLogLevel.toUpperCase());
		return of(Application.class, pModule, logFile, logLevel, pPropertyUris);
	}

	/** Creates a new {@link Application} with the given module, logger factory,
	 * and property factory.
	 * @param pModule A module for creating the component factory.
	 * @param pLogFile Path of the log file. May be null, in which case logging
	 *   occurs on {@link System#out}.
	 * @param pLogLevel Log level of the log file. May be null, in which case
	 *   {@link Level#INFO} is chosen as the default.
	 * @param pPropertyUris Paths of the Property files. At least one of these
	 *   files must exist. If multiple such files exist, then they are being
	 *   merged into a set of properties, with increasing priority.
	 * @return The created {@link Application}.
	 */
	public static Application of(Module pModule, @Nullable Path pLogFile, Level pLogLevel,
			                     String... pPropertyUris) {
		return of(Application.class, pModule, pLogFile, pLogLevel, pPropertyUris);
	}

	/** Creates a new {@link Application} with the given module, logger factory,
	 * and property factory.
	 * @param pType Type of the Application.
	 * @param pModule A module for creating the component factory.
	 * @param pLogFile Path of the log file. May be null, in which case logging
	 *   occurs on {@link System#out}.
	 * @param pLogLevel Log level of the log file. May be null, in which case
	 *   {@link Level#INFO} is chosen as the default.
	 * @param pPropertyUris Paths of the Property files. At least one of these
	 *   files must exist. If multiple such files exist, then they are being
	 *   merged into a set of properties, with increasing priority.
	 * @return The created {@link Application}.
	 * <T> Type of the Application.
	 */
	public static <A extends Application> A of(Class<A> pType, Module pModule, @Nullable Path pLogFile, Level pLogLevel,
            String... pPropertyUris) {
		try {
			@SuppressWarnings("unchecked")
			final Constructor<Application> cons = (Constructor<Application>) pType.getDeclaredConstructor(Module.class, FailableSupplier.class, FailableSupplier.class);
			final FailableSupplier<ILogFactory,?> lfSupplier = () -> SimpleLogFactory.of(pLogFile, pLogLevel);
			final FailableSupplier<IPropertyFactory,?> pfSupplier = () -> DefaultPropertyFactory.of(pPropertyUris);
			final Application app = cons.newInstance(pModule, lfSupplier, pfSupplier);
			@SuppressWarnings("unchecked")
			final A a = (A) app;
			return a;
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/** Creates a new {@link Application} with the given module, logger factory,
	 * and property factory.
	 * @param pType Type of the Application.
	 * @param pModule A module for creating the component factory.
	 * @param pLogFile Path of the log file. May be null, in which case logging
	 *   occurs on {@link System#out}.
	 * @param pLogLevel Log level of the log file. May be null, in which case
	 *   {@link Level#INFO} is chosen as the default.
	 * @param pPropertyUris Paths of the Property files. At least one of these
	 *   files must exist. If multiple such files exist, then they are being
	 *   merged into a set of properties, with increasing priority.
	 * @return The created {@link Application}.
	 * <T> Type of the Application.
	 */
	public static <A extends Application> A of(Class<A> pType, Module pModule, @Nullable String pLogFile, String pLogLevel,
            String... pPropertyUris) {
		final Path logFile = pLogFile == null ? null : Paths.get(pLogFile);
		final Level logLevel = pLogLevel == null ? null : Level.valueOf(pLogLevel.toUpperCase());
		return of(pType, pModule, logFile, logLevel, pPropertyUris);
	}
}
