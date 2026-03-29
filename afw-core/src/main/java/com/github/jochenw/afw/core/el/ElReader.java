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
package com.github.jochenw.afw.core.el;

import java.io.StringReader;

import com.github.jochenw.afw.core.el.jcc.ELParser;
import com.github.jochenw.afw.core.el.tree.ElExpression;
import com.github.jochenw.afw.core.util.Exceptions;


/** A parser for EL expressions.
 */
public class ElReader {
	/** Creates a new instance.
	 */
	public ElReader() {}

	/** Parses the given string as an EL expression.
	 * @param pExpression The EL expression string, which is being parsed.
	 * @return The parsed EL expression, which may then be evaluated using
	 * {@link ElEvaluator#evaluate(ElExpression, Object)}, or
	 * {@link ElEvaluator#evaluate(ElExpression, Object, java.util.List)}.
	 */
	public ElExpression parse(String pExpression) {
		final ELParser parser = new ELParser(new StringReader(pExpression));
		try {
			return (ElExpression) parser.ElExpression();
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}
}
