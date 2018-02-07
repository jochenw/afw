package com.github.jochenw.afw.rcm;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import javax.inject.Provider;

import com.github.jochenw.afw.rcm.api.ComponentFactory;
import com.github.jochenw.afw.rcm.api.InstalledResourceRegistry;
import com.github.jochenw.afw.rcm.api.JdbcConnectionProvider;
import com.github.jochenw.afw.rcm.api.RmLifecyclePlugin;
import com.github.jochenw.afw.rcm.api.RmLogger;
import com.github.jochenw.afw.rcm.api.RmPluginRepository;
import com.github.jochenw.afw.rcm.api.RmResourcePlugin;
import com.github.jochenw.afw.rcm.api.RmResourceRefGuesser;
import com.github.jochenw.afw.rcm.api.RmResourceRefRepository;
import com.github.jochenw.afw.rcm.api.RmTargetLifecyclePlugin;
import com.github.jochenw.afw.rcm.api.SqlExecutor;
import com.github.jochenw.afw.rcm.api.SqlReader;
import com.github.jochenw.afw.rcm.impl.ClassPathResourceRefRepository;
import com.github.jochenw.afw.rcm.impl.DefaultJdbcConnectionProvider;
import com.github.jochenw.afw.rcm.impl.DefaultSqlReader;
import com.github.jochenw.afw.rcm.impl.DirectoryResourceRefRepository;
import com.github.jochenw.afw.rcm.impl.SimpleComponentFactory;
import com.github.jochenw.afw.rcm.impl.SimpleRmLogger;
import com.github.jochenw.afw.rcm.plugins.AnnotationBasedPluginRepository;
import com.github.jochenw.afw.rcm.plugins.XmlBasedPluginRepository;

public class RcmBuilder {
	protected static class Binding {
		private final Class<Object> type;
		private final String name;
		private final Object instance;
		private final Class<Object> implementation;
		private final Provider<Object> provider;
		Binding(Class<Object> pType, String pName, Object pInstance, Class<Object> pImplementation, Provider<Object> pProvider) {
			type = pType;
			name = pName;
			instance = pInstance;
			implementation = pImplementation;
			provider = pProvider;
		}
	}
	private ComponentFactory componentFactory;
	private ClassLoader cl;
	private List<Properties> properties = new ArrayList<Properties>();
	private boolean mutable = true;
	private Charset charset = StandardCharsets.UTF_8;
	private final List<Binding> bindings = new ArrayList<>();
	private RmPluginRepository pluginRepository;
	private final List<RmResourceRefRepository> resourceRepositories = new ArrayList<>();
	private final List<RmResourceRefGuesser> resourceGuessers = new ArrayList<>();
	private final List<RmResourcePlugin> resourcePlugins = new ArrayList<>();
	private final List<RmTargetLifecyclePlugin> lifecyclePlugins = new ArrayList<>();
	private InstalledResourceRegistry resourceRegistry;
	private RmLogger logger;

	protected RcmBuilder() {
	}

	protected void assertMutable() {
		if (!mutable) {
			throw new IllegalStateException("This builder is no longer mutable.");
		}
	}

	public RcmBuilder properties(Properties... pProperties) {
		assertMutable();
		if (pProperties != null) {
			properties.addAll(Arrays.asList(pProperties));
		}
		return this;
	}

	public Properties getProperties() {
		if (properties.isEmpty()) {
			return null;
		} else {
			final Properties props = new Properties();
			for (Properties p : properties) {
				props.putAll(p);
			}
			return props;
		}
	}

	public RcmBuilder pluginRepository(RmPluginRepository pRepository) {
		assertMutable();
		pluginRepository = pRepository;
		return this;
	}

	public RmPluginRepository getRmPluginRepository() {
		if (pluginRepository == null) {
			pluginRepository = componentFactory.requireInstance(RmPluginRepository.class);
		}
		return pluginRepository;
	}

	public RcmBuilder classLoader(ClassLoader pClassLoader) {
		assertMutable();
		cl = pClassLoader;
		return this;
	}

