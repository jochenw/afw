package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import org.junit.Test;

/** Test suite for {@link MutableString}.
 */
public class MutableStringTest {
	/** Test case for {@link MutableString#of()}, and {@link MutableString#of(String)}}.
	 */
	@Test
	public void testCreate() {
		final MutableString ms0 = MutableString.of();
		assertNull(ms0.get());
		final MutableString ms1 = MutableString.of("foo");
		assertEquals("foo", ms1.get());
	}

	/** Test case for {@link MutableString#setValue(String)},
	 * and {@link MutableString#accept(String)}. 
	 */
	@Test
	public void testModify() {
		final MutableString ms = MutableString.of();
		assertNull(ms.get());
		ms.setValue("foo");
		assertEquals("foo", ms.get());
		ms.setValue("bar");
		assertEquals("bar", ms.get());
		ms.setValue(null);
		assertNull(ms.get());
		ms.accept("foo");
		assertEquals("foo", ms.get());
		ms.accept("bar");
		assertEquals("bar", ms.get());
	}

	/** Test case for
	 * {@link MutableString#isNullOrEmpty()},
	 * {@link MutableString#isEmpty()}, and
	 * {@link MutableString#isBlank()}.
	 */
	@Test
	public void testIsMethods() {
		final MutableString ms = MutableString.of();
		assertNull(ms.get());
		assertTrue(ms.isNullOrEmpty());
		assertFalse(ms.isEmpty());
		assertFalse(ms.isBlank());
		ms.setValue("");
		assertTrue(ms.isNullOrEmpty());
		assertTrue(ms.isEmpty());
		assertTrue(ms.isBlank());
		ms.setValue("  \t");
		assertFalse(ms.isNullOrEmpty());
		assertFalse(ms.isEmpty());
		assertTrue(ms.isBlank());
		ms.setValue("foo");
		assertFalse(ms.isNullOrEmpty());
		assertFalse(ms.isEmpty());
		assertFalse(ms.isBlank());
	}
}
