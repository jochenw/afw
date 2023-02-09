package com.github.jochenw.afw.di.api;

import static org.junit.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.junit.Test;


/** Test for the {@link Names} class.
 */
public class NamesTest {
	/** Test case for {@code Names#named(String)}.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testNamed() throws Exception {
		testNamed(Annotations.getProvider("javax.inject"));
		testNamed(Annotations.getProvider("jakarta.inject"));
		testNamed(Annotations.getProvider("com.google.inject"));
	}

	private void testNamed(IAnnotationProvider pProvider) throws NoSuchMethodException {
		final Annotation named = pProvider.newNamed("My Name");
		assertNotNull(named);
		assertEquals("My Name", pProvider.getNamedValue(named));
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
