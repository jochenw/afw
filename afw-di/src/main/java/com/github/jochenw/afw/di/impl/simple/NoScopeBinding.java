package com.github.jochenw.afw.di.impl.simple;

import java.util.function.Function;

import com.github.jochenw.afw.di.api.IComponentFactory;

public class NoScopeBinding extends AbstractBinding {
	public NoScopeBinding(Function<SimpleComponentFactory,Object> baseSupplier) {
		super(baseSupplier);
	}
}
