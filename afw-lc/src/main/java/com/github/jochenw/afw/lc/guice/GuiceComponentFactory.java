package com.github.jochenw.afw.lc.guice;

import com.github.jochenw.afw.lc.ComponentFactory;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

public class GuiceComponentFactory extends ComponentFactory {
	private Injector injector;

	public Injector getInjector() {
		return injector;
	}

	public void setInjector(Injector injector) {
		this.injector = injector;
	}

	@Override
	public <O> O getInstance(Class<O> pType, String pName) {
		if (pName == null) {
			return injector.getInstance(pType);
		} else {
			final Key<O> key = Key.get(pType, Names.named(pName));
			return injector.getInstance(key);
		}
	}

	@Override
	public void configure(Object pObject) {
		injector.injectMembers(pObject);
	}

}
