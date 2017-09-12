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
         */
        public ArraySuffix(Object pValue) {
            value = pValue;
        }

        /**
         * Returns the value.
         */
        public Object getValue() {
            return value;
        }
    }

    /**
     * A values array suffix.
     */
    public static class PropertySuffix {
        private final String value;

        /**
         * Creates a new instance.
         */
        public PropertySuffix(String pValue) {
            value = pValue;
        }

        /**
         * Returns the value.
         */
        public String getValue() {
            return value;
        }
    }

    private final List<Object> items;

    /**
     * Creates a new instance with the given items.
     */
    public ComplexValue(List<Object> pItems) {
        items = pItems;
    }

    /**
     * Returns the values items.
     */
    public List<Object> getItems() {
        return items;
    }
}
