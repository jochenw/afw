package com.github.jochenw.afw.core.components;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Generics;


public abstract class AbstractComponentFactory implements IComponentFactory {
	protected static final Function<Class<Object>,Object> INSTANTIATOR = (c) -> {
		try {
			return c.newInstance();
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	};

	private Map<Key,Supplier<Object>> bindings;
	public Map<Key,Supplier<Object>> getBindings() {
		return bindings;
	}

	public void setBindings(@Nonnull Map<Key,Supplier<Object>> pBindings) {
		bindings = pBindings;
	}
	
	public <O extends Object> Function<Class<O>, O> getInstantiator() {
		final Function<Class<O>,O> instantiator = Generics.cast(INSTANTIATOR);
		return instantiator;
	}

	@Override
	@Nullable public <O> O getInstance(Class<?> pType, String pName) {
		final @Nullable Supplier<O> supplier = getSupplier(pType, pName);
		if (supplier == null) {
			return null;
		} else {
			return supplier.get();
		}
	}
	@Override
	@Nullable public <O> O getInstance(Class<?> pType) {
		return getInstance(pType, "");
	}
	@Override
	@Nonnull public <O> O requireInstance(Class<?> pType, String pName) {
		final O o = getInstance(pType, pName);
		if (o == null) {
			throw new NoSuchElementException("No such instance available: type=" + pType.getName() + ", name=" + pName);
		}
		return o;
	}
	@Override
	@Nonnull public <O> O requireInstance(Class<?> pType) {
		final O o = getInstance(pType);
		if (o == null) {
			throw new NoSuchElementException("No such instance available: type=" + pType.getName());
		}
		return o;
	}
	@Override
	@Nonnull public <O> O newInstance(Class<O> pType) {
		final Function<Class<O>,O> instantiator = getInstantiator();
		final O o = instantiator.apply(pType);
		initialize(o);
		return o;
	}
}
