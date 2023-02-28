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

	/** Asserts, that the given pattern produces a matcher, that matches the given value.
	 * @param pPattern The pattern, that is being tested.
	 * @param pValue The value, that is expected to being matched by the pattern.
	 */
	protected void assertMatch(String pPattern, String pValue) {
		assertMatch(pPattern, pValue, true);
	}

	/** Asserts, that the given pattern produces a matcher,
	 * that doesn't match the given value.
	 * @param pPattern The pattern, that is being tested.
	 * @param pValue The value, that is expected to being matched by the pattern.
	 */
	protected void assertNoMatch(String pPattern, String pValue) {
		assertMatch(pPattern, pValue, false);
	}

	/** Asserts, that the given pattern produces a matcher, that returns the
	 * given result when being tested against the given value.
	 * @param pPattern The pattern, that is being tested.
	 * @param pValue The value, that is expected to being matched by the pattern.
	 * @param pMatch True, if the pattern is supposed to match the given value.
	 */
	protected void assertMatch(String pPattern, String pValue, boolean pMatch) {
		final RePatternMatcher matcher = new RePatternMatcher(pPattern);
		assertEquals(pMatch, matcher.test(pValue));
	}
}
