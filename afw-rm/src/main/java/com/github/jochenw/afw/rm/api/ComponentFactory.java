package com.github.jochenw.afw.rm.api;

import java.util.NoSuchElementException;

import com.github.jochenw.afw.rm.util.Objects;


public abstract class ComponentFactory {
	public interface Initializable {
		void init(ComponentFactory pComponentFactory);
	}
	public abstract <O> O getInstance(Class<O> pType);
	public abstract <O> O getInstance(Class<O> pType, String pName);
	public <O> O requireInstance(Class<O> pType) throws NoSuchElementException {
		Objects.requireNonNull(pType, "Type");
		final O o = getInstance(pType);
		if (o == null) {
			throw new IllegalStateException("No such instance: type=" + pType.getClass().getName());
		}
		return o;
	}
	public <O> O requireInstance(Class<O> pType, String pName) throws NoSuchElementException {
		Objects.requireNonNull(pType, "Type");
		Objects.requireNonNull(pName, "Name");
		final O o = getInstance(pType, pName);
		if (o == null) {
			throw new IllegalStateException("No such instance: type=" + pType.getClass().getName() + ", name=" + pName);
		}
		return o;
	}
}
