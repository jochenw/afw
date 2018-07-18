package com.github.jochenw.afw.lc;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Provider;

import com.github.jochenw.afw.core.DefaultResourceLoader;
import com.github.jochenw.afw.core.ResourceLocator;
import com.github.jochenw.afw.core.components.LifecycleController;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.props.DefaultPropertyFactory;
import com.github.jochenw.afw.core.props.IPropertyFactory;
import com.github.jochenw.afw.core.util.AbstractBuilder;
import com.github.jochenw.afw.lc.impl.DbInitializer;
import com.github.jochenw.afw.lc.impl.DefaultConnectionProvider;

public abstract class ComponentFactoryBuilder extends AbstractBuilder {
	public abstract static class Binder {
		public <T> void bindClass(Class<T> pType) {
			bindClass(pType, pType);
		}
		public abstract <T> void bind(Class<T> pType, String pName, T pInstance);
		public abstract <T> void bind(Class<T> pType, T pInstance);
		public <T> void bindProvider(Class<T> pType, String pName, Provider<T> pProvider) {
			bindProvider(pType, pName, pProvider, true);
		}
		public abstract <T> void bindProvider(Class<T> pType, String pName, Provider<T> pProvider, boolean pSingleton);
		public <T> void bindProvider(Class<T> pType, Provider<T> pProvider) {
			bindProvider(pType, pProvider, true);
		}
		public abstract <T> void bindProvider(Class<T> pType, Provider<T> pProvider, boolean pSingleton);
		public <T> void bindClass(Class<T> pType, String pName, Class<? extends T> pImplClass) {
			bindClass(pType, pName, pImplClass, false);
		}
		public abstract <T> void bindClass(Class<T> pType, String pName, Class<? extends T> pImplClass, boolean pSingleton);
		public <T> void bindClass(Class<T> pType, Class<? extends T> pImplClass) {
			bindClass(pType, pImplClass, false);
		}
		public abstract <T> void bindClass(Class<T> pType, Class<? extends T> pImplClass, boolean pSingleton);
		public void bindConstant(String pName, String pValue) {
			bind(String.class, pName, pValue);
		}
		void bindConstant(String pName, int pValue) {
			bind(Integer.class, pName, Integer.valueOf(pValue));
		}
		void bindConstant(String pName, long pValue) {
			bind(Long.class, pName, Long.valueOf(pValue));
		}
		void bindConstant(String pName, boolean pValue) {
			bind(Boolean.class, pName, Boolean.valueOf(pValue));
		}
	}
	public abstract static class StartableObjectProvider {
		public void addStartableObjects(ComponentFactory pComponentFactory, List<Object> pList) {
		}
	}
	public interface Module {
		void configure(Binder pBinder);
	}
	private String instanceName, applicationName;
	private boolean useHibernate, useFlyway;
	private ILogFactory logFactory;
	private IPropertyFactory propertyFactory;
	private ResourceLocator resourceLocator;
	private PropertyLoader propertyLoader;
	private List<Module> modules = new ArrayList<>();
	private ComponentFactory componentFactory;
	private Properties factoryProperties, instanceProperties, defaultProperties;
	private StartableObjectProvider startableObjectProvider;

	public ComponentFactoryBuilder applicationName(String pName) {
		assertMutable();
		applicationName = pName;
		return this;
	}

	public ComponentFactoryBuilder factoryProperties(Properties pProperties) {
		assertMutable();
		factoryProperties = pProperties;
		return this;
	}
	
	public ComponentFactoryBuilder instanceName(String pName) {
		assertMutable();
		instanceName = pName;
		return this;
	}

	public ComponentFactoryBuilder instanceProperties(Properties pProperties) {
		assertMutable();
		instanceProperties = pProperties;
		return this;
	}

	public ComponentFactoryBuilder logFactory(ILogFactory pFactory) {
		assertMutable();
		logFactory = pFactory;
		return this;
	}

	public ComponentFactoryBuilder module(Module pModule) {
		assertMutable();
		modules.add(pModule);
		return this;
		
	}

	public ComponentFactoryBuilder modules(Module... pModules) {
		assertMutable();
		modules.addAll(Arrays.asList(pModules));
		return this;
		
	}

