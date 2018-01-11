package com.github.jochenw.afw.core.components;

import java.util.NoSuchElementException;


public abstract class ComponentFactory {
	public abstract <T> T getInstance(Class<T> pType);
	public <T> T requireInstance(Class<T> pType) throws NoSuchElementException {
		final T t = getInstance(pType);
		if (t == null) {
			throw new NoSuchElementException("Component not found: type=" + pType.getName());
		}
		return t;
	}
	public abstract <T> T getInstance(Class<T> pType, String pId);
	public <T> T requireInstance(Class<T> pType, String pId) throws NoSuchElementException {
		final T t = getInstance(pType, pId);
		if (t == null) {
			throw new NoSuchElementException("Component not found: type=" + pType.getName() + ", id=" + pId);
		}
		return t;
	}
    public abstract void initialize(Object pObject);
}
