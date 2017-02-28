package com.github.jochenw.afw.core.el.tree;

import java.util.Collections;
import java.util.List;


/**
 * An "or" expression.
 */
public class OrExpression {
    private final List<AndExpression> items;

    /**
     * Creates a new instance with the given items.
     */
    public OrExpression(List<AndExpression> pItems) {
        items = pItems;
    }

    /**
     * Creates a new instance with the given items.
     */
    public OrExpression(AndExpression pItem) {
        items = Collections.singletonList(pItem);
    }

    /**
     * Returns the expressions items.
     */
    public List<AndExpression> getAndExpressions() {
        return items;
    }
}
