package com.github.jochenw.afw.validation.impl.annotations;

import java.lang.annotation.Annotation;

import com.github.jochenw.afw.validation.api.annotations.AnnotationData;
import com.github.jochenw.afw.validation.api.annotations.IValidationFactoryPlugin;

public abstract class AbstractValidationFactoryPlugin<O extends Object> implements IValidationFactoryPlugin<O> {
	protected abstract Class<? extends Annotation> getAnnotationClass();

	@Override
	public AnnotationData compile(Annotation pAnnotation, Class<?> pBeanClass, Class<? extends O> pPropertyClass, String pProperty) {
		final Class<? extends Annotation> annotationClass = getAnnotationClass();
		if (annotationClass == null) {
			return null;
		} else if (!annotationClass.isAssignableFrom(pAnnotation.getClass())) {
			return null;
		} else {
			return newAnnotationData(pAnnotation, getCode(pAnnotation), pBeanClass, pPropertyClass, pProperty);
		}
	}

	protected AnnotationData newAnnotationData(Annotation pAnnotation, String pCode, Class<?> pBeanClass, Class<? extends O> pPropertyClass, String pProperty) {
		return new AnnotationData(pAnnotation, pCode, pBeanClass, pPropertyClass, pProperty);
	}
}
