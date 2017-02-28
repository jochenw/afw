package com.github.jochenw.afw.validation.plugins;

import org.junit.Test;

import com.github.jochenw.afw.validation.api.Pattern;

public class PatternValidationPluginTest extends AbstractValidationPluginTestCase {
	private static class NullablePatternBean {
		@Pattern(code="PAT00", nullable=true, pattern="^abc.*$")
		private String value;
	}
	private static class NotNullablePatternBean {
		@Pattern(code="PAT01", pattern="^abc.*$")
		private String value;
	}

	@Test
	public void testNullablePatternBean() {
		final NullablePatternBean npb = new NullablePatternBean();
		assertValid(npb);
		npb.value = "abc";
		assertValid(npb);
		npb.value = "abcdefg";
		assertValid(npb);
		npb.value = "ab";
		assertInvalid(npb, "PAT00");
	}

	@Test
	public void testNotNullablePatternBean() {
		final NotNullablePatternBean npb = new NotNullablePatternBean();
		assertInvalid(npb, "PAT01");
		npb.value = "abc";
		assertValid(npb);
		npb.value = "abcdefg";
		assertValid(npb);
		npb.value = "ab";
		assertInvalid(npb, "PAT01");
	}
}
