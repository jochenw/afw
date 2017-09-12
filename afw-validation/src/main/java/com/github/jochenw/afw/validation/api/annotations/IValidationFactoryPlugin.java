package com.github.jochenw.afw.validation.api.annotations;

import java.lang.annotation.Annotation;

public interface IValidationFactoryPlugin<O extends Object> {
	String getCode(Annotation pAnnotation);
	AnnotationData compile(Annotation pAnnotation, Class<?> pBeanClass, Class<? extends O> pPropertyClass, String pProperty);
	String validate(AnnotationData pAnnotationData, O pValue);
}
