package com.github.jochenw.afw.di.util;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.Test;

/** Test suite for the {@link Reflection} class.
 */
public class ReflectionTest {
	/** Test class for reading field values: Basically a wrapper for an
	 * internal {@link Map}.
	 */
	public static class MapHolder {
		private final Map<String,Object> map;
		/** Creates a new instance, with the given internal {@link Map}.
		 * @param pMap The created instances internal {@link Map}.
		 */
		public MapHolder(Map<String,Object> pMap) {
			map = pMap;
		}
		/** Creates a new instance, with a newly created, empty
		 * {@link HashMap} as the internal {@link Map}.
		 */
		public MapHolder() {
			this(new HashMap<>());
		}
	}

	/**
	 * Tests, whether we can create a {@link MapHolder} using its
	 * default constructor.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testDefaultConstructor() throws Exception {
		final Constructor<?> cons = MapHolder.class.getConstructor();
		@SuppressWarnings("unchecked")
		final Constructor<Object> constructor = (Constructor<Object>) cons;
		final Supplier<Object> supplier = Reflection.newInstantiator(constructor, null);
		final MapHolder mapHolder1 = (MapHolder) supplier.get();
		assertNotNull(mapHolder1);
		assertNotNull(mapHolder1.map);
		final MapHolder mapHolder2 = (MapHolder) supplier.get();
		assertNotNull(mapHolder2);
		assertNotNull(mapHolder2.map);
		assertNotSame(mapHolder1, mapHolder2);
		assertNotSame(mapHolder1.map, mapHolder2.map);
		
	}

	/**
	 * Tests, whether we can create a {@link MapHolder} using its
	 * alternative constructor, providing the internal {@link Map}
	 * as an argument.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testOtherConstructor() throws Exception {
		final Map<String,Object> map = new HashMap<>();
		final Constructor<?> cons = MapHolder.class.getDeclaredConstructor(Map.class);
		@SuppressWarnings("unchecked")
		final Constructor<Object> constructor = (Constructor<Object>) cons;
		final Supplier<Object> supplier = Reflection.newInstantiator(constructor, (i) -> {
			if (i == 0) {
				return map;
			} else {
				throw new IllegalArgumentException(String.valueOf(i));
			}
		});
		final MapHolder mapHolder1 = (MapHolder) supplier.get();
		assertNotNull(mapHolder1);
		assertNotNull(mapHolder1.map);
		assertSame(map, mapHolder1.map);
		final MapHolder mapHolder2 = (MapHolder) supplier.get();
		assertNotNull(mapHolder2);
		assertNotNull(mapHolder2.map);
		assertNotSame(mapHolder1, mapHolder2);
		assertSame(mapHolder1.map, mapHolder2.map);
	}

	/** Test class for reading, and writing private field values.
	 */
	public static class FieldsClass {
		private static String staticStringField;
		private Object objectField;
	}

	/** Test for setting private field values by using an injector.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testFieldSetter() throws Exception {
		final Field objectFieldFld = FieldsClass.class.getDeclaredField("objectField");
		final Field staticStringFieldFld = FieldsClass.class.getDeclaredField("staticStringField");

		final BiConsumer<Object,Object> objectFieldInjector = Reflection.newInjector(objectFieldFld);
		final FieldsClass fc1 = new FieldsClass();
		assertNull(fc1.objectField);
		objectFieldInjector.accept(fc1, Boolean.TRUE);
		assertSame(Boolean.TRUE, fc1.objectField);
		assertNull(FieldsClass.staticStringField);
		final BiConsumer<Object,Object> staticStringFieldInjector = Reflection.newInjector(staticStringFieldFld);
		assertNull(FieldsClass.staticStringField);
		staticStringFieldInjector.accept(fc1, "42");
		assertSame("42", FieldsClass.staticStringField);
	}

	/** Test class for invocation of setter methods.
	 */
	private static class MethodsClass {
		private static String staticStringField;
		private Object objectField1, objectField2;
		private boolean invoked;

		/** Returns the {@code staticStringField}.
		 * @return The {@code staticStringField}
		 */
		@SuppressWarnings("unused")
		private static String getStaticStringField() {
			return staticStringField;
		}
		/** Sets the {@code staticStringField}.
		 * @param pStaticStringField The {@code staticStringField}
		 */
		@SuppressWarnings("unused")
		private void setStaticStringField(String pStaticStringField) {
			staticStringField = pStaticStringField;
		}
		/** Sets the {@code objectField1}, and the {@code objectField2}.
		 * @param pObjectField1 The {@code objectField1}
		 * @param pObjectField2 The {@code objectField2}
		 */
		@SuppressWarnings("unused")
		private void setObjectFields(Object pObjectField1, Object pObjectField2) {
			objectField1 = pObjectField1;
			objectField2 = pObjectField2;
		}
		/** Returns the {@code objectField1}.
		 * @return The {@code objectField1}
		 */
		@SuppressWarnings("unused")
		public Object getObjectField1() {
			return objectField1;
		}
		/** Returns the {@code objectField2}.
		 * @return The {@code objectField2}
		 */
		@SuppressWarnings("unused")
		public Object getObjectField2() {
			return objectField2;
		}
		/** Returns the value of the "invoked" property.
		 * @return The value of the "invoked" property.
		 */
		public boolean isInvoked() {
			return invoked;
		}
		/** Sets the "invoked" property to true.
		 */
		@SuppressWarnings("unused")
		public void setInvoked() {
			invoked = true;
		}
	}

	/** Test for invocation of methods.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testMethodInvoker() throws Exception {
		final Method objectFieldsMethod = MethodsClass.class.getDeclaredMethod("setObjectFields", Object.class, Object.class);
		final Method staticStringFieldMethod = MethodsClass.class.getDeclaredMethod("setStaticStringField", String.class);

		final BiConsumer<Object,Object[]> objectFieldInjector = Reflection.newInjector(objectFieldsMethod);
		final MethodsClass mc1 = new MethodsClass();
		assertNull(mc1.objectField1);
		assertNull(mc1.objectField2);
		objectFieldInjector.accept(mc1, new Object[] {Boolean.TRUE, Collections.EMPTY_MAP});
		assertSame(Boolean.TRUE, mc1.objectField1);
		assertSame(Collections.EMPTY_MAP, mc1.objectField2);
		assertNull(MethodsClass.staticStringField);
		final BiConsumer<Object,Object[]> staticStringFieldInjector = Reflection.newInjector(staticStringFieldMethod);
		assertNull(MethodsClass.staticStringField);
		staticStringFieldInjector.accept(mc1, new Object[]{"42"});
		assertSame("42", MethodsClass.staticStringField);
	}

	/** Test for {@link Reflection#newInvoker(Method)}
	 * @throws Exception The test failed.
	 */
	@Test
	public void testNewInvoker() throws Exception {
		final MethodsClass mc1 = new MethodsClass();
		final MethodsClass mc2 = new MethodsClass();
		final Method setInvokedMethod = MethodsClass.class.getDeclaredMethod("setInvoked");
		final Consumer<MethodsClass> invoker = Reflection.newInvoker(setInvokedMethod);
		assertFalse(mc1.isInvoked());
		assertFalse(mc2.isInvoked());
		invoker.accept(mc1);
		assertTrue(mc1.isInvoked());
		assertFalse(mc2.isInvoked());
		invoker.accept(mc2);
		assertTrue(mc1.isInvoked());
		assertTrue(mc2.isInvoked());
	}
}
