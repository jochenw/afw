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

import java.util.NoSuchElementException;
import java.util.Objects;


/** Instances of this class are used by the {@link ElEvaluator} for
 * resolving properties in the model.
 */
public abstract class PropertyResolver {
	/** Creates a new instance.
	 */
	public PropertyResolver() {}

	/**
	 * Called to resolve a nullable property in the model.
	 * @param pObject The model, which is being applied to the EL expression.
	 * @param pProperty The (possibly complex) property, like
	 *   "foo", "bar", or "foo.bar".
	 * @return The evaluated value. May be null, if the property wasn't found
	 *   to be applicable, or if applying the model resulted in a null value.
	 */
	public abstract Object getValue(Object pObject, String pProperty);
	/**
	 * Called to resolve a non-null property in the model.
	 * @param pObject The model, which is being applied to the EL expression.
	 * @param pProperty The (possibly complex) property, like
	 *   "foo", "bar", or "foo.bar".
	 * @return The evaluated value, if the property was found
	 *   to be applicable, and if applying the model resulted in a non-null
	 *   value. Otherwise, a {@link NoSuchElementException} is thrown.
	 * @throws NoSuchElementException Evaluating the property resulted in
	 *   a null value.
	 */
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
