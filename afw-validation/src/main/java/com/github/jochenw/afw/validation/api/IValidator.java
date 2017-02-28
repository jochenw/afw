package com.github.jochenw.afw.validation.api;

import java.util.ArrayList;
import java.util.List;

import com.github.jochenw.afw.core.util.MutableBoolean;

@FunctionalInterface
public interface IValidator {
	@FunctionalInterface interface Handler {
		void note(IValidationError pError, Object pObject);
	}

	void validate(String pContext, Object pObject, IValidator.Handler pHandler);

	default public boolean isValid(String pContext, Object pObject) {
		final MutableBoolean mb = new MutableBoolean();
		final Handler h = (e, o) -> mb.equals(true);
		validate(pContext, pObject, h);
		return mb.getValue();
	}

	default public List<IValidationError> checkValid(String pContext, Object pObject) {
		final List<IValidationError> errors = new ArrayList<>();
		final Handler h = (e, o) -> errors.add(e);
		validate(pContext, pObject, h);
		return errors;
	}

	static final Handler ASSERTION_HANDLER = new Handler() {
		@Override
		public void note(IValidationError pError, Object pObject) {
			throw new ValidationException(pError);
		}
	};

	default public void assertValid(String pContext, Object pObject) {
		validate(pContext, pObject, ASSERTION_HANDLER);
	}
}
