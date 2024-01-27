package com.github.jochenw.afw.core.data;

import static org.junit.Assert.*;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jspecify.annotations.NonNull;
import org.junit.Test;

import com.github.jochenw.afw.core.data.Data.Accessor.PathCriterion;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.core.util.Tests;

/**
 * Test suite for the {@link Data} class.
 */
public class DataTest {
	/** Creates a map object with basic test data.
	 * @return The created map.
	 */
	protected @NonNull Map<String, Object> getMap() {
		final Map<String, Object> map = new HashMap<>();
		map.put("foo", "bar");
		map.put("empty", "");
		map.put("answer", Integer.valueOf(42));
		map.put("test", Boolean.TRUE);
		map.put("b", "true");
		return map;
	}

	/** Creates a property set with basic test data.
	 * @return The created property set.
	 */
	protected @NonNull Properties getProperties() {
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
		final @NonNull Properties map = getProperties();
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

	/** Test case for {@link Data.Accessor#requirePath(Object, String, String, Data.Accessor.PathCriterion[])}.
	 * @throws Exception The test failed.
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
		Tests.assertThrows(NullPointerException.class, "Missing value for parameter noSuchParameter", () -> Data.MAP_ACCESSOR.requirePath(map, "noSuchParameter", "noSuchParameter"));
		Tests.assertThrows(IllegalStateException.class,
	             "Invalid value for parameter testDir: Expected existing file, got " + testDir,
	             () -> Data.MAP_ACCESSOR.requirePath(map, "testDir", "testDir", Data.FILE_EXISTS));
		Tests.assertThrows(IllegalStateException.class,
	             "Invalid value for parameter testFile: Expected existing directory, got " + existingFile,
	             () -> Data.MAP_ACCESSOR.requirePath(map, "testFile", "testFile", Data.DIR_EXISTS));
		Tests.assertThrows(IllegalStateException.class,
	             "Invalid value for parameter testFile: Expected a non-existing item, got " + existingFile,
	             () -> Data.MAP_ACCESSOR.requirePath(map, "testFile", "testFile", Data.NOT_EXISTS));
	}

	/** Test case for {@link Data.Accessible#getValue(String)}.
	 */
	@Test
	public void testAccessableGetValueString() {
		final Map<String,Object> map = getMap();
		final Data.Accessible acc = new Data.Accessible(map::get);
		assertNull(acc.getValue("unknown"));
		assertEquals("bar", acc.getValue("foo"));
		assertEquals("", acc.getValue("empty"));
		assertEquals(42, ((Integer) acc.requireValue("answer")).intValue());
		assertTrue(((Boolean) acc.requireValue("test")).booleanValue());
		assertEquals("true", acc.getValue("b"));
	}

	/** Test case for {@link Data.Accessible#getString(String)}.
	 */
	@Test
	public void testAccessableGetStringString() {
		final Map<String,Object> map = getMap();
		final Data.Accessible acc = new Data.Accessible(map::get);
		assertNull(acc.getString("unknown"));
		assertEquals("bar", acc.getString("foo"));
		assertEquals("", acc.getString("empty"));
		assertEquals("true", acc.getString("b"));
		try {
			acc.getString("answer");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for parameter answer: Expected string, got java.lang.Integer", e.getMessage());
		}
	}

	/** Test case for {@link Data.Accessible#requireString(String)}.
	 */
	@Test
	public void testAccessableRequireStringString() {
		final Map<String,Object> map = getMap();
		final Data.Accessible acc = new Data.Accessible(map::get);
		assertEquals("bar", acc.requireString("foo"));
		assertEquals("true", acc.requireString("b"));
		try {
			acc.requireString("unknown");
			fail("Expected Exception");
		} catch (NullPointerException e) {
			assertEquals("Missing value for parameter unknown", e.getMessage());
		}
		try {
			acc.requireString("empty");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Empty value for parameter empty", e.getMessage());
		}
		try {
			acc.requireString("answer");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for parameter answer: Expected string, got java.lang.Integer", e.getMessage());
		}
	}

	/** Test case for {@link Data.Accessible#getString(String, String)}
	 */
	@Test
	public void testAccessableGetStringStringString() {
		final Map<String,Object> map = getMap();
		final Data.Accessible acc = new Data.Accessible(map::get);
		assertNull(acc.getString("unknown", "unknown"));
		assertEquals("bar", acc.getString("foo", "foo"));
		assertEquals("", acc.getString("empty", "empty"));
		assertEquals("true", acc.getString("b", "b"));
		try {
			acc.getString("answer", "answ");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for parameter answ: Expected string, got java.lang.Integer", e.getMessage());
		}
	}

	/** Test case for {@link Data.Accessible#requireString(String)}.
	 */
	@Test
	public void testAccessableRequireStringStringString() {
		final Map<String,Object> map = getMap();
		final Data.Accessible acc = new Data.Accessible(map::get);
		assertEquals("bar", acc.requireString("foo", "foo"));
		assertEquals("true", acc.requireString("b", "b"));
		try {
			acc.requireString("unknown", "unknwn");
			fail("Expected Exception");
		} catch (NullPointerException e) {
			assertEquals("Missing value for parameter unknwn", e.getMessage());
		}
		try {
			acc.requireString("empty", "empt");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Empty value for parameter empt", e.getMessage());
		}
		try {
			acc.requireString("answer", "answ");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for parameter answ: Expected string, got java.lang.Integer", e.getMessage());
		}
	}

	/**
	 * Test case for {@link Data.Accessible#requirePath(String)}.
	 */
	@Test
	public void testAccessableRequirePathString() {
		final Properties props = getProperties();
		final Data.Accessible acc = new Data.Accessible(props::get);
		assertEquals("bar", acc.requirePath("foo").toString());
		try {
			acc.requirePath("bar");
			fail("Expected Exception");
		} catch (NullPointerException e) {
			assertEquals("Missing value for parameter bar", e.getMessage());
		}
		try {
			acc.requirePath("empty");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Empty value for parameter empty", e.getMessage());
		}
		try {
			acc.requirePath("test");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for parameter test: Expected path, got java.lang.Boolean", e.getMessage());
		}

		final Map<String,Object> map = Data.asMap("path", Paths.get("."), "file", new File("./tmp"));
		final Data.Accessible acc2 = new Data.Accessible(map::get);
		assertEquals(Paths.get("."), acc2.requirePath("path"));
		assertEquals(Paths.get("./tmp"), acc2.requirePath("file"));
	}

	/**
	 * Test case for {@link Data.Accessible#requirePath(String, String)}.
	 */
	@Test
	public void testAccessableRequirePathStringString() {
		final Properties props = getProperties();
		final Data.Accessible acc = new Data.Accessible(props::get);
		assertEquals("bar", acc.requirePath("foo", "foo").toString());
		try {
			acc.requirePath("bar", "br");
			fail("Expected Exception");
		} catch (NullPointerException e) {
			assertEquals("Missing value for parameter br", e.getMessage());
		}
		try {
			acc.requirePath("empty", "empt");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Empty value for parameter empt", e.getMessage());
		}
		try {
			acc.requirePath("test", "tst");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for parameter tst: Expected path, got java.lang.Boolean", e.getMessage());
		}
		try {
			acc.requirePath("empty", "empt");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Empty value for parameter empt", e.getMessage());
		}
		try {
			acc.requirePath("answer", "answ");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for parameter answ: Expected path, got java.lang.Integer", e.getMessage());
		}

		final Map<String,Object> map = Data.asMap("path", Paths.get("."), "file", new File("./tmp"));
		final Data.Accessible acc2 = new Data.Accessible(map::get);
		assertEquals(Paths.get("."), acc2.requirePath("path", "path"));
		assertEquals(Paths.get("./tmp"), acc2.requirePath("file", "file"));
	}

	/** Test case for {@link Data.Accessible#requirePath(String, String, PathCriterion[])}
	 */
	@Test
	public void testAccessableRequirePathStringStringPathCriteria() {
		final Map<String,Object> map = Data.asMap("pom", "pom.xml",
				                                  "unknown", "noSuchFile.xml",
				                                  "empty", "",
				                                  "src", "src",
				                                  "path", Paths.get("."),
				                                  "file", new File("./target"));
		final Data.Accessible acc = Data.Accessible.of(map::get);
		assertEquals(Paths.get("pom.xml"), acc.requirePath("pom", "pom", (PathCriterion[]) null));
		assertEquals(Paths.get("pom.xml"), acc.requirePath("pom", "pom", Data.FILE_EXISTS));
		assertEquals(Paths.get("src"), acc.requirePath("src", "src", (PathCriterion[]) null));
		assertEquals(Paths.get("src"), acc.requirePath("src", "src", (PathCriterion[]) null));
		assertEquals(Paths.get("src"), acc.requirePath("src", "src", Data.DIR_EXISTS));
		try {
			acc.requirePath("pom", "pom", Data.DIR_EXISTS);
			fail("Expected Exception");
		} catch (IllegalStateException e) {
			assertEquals("Invalid value for parameter pom: Expected existing directory, got pom.xml", e.getMessage());
		}
		try {
			acc.requirePath("empty", "empt", (PathCriterion[]) null);
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Empty value for parameter empt", e.getMessage());
		}
		try {
			acc.requirePath("null", "nul", (PathCriterion[]) null);
			fail("Expected Exception");
		} catch (NullPointerException e) {
			assertEquals("Missing value for parameter nul", e.getMessage());
		}
	}

    /** Test case for {@link Data.Accessible#getBoolean(String, String)}.
     */
	@Test
	public void testAccessableGetBooleanStringString() {
		final Data.Accessible acc = Data.Accessible.of(Data.asMap("true", "true",
				                                                   "TRUE", Boolean.TRUE,
				                                                   "false", "false",
				                                                   "FALSE", Boolean.FALSE,
				                                                   "answer", Integer.valueOf(42)));
		assertTrue(acc.requireBoolean("true", "true"));
		assertTrue(acc.requireBoolean("TRUE", "TRUE"));
		assertFalse(acc.requireBoolean("false", "false"));
		assertFalse(acc.requireBoolean("FALSE", "FALSE"));
		assertNull(acc.getBoolean("noSuchKey", "noSuchKey"));
		try {
			acc.getBoolean("answer", "answ");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for parameter answ: Expected string, or boolean, got java.lang.Integer", e.getMessage());
		}
	}


	/** Test case for {@link Data#getBoolean(Map,String,String)}.
     */
	@Test
	public void testAccessableGetBooleanString() {
		final @NonNull Map<String,Object> map = Data.asMap("true", "true",
				                                  "TRUE", Boolean.TRUE,
				                                  "false", "false",
				                                  "FALSE", Boolean.FALSE,
				                                  "answer", Integer.valueOf(42));
		assertTrue(Data.requireBoolean(map, "true", "true"));
		assertTrue(Data.requireBoolean(map, "TRUE", "TRUE"));
		assertFalse(Data.requireBoolean(map, "false", "false"));
		assertFalse(Data.requireBoolean(map, "FALSE", "FALSE"));
		assertNull(Data.getBoolean(map, "noSuchKey", "noSuchKey"));
		try {
			Data.getBoolean(map, "answer", "answ");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid value for parameter answ: Expected string, or boolean, got java.lang.Integer", e.getMessage());
		}
	}

	/** Test case for {@link Data.Accessible#of(Properties)}
	 */
	@Test
	public void testAccessibleOfProperties() {
		/** 		map.put("foo", "bar");
		map.put("empty", "");
		map.put("answer", Integer.valueOf(42));
		map.put("test", Boolean.TRUE);
		map.put("b", "true"); */
		final Data.Accessible acc = Data.Accessible.of(getProperties());
		assertEquals("bar", acc.getString("foo"));
		assertEquals("", acc.getString("empty"));
		final @NonNull Boolean b = Objects.requireNonNull(acc.getBoolean("test"));
		assertTrue(b.booleanValue());
	}

	/** Test case for {@link Data.Accessible#of(Map)}
	 */
	@Test
	public void testAccessibleOfMap() {
		final Data.Accessible acc = Data.Accessible.of(getMap());
		assertEquals("bar", acc.getString("foo"));
		assertEquals("", acc.getString("empty"));
		final @NonNull Boolean b = Objects.requireNonNull(acc.getBoolean("test"));
		assertTrue(b.booleanValue());
	}

	/** Test case for {@link Data#requirePath(Map,String)}
	 */
	@Test
	public void testRequirePathMapString() {
		final Map<String,Object> map = Data.asMap(
				"pom", "pom.xml",
                "unknown", "noSuchFile.xml",
                "empty", "",
                "src", "src",
                "path", Paths.get("."),
                "file", new File("./target"));
		assertEquals(Paths.get("pom.xml"), Data.requirePath(map, "pom"));
		assertEquals(Paths.get("src"), Data.requirePath(map, "src"));
		assertEquals(Paths.get("./target"), Data.requirePath(map, "file"));
		assertEquals(Paths.get("."), Data.requirePath(map, "path"));
	}

	/** Test case for {@link Data.Accessor#requirePath(Object,String)}
	 */
	@Test
	public void testAccessorRequirePathMapString() {
		final Map<String,Object> map = Data.asMap(
				"pom", "pom.xml",
                "unknown", "noSuchFile.xml",
                "empty", "",
                "src", "src",
                "path", Paths.get("."),
                "file", new File("./target"));
		assertEquals(Paths.get("pom.xml"), Data.MAP_ACCESSOR.requirePath(map, "pom"));
		assertEquals(Paths.get("src"), Data.MAP_ACCESSOR.requirePath(map, "src"));
		assertEquals(Paths.get("./target"), Data.MAP_ACCESSOR.requirePath(map, "file"));
		assertEquals(Paths.get("."), Data.MAP_ACCESSOR.requirePath(map, "path"));
	}

	/** Test case for {@link Data.Accessor#requireString(Object,String,String)}
	 */
	@Test
	public void testAccessorRequireStringObjectString() {
		final @NonNull Map<String,Object> map = getMap();
		assertEquals("bar", Data.MAP_ACCESSOR.requireString(map, "foo", "foo"));
		try {
			Data.MAP_ACCESSOR.requireString(map, "empty", "empt");
			fail("Expected Exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Empty value for parameter empt", e.getMessage());
		}
	}
}
