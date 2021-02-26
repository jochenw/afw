/*
 * Copyright 2018 Jochen Wiedmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jochenw.afw.core.el;

import java.util.Objects;

/**
 * Default implementation of {@link PropertyResolver}, which supports
 * access to complex properties like "foo.bar" in nested objects,
 * using a so-called atomic property resolver.
 */
public class DefaultPropertyResolver extends PropertyResolver {
	private final PropertyResolver atomicPropertyResolver;

	/**
	 * Creates a new instance with the default atomic property
	 * resolver.
	 */
	public DefaultPropertyResolver() {
		this(new DefaultAtomicPropertyResolver());
	}

	/**
	 * Creates a new instance with the given atomic property
	 * resolver.
	 * @param pAtomicPropertyResolver The atomic property resolver to use.
	 */
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
