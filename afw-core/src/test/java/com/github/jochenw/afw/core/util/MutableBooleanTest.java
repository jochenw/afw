package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import org.junit.Test;


/** Test suite for the {@link MutableBoolean}.
 */
public class MutableBooleanTest {
	/** Tests, whether a created instance is, indeed, mutable.
	 */
	@Test
	public void testMutable() {
		final MutableBoolean mb = new MutableBoolean();
		assertFalse(mb.isSet());
		assertFalse(mb.getValue());
		mb.set();
		assertTrue(mb.isSet());
		assertTrue(mb.getValue());
		mb.unset();
		assertFalse(mb.isSet());
		assertFalse(mb.getValue());
		mb.setValue(true);
		assert(mb.isSet());
		assertTrue(mb.getValue());
		mb.setValue(false);
		assertFalse(mb.isSet());
		assertFalse(mb.getValue());
	}

	/** Test case for {@link MutableBoolean#of(boolean)}.
	 */
	@Test
	public void testOfBoolean() {
		final MutableBoolean mb0 = MutableBoolean.of(false);
		assertNotNull(mb0);
		assertFalse(mb0.isSet());
		final MutableBoolean mb1 = MutableBoolean.of(true);
		assertNotNull(mb1);
		assertTrue(mb1.isSet());
		final MutableBoolean mb2 = MutableBoolean.of(false);
		assertNotNull(mb2);
		assertFalse(mb2.isSet());
		assertNotEquals(mb0, mb2);
		assertNotSame(mb0, mb2);
	}
}
