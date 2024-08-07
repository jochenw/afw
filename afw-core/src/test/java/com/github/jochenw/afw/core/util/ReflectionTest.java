/*
 * Copyright 2018 Jochen Wiedmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jochenw.afw.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;


/** Test for {@link Reflection}.
 */
public class ReflectionTest {
	@SuppressWarnings("unused")
	private static class A {
		private String property1;
		private boolean boolProperty1;
		private boolean boolProperty2;
		private boolean boolProperty3;
		private static String staticProperty4;

		boolean isBoolProperty2() {
			return boolProperty2;
		}

		public void setBoolProperty2(boolean boolProperty2) {
			this.boolProperty2 = boolProperty2;
		}

		public boolean isBoolProperty3() {
			return boolProperty3;
		}

		public void setBoolProperty3(boolean boolProperty3) {
			this.boolProperty3 = boolProperty3;
		}
		
		public boolean getBoolProperty1() {
			return boolProperty1;
		}

		public void setBoolProperty1(boolean boolProperty) {
			this.boolProperty1 = boolProperty;
		}

		public static String getProperty2() {
			throw new IllegalStateException("Not implemented");
		}
		
		public String getProperty1() {
			return property1;
		}

		public void setProperty1(String property1) {
			this.property1 = property1;
		}
	}

	@SuppressWarnings("unused")
	private static class B extends A {
		private String property3;

		public String getProperty3() {
			return property3;
		}

		public void setProperty3(String property3) {
			this.property3 = property3;
		}
	}

	/** Test case for {@link Reflection#setValue(Object, String, Object)}.
	 */
	@Test
	public void testSetValue() {
		final B b = new B();
		Assert.assertNull(b.getProperty3());
		Reflection.setValue(b, "property3", "Foo");
		Assert.assertEquals("Foo", b.getProperty3());
		Reflection.setValue(b, "Property3", "Bar");
		Assert.assertEquals("Bar", b.getProperty3());
		try {
			Reflection.setValue(b, "propPerty3", null);
			Assert.fail("Expected exception.");
		} catch (NoSuchFieldError e) {
			Assert.assertEquals("Field not found: com.github.jochenw.afw.core.util.ReflectionTest$B.propPerty3", e.getMessage());
		}
		try {
			Reflection.setValue(b,  null, null);
			Assert.fail("Expected Exception");
		} catch (NullPointerException e) {
			Assert.assertEquals("The field name must not be null, or empty.", e.getMessage());
		}
	}

	/** Test case for {@link Reflection#getGetters(Class)}.
	 */
	@Test
	public void testGetGetters() {
		final Map<String, Method> getters = Reflection.getGetters(B.class);
		Assert.assertNull(getters.get("boolProperty2")); // Getter isn't public
		Assert.assertNull(getters.get("property2"));     // Getter is static
		Assert.assertEquals(4, getters.size());
		Assert.assertNotNull(getters.get("property3"));  // Local property (in class B) detected?
		Assert.assertNotNull(getters.get("property1"));  // Super class property (in A) detected?
		Assert.assertNotNull(getters.get("boolProperty1")); // Boolean property using "get" detexted?
		Assert.assertNotNull(getters.get("boolProperty3")); // Boolean property using "is" detected?
	}

	/** Test case for {@link Reflection#getStaticField(Class, String)}.
	 */
	@Test
	public void testGetStaticField() {
		final Field field = Reflection.getStaticField(A.class, "staticProperty4");
		Assert.assertNotNull(field);
		Assert.assertNull(Reflection.getStaticField(A.class, "noSuchPropertyExists"));
	}

	private static class C {
		private final String value;
		private final Integer number;
		@SuppressWarnings("unused")
		C(RuntimeException pException) {
			throw pException;
		}
		@SuppressWarnings("unused")
		C(String pValue, Integer pNumber) {
			value = pValue;
			number = pNumber;
		}
	}
	/** Test case for {@link Reflection#newObject(String, Object...)}.
	 */
	@Test
	public void testNewObject() {
		final RuntimeException rte = new RuntimeException("Constructor failed");
		try {
			Reflection.newObject(C.class.getName(), rte);
			Assert.fail("Expected Exception");
		} catch (RuntimeException e) {
			Assert.assertSame(rte,  e);
		} catch (Throwable t) {
			System.err.println("Unexpected Exception:");
			t.printStackTrace(System.err);
			throw Exceptions.show(t);
		}
		final C c = Reflection.newObject(C.class.getName(), "42", Integer.valueOf(3));
		Assert.assertNotNull(c);
		Assert.assertEquals("42", c.value);
		Assert.assertEquals(Integer.valueOf(3), c.number);
	}
	
}
