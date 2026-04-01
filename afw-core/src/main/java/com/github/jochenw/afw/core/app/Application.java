package com.github.jochenw.afw.core.app;


import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.log.ILog.Level;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.log.simple.SimpleLogFactory;
import com.github.jochenw.afw.core.props.DefaultPropertyFactory;
import com.github.jochenw.afw.core.props.IIntProperty;
import com.github.jochenw.afw.core.props.ILongProperty;
import com.github.jochenw.afw.core.props.IPathProperty;
import com.github.jochenw.afw.core.props.IProperty;
import com.github.jochenw.afw.core.props.IPropertyFactory;
import com.github.jochenw.afw.core.props.IURLProperty;
import com.github.jochenw.afw.core.props.IntProperty;
import com.github.jochenw.afw.core.props.LongProperty;
import com.github.jochenw.afw.core.props.PathProperty;
import com.github.jochenw.afw.core.props.StringProperty;
import com.github.jochenw.afw.core.props.UrlProperty;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.core.util.Streams;
import com.github.jochenw.afw.di.api.ComponentFactoryBuilder;
import com.github.jochenw.afw.di.api.DefaultLifecycleController;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.ILifecycleController;
import com.github.jochenw.afw.di.api.IModule;
import com.github.jochenw.afw.di.api.LogInject;
import com.github.jochenw.afw.di.api.PropInject;
import com.github.jochenw.afw.di.api.Types;
import com.github.jochenw.afw.di.impl.DefaultAnnotationProvider;
import com.github.jochenw.afw.di.impl.LogInjectBindingProvider;
import com.github.jochenw.afw.di.impl.PropInjectBindingProvider;

import jakarta.inject.Inject;


/** Base class of an application, providing the
 * {@link IComponentFactory}, the {@link IPropertyFactory},
 * and the {@link ILogFactory}.
 */
public class Application {
	/** Interface of the component, that creates the applications
	 * {@link ILogFactory}.
	 */
	public interface LogFactoryGenerator {
		/** Called to create the applications {@link ILogFactory}.
		 * 
		 * @param pApplication The application, which owns the created
		 *   {@link ILogFactory}.
		 * @return The created {@link ILogFactory}
		 */
		public ILogFactory apply(Application pApplication);
	}
	/** Interface of the component, that creates the applications
	 * {@link IPropertyFactory}.
	 */
	public interface PropertyFactoryGenerator {
		/** Called to create the applications {@link IPropertyFactory}.
		 * 
		 * @param pApplication The application, which owns the created
		 *   {@link IPropertyFactory}.
		 * @param pLog A logger, which can be used to log the generators
		 *   progress, like information about the property file(s),
		 *   that are being read.
		 * @return The created {@link ILogFactory}
		 */
		public IPropertyFactory apply(Application pApplication, ILog pLog);
	}
	/** Interface of the component, that creates the applications
	 * {@link IComponentFactory}.
	 */
	public interface ComponentFactoryGenerator {
		/** Called to create the applications {@link IPropertyFactory}.
		 * 
		 * @param pApplication The application, which owns the created
		 *   {@link IPropertyFactory}.
		 * @param pModule The module, which configures the component
		 *   factories bindings. Some default bindings will be added
		 *   silently, including bindings for the
		 *   {@link ILogFactory}, the {@link IPropertyFactory},
		 *   the {@link IComponentFactory}, and the
		 *   {@link Application} itself.
		 * @param pLog A logger, which can be used to log the generators
		 *   progress.
		 * @return The created {@link ILogFactory}
		 */
		public IComponentFactory apply(Application pApplication, IModule pModule, ILog pLog);
	}

	private final @NonNull LogFactoryGenerator logFactoryGenerator;
	private final @NonNull PropertyFactoryGenerator propertyFactoryGenerator;
	private final @NonNull ComponentFactoryGenerator componentFactoryGenerator;
	private final @NonNull IModule module;
	private ILogFactory logFactory;
	private ILog log;
	private IPropertyFactory propertyFactory;
	private IComponentFactory componentFactory;

