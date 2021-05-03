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
import java.util.Collections;
import java.util.List;


/**
 * An "and" expression.
 */
public class AndExpression implements Serializable {
	private static final long serialVersionUID = 4979351601788101217L;
	private final List<EqualityExpression> items;

    /**
     * Creates a new instance with the given items.
     * @param pItems The expressions item list. The expressions result will be built by applying a boolean AND on these items.
     */
    public AndExpression(List<EqualityExpression> pItems) {
        items = pItems;
    }

    /**
     * Creates a new instance with a single item.
     * @param pExpr The single item nn the expressions item list. Th expressions result will be built by evaluating this
     *   item, and returning that evalutions result.
     */
    public AndExpression(EqualityExpression pExpr) {
    	this(Collections.singletonList(pExpr));
	}

	/**
     * Returns the expressions items.
     * @return The expressions item list. The expressions result will be built by applying a boolean AND on these items.
     */
    public List<EqualityExpression> getEqualityExpressions() {
        return items;
    }
}
