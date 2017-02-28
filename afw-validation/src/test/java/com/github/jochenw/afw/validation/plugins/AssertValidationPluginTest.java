package com.github.jochenw.afw.validation.plugins;

import org.junit.Test;

import com.github.jochenw.afw.validation.api.AssertFalse;
import com.github.jochenw.afw.validation.api.AssertTrue;

public class AssertValidationPluginTest extends AbstractValidationPluginTestCase {
	private static class AssertBean {
		@AssertFalse(code="AF01")
		private boolean falseProperty1;
		@AssertFalse(code="AF02", nullable=true)
		private Boolean falseProperty2;
		@AssertFalse(code="AF03")
		private Boolean falseProperty3;
		@AssertTrue(code="AT01")
		private boolean trueProperty1;
		@AssertTrue(code="AT02", nullable=true)
		private Boolean trueProperty2;
		@AssertTrue(code="AT03")
		private Boolean trueProperty3;
	}

	@Test
	public void testAssertFalse() throws Exception {
		AssertBean ab = newAssertBean();
		ab.falseProperty1 = true;
		assertInvalid(ab, "AF01");
		ab = newAssertBean();
		ab.falseProperty2 = null;
		assertValid(ab);
		ab.falseProperty2 = Boolean.TRUE;
		assertInvalid(ab, "AF02");
		ab = newAssertBean();
		ab.falseProperty3 = null;
		assertInvalid(ab, "AF03");
		ab.falseProperty3 = Boolean.TRUE;
		assertInvalid(ab, "AF03");
	}


	@Test
	public void testAssertTrue() throws Exception {
		AssertBean ab = newAssertBean();
		ab.trueProperty1 = false;
		assertInvalid(ab, "AT01");
		ab = newAssertBean();
		ab.trueProperty2 = null;
		assertValid(ab);
		ab.trueProperty2 = Boolean.FALSE;
		assertInvalid(ab, "AT02");
		ab = newAssertBean();
		ab.trueProperty3 = null;
		assertInvalid(ab, "AT03");
		ab.trueProperty3 = Boolean.FALSE;
		assertInvalid(ab, "AT03");
	}

	private AssertBean newAssertBean() {
		final AssertBean ab = new AssertBean();
		ab.falseProperty1 = false;
		ab.falseProperty2 = Boolean.FALSE;
		ab.falseProperty3 = Boolean.FALSE;
		ab.trueProperty1 = true;
		ab.trueProperty2 = Boolean.TRUE;
		ab.trueProperty3 = Boolean.TRUE;
		assertValid(ab);
		return ab;
	}
}
