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

public class ReflectionTest {
	public static class MapHolder {
		private final Map<String,Object> map;
		public MapHolder(Map<String,Object> pMap) {
			map = pMap;
		}
		public MapHolder() {
			this(new HashMap<>());
		}
	}

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

	public static class FieldsClass {
		private static String staticStringField;
		private Object objectField;
	}

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

	private static class MethodsClass {
		private static String staticStringField;
		private Object objectField1, objectField2;

		private static String getStaticStringField() {
			return staticStringField;
		}
		private void setStaticStringField(String pStaticStringField) {
			staticStringField = pStaticStringField;
		}
		private void setObjectFields(Object pObjectField1, Object pObjectField2) {
			objectField1 = pObjectField1;
			objectField2 = pObjectField2;
		}
		public Object getObjectField1() {
			return objectField1;
		}
		public Object getObjectField2() {
			return objectField2;
		}
	}

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
}
