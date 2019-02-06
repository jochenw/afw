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

public class ValueExpression {
	private final ElExpression elExpression;
	private final Boolean booleanValue;
	private final Long longValue;
	private final Double doubleValue;
	private final String stringValue;
	private final VariableReferenceExpression variableReference;
	private final Integer placeholderIndex;

	public ValueExpression(Long pLongValue) {
		longValue = pLongValue;
		booleanValue = null;
		doubleValue = null;
		stringValue = null;
		variableReference = null;
		placeholderIndex = null;
		elExpression = null;
	}

	public ValueExpression(Boolean pBooleanValue) {
		longValue = null;
		booleanValue = pBooleanValue;
		doubleValue = null;
		stringValue = null;
		variableReference = null;
		placeholderIndex = null;
		elExpression = null;
	}

	public ValueExpression(Double pDoubleValue) {
		longValue = null;
		booleanValue = null;
		doubleValue = pDoubleValue;
		stringValue = null;
		variableReference = null;
		placeholderIndex = null;
		elExpression = null;
	}

	public ValueExpression(String pStringValue) {
		longValue = null;
		booleanValue = null;
		doubleValue = null;
		stringValue = pStringValue;
		variableReference = null;
		placeholderIndex = null;
		elExpression = null;
	}

	public ValueExpression(VariableReferenceExpression pVariableReference) {
		longValue = null;
		booleanValue = null;
		doubleValue = null;
		stringValue = null;
		variableReference = pVariableReference;
		placeholderIndex = null;
		elExpression = null;
	}

	public ValueExpression(Integer pPlaceholderIndex) {
		longValue = null;
		booleanValue = null;
		doubleValue = null;
		stringValue = null;
		variableReference = null;
		placeholderIndex = pPlaceholderIndex;
		elExpression = null;
	}

	public ValueExpression() {
		longValue = null;
		booleanValue = null;
		doubleValue = null;
		stringValue = null;
		variableReference = null;
		placeholderIndex = null;
		elExpression = null;
	}

	public ValueExpression(ElExpression pElExpression) {
		longValue = null;
		booleanValue = null;
		doubleValue = null;
		stringValue = null;
		variableReference = null;
		placeholderIndex = null;
		elExpression = pElExpression;
	}

	public Boolean getBooleanValue() {
		return booleanValue;
	}

	public Long getLongValue() {
		return longValue;
	}

	public Double getDoubleValue() {
		return doubleValue;
	}

	public String getStringValue() {
		return stringValue;
	}

	public VariableReferenceExpression getVariableReference() {
		return variableReference;
	}

	public Integer getPlaceholderIndex() {
		return placeholderIndex;
	}

	public ElExpression getElExpression() {
		return elExpression;
	}

	public static ValueExpression valueOf(Object pObject) {
		if (pObject == null) {
			return new ValueExpression();
		} else if (pObject instanceof Boolean) {
			return new ValueExpression((Boolean) pObject);
		} else if (pObject instanceof Long) {
			return new ValueExpression((Long) pObject);
		} else if (pObject instanceof Double) {
			return new ValueExpression((Double) pObject);
		} else if (pObject instanceof Integer) {
			return new ValueExpression((Integer) pObject);
		} else if (pObject instanceof String) {
			return new ValueExpression((String) pObject);
		} else if (pObject instanceof ElExpression) {
			return new ValueExpression((ElExpression) pObject);
		} else if (pObject instanceof VariableReferenceExpression) {
			return new ValueExpression((VariableReferenceExpression) pObject);
		} else {
			throw new IllegalStateException("Invalid object type: " + pObject.getClass().getName());
		}
	}

	public static String ofLiteral(String pLiteral) {
		if (pLiteral == null) {
			throw new NullPointerException("A String Literal must not be null.");
		}
		final int len = pLiteral.length();
		if (len < 2) {
			throw new IllegalArgumentException("A String Literal must not be empty.");
		}
		final char c = pLiteral.charAt(0);
		if ((c != '"'  &&  c != '\'')  ||  pLiteral.charAt(len-1) != c) {
			throw new IllegalArgumentException("A String Literal must begin, and end with either ', or \".");
		}
		final StringBuilder sb = new StringBuilder();
		int offset = 1;
		while(offset < len-1) {
			final char ch = pLiteral.charAt(offset++);
			if (ch == '\\') {
				if (offset < len -1) {
					sb.append(pLiteral.charAt(offset++));
				}
			} else {
				sb.append(ch);
			}
		}
		return sb.toString();
	}
}
