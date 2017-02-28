package com.github.jochenw.afw.validation.plugins;

import java.lang.annotation.Annotation;

import com.github.jochenw.afw.validation.api.Length;

public class LengthValidationPlugin extends AbstractStringValidationPlugin<Length> {
	@Override
	protected Class<? extends Annotation> getAnnotationClass() {
		return Length.class;
	}

	@Override
	protected String getCode(Length pAnnotation) {
		return pAnnotation.code();
	}

	@Override
	protected String isValid(String pContext, String pProperty, Length pAnnotation, String pValue) {
		if (pValue == null) {
			if (!pAnnotation.nullable()) {
				return getCode(pAnnotation) + ": The value for property " + pProperty
						+ " is null at " + pContext;
			}
		} else {
			final int len = pValue.length();
			final int maxExclusive = pAnnotation.max();
			if (maxExclusive != -1) {
				if (len > maxExclusive) {
					return getCode(pAnnotation) + ": The value for property " + pProperty
							+ " has " + len + " characters (exceeding the maximum length of " + maxExclusive
							+ " characters) at " + pContext;
				}
			}
			final int maxInclusive = pAnnotation.maxInclusive();
			if (maxInclusive != -1) {
				if (len >= maxInclusive) {
					return getCode(pAnnotation) + ": The value for property " + pProperty
							+ " has " + len + " characters (exceeding the maximum length of " + maxInclusive
							+ " characters (inclusive)) at " + pContext;
				}
			}
			final int minExclusive = pAnnotation.min();
			if (minExclusive != -1) {
				if (len < minExclusive) {
					return getCode(pAnnotation) + ": The value for property " + pProperty
							+ " has " + len + " characters (not fulfilling the minimum length of " + minExclusive
							+ " characters) at " + pContext;
				}
			}
			final int minInclusive = pAnnotation.minInclusive();
			if (minInclusive != -1) {
				if (len <= minInclusive) {
					return getCode(pAnnotation) + ": The value for property " + pProperty
							+ " has " + len + " characters (not fulfilling the minimum length of " + minInclusive
							+ " characters (inclusive)) at " + pContext;
				}
			}
		}
		return null;
	}
}
