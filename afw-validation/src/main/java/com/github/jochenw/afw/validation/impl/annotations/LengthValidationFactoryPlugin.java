package com.github.jochenw.afw.validation.impl.annotations;

import java.lang.annotation.Annotation;

import com.github.jochenw.afw.validation.api.annotations.AnnotationData;
import com.github.jochenw.afw.validation.api.annotations.Length;

public class LengthValidationFactoryPlugin extends AbstractStringValidationFactoryPlugin {
	private static class LengthAnnotationData extends AnnotationData {
		private final int minInclusive, minExclusive, maxInclusive, maxExclusive;
		private final boolean nullable;
		
		public LengthAnnotationData(Annotation pAnnotation, String pCode, Class<?> pBeanClass, Class<?> pPropertyClass,
				String pProperty) {
			super(pAnnotation, pCode, pBeanClass, pPropertyClass, pProperty);
			final Length length = (Length) pAnnotation;
			minInclusive = length.minInclusive();
			minExclusive = length.minExclusive();
			maxInclusive = length.maxInclusive();
			maxExclusive = length.maxExclusive();
			nullable = length.nullable();
		}
	}

	
	
	@Override
	protected AnnotationData newAnnotationData(Annotation pAnnotation, String pCode, Class<?> pBeanClass,
			Class<? extends String> pPropertyClass, String pProperty) {
		return new LengthAnnotationData(pAnnotation, pCode, pBeanClass, pPropertyClass, pProperty);
	}

	@Override
	public String getCode(Annotation pAnnotation) {
		return ((Length) pAnnotation).code();
	}

	@Override
	public String validate(AnnotationData pAnnotationData, String pValue) {
		final LengthAnnotationData lad = (LengthAnnotationData) pAnnotationData;
		if (pValue == null) {
			if (lad.nullable) {
				return null;
			} else {
				return NotNullValidationFactoryPlugin.PROPERTY_NULL_MESSAGE;
			}
		}
		if (lad.minInclusive != -1  &&  lad.minInclusive > pValue.length()) {			
			return "The property value must have at least " + lad.minInclusive + " characters, inclusive.";
		}
		if (lad.minExclusive != -1  &&  lad.minExclusive >= pValue.length()) {			
			return "The property value must have at least " + lad.minExclusive + " characters.";
		}
		if (lad.maxInclusive != -1  &&  lad.maxInclusive < pValue.length()) {			
			return "The property value must have at most " + lad.maxInclusive + " characters, inclusive.";
		}
		if (lad.maxExclusive != -1  &&  lad.maxExclusive <= pValue.length()) {			
			return "The property value must have at most " + lad.maxExclusive + " characters.";
		}
		return null;
	}

	@Override
	protected Class<? extends Annotation> getAnnotationClass() {
		return Length.class;
	}

}
