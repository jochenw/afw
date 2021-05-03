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
 * An "or" expression.
 */
public class OrExpression implements Serializable {
	private static final long serialVersionUID = 8986714670332919378L;
	private final List<AndExpression> items;

    /**
     * Creates a new instance with the given items.
     * @param pItems The expression list.
     */
    public OrExpression(List<AndExpression> pItems) {
        items = pItems;
    }

    /**
     * Creates a new instance with the given items.
     * @param pItem The single expression item.
     */
    public OrExpression(AndExpression pItem) {
        items = Collections.singletonList(pItem);
    }

    /**
     * Returns the expressions items.
     * @return The expression list.
     */
    public List<AndExpression> getAndExpressions() {
        return items;
    }
}
