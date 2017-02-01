package com.github.jochenw.afw.core.el.tree;


/**
 * An expression for inverting the logical meaning of
 * another expression.
 */
public class UnaryExpression {
	public static enum Op {
		NOT, MINUS, EMPTY
	}
	
    private final ValueExpression value;
    private final Op op;

    /**
     * Creates a new instance.
     */
    public UnaryExpression(ValueExpression pValue) {
    	this(pValue, null);
    }

    public UnaryExpression(ValueExpression pValue, Op pOp) {
    	value = pValue;
    	op = pOp;
    }
    
    /**
     * Returns the value, which is being inverted.
     */
    public ValueExpression getValue() {
        return value;
    }

	public Op getOp() {
		return op;
	}
}
