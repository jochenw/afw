package com.github.jochenw.afw.core.io;

import static org.junit.Assert.*;

import org.junit.Test;


/** Test for the {@link RePatternMatcher}.
 */
public class RePatternMatcherTest {
	/** Basic test case.
	 */
	@Test
	public void test() {
		assertMatch("LICENSE*", "LICENSE");
		assertMatch("LICENSE*", "LICENSE.txt");
		assertNoMatch("LICENSE*", "License.txt");
		assertMatch("LICENSE*/i", "License.txt");
		assertMatch("*.gif", "SomeFile.gif");
		assertNoMatch("*.gif", "SomeOtherFile.Gif");
		assertMatch("*.gif/i", "SomeOtherFile.Gif");
	}

	protected void assertMatch(String pPattern, String pValue) {
		assertMatch(pPattern, pValue, true);
	}

	protected void assertNoMatch(String pPattern, String pValue) {
		assertMatch(pPattern, pValue, false);
	}

	protected void assertMatch(String pPattern, String pValue, boolean pMatch) {
		final RePatternMatcher matcher = new RePatternMatcher(pPattern);
		assertEquals(pMatch, matcher.test(pValue));
	}
}
