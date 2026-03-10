package com.github.jochenw.afw.di.simple;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.github.jochenw.afw.di.api.Key;
import com.github.jochenw.afw.di.impl.AbstractComponentFactory;

public class SimpleComponentFactory extends AbstractComponentFactory {
	private final ConcurrentMap<Key<Object>,IBinding<Object>> bindings = new ConcurrentHashMap<>();

	@Override
	public <T> IBinding<T> getBinding(Key<T> pKey) {
		@SuppressWarnings("unchecked")
		final IBinding<T> binding = (IBinding<T>) bindings.get(pKey);
		return binding;
	}

	@Override
	public void init(Object pObject) {
		throw new IllegalStateException("Not implemented");
	}


}
