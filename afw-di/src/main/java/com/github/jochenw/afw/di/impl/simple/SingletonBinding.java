package com.github.jochenw.afw.di.impl.simple;

import java.util.function.Function;

import com.github.jochenw.afw.di.api.Scopes;


/** Implementation of a binding with scope {@link Scopes#SINGLETON}.
 */
public class SingletonBinding extends AbstractBinding {
	/** Creates a new instance with the given base supplier.
	 * @param baseSupplier The base supplier, that provides the singleton instance.
	 */
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
