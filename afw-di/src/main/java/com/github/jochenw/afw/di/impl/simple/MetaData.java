package com.github.jochenw.afw.di.impl.simple;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.github.jochenw.afw.di.api.IComponentFactory;


public class MetaData {
	private final Function<SimpleComponentFactory,Object> instantiator;
	private final BiConsumer<SimpleComponentFactory,Object> initializer;

	public MetaData(Function<SimpleComponentFactory, Object> pInstantiator,
			        BiConsumer<SimpleComponentFactory, Object> pInitializer) {
		instantiator = pInstantiator;
		initializer = pInitializer;
	}

	public Function<SimpleComponentFactory, Object> getInstantiator() {
		return instantiator;
	}

	public BiConsumer<SimpleComponentFactory, Object> getInitializer() {
		return initializer;
	}
}
