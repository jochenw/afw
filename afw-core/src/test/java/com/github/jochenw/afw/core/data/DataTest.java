package com.github.jochenw.afw.core.data;

import static org.junit.Assert.*;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import com.github.jochenw.afw.core.util.Tests;

/**
 * Test suite for the {@link Data} class.
 */
public class DataTest {
	protected Map<String, Object> getMap() {
		final Map<String, Object> map = new HashMap<>();
		map.put("foo", "bar");
		map.put("empty", "");
		map.put("answer", Integer.valueOf(42));
		map.put("test", Boolean.TRUE);
		map.put("b", "true");
		return map;
	}

	protected Properties getProperties() {
		final Properties props = new Properties();
		props.putAll(getMap());
		return props;
	}


	/**
	 * Test case for {@link Data#getString(Map, String, String)}.
	 */
	@Test
	public void testGetStringMapOfStringObjectStringString() {
		final Map<String, Object> map = getMap();
		assertEquals("bar", Data.getString(map, "foo", "foo"));
		assertNull(Data.getString(map, "bar", "bar"));
		try {
			Data.getString(map, "test", "Description of test");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for parameter Description of test: Expected string, got java.lang.Boolean",
					e.getMessage());
		}
	}

	/**
	 * Test case for {@link Data#getString(Map, String)}.
	 */
	@Test
	public void testGetStringMapOfStringObjectString() {
		final Map<String, Object> map = getMap();
		assertEquals("bar", Data.getString(map, "foo"));
		assertNull(Data.getString(map, "bar", "bar"));
		try {
			Data.getString(map, "test");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for parameter test: Expected string, got java.lang.Boolean", e.getMessage());
		}
	}

	/**
	 * Test case for {@link Data#getString(Properties, String, String)}.
	 */
	@Test
	public void testGetStringPropertiesStringString() {
		final Properties map = getProperties();
		assertEquals("bar", Data.getString(map, "foo", "foo"));
		assertNull(Data.getString(map, "bar", "bar"));
		try {
			Data.getString(map, "test", "Description of test");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for parameter Description of test: Expected string, got java.lang.Boolean",
					e.getMessage());
		}
	}

	/**
	 * Test case for {@link Data#getString(Properties, String)}.
	 */
	@Test
	public void testGetStringPropertiesString() {
		final Properties map = getProperties();
		assertEquals("bar", Data.getString(map, "foo"));
		assertNull(Data.getString(map, "bar"));
		try {
			Data.getString(map, "test");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for parameter test: Expected string, got java.lang.Boolean", e.getMessage());
		}
	}

	/**
	 * Test case for {@link Data#requireString(Map, String, String)}.
	 */
	@Test
	public void testRequireStringMapOfStringObjectStringString() {
		final Map<String, Object> map = getMap();
		assertEquals("bar", Data.requirePath(map, "foo", "foo").toString());
		try {
			Data.requirePath(map, "bar", "baz");
			fail("Expected Exception");
		} catch (NullPointerException e) {
			assertEquals("Missing value for parameter baz", e.getMessage());
		}
		try {
			Data.requirePath(map, "empty", "baz");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Empty value for parameter baz", e.getMessage());
		}
		try {
			Data.requirePath(map, "test", "Description of test");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for parameter Description of test: Expected path, got java.lang.Boolean", e.getMessage());
		}
	}

	/**
	 * Test case for {@link Data#requireString(Map, String)}.
	 */
	@Test
	public void testRequireStringMapOfStringObjectString() {
		final Map<String, Object> map = getMap();
		assertEquals("bar", Data.requireString(map, "foo"));
		try {
			Data.requireString(map, "bar");
			fail("Expected Exception");
		} catch (NullPointerException e) {
			assertEquals("Missing value for parameter bar", e.getMessage());
		}
		try {
			Data.requireString(map, "empty");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Empty value for parameter empty", e.getMessage());
		}
		try {
			Data.requireString(map, "test");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for parameter test: Expected string, got java.lang.Boolean", e.getMessage());
		}
	}

	/**
	 * Test case for {@link Data#requireString(Properties, String, String)}.
	 */
	@Test
	public void testRequireStringPropertiesStringString() {
		final Properties props = getProperties();
		assertEquals("bar", Data.requireString(props, "foo", "foo"));
		try {
			Data.requireString(props, "NoSuchValue", "some value");
			fail("Expected Exception");
		} catch (NullPointerException e) {
			assertEquals("Missing value for parameter some value", e.getMessage());
		}
		try {
			Data.requireString(props, "empty", "some value");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Empty value for parameter some value", e.getMessage());
		}
		try {
			Data.requireString(props, "test", "some value");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for parameter some value: Expected string, got java.lang.Boolean", e.getMessage());
		}
	}

	/**
	 * Test case for {@link Data#requireString(Properties, String)}.
	 */
	@Test
	public void testRequireStringPropertiesString() {
		final Properties props = getProperties();
		assertEquals("bar", Data.requireString(props, "foo"));
		try {
			Data.requireString(props, "NoSuchValue");
			fail("Expected Exception");
		} catch (NullPointerException e) {
			assertEquals("Missing value for parameter NoSuchValue", e.getMessage());
		}
		try {
			Data.requireString(props, "empty");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Empty value for parameter empty", e.getMessage());
		}
		try {
			Data.requireString(props, "test");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for parameter test: Expected string, got java.lang.Boolean", e.getMessage());
		}
	}

	/**
	 * Test case for {@link Data#requirePath(Map, String, String)}.
	 */
	@Test
	public void testRequirePathMapOfStringObjectStringString() {
		final Map<String, Object> map = getMap();
		assertEquals("bar", Data.requireString(map, "foo", "foo"));
		try {
			Data.requireString(map, "bar", "baz");
			fail("Expected Exception");
		} catch (NullPointerException e) {
			assertEquals("Missing value for parameter baz", e.getMessage());
		}
		try {
			Data.requireString(map, "empty", "baz");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Empty value for parameter baz", e.getMessage());
		}
		try {
			Data.requireString(map, "test", "Description of test");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for parameter Description of test: Expected string, got java.lang.Boolean",
					e.getMessage());
		}
		try {
			Data.requireString(map, "answer", "Reply to the question");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for parameter Reply to the question: Expected string, got java.lang.Integer",
					e.getMessage());
		}
	}

	/**
	 * Test case for {@link Data#requirePath(Map, String)}.
	 */
	@Test
	public void testRequirePathMapOfStringObjectString() {
		final Map<String, Object> map = getMap();
		assertEquals("bar", Data.requireString(map, "foo"));
		try {
			Data.requireString(map, "bar");
			fail("Expected Exception");
		} catch (NullPointerException e) {
			assertEquals("Missing value for parameter bar", e.getMessage());
		}
		try {
			Data.requireString(map, "empty");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Empty value for parameter empty", e.getMessage());
		}
		try {
			Data.requireString(map, "test");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for parameter test: Expected string, got java.lang.Boolean",
					e.getMessage());
		}
		try {
			Data.requireString(map, "answer");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for parameter answer: Expected string, got java.lang.Integer",
					e.getMessage());
		}
	}

	/**
	 * Test case for {@link Data#requirePath(Properties, String, String)}.
	 */
	@Test
	public void testRequirePathPropertiesStringString() {
		final Properties props = getProperties();
		assertEquals("bar", Data.requirePath(props, "foo", "foo").toString());
		try {
			Data.requirePath(props, "bar", "baz");
			fail("Expected Exception");
		} catch (NullPointerException e) {
			assertEquals("Missing value for parameter baz", e.getMessage());
		}
		try {
			Data.requirePath(props, "empty", "baz");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Empty value for parameter baz", e.getMessage());
		}
		try {
			Data.requirePath(props, "test", "Description of test");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for parameter Description of test: Expected path, got java.lang.Boolean", e.getMessage());
		}
	}

	/**
	 * Test case for {@link Data#requirePath(Properties, String)}.
	 */
	@Test
	public void testRequirePathPropertiesString() {
		final Properties props = getProperties();
		assertEquals("bar", Data.requirePath(props, "foo").toString());
		try {
			Data.requirePath(props, "bar");
			fail("Expected Exception");
		} catch (NullPointerException e) {
			assertEquals("Missing value for parameter bar", e.getMessage());
		}
		try {
			Data.requirePath(props, "empty");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Empty value for parameter empty", e.getMessage());
		}
		try {
			Data.requirePath(props, "test");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for parameter test: Expected path, got java.lang.Boolean", e.getMessage());
		}
	}

	/**
	 * Test case for {@link Data#getBoolean(Map, String)}.
	 */
	@Test
	public void testGetBooleanMapOfStringObjectString() {
		final Map<String,Object> map = getMap();
		assertEquals(Boolean.TRUE, Data.getBoolean(map, "test"));
		assertNull(Data.getBoolean(map, "NoSuchValue"));
		assertEquals(Boolean.TRUE, Data.getBoolean(map, "b"));
		assertEquals(Boolean.FALSE, Data.getBoolean(map, "foo"));
		try {
			Data.getBoolean(map, "answer");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for parameter answer: Expected string, or boolean, got java.lang.Integer", e.getMessage());
		}
	}

	/**
	 * Test case for {@link Data#getBoolean(Properties, String, String)}.
	 */
	@Test
	public void testGetBooleanPropertiesStringString() {
		final Properties props = getProperties();
		assertEquals(Boolean.TRUE, Data.getBoolean(props, "test", "test"));
		assertNull(Data.getBoolean(props, "NoSuchValue", "NoSuchValue"));
		assertEquals(Boolean.TRUE, Data.getBoolean(props, "b", "b"));
		assertEquals(Boolean.FALSE, Data.getBoolean(props, "foo", "foo"));
		try {
			Data.getBoolean(props, "answer", "answer");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for parameter answer: Expected string, or boolean, got java.lang.Integer", e.getMessage());
		}
		try {
			Data.getBoolean(props, "answer", "answ");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for parameter answ: Expected string, or boolean, got java.lang.Integer", e.getMessage());
		}
	}

	/**
	 * Test case for {@link Data#getBoolean(Map, String)}.
	 */
	@Test
	public void testGetBooleanPropertiesString() {
		final Properties props = getProperties();
		assertEquals(Boolean.TRUE, Data.getBoolean(props, "test"));
		assertNull(Data.getBoolean(props, "NoSuchValue"));
		assertEquals(Boolean.TRUE, Data.getBoolean(props, "b"));
		assertEquals(Boolean.FALSE, Data.getBoolean(props, "foo"));
		try {
			Data.getBoolean(props, "answer");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for parameter answer: Expected string, or boolean, got java.lang.Integer", e.getMessage());
		}
	}

	/** Test case for {@link Accessor#requirePath(Object, String, String, PathCriterion[])}.
	 */
	@Test
	public void testRequireStringObjectStringStringPathCriteria() throws Exception {
		final Path testDir = Paths.get("target/unit-tests/DataTest");
		final Path existingFile = testDir.resolve("existingFile");
		final Path notExistingFile = testDir.resolve("noSuchFile");
		Files.createDirectories(testDir);
		// Create existingFile as an empty file.
		try (OutputStream out = Files.newOutputStream(existingFile)) {
		}
		final Map<String,Object> map = new HashMap<String, Object>();
		map.put("testDir", testDir);
		map.put("testFile", existingFile);
		map.put("noSuchFile", notExistingFile);
		Data.MAP_ACCESSOR.requirePath(map, "testDir", "testDir", Data.DIR_EXISTS);
		Data.MAP_ACCESSOR.requirePath(map, "testFile", "testFile", Data.FILE_EXISTS);
		Data.MAP_ACCESSOR.requirePath(map, "noSuchFile", "noSuchFile", Data.NOT_EXISTS);
		Tests.assertThrowing(NullPointerException.class, "Missing value for parameter noSuchParameter", () -> Data.MAP_ACCESSOR.requirePath(map, "noSuchParameter", "noSuchParameter"));
		Tests.assertThrowing(IllegalStateException.class,
	             "Invalid value for parameter testDir: Expected existing file, got " + testDir,
	             () -> Data.MAP_ACCESSOR.requirePath(map, "testDir", "testDir", Data.FILE_EXISTS));
		Tests.assertThrowing(IllegalStateException.class,
	             "Invalid value for parameter testFile: Expected existing directory, got " + existingFile,
	             () -> Data.MAP_ACCESSOR.requirePath(map, "testFile", "testFile", Data.DIR_EXISTS));
		Tests.assertThrowing(IllegalStateException.class,
	             "Invalid value for parameter testFile: Expected a non-existing item, got " + existingFile,
	             () -> Data.MAP_ACCESSOR.requirePath(map, "testFile", "testFile", Data.NOT_EXISTS));
	}
}
