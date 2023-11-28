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
 * An expression, which constitutes a single value. For example,
 * a constant value, a variable reference, or a placeholder
 * reference.
 */
public class ValueExpression implements Serializable {
	private static final long serialVersionUID = 7032680077947015321L;
	/** The value expression, or null.
	 */
	private final ElExpression elExpression;
	/** The boolean value, or null.
	 */
	private final Boolean booleanValue;
	/** The long value, or null.
	 */
	private final Long longValue;
	/** The double value, or null.
	 */
	private final Double doubleValue;
	/** The string value, or null.
	 */
	private final String stringValue;
	/** The variable reference, or null.
	 */
	private final VariableReferenceExpression variableReference;
	/** The placeholder index, or null.
	 */
	private final Integer placeholderIndex;

	/**
	 * Creates a new instance with the given constant value.
	 * @param pValue The constant value.
	 */
	public ValueExpression(Long pValue) {
		longValue = pValue;
		booleanValue = null;
		doubleValue = null;
		stringValue = null;
		variableReference = null;
		placeholderIndex = null;
		elExpression = null;
	}

	/**
	 * Creates a new instance with the given constant value.
	 * @param pValue The constant value.
	 */
	public ValueExpression(Boolean pValue) {
		longValue = null;
		booleanValue = pValue;
		doubleValue = null;
		stringValue = null;
		variableReference = null;
		placeholderIndex = null;
		elExpression = null;
	}

	/**
	 * Creates a new instance with the given constant value.
	 * @param pValue The constant value.
	 */
	public ValueExpression(Double pValue) {
		longValue = null;
		booleanValue = null;
		doubleValue = pValue;
		stringValue = null;
		variableReference = null;
		placeholderIndex = null;
		elExpression = null;
	}

	/**
	 * Creates a new instance with the given constant value.
	 * @param pValue The constant value.
	 */
	public ValueExpression(String pValue) {
		longValue = null;
		booleanValue = null;
		doubleValue = null;
		stringValue = pValue;
		variableReference = null;
		placeholderIndex = null;
		elExpression = null;
	}

	/**
	 * Creates a new instance with the given variable reference as the value.
	 * @param pVariableReference The variable reference.
	 */
	public ValueExpression(VariableReferenceExpression pVariableReference) {
		longValue = null;
		booleanValue = null;
		doubleValue = null;
		stringValue = null;
		variableReference = pVariableReference;
		placeholderIndex = null;
		elExpression = null;
	}

	/**
	 * Creates a new instance with the given placeholder reference as the value.
	 * @param pPlaceholderIndex Index of the placeholder value, that is being
	 *   referenced.
	 */
	public ValueExpression(Integer pPlaceholderIndex) {
		longValue = null;
		booleanValue = null;
		doubleValue = null;
		stringValue = null;
		variableReference = null;
		placeholderIndex = pPlaceholderIndex;
		elExpression = null;
	}

	/**
	 * Creates a new instance with the value null.
	 */
	public ValueExpression() {
		longValue = null;
		booleanValue = null;
		doubleValue = null;
		stringValue = null;
		variableReference = null;
		placeholderIndex = null;
		elExpression = null;
	}

	/**
	 * Creates a new instance with the given expressions result as the value.
	 * @param pElExpression The expression, which constitutes the value.
	 */
	public ValueExpression(ElExpression pElExpression) {
		longValue = null;
		booleanValue = null;
		doubleValue = null;
		stringValue = null;
		variableReference = null;
		placeholderIndex = null;
		elExpression = pElExpression;
	}

	/**
	 * Returns the constant boolean value, if any, or null.
	 * @return The constant boolean value, if any, or null.
	 */
	public Boolean getBooleanValue() {
		return booleanValue;
	}

	/**
	 * Returns the constant long value, if any, or null.
	 * @return The constant long value, if any, or null.
	 */
	public Long getLongValue() {
		return longValue;
	}

	/**
	 * Returns the constant double value, if any, or null.
	 * @return The constant double value, if any, or null.
	 */
	public Double getDoubleValue() {
		return doubleValue;
	}

	/**
	 * Returns the constant String value, if any, or null.
	 * @return The constant String value, if any, or null.
	 */
	public String getStringValue() {
		return stringValue;
	}

	/**
	 * Returns the variable reference, if any, or null.
	 * @return The variable reference, if any, or null.
	 */
	public VariableReferenceExpression getVariableReference() {
		return variableReference;
	}

	/**
	 * Returns the placeholder index, if any, or null.
	 * @return The placeholder index, if any, or null.
	 */
	public Integer getPlaceholderIndex() {
		return placeholderIndex;
	}

	/**
	 * Returns the inner expression, if any, or null.
	 * @return The inner expression, if any, or null.
	 */
	public ElExpression getElExpression() {
		return elExpression;
	}

	/**
	 * Creates a new instance, which constitutes the given value.
	 * @param pObject The created expressions value.
	 * @return The created instance.
	 */
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

	/**
	 * Converts the given string literal into a value.
	 * @param pLiteral The string literal, which is being parsed.
	 * @return The parsed value.
	 */
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