	public ClassLoader getClassLoader() {
		return cl;
	}
	
	public RcmBuilder charset(String pCharset) {
		Objects.requireNonNull(pCharset, "Charset");
		try {
			final Charset charset = Charset.forName(pCharset);
			return charset(charset);
		} catch (IllegalCharsetNameException|UnsupportedCharsetException e) {
			throw new IllegalArgumentException("Invalid character set name: " + pCharset);
		}
	}

	public RcmBuilder charset(Charset pCharset) {
		Objects.requireNonNull(pCharset, "Charset");
		assertMutable();
		charset = pCharset;
		return this;
	}

	public Charset getCharset() {
		return charset;
	}

	public RcmBuilder resourceRepository(File pDirectory) {
		Objects.requireNonNull(pDirectory, "Directory");
		if (!pDirectory.isDirectory()) {
			throw new IllegalArgumentException("Not a directory: " + pDirectory.getAbsolutePath());
		}
		return resourceRepository(new DirectoryResourceRefRepository(pDirectory));
	}

	public RcmBuilder resourceRepository(Path pDirectory) {
		Objects.requireNonNull(pDirectory, "Directory");
		if (!Files.isDirectory(pDirectory)) {
			throw new IllegalArgumentException("Not a directory: " + pDirectory.toAbsolutePath());
		}
		return resourceRepository(new DirectoryResourceRefRepository(pDirectory));
	}

	public RcmBuilder resourceRepository(String pResourcePrefix) {
		return resourceRepository(Thread.currentThread().getContextClassLoader(), pResourcePrefix);
	}

	public RcmBuilder resourceRepository(ClassLoader pCl, String pResourcePrefix) {
		assertMutable();
		return resourceRepository(new ClassPathResourceRefRepository(pCl, pResourcePrefix));
	}

	public RcmBuilder resourceRepository(RmResourceRefRepository pRepository) {
		assertMutable();
		resourceRepositories.add(pRepository);
		return this;
	}

	public RcmBuilder resourceRefGuesser(RmResourceRefGuesser pGuesser) {
		assertMutable();
		resourceGuessers.add(pGuesser);
		return this;
	}

	public RcmBuilder defaultResourceRefGuessers() {
		assertMutable();
		RmPluginRepository repo = getRmPluginRepository();
		for (RmResourceRefGuesser guesser : repo.getResourceRefGuessers()) {
			resourceRefGuesser(guesser);
		}
		return this;
	}

	public List<RmResourceRefGuesser> getResourceRefGuessers() {
		return resourceGuessers;
	}

	public RcmBuilder installedResourceRegistry(InstalledResourceRegistry pRegistry) {
		assertMutable();
		resourceRegistry = pRegistry;
		return this;
	}

	public RcmBuilder resourcePlugin(RmResourcePlugin pPlugin) {
		assertMutable();
		resourcePlugins.add(pPlugin);
		return this;
	}

	public RcmBuilder defaultResourcePlugins() {
		assertMutable();
		for (RmResourcePlugin plugin : getRmPluginRepository().getResourcePlugins()) {
			resourcePlugin(plugin);
		}
		return this;
	}

	public RcmBuilder targetLifecyclePlugin(RmTargetLifecyclePlugin pPlugin) {
		assertMutable();
		lifecyclePlugins.add(pPlugin);
		return this;
	}

	public List<RmTargetLifecyclePlugin> getTargetLifecyclePlugins() {
		return lifecyclePlugins;
	}

	public RcmBuilder defaultTargetLifecyclePlugins() {
		assertMutable();
		final RmPluginRepository repo = getRmPluginRepository();
		for (RmTargetLifecyclePlugin pl : repo.getTargetLifecyclePlugins()) {
			targetLifecyclePlugin(pl);
		}
		return this;
	}

	public Rcm build() {
		final ComponentFactory componentFactory = newComponentFactory();
		return componentFactory.getInstance(Rcm.class);
	}

