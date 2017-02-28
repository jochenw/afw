package com.github.jochenw.afw.validation.plugins;

import java.util.List;

import com.github.jochenw.afw.validation.api.IValidationError;

import org.junit.Assert;

public class AbstractValidationPluginTestCase {
	private static final ValidatorFactory VALIDATOR_FACTORY = new ValidatorFactory();

	public static void assertValid(Object pBean) {
		VALIDATOR_FACTORY.getValidator(pBean).assertValid(null, pBean);
	}
	public static void assertInvalid(Object pBean, String pCode) {
		final List<IValidationError> errors = VALIDATOR_FACTORY.getValidator(pBean).checkValid(null, pBean);
		Assert.assertEquals(1, errors.size());
		Assert.assertEquals(pCode, errors.get(0).getCode());
	}
}
