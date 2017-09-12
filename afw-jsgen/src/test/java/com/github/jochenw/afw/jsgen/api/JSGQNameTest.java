package com.github.jochenw.afw.jsgen.api;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;


public class JSGQNameTest {
	@Test
	public void testPrimitives() throws Exception {
		for (Class<?> primitiveClass : new Class<?>[]{
			Integer.TYPE, Long.TYPE, Short.TYPE, Byte.TYPE, Character.TYPE, Double.TYPE, Float.TYPE, Boolean.TYPE, Void.TYPE
		}) {
			final JSGQName primitiveName = JSGQName.newInstance(primitiveClass);
			String name = primitiveClass.getName();
			Assert.assertEquals(name, primitiveName.getQName());
			Assert.assertEquals(primitiveClass.getName(), primitiveName.getSimpleName());
			Assert.assertEquals("", primitiveName.getPackageName());
			assertTrue(primitiveName.isPrimitive());
			assertFalse(primitiveName.isArray());
			assertFalse(primitiveName.isInnerName());

			final String instanceName = name.toUpperCase(Locale.US);
			Field field = JSGQName.class.getField(instanceName);
			assertNotNull(field);
			final JSGQName staticInstance = (JSGQName) field.get(null);
			assertEquals(primitiveName, staticInstance);
		}
	}

	@Test
	public void testObjectTypes() throws Exception {
		for (Class<?> baseClass : new Class<?>[]{
			Integer.class, Long.class, Short.class, Byte.class, Character.class, Double.class, Float.class, Boolean.class,
			Object.class, String.class, Map.class, List.class, Set.class
		}) {
			final JSGQName baseName = JSGQName.newInstance(baseClass);
			String instanceName = baseName.getSimpleName().toUpperCase(Locale.US) + "_OBJ";
			String packageName = "java.lang";
			switch(instanceName) {
			case "LIST_OBJ":
				packageName = "java.util";
				instanceName = "LIST";
				break;
			case "MAP_OBJ":
			packageName = "java.util";
			instanceName = "MAP";
			break;
			case "INTEGER_OBJ":
				instanceName = "INT_OBJ";
				break;
			case "CHARACTER_OBJ":
				instanceName = "CHAR_OBJ";
				break;
			case "OBJECT_OBJ":
				instanceName = "OBJECT";
				break;
			case "SET_OBJ":
				packageName = "java.util";
				instanceName = "SET";
				break;
			case "STRING_OBJ":
				instanceName = "STRING";
				break;
			default:
				break;
			}
			Assert.assertEquals(baseClass.getName(), baseName.getQName());
			Assert.assertEquals(baseClass.getSimpleName(), baseName.getSimpleName());
			Assert.assertEquals(packageName, baseName.getPackageName());
			assertFalse(baseName.isPrimitive());
			assertFalse(baseName.isArray());
			assertFalse(baseName.isInnerName());

			Field field = JSGQName.class.getField(instanceName);
			assertNotNull(field);
			final JSGQName staticInstance = (JSGQName) field.get(null);
			assertEquals(baseName, staticInstance);
		}
	}

	private void assertEquals(JSGQName pExpect, JSGQName pGot) {
		Assert.assertEquals(pExpect.getQName(), pGot.getQName());
		Assert.assertEquals(pExpect.getSimpleName(), pGot.getSimpleName());
		Assert.assertEquals(pExpect.getPackageName(), pGot.getPackageName());
		Assert.assertEquals(pExpect.isArray(), pGot.isArray());
		Assert.assertEquals(pExpect.isInnerName(), pGot.isInnerName());
		Assert.assertEquals(pExpect.isPrimitive(), pGot.isPrimitive());
		if (pExpect.isArray()) {
			assertEquals(pExpect.getComponentName(), pGot.getComponentName());
		}
		if (pExpect.isInnerName()) {
			assertEquals(pExpect.getOuterName(), pGot.getOuterName());
		}
	}

	@Test
	public void testStandardClasses() {
		final JSGQName myName = JSGQName.newInstance(JSGQNameTest.class);
		final String qName = JSGQNameTest.class.getName();
		final int offset = qName.lastIndexOf('.');
		final String packageName = qName.substring(0, offset);
		final String className = qName.substring(offset+1);
		Assert.assertEquals(qName, myName.getQName());
		Assert.assertEquals(packageName, myName.getPackageName());
		Assert.assertEquals(className, myName.getSimpleName());
	}

	@Test
	public void testArrays() {
		final Class<?> cl = Object[].class;
		final JSGQName name = JSGQName.newInstance(cl);
		Assert.assertEquals(Object.class.getName(), name.getQName());
		Assert.assertEquals("java.lang", name.getPackageName());
		Assert.assertEquals(Object.class.getSimpleName(), name.getSimpleName());
		Assert.assertTrue(name.isArray());
		Assert.assertFalse(name.isPrimitive());
		Assert.assertFalse(name.isInnerName());
		assertEquals(JSGQName.OBJECT, name.getComponentName());
	}
}
