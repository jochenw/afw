package com.github.jochenw.afw.validation.api;

public class ValidationException extends RuntimeException implements IValidationError {
	private static final long serialVersionUID = -914522693336831542L;
	private final String context, property, code;

	public ValidationException(String pContext, String pProperty, String pCode, String pMessage) {
		super(pMessage);
		context = pContext;
		property = pProperty;
		code = pCode;
	}

	public ValidationException(Throwable pCause, String pContext, String pProperty, String pCode, String pMessage) {
		super(pMessage, pCause);
		context = pContext;
		property = pProperty;
		code = pCode;
	}

	public ValidationException(String pMessage) {
		this(null, null, null, null, pMessage);
	}

	public ValidationException(Throwable pCause, String pMessage) {
		this(pCause, null, null, null, pMessage);
	}

	public ValidationException(Throwable pCause) {
		this(pCause, null, null, null, null);
	}

	public ValidationException(IValidationError pError) {
		this(null, pError.getContext(), pError.getCode(), pError.getProperty(), pError.getMessage());
	}
	
	@Override
	public String getContext() {
		return context;
	}

	@Override
	public String getProperty() {
		return property;
	}

	@Override
	public String getCode() {
		return code;
	}

}
