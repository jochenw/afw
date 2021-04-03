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


/** Implemantation of a boolean {@link IProperty}.
 */
public class BooleanProperty extends AbstractProperty<Boolean> implements IBooleanProperty {
	/**
	 * Creates a new instance with the given property key, and default value.
	 * @param pKey The property key.
	 * @param pDefaultValue The default value.
	 */
    public BooleanProperty(String pKey, Boolean pDefaultValue) {
        super(pKey, pDefaultValue);
    }

    @Override
    protected Boolean convert(String pStrValue) {
        if (pStrValue == null) {
            return getDefaultValue();
        }
        final boolean b = Boolean.parseBoolean(pStrValue);
        return Boolean.valueOf(b);
    }

    @Override
    public boolean getBooleanValue() {
        return getValue().booleanValue();
    }

    @Override
    public boolean getBooleanDefaultValue() {
        return getDefaultValue().booleanValue();
    }

}
