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


/** An object, which represents a parsed EL expression as an object
 * tree.
 */
public class ElExpression implements Serializable {
	private static final long serialVersionUID = 8384812496202649423L;
	/** The single {@link OrExpression}.
	 */
	private final OrExpression orExpression;
	/** The number of placeholders.
	 */
	private final int numPlaceholders;

	/**
	 * Creates a new instance, which consists of a single {@link OrExpression},
	 * and has no placeholders.
	 * @param pOrExpression The single {@link OrExpression}.
	 */
	public ElExpression(OrExpression pOrExpression) {
		this(pOrExpression, 0);
	}

	/**
	 * Creates a new instance, which consists of the given {@link OrExpression},
	 * and has the given number of placeholders.
	 * @param pOrExpression The single {@link OrExpression}.
	 * @param pNumPlaceholders The number of placeholders.
	 */
	public ElExpression(OrExpression pOrExpression, int pNumPlaceholders) {
		orExpression = pOrExpression;
		numPlaceholders = pNumPlaceholders;
	}

	/** Returns the single {@link OrExpression}.
	 * @return The single {@link OrExpression}.
	 */
	public OrExpression getOrExpression() {
		return orExpression;
	}

	/** Returns the number of placeholders.
	 * @return The number of placeholders.
	 */
	public int getNumberOfPlaceholders() {
		return numPlaceholders;
	}
}
