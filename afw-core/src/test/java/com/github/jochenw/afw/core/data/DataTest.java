package com.github.jochenw.afw.core.data;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import org.junit.Test;

import com.github.jochenw.afw.core.util.Functions.FailableFunction;

/**
 * Test suite for the {@link Data} class.
 */
public class DataTest {
	/**
	 * Test case for {@link Data#getValue(Function, String)}.
	 */
	@Test
	public void testGetValue() {
		final Map<String, Object> map = getMap();
		assertEquals("bar", Data.getValue(map::get, "foo"));
		assertNull(Data.getValue(map::get, "bar"));
	}

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
	 * Test case for {@link Data#getString(Function, String, String)}.
	 */
	@Test
	public void testGetStringFailableFunctionOfStringObjectQStringString() {
		final Map<String, Object> map = getMap();
		assertEquals("bar", Data.getString(map::get, "foo", "foo"));
		assertNull(Data.getString(map::get, "bar", "bar"));
		try {
			Data.getString(map::get, "test", "Description of test");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for Description of test: Expected string, got java.lang.Boolean",
					e.getMessage());
		}
	}

	/**
	 * Test case for {@link Data#requireString(Function, String, String)}.
	 */
	@Test
	public void testRequireStringFailableFunctionOfStringObjectQStringString() {
		final Map<String, Object> map = getMap();
		final FailableFunction<String, Object, ?> func = (s) -> {
			return map.get(s);
		};
		assertEquals("bar", Data.requireString(func, "foo", "foo"));
		try {
			Data.requireString(func, "bar", "baz");
			fail("Expected Exception");
		} catch (NullPointerException e) {
			assertEquals("Missing value for baz", e.getMessage());
		}
		try {
			Data.requireString(func, "empty", "baz");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Empty value for baz", e.getMessage());
		}
		try {
			Data.requireString(func, "test", "Description of test");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for Description of test: Expected string, got java.lang.Boolean",
					e.getMessage());
		}
		try {
			Data.requireString(func, "answer", "Reply to the question");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for Reply to the question: Expected string, got java.lang.Integer",
					e.getMessage());
		}
	}

	/**
	 * Test case for {@link Data#requirePath(Function, String, String)}.
	 */
	@Test
	public void testRequirePathFailableFunctionOfStringObjectQStringString() {
		final Map<String, Object> map = getMap();
		final FailableFunction<String, Object, ?> func = (s) -> {
			return map.get(s);
		};
		assertEquals("bar", Data.requirePath(func, "foo", "foo").toString());
		try {
			Data.requirePath(func, "bar", "baz");
			fail("Expected Exception");
		} catch (NullPointerException e) {
			assertEquals("Missing value for baz", e.getMessage());
		}
		try {
			Data.requirePath(func, "empty", "baz");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Empty value for baz", e.getMessage());
		}
		try {
			Data.requirePath(func, "test", "Description of test");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for Description of test: Expected path, got java.lang.Boolean", e.getMessage());
		}
		Path path = Paths.get(".");
		map.put("path", path);
		assertSame(path, Data.requirePath(func, "path", "path"));
		File file = new File(".");
		map.put("file", file);
		assertEquals(path, Data.requirePath(func, "file", "file"));
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
			assertEquals("Invalid value for Description of test: Expected string, got java.lang.Boolean",
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
			assertEquals("Invalid value for Map key test: Expected string, got java.lang.Boolean", e.getMessage());
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
			assertEquals("Invalid value for Description of test: Expected string, got java.lang.Boolean",
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
			assertEquals("Invalid value for property test: Expected string, got java.lang.Boolean", e.getMessage());
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
			assertEquals("Missing value for baz", e.getMessage());
		}
		try {
			Data.requirePath(map, "empty", "baz");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Empty value for baz", e.getMessage());
		}
		try {
			Data.requirePath(map, "test", "Description of test");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for Description of test: Expected path, got java.lang.Boolean", e.getMessage());
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
			assertEquals("Missing value for Map key bar", e.getMessage());
		}
		try {
			Data.requireString(map, "empty");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Empty value for Map key empty", e.getMessage());
		}
		try {
			Data.requireString(map, "test");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for Map key test: Expected string, got java.lang.Boolean", e.getMessage());
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
			assertEquals("Missing value for some value", e.getMessage());
		}
		try {
			Data.requireString(props, "empty", "some value");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Empty value for some value", e.getMessage());
		}
		try {
			Data.requireString(props, "test", "some value");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for some value: Expected string, got java.lang.Boolean", e.getMessage());
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
			assertEquals("Missing value for Property NoSuchValue", e.getMessage());
		}
		try {
			Data.requireString(props, "empty");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Empty value for Property empty", e.getMessage());
		}
		try {
			Data.requireString(props, "test");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for Property test: Expected string, got java.lang.Boolean", e.getMessage());
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
			assertEquals("Missing value for baz", e.getMessage());
		}
		try {
			Data.requireString(map, "empty", "baz");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Empty value for baz", e.getMessage());
		}
		try {
			Data.requireString(map, "test", "Description of test");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for Description of test: Expected string, got java.lang.Boolean",
					e.getMessage());
		}
		try {
			Data.requireString(map, "answer", "Reply to the question");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for Reply to the question: Expected string, got java.lang.Integer",
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
			assertEquals("Missing value for Map key bar", e.getMessage());
		}
		try {
			Data.requireString(map, "empty");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Empty value for Map key empty", e.getMessage());
		}
		try {
			Data.requireString(map, "test");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for Map key test: Expected string, got java.lang.Boolean",
					e.getMessage());
		}
		try {
			Data.requireString(map, "answer");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for Map key answer: Expected string, got java.lang.Integer",
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
			assertEquals("Missing value for baz", e.getMessage());
		}
		try {
			Data.requirePath(props, "empty", "baz");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Empty value for baz", e.getMessage());
		}
		try {
			Data.requirePath(props, "test", "Description of test");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for Description of test: Expected path, got java.lang.Boolean", e.getMessage());
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
			assertEquals("Missing value for Property bar", e.getMessage());
		}
		try {
			Data.requirePath(props, "empty");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Empty value for Property empty", e.getMessage());
		}
		try {
			Data.requirePath(props, "test");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for Property test: Expected path, got java.lang.Boolean", e.getMessage());
		}
	}

	/** Test case for {@link Data#getBoolean(FailableFunction, String, String)}.
	 */
	@Test
	public void testGetBooleanFailableFunctionOfStringObjectQStringString() {
		final Map<String,Object> map = getMap();
		final FailableFunction<String,Object,?> func = map::get;
		assertEquals(Boolean.TRUE, Data.getBoolean(func, "test", "test"));
		assertNull(Data.getBoolean(func, "NoSuchValue", "NoSuchValue"));
		assertEquals(Boolean.TRUE, Data.getBoolean(func, "b", "b"));
		assertEquals(Boolean.FALSE, Data.getBoolean(func, "foo", "foo"));
		try {
			Data.getBoolean(func, "answer", "answer");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for answer: Expected boolean, or string, got java.lang.Integer", e.getMessage());
		}
		try {
			Data.getBoolean(func, "answer", "answ");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for answ: Expected boolean, or string, got java.lang.Integer", e.getMessage());
		}
	}

	/**
	 * Test case for {@link Data#getBoolean(Map, String, String)}.
	 */
	@Test
	public void testGetBooleanMapOfStringObjectStringString() {
		final Map<String,Object> map = getMap();
		assertEquals(Boolean.TRUE, Data.getBoolean(map, "test", "test"));
		assertNull(Data.getBoolean(map, "NoSuchValue", "NoSuchValue"));
		assertEquals(Boolean.TRUE, Data.getBoolean(map, "b", "b"));
		assertEquals(Boolean.FALSE, Data.getBoolean(map, "foo", "foo"));
		try {
			Data.getBoolean(map, "answer", "answer");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for answer: Expected boolean, or string, got java.lang.Integer", e.getMessage());
		}
		try {
			Data.getBoolean(map, "answer", "answ");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for answ: Expected boolean, or string, got java.lang.Integer", e.getMessage());
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
			assertEquals("Invalid value for Map key answer: Expected boolean, or string, got java.lang.Integer", e.getMessage());
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
			assertEquals("Invalid value for answer: Expected boolean, or string, got java.lang.Integer", e.getMessage());
		}
		try {
			Data.getBoolean(props, "answer", "answ");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for answ: Expected boolean, or string, got java.lang.Integer", e.getMessage());
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
			assertEquals("Invalid value for Property answer: Expected boolean, or string, got java.lang.Integer", e.getMessage());
		}
	}
}
