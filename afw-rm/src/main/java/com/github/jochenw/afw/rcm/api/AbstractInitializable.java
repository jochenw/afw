package com.github.jochenw.afw.rcm.api;

import com.github.jochenw.afw.rcm.api.ComponentFactory.Initializable;

public abstract class AbstractInitializable implements Initializable {
	private ComponentFactory componentFactory;
	private RmLogger logger;

	@Override
	public void init(ComponentFactory pComponentFactory) {
		componentFactory = pComponentFactory;
		logger = componentFactory.requireInstance(RmLogger.class);
	}

	public ComponentFactory getComponentFactory() {
		return componentFactory;
	}

	public RmLogger getLogger() {
		return logger;
	}

	protected <O> O getInstance(Class<O> pType) {
		return getComponentFactory().getInstance(pType);
	}

	protected <O> O getInstance(Class<O> pType, String pName) {
		return getComponentFactory().getInstance(pType, pName);
	}

	protected <O> O requireInstance(Class<O> pType) {
		return getComponentFactory().requireInstance(pType);
	}

	protected <O> O requireInstance(Class<O> pType, String pName) {
		return getComponentFactory().requireInstance(pType, pName);
	}
}
