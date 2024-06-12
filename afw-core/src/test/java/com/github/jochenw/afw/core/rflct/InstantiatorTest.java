package com.github.jochenw.afw.core.rflct;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;

import org.jspecify.annotations.NonNull;
import org.junit.Test;

public class InstantiatorTest {
	/** Instance class, that has a no-args constructor.
	 */
	public static class A {
		private boolean valid;
		/** Creates a new instance with {@link #isValid() valid} == false.
		 */
		public A() { this(false); }
		A(boolean pValid) { valid = pValid; }
		boolean isValid() { return valid; }
		void setValid(boolean pValid) { valid = pValid; }
	}

	/** Test case for {@link IInstantiator#of(Constructor)}
	 * with a no-args constructor.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testOfConstructorWithoutParams() throws Exception {
		@SuppressWarnings("null")
		final @NonNull Constructor<A> constructor = A.class.getConstructor();
		final IInstantiator<A> instantiator = IInstantiator.of(constructor);
		assertNotNull(instantiator);
		final A a1 = instantiator.newInstance();
		assertNotNull(a1);
		final A a2 = instantiator.newInstance();
		assertNotNull(a2);
		assertNotSame(a1, a2);
	}
}
