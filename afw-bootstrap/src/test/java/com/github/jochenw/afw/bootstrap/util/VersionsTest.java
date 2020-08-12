package com.github.jochenw.afw.bootstrap.util;

import static org.junit.Assert.*;

import org.junit.Test;

import com.github.jochenw.afw.bootstrap.util.Versions.Version;

public class VersionsTest {
	@Test
	public void testValueOf() {
		assertValid("0.2.1", 0, 2, 1);
		assertValid(".2.1", 0, 2, 1);
		assertInvalid("..1", "Invalid version number: ..1");
		assertInvalid("", "Invalid version number: ");
		assertInvalid("0.1.2a", "Invalid version number: 0.1.2a");
	}

	@Test
	public void testGreaterOrEqual() {
		assertGreaterOrEqual("0.2.1", "0.2.0", true);
		assertGreaterOrEqual("0.2.1", "0.2.1", true);
		assertGreaterOrEqual("0.2.1", "0.2.2", false);
		assertGreaterOrEqual("0.2.1", "0.3.0", false);
		assertGreaterOrEqual("0.2.0", "0.2", true);
		assertGreaterOrEqual("0.2", "0.2.0", true);
	}

	protected void assertValid(String pVersionStr, int... pNumbers) {
		final Version version = Versions.valueOf(pVersionStr);
		final int[] numbers = version.getNumbers();
		assertEquals(pNumbers.length, numbers.length);
		for (int i = 0;  i < numbers.length;  i++) {
			assertEquals(pNumbers[i], numbers[i]);
		}
	}

	protected void assertGreaterOrEqual(String pVersion1, String pVersion2, boolean pValue) {
		final Version v1 = Versions.valueOf(pVersion1);
		final Version v2 = Versions.valueOf(pVersion2);
		assertEquals(pValue, v1.isGreaterOrEqual(v2));
	}

	protected void assertInvalid(String pVersionStr, String pMsg) {
		try {
			Versions.valueOf(pVersionStr);
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals(pMsg, e.getMessage());
		}
	}
}
