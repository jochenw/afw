package com.github.jochenw.afw.jsgen.api;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.junit.Test;

public class JSGQNameTest {
	@Test
	public void testInstanceFromClass() {
		final JSGQName name = JSGQName.valueOf(JSGQNameTest.class);
		assertNotNull(name);
		assertEquals("com.github.jochenw.afw.jsgen.api", name.getPackageName());
		assertEquals("JSGQNameTest", name.getClassName());
		assertEquals(JSGQNameTest.class.getName(), name.getQName());
	}

	@Test
	public void testInstanceFromString() {
		final JSGQName name = JSGQName.valueOf(JSGQNameTest.class.getName());
		assertNotNull(name);
		assertEquals("com.github.jochenw.afw.jsgen.api", name.getPackageName());
		assertEquals("JSGQNameTest", name.getClassName());
		assertEquals(JSGQNameTest.class.getName(), name.getQName());
	}

	@Test
	public void testPrimitiveClasses() {
		assertPrimitiveType(JSGQName.BOOLEAN_TYPE, Boolean.TYPE);
		assertPrimitiveType(JSGQName.BYTE_TYPE, Byte.TYPE);
		assertPrimitiveType(JSGQName.CHAR_TYPE, Character.TYPE);
		assertPrimitiveType(JSGQName.DOUBLE_TYPE, Double.TYPE);
		assertPrimitiveType(JSGQName.FLOAT_TYPE, Float.TYPE);
		assertPrimitiveType(JSGQName.INT_TYPE, Integer.TYPE);
		assertPrimitiveType(JSGQName.LONG_TYPE, Long.TYPE);
		assertPrimitiveType(JSGQName.SHORT_TYPE, Short.TYPE);
	}

	public void testObjectClasses() {
		assertObjectType(JSGQName.BOOLEAN_OBJ, Boolean.class);
		assertObjectType(JSGQName.BYTE_OBJ, Byte.class);
		assertObjectType(JSGQName.CHAR_OBJ, Character.class);
		assertObjectType(JSGQName.DOUBLE_OBJ, Double.class);
		assertObjectType(JSGQName.FLOAT_OBJ, Float.class);
		assertObjectType(JSGQName.INT_OBJ, Integer.class);
		assertObjectType(JSGQName.LONG_OBJ, Long.class);
		assertObjectType(JSGQName.SHORT_OBJ, Short.class);
		assertObjectType(JSGQName.OBJECT, Object.class);
		assertObjectType(JSGQName.STRING, String.class);
		assertObjectType(JSGQName.COLLECTION, Collection.class);
		assertObjectType(JSGQName.SET, Set.class);
		assertObjectType(JSGQName.LIST, List.class);
		assertObjectType(JSGQName.MAP, Map.class);
		assertObjectType(JSGQName.ARRAYLIST, ArrayList.class);
		assertObjectType(JSGQName.HASHMAP, HashMap.class);
	}

	private void assertObjectType(@Nonnull JSGQName pType, @Nonnull Class<?> pClass) {
		assertFalse(pClass.isPrimitive());
		assertFalse(pType.isPrimitive());
		assertFalse(pClass.isArray());
		assertFalse(pType.isArray());
		assertEquals(pClass.getName(), pType.getQName());
		assertEquals(pClass.getSimpleName(), pType.getClassName());
		assertEquals(pClass.getName(), pType.getPackageName() + "." + pType.getClassName());
		assertEquals(pClass.getName(), pType.toString());
		assertFalse(pType.hasQualifiers());
		assertEquals(0, pType.getQualifiers().size());
		assertEquals(pType, JSGQName.valueOf(pClass));
	}

	private void assertPrimitiveType(@Nonnull JSGQName pType, @Nonnull Class<?> pClass) {
		assertTrue(pClass.isPrimitive());
		assertTrue(pType.isPrimitive());
		assertFalse(pClass.isArray());
		assertFalse(pType.isArray());
		assertEquals("", pType.getPackageName());
		assertEquals(pClass.getName(), pType.getClassName());
		assertEquals(pClass.getName(), pType.getQName());
		assertEquals(pClass.getName(), pType.toString());
		assertFalse(pType.hasQualifiers());
		assertEquals(0, pType.getQualifiers().size());
		assertSame(JSGQName.valueOf(pClass), pType);
	}

	
	@Test
	public void testErrors() {
		try {
			JSGQName.valueOf((String) null);
			fail("Expected Exception");
		} catch (NullPointerException e) {
			assertEquals("Qualified Name", e.getMessage());
		}

		try {
			JSGQName.valueOf((Class<?>) null);
			fail("Expected Exception");
		} catch (NullPointerException e) {
			assertEquals("Type", e.getMessage());
		}

		try {
			JSGQName.valueOf("MyType");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid class name (Missing package): MyType", e.getMessage());
		}

		try {
			JSGQName.valueOf("com.foo.A$B");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid class name (Inner classes are unsupported): com.foo.A$B", e.getMessage());
		}

	}
}
