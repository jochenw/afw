package com.github.jochenw.afw.validation.impl.annotations;

import java.lang.annotation.Annotation;

import com.github.jochenw.afw.validation.api.annotations.AnnotationData;
import com.github.jochenw.afw.validation.api.annotations.NotEmpty;

public class NotEmptyValidationFactoryPlugin extends AbstractStringValidationFactoryPlugin {
	private static class NotEmptyAnnotationData extends AnnotationData {
		private final boolean nullable, trimming;

		public NotEmptyAnnotationData(Annotation pAnnotation, String pCode, Class<?> pBeanClass, Class<?> pPropertyClass,
				String pProperty) {
			super(pAnnotation, pCode, pBeanClass, pPropertyClass, pProperty);
			final NotEmpty notEmpty = (NotEmpty) pAnnotation;
			nullable = notEmpty.nullable();
			trimming = notEmpty.trim();
		}

		public boolean isNullable() {
			return nullable;
		}

		public boolean isTrimming() {
			return trimming;
		}
	}

	@Override
	protected AnnotationData newAnnotationData(Annotation pAnnotation, String pCode, Class<?> pBeanClass,
			Class<? extends String> pPropertyClass, String pProperty) {
		return new NotEmptyAnnotationData(pAnnotation, pCode, pBeanClass, pPropertyClass, pProperty);
	}

	@Override
	public String getCode(Annotation pAnnotation) {
		return ((NotEmpty) pAnnotation).code();
	}
	
	@Override
	public String validate(AnnotationData pAnnotationData, String pValue) {
		final NotEmptyAnnotationData nead = (NotEmptyAnnotationData) pAnnotationData;
		if (pValue == null) {
			if (nead.isNullable()) {
				return null;
			} else {
				return NotNullValidationFactoryPlugin.PROPERTY_NULL_MESSAGE;
			}
		} else {
			if (nead.isTrimming()) {
				if (pValue.trim().length() == 0) {
					return "The property value must not be empty (after trimming).";
				}
			} else {
				if (pValue.length() == 0) {
					return "The property value must not be empty.";
				}
			}
			return null;
		}
	}

	@Override
	protected Class<? extends Annotation> getAnnotationClass() {
		return NotEmpty.class;
	}

}
