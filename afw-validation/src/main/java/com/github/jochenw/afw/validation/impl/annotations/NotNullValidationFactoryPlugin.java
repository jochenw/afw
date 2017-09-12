package com.github.jochenw.afw.validation.impl.annotations;

import java.lang.annotation.Annotation;

import com.github.jochenw.afw.validation.api.annotations.AnnotationData;
import com.github.jochenw.afw.validation.api.annotations.NotNull;

public class NotNullValidationFactoryPlugin extends AbstractValidationFactoryPlugin<Object> {
	public static final String PROPERTY_NULL_MESSAGE = "The property value must not be null.";

	@Override
	public String validate(AnnotationData pAnnotationData, Object pValue) {
		if (pValue == null) {
			return PROPERTY_NULL_MESSAGE;
		} else {
			return null;
		}
	}

	@Override
	protected Class<? extends Annotation> getAnnotationClass() {
		return NotNull.class;
	}

	@Override
	public String getCode(Annotation pAnnotation) {
		return ((NotNull) pAnnotation).code();
	}
}
