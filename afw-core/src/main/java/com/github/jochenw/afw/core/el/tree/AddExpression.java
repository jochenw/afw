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
 * An expression for adding or subtracting items.
 */
public class AddExpression implements Serializable {
	private static final long serialVersionUID = -8535863277489942132L;

	/**
     * The operators, which are handled by this expression.
     */
    public enum Op {
        /**
         * An operator for adding items.
         */
        PLUS,
        /**
         * An operator for subtracting items.
         */
        MINUS
    }

    /** The expressions atoms.
     */
    private final List<Serializable> objects;

    /**
     * Creates a new instance with the given items.
     * @param pItems The expressions item list. The expressions result will be built by adding these items.
     */
    public AddExpression(List<Serializable> pItems) {
        objects = pItems;
    }

    /**
     * Creates a new instance with the given item.
     * @param pItem The expressions item. For a single item, this will also be the result.
     */
    public AddExpression(Serializable pItem) {
    	this(Collections.singletonList(pItem));
    }

    /**
     * Returns the expressions items.
     * @return The expressions result item.
     */
    public List<Serializable> getObjects() {
        return objects;
    }
}
