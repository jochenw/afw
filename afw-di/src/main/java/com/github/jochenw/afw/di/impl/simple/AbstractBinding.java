package com.github.jochenw.afw.di.impl.simple;

import java.util.function.Function;

import com.github.jochenw.afw.di.api.IComponentFactoryAware;
import com.github.jochenw.afw.di.util.Exceptions;


/** Abstract default implementation of a binding.
 */
public abstract class AbstractBinding implements Binding {
	private final Function<SimpleComponentFactory,Object> baseSupplier;

	/** Creates a new instance. The given supplier will be invoked, if
	 * the binding needs to create an instance.
	 * @param pBaseSupplier The supplier, which is actually providing
	 *   instances on behalf of the binding.
	 */
	protected AbstractBinding(Function<SimpleComponentFactory,Object> pBaseSupplier) {
		baseSupplier = pBaseSupplier;
	}

	@Override
	public Object apply(SimpleComponentFactory pFactory) {
		final Object o = baseSupplier.apply(pFactory);
		pFactory.init(o);
		if (o instanceof IComponentFactoryAware) {
			try {
				((IComponentFactoryAware) o).init(pFactory);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		}
		return o;
	}
}
