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
 * An expression for inverting the logical meaning of
 * another expression.
 */
public class UnaryExpression implements Serializable {
	private static final long serialVersionUID = 7351814301502101436L;

	/** The unary expressions operator.
	 */
	public static enum Op {
		/** The "!" (not) operator.
		 */
		NOT,
		/** The unary "-" (minus) operator.
		 */
		MINUS,
		/** The "empty" operator.
		 */
		EMPTY
	}
	
    private final ValueExpression value;
    private final Op op;

    /**
     * Creates a new instance with no operator.
     * @param pValue The expression value.
     */
    public UnaryExpression(ValueExpression pValue) {
    	this(pValue, null);
    }

    /**
     * Creates a new instance with the given operator.
     * @param pValue The expression value.
     * @param pOp The expressions operator.
     */
    public UnaryExpression(ValueExpression pValue, Op pOp) {
    	value = pValue;
    	op = pOp;
    }
    
    /**
     * Returns the value, which is being inverted.
     * @return The expression value.
     */
    public ValueExpression getValue() {
        return value;
    }

    /**
     * Returns the operator.
     * @return The operator.
     */
	public Op getOp() {
		return op;
	}
}
