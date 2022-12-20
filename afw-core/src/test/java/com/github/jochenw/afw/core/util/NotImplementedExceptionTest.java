package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import org.junit.Test;

/** Test for the {@link NotImplementedException}. (Actually, more of a duty towards coverage.)
 */
public class NotImplementedExceptionTest {
	@Test
	public void testNotImplementedExceptionString() {
		test("Not implemented.", new NotImplementedException());
	}

	@Test
	public void testNotImplementedException() {
		test("Still not implemented", new NotImplementedException("Still not implemented"));
	}

	protected void test(String pMsg, NotImplementedException pExc) {
		try {
			throw pExc;
		} catch (NotImplementedException ex) {
			assertSame(pExc, ex);
			assertEquals(pMsg, ex.getMessage());
		}
	}
}