	/** Creates a new instance with the given generators for
	 * {@link ILogFactory}, {@link IPropertyFactory}, and
	 * {@link IComponentFactory}, and the given module.
	 * @param pLogFactoryGenerator The log factory generator.
	 * @param pPropertyFactoryGenerator The property factory generator.
	 * @param pModule The module, which configures the generated
	 *   component factory.
	 * @param pComponentFactoryGenerator the component factory
	 *   generator.
	 */
	public Application(LogFactoryGenerator pLogFactoryGenerator,
			           PropertyFactoryGenerator pPropertyFactoryGenerator,
			           IModule pModule,
			           ComponentFactoryGenerator pComponentFactoryGenerator) {
		logFactoryGenerator = Objects.requireNonNull(pLogFactoryGenerator, "LogFactoryGenerator");
		propertyFactoryGenerator = Objects.requireNonNull(pPropertyFactoryGenerator, "PropertyFactoryGnerator");
		componentFactoryGenerator = Objects.requireNonNull(pComponentFactoryGenerator, "ComponentFactoryGenerator");
		module = Objects.requireNonNull(pModule, "Module");
	}

	/** Returns the applications log factory.
	 * @return The applications log factory.
	 */
	public ILogFactory getLogFactory() {
		synchronized(logFactoryGenerator) {
			if (logFactory == null) {
				logFactory = Objects.requireNonNull(logFactoryGenerator.apply(this), "LogFactory");
				log = logFactory.getLog(Application.class);
			}
			return logFactory;
		}
	}

	/** Returns the application instances logger.
	 * @return The application instances logger.
	 */
	protected ILog getLog() {
		// Make sure, that the logger is initialized.
		getLogFactory();
		return log;
	}
	
	/** Returns the applications property factory.
	 * @return The applications property factory.
	 */
	public IPropertyFactory getPropertyFactory() {
		synchronized(propertyFactoryGenerator) {
			if (propertyFactory == null) {
				propertyFactory = Objects.requireNonNull(propertyFactoryGenerator.apply(this, getLog()), "PropertyFactory");
			}
			return propertyFactory;
		}
	}

	/** Returns the applications component factory.
	 * @return The applications component factory.
	 */
	public IComponentFactory getComponentFactory() {
		// Make sure, that the log factory, and the property factory are initialized.
		getLogFactory();
		getPropertyFactory();
		synchronized(componentFactoryGenerator) {
			if (componentFactory == null) {
				componentFactory = Objects.requireNonNull(componentFactoryGenerator.apply(this, module, getLog()), "ComponentFactory");
			}
			return componentFactory;
		}
	}

	/** Creates a new application with the given module, the given log level, and the properties, that are read from the given URI's.
	 * @param pModule The module, which configures the generated
	 *   component factory.
	 * @param pLevel The generated loggers log level.
	 * @param pUris A set of URI's, each of which refers to a property file, which is being loaded.
	 *   See {@link #propertyFactoryGenerator(ClassLoader, Path, String...)} for details on the
	 *   handling of the URI strings. 
	 * @return The created application.
	 */
	public static Application of(IModule pModule, Level pLevel, String... pUris) {
		final LogFactoryGenerator logFactoryGenerator = logFactoryGenerator(null, pLevel);
		final PropertyFactoryGenerator propertyFactoryGenerator = propertyFactoryGenerator(Thread.currentThread().getContextClassLoader(), null, pUris);
		final ComponentFactoryGenerator componentFactoryGenerator = componentFactoryGenerator(pModule);
		return new Application(logFactoryGenerator, propertyFactoryGenerator, pModule, componentFactoryGenerator);
	}

	/** Creates a new generator for a log factory. The generated log factory will
	 * write its output to the given path, or to {@link System#out}, if the path
	 * is non-null. The log factories will be {@code pLevel}.
	 * @param pPath Path of the log file, which is being written. May be null, in which case
	 *   {@link System#out System.out} will be used for logging.
	 * @param pLevel The log level.
	 * @return The created log file generator.
	 */
	public static LogFactoryGenerator logFactoryGenerator(Path pPath, Level pLevel) {
		final Level level = Objects.requireNonNull(pLevel, "Level");
		return (app) -> {
			if (pPath == null) {
				return SimpleLogFactory.ofSystemOut(level);
			} else {
				return SimpleLogFactory.of(pPath, pLevel);
			}
		};
	}

