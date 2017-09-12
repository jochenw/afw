package com.github.jochenw.afw.validation.api.annotations;

import java.lang.annotation.Annotation;

public class AnnotationData {
	private final Annotation annotation;
	private final String code;
	private final String beanClass;
	private final String propertyClass;
	private final String property;

	public AnnotationData(Annotation pAnnotation, String pCode, Class<?> pBeanClass, Class<?> pPropertyClass, String pProperty) {
		annotation = pAnnotation;
		code = pCode;
		beanClass = pBeanClass.getName();
		propertyClass = pPropertyClass.getName();
		property = pProperty;
	}

	public Annotation getAnnotation() {
		return annotation;
	}

	public String getCode() {
		return code;
	}
	
	public String getBeanClass() {
		return beanClass;
	}

	public String getPropertyClass() {
		return propertyClass;
	}

	public String getProperty() {
		return property;
	}
}