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
 * An expression for adding or subtracting items.
 */
public class MultiplyExpression implements Serializable {
	private static final long serialVersionUID = 1328072851579351454L;

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

    /** Creates a new instance with the given left hand side, the given operator, and
     * the given right hand side. 
     * @param pLeft The expressions left hand side.
     * @param pOp The expressions operator.
     * @param pRight The expressions right hand side.
     */
    public MultiplyExpression(UnaryExpression pLeft, Op pOp, UnaryExpression pRight) {
    	left = pLeft;
    	op = pOp;
    	right = pRight;
    }

    /** Creates a new instance with the given left hand side, no operator, and
     * no right hand side. 
     * @param pLeft The expressions left hand side.
     */
    public MultiplyExpression(UnaryExpression pLeft) {
    	this(pLeft, null, null);
    }

    /** Returns the expressions left hand side.
     * @return The expressions left hand side.
     */
	public UnaryExpression getLeft() {
		return left;
	}

    /** Returns the expressions right hand side.
     * @return The expressions right hand side.
     */
	public UnaryExpression getRight() {
		return right;
	}

    /** Returns the expressions operator.
     * @return The expressions operator.
     */
	public Op getOp() {
		return op;
	}
}