	public ComponentFactoryBuilder propertyFactory(IPropertyFactory pFactory) {
		assertMutable();
		propertyFactory = pFactory;
		return this;
	}

	public ComponentFactoryBuilder propertyLoader(PropertyLoader pLoader) {
		assertMutable();
		propertyLoader = pLoader;
		return this;
	}

	public ComponentFactoryBuilder resourceLocator(ResourceLocator pLocator) {
		assertMutable();
		resourceLocator = pLocator;
		return this;
	}

	public ComponentFactoryBuilder startableObjectProvider(StartableObjectProvider pProvider) {
		assertMutable();
		startableObjectProvider = pProvider;
		return this;
	}
	
	public ComponentFactoryBuilder usingHibernate() {
		return usingHibernate(true);
	}

	public ComponentFactoryBuilder usingHibernate(boolean pUsingHibernate) {
		assertMutable();
		useHibernate = pUsingHibernate;
		return this;
	}
	
	public ComponentFactoryBuilder usingFlyway() {
		return usingFlyway(true);
	}

	public ComponentFactoryBuilder usingFlyway(boolean pUsingFlyway) {
		assertMutable();
		useFlyway = pUsingFlyway;
		return this;
	}
	
	public String getApplicationName() {
		return applicationName;
	}

	public String getInstanceName() {
		return instanceName;
	}
	
	protected void validate() {
		if (getApplicationName() == null) {
			throw new IllegalStateException("The application name must be set.");
		}
		final ResourceLocator resLocator = getResourceLocator();
		if (resLocator == null) {
			throw new IllegalStateException("Unable to create a resource locator.");
		}
		final ILogFactory lf = getLogFactory();
		if (lf == null) {
			throw new IllegalStateException("No ILogFactory has been configured.");
		}
		lf.setResourceLocator(resLocator);
		lf.start();
		if (getPropertyLoader() == null) {
			throw new IllegalStateException("Unable to create a property loader.");
		}
		if (getFactoryProperties() == null) {
			throw new IllegalStateException("Factory properties not found.");
		}
		getInstanceProperties();
		getDefaultProperties();
		final IPropertyFactory pf = getPropertyFactory();
		if (pf == null) {
			throw new IllegalStateException("No IPropertyFactory has been configured.");
		}
	}

	public Properties getDefaultProperties() {
		if (defaultProperties == null) {
			defaultProperties = newDefaultProperties();
		}
		return defaultProperties;
	}

	public ResourceLocator getResourceLocator() {
		if (resourceLocator == null) {
			resourceLocator = newResourceLocator();
		}
		return resourceLocator;
	}

	protected ResourceLocator newResourceLocator() {
		return new DefaultResourceLoader(getApplicationName(), getInstanceName());
	}

	public List<Module> getModules() {
		return modules;
	}

	public Properties getFactoryProperties() {
		if (factoryProperties == null) {
			factoryProperties = newFactoryProperties();
		}
		return factoryProperties;
	}

	public Properties getInstanceProperties() {
		if (instanceProperties == null) {
			instanceProperties = newInstanceProperties();
		}
		return instanceProperties;
	}

	public ILogFactory getLogFactory() {
		return logFactory;
	}

	public IPropertyFactory getPropertyFactory() {
		if (propertyFactory == null) {
			propertyFactory = newPropertyFactory();
		}
		return propertyFactory;
	}
	
	public PropertyLoader getPropertyLoader() {
		if (propertyLoader == null) {
			propertyLoader = newPropertyLoader();
		}
		return propertyLoader;
	}

	public StartableObjectProvider getStartableObjectProvider() {
		return startableObjectProvider;
	}

	public boolean isUsingFlyway() {
		return useFlyway;
	}

	public boolean isUsingHibernate() {
		return useHibernate;
	}
	
	protected PropertyLoader newPropertyLoader() {
		return new PropertyLoader(getResourceLocator());
	}

	protected abstract ComponentFactory newComponentFactory();
	protected abstract void configure(ComponentFactory pComponentFactory, Module pModule);
	
