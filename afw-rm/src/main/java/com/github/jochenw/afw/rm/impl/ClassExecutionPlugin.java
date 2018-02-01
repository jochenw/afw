package com.github.jochenw.afw.rm.impl;

import com.github.jochenw.afw.rm.api.AbstractInitializable;
import com.github.jochenw.afw.rm.api.AbstractInstallable;
import com.github.jochenw.afw.rm.api.ComponentFactory;
import com.github.jochenw.afw.rm.api.RmLogger;
import com.github.jochenw.afw.rm.api.RmResourcePlugin;

public class ClassExecutionPlugin extends AbstractInitializable implements RmResourcePlugin {
	private final ClassLoader cl;
	private RmLogger logger;

	public ClassExecutionPlugin(ClassLoader pCl) {
		cl = pCl;
	}
	public ClassExecutionPlugin() {
		this(Thread.currentThread().getContextClassLoader());
	}

	@Override
	public boolean isInstallable(ResourceInstallationRequest pRequest) {
		final String type = pRequest.getResource().getType();
		if (type.startsWith("class:")) {
			final String className = type.substring("class:".length());
			try {
				final Class<?> clazz = cl.loadClass(className);
				if (!AbstractInstallable.class.isAssignableFrom(clazz)) {
					logger.warning("The installable class " + className + " isn't implementing " + AbstractInstallable.class.getName() + ", ignoring.");
					return false;
				}
				return true;
			} catch (Throwable t) {
				logger.error("Failed to inspect installable class " + className + ", ignoring", t);
				return false;
			}
		}
		return false;
	}

	@Override
	public void install(ResourceInstallationRequest pRequest) {
		final String type = pRequest.getResource().getType();
		final String className = type.substring("class:".length());
		AbstractInstallable installable = null;
		try {
			final Class<?> clazz = cl.loadClass(className);
			if (!AbstractInstallable.class.isAssignableFrom(clazz)) {
				logger.warning("The installable class " + className + " isn't implementing " + AbstractInstallable.class.getName() + ", ignoring.");
				return;
			}
			installable = (AbstractInstallable) clazz.newInstance();
			getComponentFactory().init(installable);
		} catch (Throwable t) {
			logger.error("Failed to create an instance of " + className + ", ignoring", t);
			return;
		}
		try {
			installable.install(pRequest);
		} catch (Throwable t) {
			logger.error("Failed to install an instance of " + className + ": " + t.getMessage(), t);
		}
	}

	@Override
	public void init(ComponentFactory pComponentFactory) {
		super.init(pComponentFactory);
		logger = pComponentFactory.requireInstance(RmLogger.class);
	}
}
