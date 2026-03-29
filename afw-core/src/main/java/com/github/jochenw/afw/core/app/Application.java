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
import com.github.jochenw.afw.di.api.DefaultLifecycleController;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.ILifecycleController;
import com.github.jochenw.afw.di.api.IModule;
import com.github.jochenw.afw.di.api.PropInject;
import com.github.jochenw.afw.di.api.Types;
import com.github.jochenw.afw.di.impl.LogInjectBindingProvider;
import com.github.jochenw.afw.di.impl.PropInjectBindingProvider;

/** Base class of an application, providing the
 * {@link IComponentFactory}, the {@link IPropertyFactory},
 * and the {@link ILogFactory}.
 */
public class Application {
	public interface LogFactoryGenerator {
		public ILogFactory apply(Application pApplication);
	}
	public interface PropertyFactoryGenerator {
		public IPropertyFactory apply(Application pApplication, ILog pLog);
	}
	public interface ComponentFactoryGenerator {
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

	public Application(LogFactoryGenerator pLogFactoryGenerator,
			           PropertyFactoryGenerator pPropertyFactoryGenerator,
			           IModule pModule,
			           ComponentFactoryGenerator pComponentFactoryGenerator) {
		logFactoryGenerator = Objects.requireNonNull(pLogFactoryGenerator, "LogFactoryGenerator");
		propertyFactoryGenerator = Objects.requireNonNull(pPropertyFactoryGenerator, "PropertyFactoryGnerator");
		componentFactoryGenerator = Objects.requireNonNull(pComponentFactoryGenerator, "ComponentFactoryGenerator");
		module = Objects.requireNonNull(pModule, "Module");
	}

	public ILogFactory getLogFactory() {
		synchronized(logFactoryGenerator) {
			if (logFactory == null) {
				logFactory = Objects.requireNonNull(logFactoryGenerator.apply(this), "LogFactory");
				log = logFactory.getLog(Application.class);
			}
			return logFactory;
		}
	}

	protected ILog getLog() {
		// Make sure, that the logger is initialized.
		getLogFactory();
		return log;
	}
	
	public IPropertyFactory getPropertyFactory() {
		synchronized(propertyFactoryGenerator) {
			if (propertyFactory == null) {
				propertyFactory = Objects.requireNonNull(propertyFactoryGenerator.apply(this, getLog()), "PropertyFactory");
			}
			return propertyFactory;
		}
	}

	public IComponentFactory getComponentFactory() {
		synchronized(componentFactoryGenerator) {
			if (componentFactory == null) {
				componentFactory = Objects.requireNonNull(componentFactoryGenerator.apply(this, module, getLog()), "ComponentFactory");
			}
			return componentFactory;
		}
	}

	public static Application of(IModule pModule, Level pLevel, String... pUris) {
		final LogFactoryGenerator logFactoryGenerator = logFactoryGenerator(null, pLevel);
		final PropertyFactoryGenerator propertyFactoryGenerator = propertyFactoryGenerator(Thread.currentThread().getContextClassLoader(), null, pUris);
		final ComponentFactoryGenerator componentFactoryGenerator = componentFactoryGenerator(pModule);
		return new Application(logFactoryGenerator, propertyFactoryGenerator, pModule, componentFactoryGenerator);
	}

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

	public static ComponentFactoryGenerator componentFactoryGenerator(IModule pModule) {
		return (app,mod,log) -> {
			final ILogFactory logFactory = app.getLogFactory();
			final IPropertyFactory propertyFactory = app.getPropertyFactory();
			final ILifecycleController lc = new DefaultLifecycleController();
			final IModule module = mod.extend((b) -> {
				b.bind(Application.class).toInstance(app);
				b.bind(ILogFactory.class).toInstance(logFactory);
				b.bind(IPropertyFactory.class).to((cf) -> propertyFactory);
				b.bind(ILifecycleController.class).toInstance(lc);
				b.addFinalizer((cf) -> lc.start());
			});
			final LogInjectBindingProvider.LoggerFactory<Object> injectLoggerFactory = bindingLoggerFactory(logFactory);
			final Class<@NonNull Object> objClass = (Class<@NonNull Object>) Object.class;
			final LogInjectBindingProvider<Object> libp = new LogInjectBindingProvider<Object>(objClass, injectLoggerFactory);
			final PropInjectBindingProvider.PropertyFactory injectPropertyFactory = bindingPropertyFactory(propertyFactory);
			final PropInjectBindingProvider<Object> pibp = new PropInjectBindingProvider<Object>(objClass, injectPropertyFactory);
			return IComponentFactory.builder()
					.bindingProvider(libp)
					.bindingProvider(pibp)
					.jakarta()
					.module(module)
					.build();
		};
	}

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
