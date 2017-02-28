package com.github.jochenw.afw.core.el.tree;

import java.util.Collections;
import java.util.List;


/**
 * An expression for adding or subtracting items.
 */
public class AddExpression {
    /**
     * The operators, which are handled by this expression.
     */
    public enum Op {
        /**
         * An operator for adding items.
         */
        PLUS,
        /**
         * An operator for subtracting items.
         */
        MINUS
    }

    private final List<Object> objects;

    /**
     * Creates a new instance with the given items.
     */
    public AddExpression(List<Object> pItems) {
        objects = pItems;
    }

    /**
     * Creates a new instance with the given item.
     */
    public AddExpression(Object pItem) {
    	this(Collections.singletonList(pItem));
    }

    /**
     * Returns the expressions items.
     */
    public List<Object> getObjects() {
        return objects;
    }
}
