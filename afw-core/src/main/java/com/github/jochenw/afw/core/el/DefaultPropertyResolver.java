package com.github.jochenw.afw.core.el;

import java.util.Objects;

public class DefaultPropertyResolver extends PropertyResolver {
	private final PropertyResolver atomicPropertyResolver;

	public DefaultPropertyResolver() {
		this(new DefaultAtomicPropertyResolver());
	}

	public DefaultPropertyResolver(PropertyResolver pAtomicPropertyResolver) {
		atomicPropertyResolver = pAtomicPropertyResolver;
	}

	@Override
	public Object getValue(Object pObject, String pProperty) {
		Objects.requireNonNull(pObject, "Object");
		Objects.requireNonNull(pProperty, "Property");
		Object object = pObject;
		String property = pProperty;
		String context = "";
		for (;;) {
			int offset = property.indexOf('.');
			if (offset == -1) {
				return atomicPropertyResolver.getValue(object, property);
			} else {
				final String topMostProperty = property.substring(0, offset);
				final String secondaryProperty = property.substring(offset+1);
				if (context.length() == 0) {
					context = topMostProperty;
				} else {
					context = context + "." + topMostProperty;
				}
				object = atomicPropertyResolver.requireValue(object, topMostProperty);
				if (secondaryProperty.length() == 0) {
					return object;
				} else if (object == null) {
					throw new NullPointerException("Intermediate value null for property " + context
							+ " while resolving " + pProperty);
				} else {
					property = secondaryProperty;
				}
			}
		}
	}
}