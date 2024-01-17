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

import org.jspecify.annotations.NonNull;

/**
 * Default implementation of {@link ILongProperty}.
 */
public class LongProperty extends AbstractProperty<@NonNull Long> implements ILongProperty {
	/**
	 * Creates a new instance with the given property key, and default value.
	 * @param pKey The properties key.
	 * @param pDefaultValue The properties default value.
	 */
    public LongProperty(@NonNull String pKey, @NonNull Long pDefaultValue) {
        super(pKey, pDefaultValue);
    }

    @Override
    protected @NonNull Long convert(String pStrValue) {
        if (pStrValue == null) {
            return getDefaultValue();
        }
        try {
            final long l = Long.parseLong(pStrValue);
            @SuppressWarnings("null")
			final @NonNull Long v = Long.valueOf(l);
            return v;
        } catch (NumberFormatException e) {
            return getDefaultValue();
        }
    }

    @Override
    public long getLongValue() {
        return getValue().longValue();
    }

    @Override
    public long getLongDefaultValue() {
        return getDefaultValue().longValue();
    }

}
