package com.github.jochenw.afw.di.impl.simple;

import java.util.function.Function;


/** Implementation of a binding for a provider. Such bindings are
 * created implicitly by creating a binding for the object, that
 * the provider returns.
 */
public class ProviderBinding implements Binding {
	private final Function<SimpleComponentFactory,Object> parentBinding;

	/** Creates a new instance. The bindings provider will
	 * be bound to the object, that is being returned by the
	 * given binding.
	 * @param pParentBinding The binding, that has explicitly been registered.
	 */
	public ProviderBinding(Function<SimpleComponentFactory,Object> pParentBinding) {
		this.parentBinding = pParentBinding;
	}

	@Override
	public Object apply(SimpleComponentFactory pScf) {
		return parentBinding.apply(pScf);
	}
}
