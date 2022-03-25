package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Test;


/** Test for the {@link Tests} class.
 */
public class TestsTest {
	/** Test case for {@link Tests#requireResource(ClassLoader, String)}.
	 */
	@Test
	public void testRequireResource() {
		final ClassLoader cl = new ClassLoader(Thread.currentThread().getContextClassLoader()) {
			@Override
			public String toString() {
				return "TestClassLoader";
			}
		};
		assertNotNull(Tests.requireResource(cl, Tests.class.getName().replace('.', '/') + ".class"));
		try {
			Tests.requireResource(cl, Tests.class.getName().replace('.', '/') + ".clazz");
			Assert.fail("Expected Exception");
		 } catch (IllegalStateException e) {
			 assertEquals("Failed to locate resource: com/github/jochenw/afw/core/util/Tests.clazz (via ClassLoader TestClassLoader)", e.getMessage());
		 }
		Thread.currentThread().setContextClassLoader(cl);
		assertNotNull(Tests.requireResource(Tests.class.getName().replace('.', '/') + ".class"));
		try {
			Tests.requireResource(Tests.class.getName().replace('.', '/') + ".clazz");
			fail("Expected Exception");
		 } catch (IllegalStateException e) {
			 assertEquals("Failed to locate resource: com/github/jochenw/afw/core/util/Tests.clazz (via ClassLoader TestClassLoader)", e.getMessage());
		}
		Thread.currentThread().setContextClassLoader(cl.getParent());
	}

	/** Test case for {@link Tests#fail(String)}.
	 */
	@Test
	public void testFail() {
		try {
			Tests.fail("Message");
			Assert.fail("Expected Exception");
		} catch (IllegalStateException e) {
			assertEquals("Message", e.getMessage());
		}
	}

	/** Test case for {@link Tests#requireTestDirectory()}.
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testRequireTestDirectory() {
		final File file = Tests.requireTestDirectory();
		assertTrue(file.isDirectory());
		final Path f1 = Tests.requireTestDirectory("F1");
		assertEquals(file.toPath().resolve("F1"), f1);
		assertTrue(Files.isDirectory(f1));
		final Path f2 = Tests.requireTestDirectory(TestsTest.class);
		assertEquals(file.toPath().resolve("TestsTest"), f2);
		assertTrue(Files.isDirectory(f2));
	}

	/** Test case for {@link Tests#assertEquals(byte[], byte[])}.
	 */
	@Test
	public void testAssertEqualsByteArrays() {
		Tests.assertEquals("Foo".getBytes(StandardCharsets.UTF_8), new byte[] {'F', 'o', 'o'});
		try {
			Tests.assertEquals("Foo".getBytes(StandardCharsets.UTF_8), new byte[] {'F', 'o', 'a'});
			Assert.fail("Expected Exception");
		} catch (IllegalStateException e) {
			assertEquals("Assertion failed, expected 111, got 97", e.getMessage());
		}
	}

	/** Test case for {@link Tests#assertEquals(int, int)}.
	 */
	@Test
	public void testAssertEqualsInts() {
		Tests.assertEquals(4, 2+2);
		try {
			Tests.assertEquals(3, 2+2);
			Assert.fail("Expected Exception");
		} catch (IllegalStateException e) {
			assertEquals("Assertion failed, expected 3, got 4", e.getMessage());
		}
	}

	/** Test case for {@link Tests#assertEquals(Object, Object)}.
	 */
	@Test
	public void testAssertEqualsObjects() {
		Tests.assertEquals("FooBar", "Foo" + "Bar");
		try {
			Tests.assertEquals("FooBar", "Foo" + "bar");
			Assert.fail("Expected Exception");
		} catch (IllegalStateException e) {
			assertEquals("Assertion failed, expected FooBar, got Foobar", e.getMessage());
		}
	}

	/** Test case for {@link Tests#assertTrue(boolean)}.
	 */
	@Test
	public void testAssertTrue() {
		Tests.assertTrue(true);
		try {
			Tests.assertTrue(false);
			Assert.fail("Expected Exception");
		} catch (IllegalStateException e) {
			assertEquals("Assertion failed, expected true, got false", e.getMessage());
		}
	}
}
