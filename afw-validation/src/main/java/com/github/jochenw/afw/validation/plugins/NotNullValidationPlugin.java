package com.github.jochenw.afw.validation.plugins;

import java.lang.annotation.Annotation;

import com.github.jochenw.afw.validation.api.NotNull;


public class NotNullValidationPlugin extends AbstractObjectValidationPlugin<NotNull> {
	@Override
	protected Class<? extends Annotation> getAnnotationClass() {
		return NotNull.class;
	}

	@Override
	protected String getCode(NotNull pNotNull) {
		return pNotNull.code();
	}

	@Override
	protected String isValid(String pContext, String pProperty, NotNull pNotNull, Object pValue) {
		if (pValue == null) {
			return getCode(pNotNull) + ": The value for property " + pProperty + " is null at " + pContext;
		} else {
			return null;
		}
	}
}
