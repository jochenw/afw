package com.github.jochenw.afw.core.rflct;

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.junit.Test;

import com.github.jochenw.afw.core.function.Functions;

/** Test suite for {@link IGetter}.
 */
public class GetterTest {
	/**
	 * 
	 */
	/** Test bean class.
	 */
	public static class Bean {
		private String a;

		/** A getter, which can be converted into an {@link IGetter}.
		 * @return The value of the beans "a" attribute.
		 */
		public String getA() { return a; }
		/** A method, which cannot be converted into an {@link IGetter},
		 * because it has the type "void".
		 */
		public void test() { /* Do nothing */ }
		/** A method, which cannot be converted into an {@link IGetter},
		 * because it takes a parameter.
		 * @param pA The new value of the "a" attribute.
		 * @return This bean.
		 */
		public Bean setA(String pA) { a = pA; return this; }
	}

	/** Test case for {@link IGetter#of(Method)}.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testOfMethod() throws Exception {
		final Method getAMethod = Bean.class.getDeclaredMethod("getA");
		final IGetter<Bean,String> aGetter = IGetter.of(getAMethod);
		final Bean bean = new Bean();
		assertNull(aGetter.get(bean));
		bean.a = "foo";
		assertEquals("foo", aGetter.get(bean));
		Functions.assertFail(NullPointerException.class, "Method", () -> IGetter.of((Method) null));
	}
}
