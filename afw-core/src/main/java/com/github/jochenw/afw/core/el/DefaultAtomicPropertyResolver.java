package com.github.jochenw.afw.core.el;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Reflection;

public class DefaultAtomicPropertyResolver extends PropertyResolver {
	@Override
	public Object getValue(Object pObject, String pProperty) {
		Objects.requireNonNull(pObject, "Object");
		Objects.requireNonNull(pProperty, "Property");
		if (pObject instanceof Map) {
			@SuppressWarnings("unchecked")
			final Map<Object,Object> map = (Map<Object,Object>) pObject;
			return map.get(pProperty);
		} else {
			final Method getter = Reflection.getPublicGetter(pObject.getClass(), pProperty);
			if (getter == null) {
				return null;
			} else {
				try {
					return getter.invoke(pObject);
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
			}
		}
	}
}
