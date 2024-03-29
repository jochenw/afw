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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;



/**
 * Abstract base implementation of {@link IProperty}.
 * @param <O> The property type.
 */
public abstract class AbstractProperty<O> implements IProperty<O>, IPropertyFactory.ChangeListener {
    private final List<ChangeListener<O>> listeners = new ArrayList<>();
    private final @NonNull String key;
    private String strValue;
    private final @Nullable O defaultValue;
    private @Nullable O value;

	AbstractProperty(@NonNull String pKey, @Nullable O pDefaultValue) {
        key = pKey;
        defaultValue = pDefaultValue;
    }
    
    @Override
    public String getKey() {
        return key;
    }

    @Override
    public synchronized String getStringValue() {
        return strValue;
    }

    @Override
    public synchronized O getValue() {
		@SuppressWarnings("null")
		final O o = value;
		return o;
    }

    @Override
    public O getDefaultValue() {
    	@SuppressWarnings("null")
		final O o = defaultValue;
    	return o;
    }

    @Override
    public synchronized void addListener(ChangeListener<O> pListener) {
        listeners.add(pListener);
    }

    @Override
    public synchronized void valueChanged(IPropertyFactory pFactory, Map<String, String> pOldValue,
            Map<String, String> pNewValue) {
        strValue = pNewValue.get(key);
        @SuppressWarnings("null")
		final O oldValue = value;
        value = convert(strValue);
        @SuppressWarnings("null")
		final O newValue = value;
        for (ChangeListener<O> listener : listeners) {
            listener.valueChanged(this, oldValue, newValue);
        }
    }

    /** Called internally to convert the properties string value into the actual type.
     * @param pStrValue The string value, that is being converted.
     * @return The converted value, if conversion has been done successfully.
     *   Otherwise the default value.
     */
    protected abstract O convert(String pStrValue);
}
