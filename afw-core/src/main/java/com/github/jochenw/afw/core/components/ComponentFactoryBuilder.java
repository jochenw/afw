package com.github.jochenw.afw.core.components;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Provider;

import com.github.jochenw.afw.core.DefaultResourceLoader;
import com.github.jochenw.afw.core.ResourceLocator;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.log.simple.SimpleLogFactory;
import com.github.jochenw.afw.core.props.DefaultPropertyFactory;
import com.github.jochenw.afw.core.props.IPropertyFactory;
import com.github.jochenw.afw.core.props.PropertyLoader;
import com.github.jochenw.afw.core.util.AbstractBuilder;

public abstract class ComponentFactoryBuilder extends AbstractBuilder {
	public abstract static class Binder {
		public abstract <T> void bindClass(Class<T> pType);
		public abstract <T> void bind(Class<T> pType, String pName, T pInstance);
		public abstract <T> void bind(Class<T> pType, T pInstance);
		public abstract <T> void bindProvider(Class<T> pType, String pName, Provider<T> pProvider);
		public abstract <T> void bindProvider(Class<T> pType, String pName, Provider<T> pProvider, boolean pSingleton);
		public abstract <T> void bindClass(Class<T> pType, String pName, Class<? extends T> pImplClass);
		public abstract <T> void bindClass(Class<T> pType, String pName, Class<? extends T> pImplClass, boolean pSingleton);
		public abstract <T> void bindClass(Class<T> pType, Class<? extends T> pImplClass);
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

	private String instanceName, applicationName, resourcePrefix;
	private ILogFactory logFactory;
	private IPropertyFactory propertyFactory;
	private ResourceLocator resourceLocator;
	private com.github.jochenw.afw.core.props.PropertyLoader propertyLoader;
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

	public ComponentFactoryBuilder resourcePrefix(String pResourcePrefix) {
		assertMutable();
		resourcePrefix = pResourcePrefix;
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
	
	public String getApplicationName() {
		return applicationName;
	}

	public String getInstanceName() {
		return instanceName;
	}

	public String getResourcePrefix() {
		return resourcePrefix;
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
		return new DefaultResourceLoader(getApplicationName(), getInstanceName(), getResourcePrefix());
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
		if (logFactory == null) {
			logFactory = new SimpleLogFactory();
		}
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
		URL factoryPropertiesUrl;
		factoryPropertiesUrl = getResourceLocator().getResource(factoryPropertiesUri);
		if (factoryPropertiesUrl == null  &&  !factoryPropertiesUri.endsWith(".xml")) {
			factoryPropertiesUrl = getResourceLocator().getResource(factoryPropertiesUri + ".xml");
			if (factoryPropertiesUrl == null) {
				throw new IllegalStateException("Unable to locate property file: " + factoryPropertiesUri);
			}
		}
		final String instancePropertiesUri = getInstancePropertiesUri();
		URL instancePropertiesUrl= getResourceLocator().getResource(instancePropertiesUri);
		if (instancePropertiesUrl == null  &&  !instancePropertiesUri.endsWith(".xml")) {
			instancePropertiesUrl = getResourceLocator().getResource(instancePropertiesUri + ".xml");
			if (instancePropertiesUrl == null) {
				throw new IllegalStateException("Unable to locate property file: " + instancePropertiesUri);
			}
		}
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
