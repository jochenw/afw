package com.github.jochenw.afw.lc;


public abstract class ComponentFactory {
	public <O> O getInstance(Class<O> pType) {
		return getInstance(pType, null);
	}
	public <O> O requireInstance(Class<O> pType) {
		final O o = getInstance(pType);
		if (o == null) {
			throw new IllegalStateException("No component registered for type=" + pType.getName());
		}
		return o;
	}
	public abstract <O> O getInstance(Class<O> pType, String pName);
	public <O> O requireInstance(Class<O> pType, String pName) {
		final O o = getInstance(pType, pName);
		if (o == null) {
			if (pName == null) {
				throw new IllegalStateException("No component registered for type=" + pType.getName());
			} else {
				throw new IllegalStateException("No component registered for name=" + pName + ", type=" + pType.getName());
			}
		}
		return o;
	}
	public abstract void configure(Object pObject);
}
