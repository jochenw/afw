package com.github.jochenw.afw.core.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import com.github.jochenw.afw.core.props.Interpolator;


public class DefaultInterpolator implements Interpolator {
	private String startToken;
	private String endToken;
	private final Function<String,String> propertyProvider;

	public Function<String, String> getPropertyProvider() {
		return propertyProvider;
	}

	public void setStartToken(String startToken) {
		this.startToken = startToken;
	}

	public void setEndToken(String endToken) {
		this.endToken = endToken;
	}

	public DefaultInterpolator(Function<String,String> pPropertyProvider) {
		propertyProvider = pPropertyProvider;
	}

	public DefaultInterpolator(final Properties pProps) {
		Objects.requireNonNull(pProps, "Properties");
		propertyProvider = (s) -> pProps.getProperty(s);
	}
	
	public String getStartToken() {
		if (startToken == null) {
			return "${";
		} else {
			return startToken;
		}
	}

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

	protected String getPropertyValue(String pKey) {
		return propertyProvider.apply(pKey);
	}
}
