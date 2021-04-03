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

/** Default implementation of a string property.
 */
public class StringProperty extends AbstractProperty<String> {
	/**
	 * Creates a new instance with the given property key, and default value.
	 * @param pKey The properties key.
	 * @param pDefaultValue The properties default value. (May be null.)
	 */
    public StringProperty(String pKey, String pDefaultValue) {
        super(pKey, pDefaultValue);
    }

    @Override
    protected String convert(String pStrValue) {
        return pStrValue;
    }

}