	/** Creates a new generator for a property factory. A set of property files will be read,
	 * as given by the parameter {@code pUris}.  Uri's are interpreted as
	 * follows:
	 * <ol>
	 *   <li>If an URI ends with a question mark (the '?' character), then the prefix
	 *     (without the question mark) will be taken as the actual URI, and the URI counts
	 *     as optional. (It is okay, if no matching property file is found for that URI.)
	 *    /li>
	 *    <li>If a URI contains the pipe character (the '|' character), then the URI will
	 *      be splitted into the segments, that are separated by the '|' characters. Any
	 *      such segment will be taken as an atomic URI. Otherwise (there is no '|'
	 *      character), the whole URI will be taken as an atomic URI.</li>
	 *    <li>For every atomic URI, an attempt will be made to locate a property file.
	 *      First, via the {@code pClassLoader} parameter, then via the {@code pBaseDir}
	 *      parameter, and finally via the current directory.</li>
	 * </ol>
	 *
	 * <em>Order, and precedence</em>: The final property set will be built as follows:
	 * First, a new, empty property set is created. Then, for every given URI, an attempt is
	 * made to locate a property file for that URI. If such a file is found, it will be
	 * loaded, and added to the existing property set. (Note: This may overwrite existing
	 * properties.) Otherwise, if the URI is optional, it will be ignored, and the
	 * process continues. However, if the URI isn't optional, and no property file has been
	 * found, then an {@link IllegalStateException} will be thrown.
	 * 
	 * In other words: Property files will be loaded, and added, in the order, that's
	 * given by the URI's. Property files that come later in the order can override
	 * the values from earlier files. In effect, the later property files take
	 * precedence.
	 *  
	 * @param pClassLoader A class loader, which may be used to resolve URI's.
	 *   May be null, in which case no attempt is made to load property files from the
	 *   class path.
	 * @param pBaseDir A base directory. If this parameter is non-null, then the property factory
	 *   will attempt to find property files by {@link Path#resolve(String) resolving} them
	 *   against the base directory. If this parameter is null, then URI's must either
	 *   contain absolute file names, or file names, that are relevant to the JVM's current
	 *   directory.
	 * @param pUris A set of strings with property file locations. See above for a
	 *   description of these URI's.
	 * @return The created property file generator.
	 */
	public static PropertyFactoryGenerator propertyFactoryGenerator(ClassLoader pClassLoader, Path pBaseDir, String... pUris) {
		return (app, log) -> {
			final Properties properties = new Properties();
			for (String u : Objects.requireAllNonNull(pUris, "Uri")) {
				final String uriList;
				final boolean optional;
				if (u.endsWith("?")) {
					uriList = u.substring(0, u.length()-1);
					optional = true;
				} else {
					uriList = u;
					optional = false;
				}
				boolean found = false;
				final String[] uriArray = uriList.split("\\|");
				for (String uri : uriArray) {
					Properties props = null;
					if (pClassLoader != null) {
						final URL url = pClassLoader.getResource(uri);
						if (url != null) {
							log.info("propertyFactoryGenerator", "Loading properties from URL: {}", url);
							props = Streams.load(url);
						}
					}
					if (props == null  &&  pBaseDir != null) {
						final @NonNull Path p = Objects.requireNonNull(pBaseDir.resolve(uri));
						if (Files.isRegularFile(p)) {
							log.info("propertyFactoryGenerator", "Loading properties via basedir from file: {}", p);
							props = Streams.load(p);
						}
					}
					if (props == null) {
						final @NonNull Path p = Objects.requireNonNull(Paths.get(uri));
						if (Files.isRegularFile(p)) {
							log.info("propertyFactoryGenerator", "Loading properties from file: {}", p);
							props = Streams.load(p);
						}
					}
					if (props != null) {
						properties.putAll(props);
						found = true;
						break;
					}
				}
				if (!found  &&  !optional) {
					if (uriArray.length == 1) {
						throw new IllegalStateException("Unable to locate property file for URI " + uriList);
					} else {
						throw new IllegalStateException("Unable to locate property file for URI list " + uriList);
					}
				}
			}
			return new DefaultPropertyFactory(properties);
		};
	}

