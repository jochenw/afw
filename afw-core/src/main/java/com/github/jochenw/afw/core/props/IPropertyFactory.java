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

import java.net.URL;
import java.nio.file.Path;
import java.util.Map;


/**
 * Interface of a property factory.
 */
public interface IPropertyFactory {
	/** Interface of a change listener, that will be invoked, if the
	 * property values have changed.
	 */
    public interface ChangeListener {
    	/**
    	 * Called, if the property factory has reloaded it's values.
    	 * @param pFactory The property factory, that changed it's values.
    	 * @param oldValues The old value set.
    	 * @param newValues The new value set.
    	 */
        void valueChanged(IPropertyFactory pFactory, Map<String,String> oldValues, Map<String,String> newValues);
    }
    /** Adds a change listener.
     * @param pListener The change listener, that is being added.
     */
    void addListener(ChangeListener pListener);
    /**
     * Gets a map, which contains all the properties.
     * @return A map, which contains all the properties.
     */
    Map<String,String> getPropertyMap();
    /** Returns a string property object with the given key,
     * and the default value null.
     * @param pKey The property objects key.
     * @return A property object with the given key,
     *   and the default value null.
     */
    IProperty<String> getProperty(String pKey);
    /** Returns a string property object with the given key,
     * and the given default value.
     * @param pKey The property objects key.
     * @param pDefaultValue The default value, which is being used, if the property is not available.
     * @return A property object with the given key,
     *   and the given default value.
     */
    IProperty<String> getProperty(String pKey, String pDefaultValue);
    /** Returns a string property object with the given key,
     * and the given default value.
     * @param pKey The property objects key.
     * @param pDefaultValue The default value, which is being used, if the property is not available.
     * @param pListener A change listener, which is being attached to the property object.
     * @return A property object with the given key,
     *   and the given default value.
     */
    IProperty<String> getProperty(String pKey, String pDefaultValue, IProperty.ChangeListener<String> pListener);
    /** Returns an integer property object with the given key,
     * and the given default value.
     * @param pKey The property objects key.
     * @param pDefaultValue The property objects default value.
     * @return A property object with the given key,
     *   and the given default value.
     */
    IIntProperty getIntProperty(String pKey, int pDefaultValue);
    /** Returns an integer property object with the given key,
     * and the given default value.
     * @param pKey The property objects key.
     * @param pDefaultValue The property objects default value.
     * @param pListener A change listener, which is being attached to the property object.
     * @return A property object with the given key,
     *   and the given default value.
     */
    IIntProperty getIntProperty(String pKey, int pDefaultValue, IProperty.ChangeListener<Integer> pListener);
    /** Returns a long property object with the given key,
     * and the given default value.
     * @param pKey The property objects key.
     * @param pDefaultValue The property objects default value.
     * @return A property object with the given key,
     *   and the given default value.
     */
    ILongProperty getLongProperty(String pKey, long pDefaultValue);
    /** Returns a long property object with the given key,
     * and the given default value.
     * @param pKey The property objects key.
     * @param pDefaultValue The property objects default value.
     * @param pListener A change listener, which is being attached to the property object.
     * @return A property object with the given key,
     *   and the given default value.
     */
    ILongProperty getLongProperty(String pKey, long pDefaultValue, IProperty.ChangeListener<Long> pListener);
    /** Returns a boolean property object with the given key,
     * and the given default value.
     * @param pKey The property objects key.
     * @param pDefaultValue The property objects default value.
     * @return A property object with the given key,
     *   and the given default value.
     */
    IBooleanProperty getBooleanProperty(String pKey, boolean pDefaultValue);
    /** Returns a boolean property object with the given key,
     * and the given default value.
     * @param pKey The property objects key.
     * @param pDefaultValue The property objects default value.
     * @param pListener A change listener, which is being attached to the property object.
     * @return A property object with the given key,
     *   and the given default value.
     */
    IBooleanProperty getBooleanProperty(String pKey, boolean pDefaultValue, IProperty.ChangeListener<Boolean> pListener);
    /** Returns a boolean property object with the given key,
     * and the default value false.
     * @param pKey The property objects key.
     * @return A property object with the given key,
     *   and the given default value.
     */
    IBooleanProperty getBooleanProperty(String pKey);
    /** Returns a boolean property object with the given key,
     * and the default value false.
     * @param pKey The property objects key.
     * @param pListener A change listener, which is being attached to the property object.
     * @return A property object with the given key,
     *   and the given default value.
     */
    IBooleanProperty getBooleanProperty(String pKey, IProperty.ChangeListener<Boolean> pListener);
    /** Returns an URL property object with the given key, no default value,
     * and no change listener.
     * @param pKey The property objects key.
     * @return An URL property object with the given key, no default value,
     * and no change listener.
     */
    IURLProperty getUrlProperty(String pKey);
    /** Returns an URL property object with the given key, the given default value,
     * and no change listener.
     * @param pKey The property objects key.
     * @param pDefaultValue The property objects default value. May be null, in which case
     *   the returned property object has no default value.
     * @return An URL property object with the given key, no default value,
     *   and no change listener.
     */
    IURLProperty getUrlProperty(String pKey, URL pDefaultValue);
    /** Returns an URL property object with the given key, the given default value,
     * and the given change listener.
     * @param pKey The property objects key.
     * @param pDefaultValue The property objects default value. May be null, in which case
     *   the returned property object has no default value.
     * @param pListener A change listener, which is being attached to the property object.
     *   May be null, in which case the property object doesn't have a change
     *   listener.
     * @return An URL property object with the given key, no default value,
     *   and no change listener.
     */
    IURLProperty getUrlProperty(String pKey, URL pDefaultValue, IProperty.ChangeListener<URL> pListener);
    /** Returns a path property object with the given key, no default value,
     * and no change listener.
     * @param pKey The property objects key.
     * @return A path property object with the given key, no default value,
     * and no change listener.
     */
    IPathProperty getPathProperty(String pKey);
    /** Returns a path property object with the given key, the given default value,
     * and no change listener.
     * @param pKey The property objects key.
     * @param pDefaultValue The property objects default value. May be null, in which case
     *   the returned property object has no default value.
     * @return A path property object with the given key, no default value,
     *   and no change listener.
     */
    IPathProperty getPathProperty(String pKey, Path pDefaultValue);
    /** Returns a path property object with the given key, the given default value,
     * and the given change listener.
     * @param pKey The property objects key.
     * @param pDefaultValue The property objects default value. May be null, in which case
     *   the returned property object has no default value.
     * @param pListener A change listener, which is being attached to the property object.
     *   May be null, in which case the property object doesn't have a change
     *   listener.
     * @return A path property object with the given key, no default value,
     *   and no change listener.
     */
    IPathProperty getPathProperty(String pKey, Path pDefaultValue, IProperty.ChangeListener<Path> pListener);
    
    /** Returns the property value with the given key, or null.
     * @return The property value with the given key, or null.
     * @param pKey Key of the property, that's being looked up.
     */
	String getPropertyValue(String pKey);
}
