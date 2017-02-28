package com.github.jochenw.afw.validation.plugins;

import java.lang.annotation.Annotation;

import com.github.jochenw.afw.validation.api.AssertTrue;

public class AssertTrueValidationPlugin extends AbstractBooleanValidationPlugin<AssertTrue> {
	@Override
	protected Class<? extends Annotation> getAnnotationClass() {
		return AssertTrue.class;
	}

	@Override
	protected String getCode(AssertTrue pAnnotation) {
		return pAnnotation.code();
	}

	@Override
	protected String isValid(String pContext, String pProperty, AssertTrue pAnnotation, Boolean pValue) {
		if (pValue == null) {
			if (!pAnnotation.nullable()) {
				return 	getCode(pAnnotation) + ": The value for property " + pProperty
						+ " is null at " + pContext;
			}
		} else {
			if (!pValue.booleanValue()) {
				return 	getCode(pAnnotation) + ": The value for property " + pProperty
						+ " is false at " + pContext;
			}
		}
		return null;
	}
}