	/** Creates a new generator for a component factory. The generator will
	 * use the applications {@link #getLogFactory() log factory}, and
	 * {@link #getPropertyFactory() property factory}, and the
	 * given module to configure a {@link ComponentFactoryBuilder builder}
	 * with an {@link LogInjectBindingProvider}, and an
	 * {@link PropInjectBindingProvider}, and a
	 * {@link DefaultAnnotationProvider}. In other words, the
	 * generated component factory will support Jakarta annotations, like
	 * {@link Inject}, or {@link jakarta.annotation.PostConstruct}, Javax annotations, like
	 * {@link javax.inject.Inject}, {@link javax.annotation.PostConstruct},
	 * and Google annotations like {@link com.google.inject.Inject},
	 * but also AFW annotations, like {@link LogInject}, and
	 * {@link PropInject}.
	 * @param pModule The module, which configures the component factories
	 * bindings.
	 * @return The created component factory generator.
	 */
	public static ComponentFactoryGenerator componentFactoryGenerator(IModule pModule) {
		return (app,mod,log) -> {
			final @NonNull ILogFactory logFactory = Objects.requireNonNull(app.getLogFactory());
			final @NonNull IPropertyFactory propertyFactory = Objects.requireNonNull(app.getPropertyFactory());
			final ILifecycleController lc = new DefaultLifecycleController();
			final IModule module = mod.extend((b) -> {
				b.bind(Application.class).toInstance(app);
				b.bind(ILogFactory.class).toInstance(logFactory);
				b.bind(IPropertyFactory.class).to((cf) -> propertyFactory);
				b.bind(ILifecycleController.class).toInstance(lc);
				b.addFinalizer((cf) -> lc.start());
			});
			final LogInjectBindingProvider.LoggerFactory<Object> injectLoggerFactory = bindingLoggerFactory(logFactory);
			final PropInjectBindingProvider.PropertyFactory injectPropertyFactory = bindingPropertyFactory(propertyFactory);
			@SuppressWarnings("null")
			final PropInjectBindingProvider<Object> pibp = new PropInjectBindingProvider<Object>(Object.class, injectPropertyFactory);
			@SuppressWarnings("null")
			final LogInjectBindingProvider<Object> libp = new LogInjectBindingProvider<Object>(Object.class, injectLoggerFactory);
			return IComponentFactory.builder()
					.bindingProvider(libp)
					.bindingProvider(pibp)
					.jakarta()
					.module(module)
					.build();
		};
	}

	/** Creates a logger factory for use by the {@link LogInjectBindingProvider}.
	 * @param pLogFactory The applications log factory, which will be used
	 *   internally by the creazed logger factory.
	 * @return The created logger factory.
	 */
	public static LogInjectBindingProvider.LoggerFactory<Object> bindingLoggerFactory(ILogFactory pLogFactory) {
		return (cf, id, mNm) -> {
			if (mNm == null  ||  mNm.length() == 0) {
				return pLogFactory.getLog(id);
			} else {
				return pLogFactory.getLog(id, mNm);
			}
		};
	}

	private static final Type STRING_PROPERTY_TYPE = new Types.Type<IProperty<String>>(){}.getType();
	private static final Type LONG_PROPERTY_TYPE = new Types.Type<IProperty<Long>>(){}.getType();
	private static final Type INT_PROPERTY_TYPE = new Types.Type<IProperty<Integer>>(){}.getType();
	private static final Type URL_PROPERTY_TYPE = new Types.Type<IProperty<URL>>(){}.getType();
	private static final Type PATH_PROPERTY_TYPE = new Types.Type<IProperty<Path>>(){}.getType();

