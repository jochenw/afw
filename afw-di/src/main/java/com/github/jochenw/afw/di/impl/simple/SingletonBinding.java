package com.github.jochenw.afw.di.impl.simple;

import java.util.function.Function;


public class SingletonBinding extends AbstractBinding {
	public SingletonBinding(Function<SimpleComponentFactory,Object> baseSupplier) {
		super(baseSupplier);
	}

	private boolean initialized;
	private Object instance;

	@Override
	public Object apply(SimpleComponentFactory pFactory) {
		synchronized(this) {
			if (!initialized) {
				instance = super.apply(pFactory);
				initialized = true;
			}
		}
		return instance;
	}
	
}
