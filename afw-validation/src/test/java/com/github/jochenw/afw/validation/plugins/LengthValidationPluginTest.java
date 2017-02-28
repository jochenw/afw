package com.github.jochenw.afw.validation.plugins;

import org.junit.Test;

import com.github.jochenw.afw.validation.api.Length;
import com.github.jochenw.afw.validation.api.NotEmpty;

public class LengthValidationPluginTest extends AbstractValidationPluginTestCase {
	private static class LengthMaxExclusiveBean {
		@Length(code="LNGTHE01", max=5)
		private String value;
	}
	private static class LengthMaxInclusiveBean {
		@Length(code="LNGTHI01", maxInclusive=5)
		private String value;
	}
	private static class LengthMinExclusiveBean {
		@Length(code="LNGTHE02", min=5)
		private String value;
	}
	private static class LengthMinInclusiveBean {
		@Length(code="LNGTHI02", minInclusive=5)
		private String value;
	}
	private static class NotEmptyBean {
		@NotEmpty(code="NE01", trimming=true)
		private String value1;
		@NotEmpty(code="NE02")
		private String value2;
	}

	@Test
	public void testMaxLengthExclusive() throws Exception {
		final LengthMaxExclusiveBean lmb = new LengthMaxExclusiveBean();
		final StringBuilder sb = new StringBuilder();
		for (int i = 0;  i < 10;  i++) {
			final String s = sb.toString();
			lmb.value = s;
			if (s.length() < 5) {
				assertValid(lmb);
			} else {
				assertInvalid(lmb, "LNGTHE01");
			}
		}
	}

	@Test
	public void testMaxInclusiveLength() throws Exception {
		final LengthMaxInclusiveBean lmb = new LengthMaxInclusiveBean();
		final StringBuilder sb = new StringBuilder();
		for (int i = 0;  i < 10;  i++) {
			final String s = sb.toString();
			lmb.value = s;
			if (s.length() <= 5) {
				assertValid(lmb);
			} else {
				assertInvalid(lmb, "LNGTHI01");
			}
		}
	}

	@Test
	public void testMinExclusiveLength() throws Exception {
		final LengthMinExclusiveBean lmb = new LengthMinExclusiveBean();
		final StringBuilder sb = new StringBuilder();
		for (int i = 0;  i < 10;  i++) {
			final String s = sb.toString();
			lmb.value = s;
			if (s.length() > 5) {
				assertValid(lmb);
			} else {
				assertInvalid(lmb, "LNGTHE02");
			}
		}
	}

	@Test
	public void testMinInclusiveLength() throws Exception {
		final LengthMinInclusiveBean lmb = new LengthMinInclusiveBean();
		final StringBuilder sb = new StringBuilder();
		for (int i = 0;  i < 10;  i++) {
			final String s = sb.toString();
			lmb.value = s;
			if (s.length() >= 5) {
				assertValid(lmb);
			} else {
				assertInvalid(lmb, "LNGTHI02");
			}
		}
	}

	@Test
	public void testNotEmpty() throws Exception {
		NotEmptyBean neb = newNotEmptyBean();
		neb = newNotEmptyBean();
		neb.value1 = " ";
		assertInvalid(neb, "NE01");
		neb.value1 = "";
		assertInvalid(neb, "NE01");
		neb.value1 = null;
		assertInvalid(neb, "NE01");
		neb = newNotEmptyBean();
		neb.value2 = " ";
		assertValid(neb);
		neb.value2 = "";
		assertInvalid(neb, "NE02");
		neb.value2 = null;
		assertInvalid(neb, "NE02");
	}

	protected NotEmptyBean newNotEmptyBean() {
		final NotEmptyBean neb = new NotEmptyBean();
		neb.value1 = "abc";
		neb.value2 = "abc";
		assertValid(neb);
		return neb;
	}
}
