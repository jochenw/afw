package com.github.jochenw.afw.core.el.tree;

/**
 * An equality expression.
 */
public class EqualityExpression {
    /**
     * Enumeration of the equality operators.
     */
    public enum Op {
        /**
         * The "equal to" operator.
         */
        EQ,
        /**
         * The "not equal to" operator.
         */
        NE
    }
    private final RelationalExpression left, right;
    private final Op op;

    /**
     * Creates a new instance with the given items.
     */
    public EqualityExpression(RelationalExpression pLeft, Op pOp, RelationalExpression pRight) {
    	left = pLeft;
    	op = pOp;
    	right = pRight;
    }

    /**
     * Creates a new instance with the given items.
     */
    public EqualityExpression(RelationalExpression pLeft) {
    	this(pLeft, null, null);
    }

	public RelationalExpression getLeft() {
		return left;
	}

	public RelationalExpression getRight() {
		return right;
	}

	public Op getOp() {
		return op;
	}
}
