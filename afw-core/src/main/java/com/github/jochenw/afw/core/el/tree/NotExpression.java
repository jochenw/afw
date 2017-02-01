package com.github.jochenw.afw.core.el.tree;


/**
 * An expression for inverting the logical meaning of
 * another expression.
 */
public class NotExpression {
    private final Object value;

    /**
     * Creates a new instance.
     */
    public NotExpression(Object pValue) {
        value = pValue;
    }

    /**
     * Returns the value, which is being inverted.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Creates a new instance.
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
