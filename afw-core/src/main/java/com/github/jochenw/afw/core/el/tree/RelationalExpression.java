package com.github.jochenw.afw.core.el.tree;


/**
 * A relational expression.
 */
public class RelationalExpression {
    /**
     * Enumeration of the relational operators.
     */
    public enum Op {
        /**
         * The "greater than" operator.
         */
        GT,
        /**
         * The "greater than or equal to" operator.
         */
        GE,
        /**
         * The "lower than" operator.
         */
        LT,
        /**
         * The "lower than or equal to" operator.
         */
        LE
    }
    

    private final AddExpression left, right;
    private final Op op;

    /**
     * Creates a new instance with the given items.
     */
    public RelationalExpression(AddExpression pLeft, Op pOp, AddExpression pRight) {
    	left = pLeft;
    	op = pOp;
    	right = pRight;
    }

    /**
     * Creates a new instance with the given items.
     */
    public RelationalExpression(AddExpression pLeft) {
    	this(pLeft, null, null);
    }

	public AddExpression getLeft() {
		return left;
	}

	public AddExpression getRight() {
		return right;
	}

	public Op getOp() {
		return op;
	}
}
