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

import java.util.List;


/**
 * A value, which must be gathered from the model.
 */
public class ComplexValue {
    /**
     * A values array suffix.
     */
    public static class ArraySuffix {
        private final Object value;

        /**
         * Creates a new instance.
         * @param pIndex The array index.
         */
        public ArraySuffix(Object pIndex) {
            value = pIndex;
        }

        /**
         * Returns the index value.
         * @return The index value.
         */
        public Object getValue() {
            return value;
        }
    }

    /**
     * A values property suffix.
     */
    public static class PropertySuffix {
        private final String value;

        /**
         * Creates a new instance.
         * @param pKey The property key.
         */
        public PropertySuffix(String pKey) {
            value = pKey;
        }

        /**
         * Returns the property key.
         * @return The property key.
         */
        public String getValue() {
            return value;
        }
    }

    private final List<Object> items;

    /**
     * Creates a new instance with the given items.
     * @param pItems The items, which are building the complex value.
     */
    public ComplexValue(List<Object> pItems) {
        items = pItems;
    }

    /**
     * Returns the values items.
     * @return The items, which are building the complex value.
     */
    public List<Object> getItems() {
        return items;
    }
}
