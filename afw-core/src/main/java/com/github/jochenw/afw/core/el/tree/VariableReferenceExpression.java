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
 * An expression, which is being evaluated by referencing a property in the data model.
 */
public class VariableReferenceExpression {
	private final String var;

	/**
	 * An expression, which is being evaluated by referencing the given
	 * property in the data model.
	 * @param pVar The property reference, possibly complex, like "foo.bar.baz".
	 */
	public VariableReferenceExpression(String pVar) {
		var = pVar;
	}
	
	/**
	 * Returns the property reference, possibly complex, like "foo.bar.baz".
	 * @return The property reference, possibly complex, like "foo.bar.baz".
	 */
	public String getVar() {
		return var;
	}
}
