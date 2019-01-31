/**
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

import java.util.Map;

public interface IPropertyFactory {
    public interface ChangeListener {
        void valueChanged(IPropertyFactory pFactory, Map<String,String> oldValues, Map<String,String> newValues);
    }
    void addListener(ChangeListener pListener);
    Map<String,String> getPropertyMap();
    IProperty<String> getProperty(String pKey);
    IProperty<String> getProperty(String pKey, String pDefaultValue);
    IProperty<String> getProperty(String pKey, String pDefaultValue, IProperty.ChangeListener<String> pListener);
    IIntProperty getIntProperty(String pKey, int pDefaultValue);
    IIntProperty getIntProperty(String pKey, int pDefaultValue, IProperty.ChangeListener<Integer> pListener);
    ILongProperty getLongProperty(String pKey, long pDefaultValue);
    ILongProperty getLongProperty(String pKey, long pDefaultValue, IProperty.ChangeListener<Long> pListener);
    IBooleanProperty getBooleanProperty(String pKey, boolean pDefaultValue);
    IBooleanProperty getBooleanProperty(String pKey, boolean pDefaultValue, IProperty.ChangeListener<Boolean> pListener);
    IBooleanProperty getBooleanProperty(String pKey);
    IBooleanProperty getBooleanProperty(String pKey, IProperty.ChangeListener<Boolean> pListener);
	String getPropertyValue(String pKey);
}
