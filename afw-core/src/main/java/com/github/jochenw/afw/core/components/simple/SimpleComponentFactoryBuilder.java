package com.github.jochenw.afw.core.components.simple;

import com.github.jochenw.afw.core.components.ComponentFactory;
import com.github.jochenw.afw.core.components.ComponentFactoryBuilder;

public class SimpleComponentFactoryBuilder extends ComponentFactoryBuilder {
	@Override
	protected ComponentFactory newComponentFactory() {
		return new SimpleComponentFactory();
	}

	@Override
	protected void configure(ComponentFactory pComponentFactory, Module pModule) {
		final Binder binder = ((SimpleComponentFactory) pComponentFactory).getBinder();
		pModule.configure(binder);
	}

	public static ComponentFactoryBuilder builder() {
		return new SimpleComponentFactoryBuilder();
	}
}
