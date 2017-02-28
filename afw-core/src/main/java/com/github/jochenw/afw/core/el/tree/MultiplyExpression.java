package com.github.jochenw.afw.core.el.tree;


/**
 * An expression for adding or subtracting items.
 */
public class MultiplyExpression {
    /**
     * The operators, which are handled by this expression.
     */
    public enum Op {
        /**
         * An operator for multiplying items.
         */
        MULTIPLY,
        /**
         * An operator for dividing items.
         */
        DIVIDE,
        /**
         * An operator for building items modulus.
         */
        MODULUS
    }

    private final UnaryExpression left, right;
    private final Op op;

    public MultiplyExpression(UnaryExpression pLeft, Op pOp, UnaryExpression pRight) {
    	left = pLeft;
    	op = pOp;
    	right = pRight;
    }

    public MultiplyExpression(UnaryExpression pLeft) {
    	this(pLeft, null, null);
    }

	public UnaryExpression getLeft() {
		return left;
	}

	public UnaryExpression getRight() {
		return right;
	}

	public Op getOp() {
		return op;
	}
}
