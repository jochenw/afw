package com.github.jochenw.afw.validation.plugins;

import com.github.jochenw.afw.validation.api.IValidationError;

public interface IAtomicValidator<T> {
	IValidationError isValid(String pContext, T pObject);
}
