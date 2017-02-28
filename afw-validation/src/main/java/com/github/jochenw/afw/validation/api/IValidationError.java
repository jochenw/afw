package com.github.jochenw.afw.validation.api;

public interface IValidationError {
	String getContext();
	String getProperty();
	String getCode();
	String getMessage();
}
