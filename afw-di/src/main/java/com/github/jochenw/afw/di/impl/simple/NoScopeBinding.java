package com.github.jochenw.afw.di.impl.simple;

import java.util.function.Function;

import com.github.jochenw.afw.di.api.Scopes;


/** Implementation of a binding with scope {@link Scopes#NO_SCOPE}.
 */
public class NoScopeBinding extends AbstractBinding {
	/** Creates a new instance with the given base supplier.
	 * @param pBaseSupplier The supplier, that is actually creating instances.
	 */
	public NoScopeBinding(Function<SimpleComponentFactory,Object> pBaseSupplier) {
		super(pBaseSupplier);
	}
}
