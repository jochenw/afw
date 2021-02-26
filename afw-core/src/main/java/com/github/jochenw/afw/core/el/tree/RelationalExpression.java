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
     * @param pLeft The left expression, which is being compared against the right expression.
     * @param pOp The operator to apply
     * @param pRight The right expression, which is being compared against the left expression.
     */
    public RelationalExpression(AddExpression pLeft, Op pOp, AddExpression pRight) {
    	left = pLeft;
    	op = pOp;
    	right = pRight;
    }

    /**
     * Creates a new instance with the given items.
     * @param pLeft The unary expression item.
     */
    public RelationalExpression(AddExpression pLeft) {
    	this(pLeft, null, null);
    }

    /** Returns the expressions left hand side.
     * @return The expressions left hand side.
     */
	public AddExpression getLeft() {
		return left;
	}

    /** Returns the expressions right hand side.
     * @return The expressions right hand side.
     */
	public AddExpression getRight() {
		return right;
	}

    /** Returns the expressions operator.
     * @return The expressions operator.
     */
	public Op getOp() {
		return op;
	}
}
