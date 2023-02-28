package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import org.junit.Test;

/** Test for the {@link NotImplementedException}. (Actually, more of a duty towards coverage.)
 */
public class NotImplementedExceptionTest {
	/** Test case for the message of an instance, that has been created
	 * with the {@link NotImplementedException#NotImplementedException() default constructor}.
	 */
	@Test
	public void testNotImplementedExceptionString() {
		test("Not implemented.", new NotImplementedException());
	}

	/** Test case for the message of an instance, that has been created
	 * with the {@link NotImplementedException#NotImplementedException(String)
	 * String-arg constructor}.
	 */
	@Test
	public void testNotImplementedException() {
		test("Still not implemented", new NotImplementedException("Still not implemented"));
	}

	/** Tests, whther the given instance has the given message.
	 * @param pMsg The expected message string.
	 * @param pExc The instance, that is being tested.
	 */
	protected void test(String pMsg, NotImplementedException pExc) {
		try {
			throw pExc;
		} catch (NotImplementedException ex) {
			assertSame(pExc, ex);
			assertEquals(pMsg, ex.getMessage());
		}
	}
}
