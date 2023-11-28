package com.github.jochenw.afw.di.impl.simple;

import java.util.function.BiConsumer;
import java.util.function.Function;


/** This class provides meta data about the type of an object, into which values
 * can be injected.
 *
 */
public class MetaData {
	private final Function<SimpleComponentFactory,Object> instantiator;
	private final BiConsumer<SimpleComponentFactory,Object> initializer;

	/**
	 * Creates a new instance with the given instantiator (the object,
	 *   that actually creates an instance), and the injector (the
	 *   object, that injects values into the created instance).
	 * @param pInstantiator The instantiator
	 * @param pInitializer The injector
	 */
	public MetaData(Function<SimpleComponentFactory, Object> pInstantiator,
			        BiConsumer<SimpleComponentFactory, Object> pInitializer) {
		instantiator = pInstantiator;
		initializer = pInitializer;
	}

	/** Returns the instantiator.
	 * @return The instantiator
	 */
	public Function<SimpleComponentFactory, Object> getInstantiator() {
		return instantiator;
	}

	/** Returns the injector.
	 * @return The injector
	 */
	public BiConsumer<SimpleComponentFactory, Object> getInitializer() {
		return initializer;
	}
}
