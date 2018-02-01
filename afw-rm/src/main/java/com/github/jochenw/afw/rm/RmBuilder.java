package com.github.jochenw.afw.rm;

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

import com.github.jochenw.afw.rm.api.ComponentFactory;
import com.github.jochenw.afw.rm.api.InstalledResourceRegistry;
import com.github.jochenw.afw.rm.api.JdbcConnectionProvider;
import com.github.jochenw.afw.rm.api.RmLogger;
import com.github.jochenw.afw.rm.api.RmResourcePlugin;
import com.github.jochenw.afw.rm.api.RmResourceRefGuesser;
import com.github.jochenw.afw.rm.api.RmResourceRefRepository;
import com.github.jochenw.afw.rm.api.SqlExecutor;
import com.github.jochenw.afw.rm.api.SqlReader;
import com.github.jochenw.afw.rm.impl.AnnotationScanningResourceRefGuesser;
import com.github.jochenw.afw.rm.impl.AsmClassInspectingResourceRefGuesser;
import com.github.jochenw.afw.rm.impl.ClassExecutionPlugin;
import com.github.jochenw.afw.rm.impl.ClassPathResourceRefRepository;
import com.github.jochenw.afw.rm.impl.DefaultJdbcConnectionProvider;
import com.github.jochenw.afw.rm.impl.DefaultSqlReader;
import com.github.jochenw.afw.rm.impl.DirectoryResourceRefRepository;
import com.github.jochenw.afw.rm.impl.JdbcResourcePlugin;
import com.github.jochenw.afw.rm.impl.SimpleComponentFactory;
import com.github.jochenw.afw.rm.impl.SimpleRmLogger;

public class RmBuilder {
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
	private ClassLoader cl;
	private List<Properties> properties = new ArrayList<Properties>();
	private boolean mutable = true;
	private Charset charset = StandardCharsets.UTF_8;
	private final List<Binding> bindings = new ArrayList<>();
	private final List<RmResourceRefRepository> resourceRepositories = new ArrayList<>();
	private final List<RmResourceRefGuesser> resourceGuessers = new ArrayList<>();
	private final List<RmResourcePlugin> resourcePlugins = new ArrayList<>();
	private InstalledResourceRegistry resourceRegistry;
	private RmLogger logger;

	// Package private constructor, to prevent accidental instantiation.
	RmBuilder() {
	}

	protected void assertMutable() {
		if (!mutable) {
			throw new IllegalStateException("This builder is no longer mutable.");
		}
	}

	public RmBuilder properties(Properties... pProperties) {
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
	
	public RmBuilder classLoader(ClassLoader pClassLoader) {
		assertMutable();
		cl = pClassLoader;
		return this;
	}

	public ClassLoader getClassLoader() {
		return cl;
	}
	
	public RmBuilder charset(String pCharset) {
		Objects.requireNonNull(pCharset, "Charset");
		try {
			final Charset charset = Charset.forName(pCharset);
			return charset(charset);
		} catch (IllegalCharsetNameException|UnsupportedCharsetException e) {
			throw new IllegalArgumentException("Invalid character set name: " + pCharset);
		}
	}

	public RmBuilder charset(Charset pCharset) {
		Objects.requireNonNull(pCharset, "Charset");
		assertMutable();
		charset = pCharset;
		return this;
	}

	public Charset getCharset() {
		return charset;
	}

	public RmBuilder resourceRepository(File pDirectory) {
		Objects.requireNonNull(pDirectory, "Directory");
		if (!pDirectory.isDirectory()) {
			throw new IllegalArgumentException("Not a directory: " + pDirectory.getAbsolutePath());
		}
		return resourceRepository(new DirectoryResourceRefRepository(pDirectory));
	}

	public RmBuilder resourceRepository(Path pDirectory) {
		Objects.requireNonNull(pDirectory, "Directory");
		if (!Files.isDirectory(pDirectory)) {
			throw new IllegalArgumentException("Not a directory: " + pDirectory.toAbsolutePath());
		}
		return resourceRepository(new DirectoryResourceRefRepository(pDirectory));
	}

	public RmBuilder resourceRepository(String pResourcePrefix) {
		return resourceRepository(Thread.currentThread().getContextClassLoader(), pResourcePrefix);
	}

	public RmBuilder resourceRepository(ClassLoader pCl, String pResourcePrefix) {
		assertMutable();
		return resourceRepository(new ClassPathResourceRefRepository(pCl, pResourcePrefix));
	}

	public RmBuilder resourceRepository(RmResourceRefRepository pRepository) {
		assertMutable();
		resourceRepositories.add(pRepository);
		return this;
	}

	public RmBuilder resourceRefGuesser(RmResourceRefGuesser pGuesser) {
		assertMutable();
		resourceGuessers.add(pGuesser);
		return this;
	}

	public RmBuilder defaultResourceRefGuessers() {
		assertMutable();
		resourceGuessers.add(new AsmClassInspectingResourceRefGuesser());
		resourceGuessers.add(new AnnotationScanningResourceRefGuesser());
		return this;
	}

	public List<RmResourceRefGuesser> getResourceRefGuessers() {
		return resourceGuessers;
	}

	public RmBuilder installedResourceRegistry(InstalledResourceRegistry pRegistry) {
		assertMutable();
		resourceRegistry = pRegistry;
		return this;
	}

	public RmBuilder resourcePlugin(RmResourcePlugin pPlugin) {
		assertMutable();
		resourcePlugins.add(pPlugin);
		return this;
	}

	public RmBuilder defaultResourcePlugins() {
		assertMutable();
		resourcePlugin(new JdbcResourcePlugin());
		resourcePlugin(new ClassExecutionPlugin());
		return this;
	}
	
	public Rm build() {
		final ComponentFactory componentFactory = newComponentFactory();
		return componentFactory.getInstance(Rm.class);
	}

	public RmBuilder logger(RmLogger pLogger) {
		assertMutable();
		logger = pLogger;
		return this;
	}

	public RmLogger getLogger() {
		return logger;
	}
	
	protected ComponentFactory newComponentFactory() {
		if (resourcePlugins.isEmpty()) {
			defaultResourcePlugins();
		}
		final SimpleComponentFactory cf = new SimpleComponentFactory();
		cf.setInstanceClass(Rm.class, Rm.class);
		cf.setInstance(List.class, RmResourceRefRepository.class.getName(), resourceRepositories);
		cf.setInstance(List.class, RmResourceRefGuesser.class.getName(), resourceGuessers);
		cf.setInstance(List.class, RmResourcePlugin.class.getName(), resourcePlugins);
		cf.setInstance(InstalledResourceRegistry.class, resourceRegistry);
		cf.setInstance(String.class, Charset.class.getName(), charset.name());
		cf.setInstanceClass(JdbcConnectionProvider.class, DefaultJdbcConnectionProvider.class);
		cf.setInstanceClass(SqlExecutor.class, DefaultSqlExecutor.class);
		cf.setInstanceClass(SqlReader.class, DefaultSqlReader.class);
		cf.setInstance(ClassLoader.class, cl == null ? Thread.currentThread().getContextClassLoader() : cl);
		final Properties props = getProperties();
		if (props != null) {
			cf.setInstance(Properties.class, props);
		}
		if (logger == null) {
			logger = new SimpleRmLogger();
		}
		cf.setInstance(RmLogger.class, logger);
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
		return cf;
	}
}
