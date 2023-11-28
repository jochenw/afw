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
package com.github.jochenw.afw.core.props;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import javax.annotation.Nonnull;

import com.github.jochenw.afw.core.util.Objects;


/**
 * Default implementation of {@link Interpolator}.
 * Replaces property references, like "abc${foo}def" with the
 * respective property values.
 */
public class DefaultInterpolator implements Interpolator {
	private String startToken;
	private String endToken;
	private final Function<String,String> propertyProvider;

	/**
	 * Returns a function, that resolves property values.
	 * @return A function, that resolves property values.
	 */
	public Function<String, String> getPropertyProvider() {
		return propertyProvider;
	}

	/**
	 * Sets a property references start token, for example
	 * "${".
	 * @param pStartToken The start token of a property
	 * reference.
	 */
	public void setStartToken(String pStartToken) {
		startToken = pStartToken;
	}

	/**
	 * Sets a property references end token, for example
	 * "}".
	 * @param pEndToken The end token of a property
	 * reference.
	 */
	public void setEndToken(String pEndToken) {
		endToken = pEndToken;
	}

	/**
	 * Creates a new instance with the given property provider.
	 * @param pPropertyProvider A function, which is invoked
	 *   to resolve property values, that are being referenced.
	 */
	public DefaultInterpolator(@Nonnull Function<String,String> pPropertyProvider) {
		propertyProvider = pPropertyProvider;
	}

	/**
	 * Creates a new instance with a property provider, that
	 * resolves a property value by looking into the given
	 * property set.
	 * @param pProps A property set, which acts as the backend
	 *   for looking up property values.
	 */
	public DefaultInterpolator(final @Nonnull Properties pProps) {
		Objects.requireNonNull(pProps, "Properties");
		propertyProvider = (s) -> pProps.getProperty(s);
	}
	
	/**
	 * Returns a property references start token, for example
	 * "${".
	 * @return The start token of a property
	 * reference.
	 */
	public String getStartToken() {
		if (startToken == null) {
			return "${";
		} else {
			return startToken;
		}
	}

	/**
	 * Returns a property references end token, for example
	 * "${".
	 * @return The end token of a property
	 * reference.
	 */
	public String getEndToken() {
		if (endToken == null) {
			return "}";
		} else {
			return endToken;
		}
	}
	
	@Override
	public boolean isInterpolatable(String pValue) {
		Objects.requireNonNull(pValue, "Value");
		final String strtToken = getStartToken();
		int startOffset = pValue.indexOf(strtToken);
		if (startOffset == -1) {
			return false;
		} else {
			final int endOffset = pValue.indexOf(getEndToken(), startOffset+strtToken.length());
			return endOffset != -1;
		}
	}

	@Override
	public String interpolate(String pValue) {
		Objects.requireNonNull(pValue, "Value");
		final String strtToken = getStartToken();
		final String ndToken = getEndToken();
		String value = pValue;
		boolean finished = false;
		while (!finished) {
			finished = true;
			final int startOffset = value.lastIndexOf(strtToken);
			if (startOffset != -1) {
				final int endOffset = value.indexOf(ndToken, startOffset+strtToken.length());
				if (endOffset != -1) {
					final String key = value.substring(startOffset+strtToken.length(), endOffset);
					value = value.substring(0, startOffset) + getPropertyValue(key) + value.substring(endOffset+ndToken.length());
					finished = false;
				}
			}
		}
		return value;
	}

	@Override
	public void interpolate(StringSet pValues) {
		boolean finished = false;
		while (!finished) {
			finished = true;
			final Iterator<Map.Entry<String,String>> iter = pValues.getValues();
			while (iter.hasNext()) {
				final Map.Entry<String,String> en = iter.next();
				final String value = en.getValue();
				if (isInterpolatable(value)) {
					en.setValue(interpolate(value));
					finished = false;
				}
			}
		}
	}

	/** Returns the configured value for the variable {@code pKey}.
	 * @param pKey The requested variable name.
	 * @return The requested variables value, possibly null.
	 */
	protected String getPropertyValue(String pKey) {
		return propertyProvider.apply(pKey);
	}
}
