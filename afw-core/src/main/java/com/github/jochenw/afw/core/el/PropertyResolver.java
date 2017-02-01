package com.github.jochenw.afw.core.el;

import java.util.NoSuchElementException;
import java.util.Objects;

public abstract class PropertyResolver {
	public abstract Object getValue(Object pObject, String pProperty);
	public Object requireValue(Object pObject, String pProperty) throws NoSuchElementException {
		Objects.requireNonNull(pObject, "Object");
		Objects.requireNonNull(pProperty, "Property");
		final Object value = getValue(pObject, pProperty);
		if (value == null) {
			throw new NoSuchElementException("Object of type " + pObject.getClass().getName()
					+ " returned null for property " + pProperty);
		}
		return value;
	}
}
