package com.github.jochenw.afw.rm.api;

import com.github.jochenw.afw.rm.api.ComponentFactory.Initializable;

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
}
