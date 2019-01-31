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
package com.github.jochenw.afw.core.el.tree;


/**
 * An expression for checking, whether a string is null or empty.
 */
public class EmptyExpression {
    private Object value;

    /**
     * Creates a new instance with the given value.
     */
    public EmptyExpression(Object pValue) {
        value = pValue;
    }

    /**
     * Returns the value to check.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Creates a new instance with the given value.
     */
    public static Object valueOf(Object pValue) {
        if (pValue == null) {
            return Boolean.TRUE;
        }
        if (pValue instanceof String) {
            return Boolean.valueOf(((String) pValue).length() == 0);
        }
        if (pValue instanceof Number) {
            throw new IllegalArgumentException("A number is no valid argument for the empty operator.");
        }
        if (pValue instanceof Boolean) {
            throw new IllegalArgumentException("A boolean is no valid argument for the empty operator.");
        }
        return new EmptyExpression(pValue);
    }
}
