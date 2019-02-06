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
