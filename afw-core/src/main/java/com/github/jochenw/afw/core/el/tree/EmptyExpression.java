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
