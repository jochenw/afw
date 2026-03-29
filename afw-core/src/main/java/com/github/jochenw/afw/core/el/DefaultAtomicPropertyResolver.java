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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Reflection;

/**
 * Default implementation of {@link PropertyResolver}, which supports
 * the following strategies of reading a property:
 * <ol>
 *   <li>Using {@link Map#get(Object)} on Map objects.</li>
 *   <li>Finding, and using corresponding public getters.
 * </ol>
 * Note, that this property resolver is only suitable for
 * simple properties (like "foo", or "bar", in objects of depth 1) not for complex
 * properties like "foo.bar" in nested objects.
 */
public class DefaultAtomicPropertyResolver extends PropertyResolver {
	/** Creates a new instance.
	 */
	public DefaultAtomicPropertyResolver() {}

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
