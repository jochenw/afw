package com.github.jochenw.afw.rm.api;

import java.util.List;
import java.util.NoSuchElementException;

import com.github.jochenw.afw.rm.util.Objects;


public abstract class ComponentFactory {
	public interface Initializable {
		void init(ComponentFactory pComponentFactory);
	}
	public <O> List<O> getList(Class<O> pType) {
		@SuppressWarnings("unchecked")
		final List<O> list = getInstance(List.class, pType.getName());
		if (list != null) {
			for (O o : list) {
				init(o);
			}
		}
		return list;
	}
	public abstract <O> O getInstance(Class<O> pType);
	public abstract <O> O getInstance(Class<O> pType, String pName);
	public <O> O requireInstance(Class<O> pType) throws NoSuchElementException {
		Objects.requireNonNull(pType, "Type");
		final O o = getInstance(pType);
		if (o == null) {
			throw new IllegalStateException("No such instance: type=" + pType.getName());
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
	public <O> List<O> requireList(Class<O> pType) throws NoSuchElementException {
		Objects.requireNonNull(pType, "Type");
		final List<O> list = getList(pType);
		if (list == null) {
			throw new IllegalStateException("No such list: type=" + pType.getName());
		}
		return list;
	}
	public abstract void init(Object pObject);
}
