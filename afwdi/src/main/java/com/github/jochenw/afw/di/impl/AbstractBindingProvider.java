package com.github.jochenw.afw.di.impl;

import com.github.jochenw.afw.di.api.IBindingProvider;
import com.github.jochenw.afw.di.impl.AbstractComponentFactory.Configuration;


/** Abstract base class for deriving implementations of
 * {@link IBindingProvider}.
 */
public abstract class AbstractBindingProvider implements IBindingProvider {
	/** Initializes the binding provider by passing the 
	 * component factories configuration.
	 * @param pConfiguration The component factories configuration.
	 */
	public abstract void init(Configuration pConfiguration);
}
