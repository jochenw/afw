package com.github.jochenw.afw.validation.plugins;

import java.lang.annotation.Annotation;

import com.github.jochenw.afw.core.util.Strings;
import com.github.jochenw.afw.validation.api.IValidationError;
import com.github.jochenw.afw.validation.plugins.ValidatorFactory.TypedProvider;


public abstract class AbstractValidationPlugin<A extends Annotation, T> implements IValidationPlugin<A> {
	public interface SimpleValidator<O> {
		public String isValid(String pContext, O pValue);
	}
	protected abstract Class<? extends Annotation> getAnnotationClass();
	protected abstract String getCode(A pAnnotation);
	protected abstract String isValid(String pContext, String pProperty, A pAnnotation, T pValue);

	protected SimpleValidator<T> newSimpleValidator(final TypedProvider<T> pProvider, final String pProperty, final A pAnnotation) {
		return new SimpleValidator<T>() {
			@Override
			public String isValid(String pContext, T pValue) {
				return AbstractValidationPlugin.this.isValid(pContext, pProperty, pAnnotation, pValue);
			}
		};
	}
	
	@Override
	public boolean isApplicable(Annotation pAnnotation) {
		return getAnnotationClass().isAssignableFrom(pAnnotation.getClass());
	}

	@Override
	public IPropertyValidator<Object> getAtomicValidator(String pProperty, TypedProvider<Object> pProvider,
			A pAnnotation) {
		final A annotation = (A) pAnnotation;
		final String code = getCode(annotation);
		if (Strings.isEmpty(code)) {
			throw new IllegalStateException("Missing value for annotation NotNull, property code");
		}
		@SuppressWarnings("unchecked")
		final TypedProvider<T> prov = (TypedProvider<T>) pProvider;
		final SimpleValidator<T> earlyValidator = newSimpleValidator(prov, pProperty, pAnnotation);
		return new IPropertyValidator<Object>() {
			@Override
			public IValidationError isValid(String pContext, Object pObject) {
				@SuppressWarnings("unchecked")
				final T value = (T) pProvider.get(pObject);
				final String message = earlyValidator.isValid(pContext, value);
				if (message != null) {
					return new IValidationError() {
						@Override
						public String getProperty() {
							return pProperty;
						}

						@Override
						public String getMessage() {
							return message;
						}

						@Override
						public String getContext() {
							return pContext;
						}

						@Override
						public String getCode() {
							return code;
						}
					};
				}
				return null;
			}

			@Override
			public String getProperty() {
				return pProperty;
			}
		};
	}

}
