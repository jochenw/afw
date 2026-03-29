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
package com.github.jochenw.afw.core.props;


/**
 * Interface of a property.
 * @param <O> The properties type.
 */
public interface IProperty<O> {
	/**
	 * Interface of a change listener, that is being notified, if the property changes.
	 * @param <T> The type of the property value.
	 */
    public interface ChangeListener<T> {
    	/** Called, if the property value has changed.
    	 * @param pProperty The property, that has changed it's value.
    	 * @param pOldValue The old property value. May be null, in case of the initial value
    	 *   assignment.
    	 * @param pNewValue The new property value.
    	 */
        void valueChanged(IProperty<T> pProperty, T pOldValue, T pNewValue);
    }
    /**
     * Returns the properties key.
     * @return The properties key.
     */
    String getKey();
    /**
     * Returns the properties string value.
     * @return The properties string value, before conversion into the actual value.
     */
    String getStringValue();
    /**
     * Returns the actual property value.
     * @return The actual property value.
     */
    O getValue();
    /**
     * Returns the default property value, which is being used, if conversion of the
     * string value into the actual value fails.
     * @return The default property value, which is being used, if conversion of the
     * string value into the actual value fails.
     */
    O getDefaultValue();
    /** Adds a change listener to the property.
     * @param pListener The change listener, that is being added.
     */
    void addListener(ChangeListener<O> pListener);
}
