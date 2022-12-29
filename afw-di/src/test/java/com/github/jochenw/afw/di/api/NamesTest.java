package com.github.jochenw.afw.di.api;

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import javax.inject.Named;

import org.junit.Test;


/** Test for the {@link Names} class.
 */
public class NamesTest {
	/** Test case for {@link Names#named(String)}.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testNamed() throws Exception {
		final Named named = Names.named("My Name");
		assertNotNull(named);
		assertEquals("My Name", named.value());
		assertFalse(named.equals(null));
		final Method toStringMethod = Object.class.getDeclaredMethod("toString");
		try {
			named.toString();
			fail("Expected Exception");
		} catch (IllegalStateException e) {
			assertEquals("Not implemented: " + toStringMethod, e.getMessage());
		}
	}

	/** Test case for {@link Names#upperCased(String, String)}.
	 */
	@Test
	public void testUpperCased() {
		assertEquals("getValue", Names.upperCased("get", "value"));
	}

	/** Test for the public default constructor. (This default
	 * constructor isn't actually used, but it is considered
	 * as part of the coverage.)
	 */
	@Test
	public void testConstructor() {
		assertNotNull(new Names());
	}
}
