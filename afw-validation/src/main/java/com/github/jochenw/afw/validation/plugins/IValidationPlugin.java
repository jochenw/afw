package com.github.jochenw.afw.validation.plugins;

import java.lang.annotation.Annotation;

import com.github.jochenw.afw.validation.plugins.ValidatorFactory.TypedProvider;

public interface IValidationPlugin<A extends Annotation> {
	public interface Provider<T> {
		Object get(T pObject);
	}
	boolean isApplicable(Annotation pAnnotation);
	IPropertyValidator<Object> getAtomicValidator(String pProperty, TypedProvider<Object> pProvider, A pAnnotation);
}
