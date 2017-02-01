package com.github.jochenw.afw.core.el.tree;

import java.util.Collections;
import java.util.List;


/**
 * An "and" expression.
 */
public class AndExpression {
    private final List<EqualityExpression> items;

    /**
     * Creates a new instance with the given items.
     */
    public AndExpression(List<EqualityExpression> pItems) {
        items = pItems;
    }

    public AndExpression(EqualityExpression pExpr) {
    	this(Collections.singletonList(pExpr));
	}

	/**
     * Returns the expressions items.
     */
    public List<EqualityExpression> getEqualityExpressions() {
        return items;
    }
}
