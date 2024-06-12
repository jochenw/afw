package com.github.jochenw.afw.core.rflct;

import static org.junit.Assert.*;

import java.lang.reflect.Field;

import org.junit.Test;


/** Test suite for the {@link ISetter} interface.
 */
public class SetterTest {
	public static class Bean {
		private String a, b;

		/** Getter for the "a" property.
		 * @return The value of the "a" property. 
		 */
		public String getA() { return a; }
		/** Getter for the "b" property, using the property name "foo".
		 * @return The value of the "b" property. 
		 */
		public String getFoo() { return b; }
		/** Setter for the "a" property.
		 * @param pA The new value of the "a" property.
		 */
		public void setA(String pA) {
			a = pA;
		}
		/** Setter for the "b" property, using the property name "foo".
		 * @param pFoo The new value of the "b" property.
		 */
		public void setFoo(String pFoo) {
			b = pFoo;
		}
	}

	/** Test case for {@link ISetter#of(Field)}.
	 * @throws Exception The test has failed.
	 */
	@Test
	public void testOfField() throws Exception {
		final Field bField = Bean.class.getDeclaredField("b");
		final ISetter<Bean,String> setter = ISetter.of(bField);
		validateSetterForB(setter);
	}

	private void validateSetterForB(final ISetter<Bean, String> setter) {
		final Bean bean = new Bean();
		assertNotNull(bean);
		assertNull(bean.b);
		assertNull(bean.a);
		setter.set(bean,  "bar");
		assertEquals("bar", bean.b);
		assertNull(bean.a);
		setter.set(bean,  "baz");
		assertEquals("baz", bean.b);
		setter.set(bean,  null);
		assertNull(bean.b);
	}

	/** Test case for {@link ISetter#of(Class, String)} with a field.
	 * @throws Exception The test has failed.
	 */
	@Test
	public void testOfString() throws Exception {
		@SuppressWarnings("null")
		final Class<Bean> beanClass = Bean.class;
		final ISetter<Bean,String> setter = ISetter.of(beanClass, "b");
		validateSetterForB(setter);
	}
}
