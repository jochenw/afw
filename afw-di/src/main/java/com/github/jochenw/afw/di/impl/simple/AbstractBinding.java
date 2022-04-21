package com.github.jochenw.afw.di.impl.simple;

import java.util.function.Function;

import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IComponentFactoryAware;
import com.github.jochenw.afw.di.util.Exceptions;

public abstract class AbstractBinding implements Binding {
	private final Function<SimpleComponentFactory,Object> baseSupplier;

	protected AbstractBinding(Function<SimpleComponentFactory,Object> baseSupplier) {
		this.baseSupplier = baseSupplier;
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
