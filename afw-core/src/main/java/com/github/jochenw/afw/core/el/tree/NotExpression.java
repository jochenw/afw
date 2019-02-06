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
package com.github.jochenw.afw.core.el.tree;


/**
 * An expression for inverting the logical meaning of
 * another expression.
 */
public class NotExpression {
    private final Object value;

    /**
     * Creates a new instance.
     * @param pValue The value, which is being negated.
     */
    public NotExpression(Object pValue) {
        value = pValue;
    }

    /**
     * Returns the value, which is being inverted.
     * @return The value, which is being negated.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Creates a new instance.
     * @param pObject Evaluates the given value by negating it.
     * @return The evaluated value.
     * @throws IllegalArgumentException The value is a string, or a number, and cannot be negated.
     */
    public static Object valueOf(Object pObject) {
        if (pObject == null) {
            throw new IllegalArgumentException("Null is no valid argument for the not operator.");
        }
        if (pObject instanceof Boolean) {
            return ((Boolean) pObject).booleanValue() ? Boolean.FALSE : Boolean.TRUE;
        }
        if (pObject instanceof String) {
            throw new IllegalArgumentException("A string is no valid argument for the not operator.");
        }
        if (pObject instanceof Number) {
            throw new IllegalArgumentException("A number is no valid argument for the not operator.");
        }
        return new NotExpression(pObject);
    }
}
