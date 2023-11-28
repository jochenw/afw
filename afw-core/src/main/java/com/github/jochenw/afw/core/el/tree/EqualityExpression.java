/*
 * Copyright 2018 Jochen Wiedmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jochenw.afw.core.el.tree;

import java.io.Serializable;

/**
 * An equality expression.
 */
public class EqualityExpression implements Serializable {
	private static final long serialVersionUID = -441090213494783920L;

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
    /** The left hand side, and the right hand side of the expression.
     * (The terms, that are being compared.)
     */
    private final RelationalExpression left, right;
    /** The operand.
     */
    private final Op op;

    /**
     * Creates a new instance with the given items.
     * @param pLeft The left operand, which is being tested for equality with the right operand.
     * @param pOp Either of {@link Op#EQ}, or {@link Op#NE}, depending on whether to test for
     *   "equals", or "not equals".
     * @param pRight The right operand, which is being tested for equality with the left operand.
     */
    public EqualityExpression(RelationalExpression pLeft, Op pOp, RelationalExpression pRight) {
    	left = pLeft;
    	op = pOp;
    	right = pRight;
    }

    /**
     * Creates a new instance with the given items.
     * @param pLeft The unary operand.
     */
    public EqualityExpression(RelationalExpression pLeft) {
    	this(pLeft, null, null);
    }

    /**
     * Returns the left hand side of the equality expression.
     * @return The left hand side of the equality expression.
     */
	public RelationalExpression getLeft() {
		return left;
	}

    /**
     * Returns the right hand side of the equality expression.
     * @return The right hand side of the equality expression.
     */
	public RelationalExpression getRight() {
		return right;
	}

	/** Returns the operator, which compares the left hand side, and the right hand side.
	 * @return The operator, which compares the left hand side, and the right hand side.
	 */
	public Op getOp() {
		return op;
	}
}