	public RcmBuilder logger(RmLogger pLogger) {
		assertMutable();
		logger = pLogger;
		return this;
	}

	public RmLogger getLogger() {
		return logger;
	}
	
	protected ComponentFactory newComponentFactory() {
		final SimpleComponentFactory cf = new SimpleComponentFactory();
		componentFactory = cf;
		if (logger == null) {
			logger = new SimpleRmLogger();
		}
		cf.setInstanceClass(Rcm.class, Rcm.class);
		cf.setInstanceClass(RmPluginRepository.class, XmlBasedPluginRepository.class);
		cf.setInstance(List.class, RmResourceRefRepository.class.getName(), resourceRepositories);
		cf.setInstance(List.class, RmResourceRefGuesser.class.getName(), resourceGuessers);
		cf.setInstance(List.class, RmResourcePlugin.class.getName(), resourcePlugins);
		cf.setInstance(List.class, RmTargetLifecyclePlugin.class.getName(), lifecyclePlugins);
		if (resourceRegistry == null) {
			throw new IllegalStateException("No instance of " + InstalledResourceRegistry.class.getName() + " has been configured. Did you call installedResourceRegistry()?");
		}
		cf.setInstance(InstalledResourceRegistry.class, resourceRegistry);
		cf.setInstance(String.class, Charset.class.getName(), charset.name());
		cf.setInstanceClass(SqlExecutor.class, DefaultSqlExecutor.class);
		cf.setInstanceClass(SqlReader.class, DefaultSqlReader.class);
		cf.setInstanceClass(JdbcConnectionProvider.class, DefaultJdbcConnectionProvider.class);
		cf.setInstance(ClassLoader.class, cl == null ? Thread.currentThread().getContextClassLoader() : cl);
		cf.setInstance(RmLogger.class, logger);
		final Properties props = getProperties();
		if (props != null) {
			cf.setInstance(Properties.class, props);
		}
		for (Binding binding : bindings) {
			if (binding.instance != null) {
				if (binding.name == null) {
					cf.setInstance(binding.type, binding.instance);
				} else {
					cf.setInstance(binding.type, binding.name, binding.instance);
				}
			} else if (binding.implementation != null) {
				if (binding.name == null) {
					cf.setInstanceClass(binding.type, binding.implementation);
				} else {
					cf.setInstanceClass(binding.type, binding.name, binding.implementation);
				}
			} else if (binding.provider != null) {
				if (binding.name == null) {
					cf.setInstanceProvider(binding.type, binding.provider);
				} else {
					cf.setInstanceProvider(binding.type, binding.name, binding.provider);
				}
			} else {
				throw new IllegalStateException("Either of instance, implementation, or provider, must be non-null");
			}
		}
		if (resourcePlugins.isEmpty()) {
			defaultResourcePlugins();
		}
		if (lifecyclePlugins.isEmpty()) {
			defaultTargetLifecyclePlugins();
		}
		if (resourceGuessers.isEmpty()) {
			defaultResourceRefGuessers();
		}
		return cf;
	}

	public <S,T extends S> RcmBuilder bind(Class<S> pType, T pInstance) {
		@SuppressWarnings("unchecked")
		final Class<Object> cl = (Class<Object>) pType;
		final Binding binding = new Binding(cl, null, (Object) pInstance, null, null);
		bindings.add(binding);
		return this;
	}

	public <S,T extends S> RcmBuilder bind(Class<S> pType, String pName, T pInstance) {
		@SuppressWarnings("unchecked")
		final Class<Object> cl = (Class<Object>) pType;
		final Binding binding = new Binding(cl, pName, (Object) pInstance, null, null);
		bindings.add(binding);
		return this;
	}

	public RcmBuilder bindClass(Class<?> pClass) {
		@SuppressWarnings("unchecked")
		final Class<Object> cl = (Class<Object>) pClass;
		final Binding binding = new Binding(cl, null, null, cl, null);
		bindings.add(binding);
		return this;
	}
}
