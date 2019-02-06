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
     * @param pValue The expression value.
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
     * @return The expression value.
     */
    public ValueExpression getValue() {
        return value;
    }

	public Op getOp() {
		return op;
	}
}
