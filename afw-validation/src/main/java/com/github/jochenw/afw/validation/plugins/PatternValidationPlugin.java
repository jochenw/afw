package com.github.jochenw.afw.validation.plugins;

import java.lang.annotation.Annotation;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

import com.github.jochenw.afw.validation.api.Pattern;
import com.github.jochenw.afw.validation.plugins.ValidatorFactory.TypedProvider;


public class PatternValidationPlugin extends AbstractStringValidationPlugin<Pattern> {
	@Override
	protected Class<? extends Annotation> getAnnotationClass() {
		return Pattern.class;
	}

	@Override
	protected String getCode(Pattern pAnnotation) {
		return pAnnotation.code();
	}

	@Override
	protected SimpleValidator<String> newSimpleValidator(final TypedProvider<String> pProvider, final String pProperty, final Pattern pAnnotation) {
		final String pat = pAnnotation.pattern();
		if (pat == null  ||  pat.length() == 0) {
			throw new IllegalStateException("A Pattern annotation must have its 'pattern' value set to a valid regular expression."
					+ " Null, and empty string, are invalid values.");
		}
		try {
			final java.util.regex.Pattern re = java.util.regex.Pattern.compile(pat);
			return new SimpleValidator<String>() {
				@Override
				public String isValid(String pContext, String pValue) {
					if (pValue == null) {
						if (pAnnotation.nullable()) {
							return null;
						}
						return "The value NULL doesn't match the pattern " + pat;
					} else {
						final Matcher matcher = re.matcher(pValue);
						if (matcher.matches()) {
							return null;
						} else {
							return "The value " + pValue + " doesn't match the pattern " + pat;
						}
					}
				}
			};
		} catch (PatternSyntaxException pse) {
			throw new IllegalStateException("A Pattern annotation must have its 'pattern' value set to a valid regular expression."
					+ " Invalid value: " + pat);
		}
		
	}

	@Override
	protected String isValid(String pContext, String pProperty, Pattern pAnnotation, String pValue) {
		throw new IllegalStateException("Not implemented!");
	}
}
