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


/** Default implementation of an {@link IIntProperty integer property}.
 */
public class IntProperty extends AbstractProperty<Integer> implements IIntProperty {
	/**
	 * Creates a new instance with the given property key, and default value.
	 * @param pKey The properties key.
	 * @param pDefaultValue The properties default value.
	 */
    public IntProperty(String pKey, Integer pDefaultValue) {
        super(pKey, pDefaultValue);
    }

    @Override
    protected Integer convert(String pStrValue) {
        if (pStrValue == null) {
            return getDefaultValue();
        }
        try {
            final int i = Integer.parseInt(pStrValue);
            return Integer.valueOf(i);
        } catch (NumberFormatException e) {
            return getDefaultValue();
        }
    }

    @Override
    public int getIntValue() {
        return getValue().intValue();
    }

    @Override
    public int getIntDefaultValue() {
        return getDefaultValue().intValue();
    }

}
