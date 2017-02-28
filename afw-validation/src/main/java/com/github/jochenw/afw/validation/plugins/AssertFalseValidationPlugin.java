package com.github.jochenw.afw.validation.plugins;

import java.lang.annotation.Annotation;

import com.github.jochenw.afw.validation.api.AssertFalse;


public class AssertFalseValidationPlugin extends AbstractBooleanValidationPlugin<AssertFalse> {
	@Override
	protected Class<? extends Annotation> getAnnotationClass() {
		return AssertFalse.class;
	}

	@Override
	protected String getCode(AssertFalse pAnnotation) {
		return pAnnotation.code();
	}

	@Override
	protected String isValid(String pContext, String pProperty, AssertFalse pAnnotation, Boolean pValue) {
		if (pValue == null) {
			if (!pAnnotation.nullable()) {
				return getCode(pAnnotation) + ": The value for property " + pProperty + " is null at " + pContext;
			}
		} else {
			if (pValue.booleanValue()) {
				return 	getCode(pAnnotation) + ": The value for property " + pProperty
					+ " is true at " + pContext;
			}
		}
		return null;
	}
}
