package com.github.jochenw.afw.di.impl;

import java.util.Objects;

import com.github.jochenw.afw.di.api.ComponentFactoryBuilder;
import com.github.jochenw.afw.di.api.IComponentFactory;



/** Abstract base class for instances of {@link IComponentFactory}, that can
 * be created by the {@link ComponentFactoryBuilder}.
 */
public abstract class AbstractComponentFactory implements IComponentFactory {
	private IConfiguration configuration;

	/** Creates a new instance. The created instance needs to be
	 * configured using {@link IComponentFactory#init(com.github.jochenw.afw.di.api.IComponentFactory.IConfiguration)},
	 * before using it.
	 */
	protected AbstractComponentFactory() {
	}

	@Override
	public void init(IConfiguration pConfiguration) {
		final IConfiguration config = Objects.requireNonNull(pConfiguration, "Configuration");
		if (configuration != null) {
			throw new IllegalStateException("The component factory is already initialized.");
		}
		configuration = config;
	}


	/** Returns the component factories configuration.
	 * @return The component factories configuration.
	 */
	public IConfiguration getConfiguration() {
		return configuration;
	}
}
