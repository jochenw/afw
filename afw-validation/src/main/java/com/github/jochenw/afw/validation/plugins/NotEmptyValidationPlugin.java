package com.github.jochenw.afw.validation.plugins;

import java.lang.annotation.Annotation;

import com.github.jochenw.afw.core.util.Strings;
import com.github.jochenw.afw.validation.api.NotEmpty;


public class NotEmptyValidationPlugin extends AbstractStringValidationPlugin<NotEmpty> {
	@Override
	protected Class<? extends Annotation> getAnnotationClass() {
		return NotEmpty.class;
	}

	@Override
	protected String getCode(NotEmpty pNotEmpty) {
		return pNotEmpty.code();
	}

	@Override
	protected String isValid(String pContext, String pProperty, NotEmpty pNotEmpty, String pValue) {
		final boolean empty;
		final String trimming;
		if (pNotEmpty.trimming()) {
			empty = Strings.isTrimmedEmpty(pValue);
			trimming = "(after trimming) ";
		} else {
			empty = Strings.isEmpty(pValue);
			trimming = "";
		}
		if (empty) {
			return getCode(pNotEmpty) + ": The value for property " + pProperty
					+ " is empty " + trimming + " at " + pContext;
		} else {
			return null;
		}
	}
}
