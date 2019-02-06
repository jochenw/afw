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

public class ElExpression implements Serializable {
	private static final long serialVersionUID = 8384812496202649423L;
	private final OrExpression orExpression;
	private final int numPlaceholders;

	public ElExpression(OrExpression pOrExpression) {
		this(pOrExpression, 0);
	}

	public ElExpression(OrExpression pOrExpression, int pNumPlaceholders) {
		orExpression = pOrExpression;
		numPlaceholders = pNumPlaceholders;
	}

	public OrExpression getOrExpression() {
		return orExpression;
	}

	public int getNumberOfPlaceholders() {
		return numPlaceholders;
	}
}
