package com.github.jochenw.afw.di.impl;

import java.util.List;
import java.util.Set;

import com.github.jochenw.afw.di.api.ComponentFactoryBuilder;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IOnTheFlyBinder;
import com.github.jochenw.afw.di.api.Key;


/** Abstract base class for implementations of {@link IComponentFactory}.
 * The {@link ComponentFactoryBuilder} assumes, that the type of the
 * created instance is derived from this class.
 */
public abstract class AbstractComponentFactory implements IComponentFactory {
	private Key<IComponentFactory> key;

	/** Called by the {@link ComponentFactoryBuilder} to configure the
	 * component factory, after it has been created.
	 * @param pOnTheFlyBinder Sets the on-the-fly binder, if any.
	 * @param pBuilders The list of binding builders
	 * @param pStaticInjectionClasses The set of classes, that need static
	 *   injection.
	 */
	public abstract void configure(IOnTheFlyBinder pOnTheFlyBinder,
			                       List<BindingBuilder<Object>> pBuilders,
			                       Set<Class<?>> pStaticInjectionClasses);

	protected Key<IComponentFactory> getKey() {
		return key;
	}
}