	public ComponentFactory build() {
		if (componentFactory == null) {
			validate();
			final ComponentFactory cf = newComponentFactory();
			configure(cf, getModule(cf));
			componentFactory = cf;
			final LifecycleController lc = cf.getInstance(LifecycleController.class);
			final List<Object> startableObjects = getStartableObjects(cf);
			for (Object o : startableObjects) {
				lc.addListener(o);
			}
			lc.start();
			makeImmutable();
		}
		return componentFactory;
	}

	protected List<Object> getStartableObjects(ComponentFactory pComponentFactory) {
		final List<Object> startableObjects = new ArrayList<>();
		if (isUsingHibernate()  ||  isUsingFlyway()) {
			startableObjects.add(pComponentFactory.requireInstance(IConnectionProvider.class));
		}
		if (isUsingHibernate()) {
			startableObjects.add(pComponentFactory.requireInstance(ISessionProvider.class));
		}
		if (isUsingFlyway()) {
			startableObjects.add(pComponentFactory.requireInstance(DbInitializer.class));
		}
		if (startableObjectProvider != null) {
			startableObjectProvider.addStartableObjects(pComponentFactory, startableObjects);
		}
		return startableObjects;
	}
	
	protected Module getModule(final ComponentFactory pComponentFactory) {
		return new Module() {
			@Override
			public void configure(Binder pBinder) {
				pBinder.bind(ComponentFactory.class, pComponentFactory);
				pBinder.bind(ILogFactory.class, getLogFactory());
				pBinder.bind(IPropertyFactory.class, getPropertyFactory());
				pBinder.bindClass(LifecycleController.class);
				pBinder.bindConstant("applicationName", getApplicationName());
				pBinder.bindConstant("instanceName", getInstanceName());
				pBinder.bind(ResourceLocator.class, getResourceLocator());
				pBinder.bind(PropertyLoader.class, getPropertyLoader());
				pBinder.bind(Properties.class, "factory", getFactoryProperties());
				pBinder.bind(Properties.class, "instance", getInstanceProperties());
				Properties props = getDefaultProperties();
				pBinder.bind(Properties.class, "default", props);
				pBinder.bind(Properties.class, props);
				if (isUsingHibernate()  ||  isUsingFlyway()) {
					pBinder.bindClass(IConnectionProvider.class, DefaultConnectionProvider.class);
				}
				if (isUsingHibernate()) {
					pBinder.bindClass(ISessionProvider.class, DefaultSessionProvider.class);
				}
				if (isUsingFlyway()) {
					pBinder.bindClass(DbInitializer.class);
				}
				for (Map.Entry<Object, Object> en : props.entrySet()) {
					final String key = en.getKey().toString();
					final Object v = en.getValue();
					final String value = (v == null) ? (String) null : v.toString();
					pBinder.bindConstant(key, value);
				}

				for (Module customModule : getModules()) {
					customModule.configure(pBinder);
				}
			}
		};
	}

	protected IPropertyFactory newPropertyFactory() {
		final String factoryPropertiesUri = getFactoryPropertiesUri();
		final URL factoryPropertiesUrl = getResourceLocator().requireResource(factoryPropertiesUri);
		final String instancePropertiesUri = getInstancePropertiesUri();
		final URL instancePropertiesUrl = getResourceLocator().requireResource(instancePropertiesUri);
		return new DefaultPropertyFactory(instancePropertiesUrl, factoryPropertiesUrl);
	}
	
	protected Properties newDefaultProperties() {
		final Properties props = new Properties();
		final Properties fp = getFactoryProperties();
		final Properties ip = getInstanceProperties();
		if (fp != null) {
			props.putAll(fp);
		}
		if (ip != null) {
			props.putAll(ip);
		}
		return props;
	}

	protected Properties newFactoryProperties() {
		final String uri = getFactoryPropertiesUri();
		return getPropertyLoader().load(uri);
	}

	protected String getFactoryPropertiesUri() {
		final String uri = getApplicationName() + "-factory.properties";
		return uri;
	}

	protected Properties newInstanceProperties() {
		final String uri = getInstancePropertiesUri();
		return getPropertyLoader().load(uri);
	}

	protected String getInstancePropertiesUri() {
		final String uri = getApplicationName() + ".properties";
		return uri;
	}
}