	/** Creates a property factory for use by the {@link PropInjectBindingProvider}.
	 * @param pPropertyFactory The applications property factory, which will be used
	 *   internally by the creazed property factory.
	 * @return The created property factory.
	 */
	public static PropInjectBindingProvider.PropertyFactory bindingPropertyFactory(IPropertyFactory pPropertyFactory) {
		return (cf, type, pId, dfltVal, nullable) -> {
			final @NonNull String id = Objects.requireNonNull(pId, "Property Id");
			final Supplier<String> valueSupplier = () -> {
				final String value = pPropertyFactory.getPropertyValue(id);
				if (value == null  ||  value == PropInject.NO_DEFAULT) {
					if (nullable) {
						return null;
					} else {
						return dfltVal;
					}
				} else {
					return value;
				}

			};
			final Predicate<Type> isType = (t) -> {
				return t == type  ||  t.equals(type);
			};
			if (isType.test(String.class)) {
				return valueSupplier.get();
			} else if (isType.test(STRING_PROPERTY_TYPE)
					   ||  isType.test(StringProperty.class)
					   ||  isType.test(IProperty.class)) {
				if (dfltVal == null  ||  dfltVal == PropInject.NO_DEFAULT) {
					return pPropertyFactory.getProperty(id);
				} else {
					return pPropertyFactory.getProperty(id, dfltVal);
				}
			} else if (isType.test(LONG_PROPERTY_TYPE)
					   ||  isType.test(LongProperty.class)
					   ||  isType.test(ILongProperty.class)) {
				long defaultValue;
				try {
					defaultValue = Long.parseLong(dfltVal);
				} catch (NumberFormatException nfe) {
					throw new IllegalStateException("Expected default value for property " + pId +
							" to be a long integer number, got '" + dfltVal + "'", nfe);
				}
				return pPropertyFactory.getLongProperty(id, defaultValue);
			} else if (isType.test(INT_PROPERTY_TYPE)
					   ||  isType.test(IntProperty.class)
					   ||  isType.test(IIntProperty.class)) {
				long defaultValue;
				try {
					defaultValue = Long.parseLong(dfltVal);
				} catch (NumberFormatException nfe) {
					throw new IllegalStateException("Expected default value for property " + pId +
							" to be an integer number, got '" + dfltVal + "'", nfe);
				}
				return pPropertyFactory.getLongProperty(id, defaultValue);
			} else if (isType.test(URL_PROPERTY_TYPE)
					   ||  isType.test(UrlProperty.class)
					   ||  isType.test(IURLProperty.class)) {
				if (dfltVal == null  ||  dfltVal == PropInject.NO_DEFAULT) {
					return pPropertyFactory.getUrlProperty(id);
				} else {
					URL defaultValue;
					try {
						defaultValue = URI.create(dfltVal).toURL();
					} catch (MalformedURLException mue) {
						throw new IllegalStateException("Expected default value for property " + pId +
								" to be an URL, got " + dfltVal, mue);
					}
					return pPropertyFactory.getUrlProperty(id, defaultValue);
				}
			} else if (isType.test(PATH_PROPERTY_TYPE)
					   ||  isType.test(PathProperty.class)
					   ||  isType.test(IPathProperty.class)) {
				if (dfltVal == null  ||  dfltVal == PropInject.NO_DEFAULT) {
					return pPropertyFactory.getPathProperty(id);
				} else {
					return pPropertyFactory.getPathProperty(id, Paths.get(dfltVal));
				}
			} else if (isType.test(Long.class)  ||  isType.test(Long.TYPE)) {
				final String strValue = valueSupplier.get();
				try {
					return Long.valueOf(strValue);
				} catch (NumberFormatException nfe) {
					throw new IllegalStateException("Expected value for property " + pId +
							" to be a long integer number, got '" + strValue + "'", nfe);
				}
			} else if (isType.test(Integer.class)  ||  isType.test(Integer.TYPE)) {
				final String strValue = valueSupplier.get();
				try {
					return Integer.valueOf(strValue);
				} catch (NumberFormatException nfe) {
					throw new IllegalStateException("Expected value for property " + pId +
							" to be an integer number, got '" + strValue + "'", nfe);
				}
			} else if (isType.test(URL.class)) {
				final String strValue = valueSupplier.get();
				try {
					return URI.create(strValue).toURL();
				} catch (MalformedURLException mue) {
					throw new IllegalStateException("Expected value for property " + pId +
							" to be an URL, got " + strValue, mue);
				}
			} else if (isType.test(Path.class)) {
				final String strValue = valueSupplier.get();
				return Paths.get(strValue);
			} else {
				throw new IllegalStateException("Invalid property type for property id " + id + ": " + type);
			}
		};
	}
}
