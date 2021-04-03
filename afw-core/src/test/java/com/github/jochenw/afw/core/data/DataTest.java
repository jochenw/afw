package com.github.jochenw.afw.core.data;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import com.github.jochenw.afw.core.util.Functions.FailableFunction;

/** Test suite for the {@link Data} class.
 */
public class DataTest {
	/** Test case for {@link Data#getValue(Function, String)}.
	 */
	@Test
	public void testGetValue() {
		final Map<String, Object> map = getMap();
		assertEquals("bar", Data.getValue(map::get, "foo"));
		assertNull(Data.getValue(map::get, "bar"));
	}

	protected Map<String, Object> getMap() {
		final Map<String,Object> map = new HashMap<>();
		map.put("foo", "bar");
		map.put("empty", "");
		map.put("answer", Integer.valueOf(42));
		map.put("test", Boolean.TRUE);
		return map;
	}
	protected Properties getProperties() {
		final Properties props = new Properties();
		props.putAll(getMap());
		return props;
	}

	/** Test case for {@link Data#getString(Function, String, String)}.
	 */
	@Test
	public void testGetStringFailableFunctionOfStringObjectQStringString() {
		final Map<String, Object> map = getMap();
		assertEquals("bar", Data.getString(map::get, "foo", (String) null));
		assertNull(Data.getString(map::get, "bar", null));
		try {
			Data.getString(map::get, "test", "Description of test");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for Description of test: Expected string, got java.lang.Boolean", e.getMessage());
		}
	}

	/** Test case for {@link Data#requireString(Function, String, String)}.
	 */
	@Test
	public void testRequireStringFailableFunctionOfStringObjectQStringString() {
		final Map<String, Object> map = getMap();
		final FailableFunction<String, Object, ?> func = (s) -> { return map.get(s); };
		assertEquals("bar", Data.requireString(func, "foo", (String) null));
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
			assertEquals("Invalid value for Description of test: Expected string, got java.lang.Boolean", e.getMessage());
		}
		try {
			Data.requireString(func, "answer", "Reply to the question");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for Reply to the question: Expected string, got java.lang.Integer", e.getMessage());
		}
	}

	/** Test case for {@link Data#requirePath(Function, String, String)}.
	 */
	@Test
	public void testRequirePathFailableFunctionOfStringObjectQStringString() {
		final Map<String, Object> map = getMap();
		final FailableFunction<String, Object, ?> func = (s) -> { return map.get(s); };
		assertEquals("bar", Data.requirePath(func, "foo", (String) null).toString());
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
	}

	/**
	 * Test case for {@link Data#getString(Map, String, String)}.
	 */
	@Test
	public void testGetStringMapOfStringObjectStringString() {
		final Map<String, Object> map = getMap();
		assertEquals("bar", Data.getString(map, "foo", (String) null));
		assertNull(Data.getString(map, "bar", null));
		try {
			Data.getString(map, "test", "Description of test");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for Description of test: Expected string, got java.lang.Boolean", e.getMessage());
		}
	}

	/**
	 * Test case for {@link Data#getString(Map, String)}.
	 */
	@Test
	public void testGetStringMapOfStringObjectString() {
		final Map<String, Object> map = getMap();
		assertEquals("bar", Data.getString(map, "foo"));
		assertNull(Data.getString(map, "bar", null));
		try {
			Data.getString(map, "test");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for map value test: Expected string, got java.lang.Boolean", e.getMessage());
		}
	}

	/**
	 * Test case for {@link Data#getString(Properties, String, String)}.
	 */
	@Test
	public void testGetStringPropertiesStringString() {
		final Properties map = getProperties();
		assertEquals("bar", Data.getString(map, "foo", (String) null));
		assertNull(Data.getString(map, "bar", null));
		try {
			Data.getString(map, "test", "Description of test");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for Description of test: Expected string, got java.lang.Boolean", e.getMessage());
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
		final Map<String,Object> map = getMap();
		assertEquals("bar", Data.requireString(map, "foo", null));
		try {
			Data.requireString(map, "NoSuchValue", "some value");
			fail("Expected Exception");
		} catch (NullPointerException e) {
			assertEquals("Missing value for some value", e.getMessage());
		}
		try {
			Data.requireString(map, "empty", "some value");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Empty value for some value", e.getMessage());
		}
		try {
			Data.requireString(map, "test", "some value");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for some value: Expected string, got java.lang.Boolean", e.getMessage());
		}
	}

	/** Test case for {@link Data#requireString(Map, String)}.
	 */
	@Test
	public void testRequireStringMapOfStringObjectString() {
		// TODO
	}

	@Test
	public void testRequireStringPropertiesStringString() {
		// TODO fail("Not yet implemented");
	}

	@Test
	public void testRequireStringPropertiesString() {
		// TODO fail("Not yet implemented");
	}

	@Test
	public void testRequirePathMapOfStringObjectStringString() {
		// TODO fail("Not yet implemented");
	}

	@Test
	public void testRequirePathMapOfStringObjectString() {
		// TODO fail("Not yet implemented");
	}

	@Test
	public void testRequirePathPropertiesStringString() {
		// TODO fail("Not yet implemented");
	}

	@Test
	public void testRequirePathPropertiesString() {
		// TODO fail("Not yet implemented");
	}

	@Test
	public void testGetBooleanFailableFunctionOfStringObjectQStringString() {
		// TODO fail("Not yet implemented");
	}

	@Test
	public void testGetBooleanMapOfStringObjectStringString() {
		// TODO fail("Not yet implemented");
	}

	@Test
	public void testGetBooleanMapOfStringObjectString() {
		// TODO fail("Not yet implemented");
	}

	@Test
	public void testGetBooleanPropertiesStringString() {
		// TODO fail("Not yet implemented");
	}

	@Test
	public void testGetBooleanPropertiesString() {
		// TODO fail("Not yet implemented");
	}
}
