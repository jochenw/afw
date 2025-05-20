package com.github.jochenw.afw.core.json;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;

import org.junit.Test;

import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.core.util.MutableInteger;
import com.github.jochenw.afw.core.util.tests.Tests;




/** Test suite for the {@link JsnWriter} class.
 */
public class JsnWriterTest {
	private static final BigInteger TWO = new BigInteger("2");

	/** Test case for {@link JsnWriter#toBytes(Object)}.
	 * @throws Exception The test fails.
	 */
	@Test
	public void testToBytes() throws Exception {
		final Map<String,Object> map = newSampleMap();
		final byte[] bytes = JsnUtils.writer().toBytes(map);
		assertFalse(new String(bytes, StandardCharsets.UTF_8).contains("\n"));
		try (InputStream in = new ByteArrayInputStream(bytes);
			 JsonReader jr = Json.createReader(in)) {
			final JsonObject jo = jr.readObject();
			validateSampleMap(jo);
		}
	}

	/** Test case for {@link JsnWriter#write(java.nio.file.Path, Object)}.
	 * @throws Exception The test fails.
	 */
	@Test
	public void testWritePath() throws Exception {
		final Path testDir = Tests.requireTestDirectory(JsnWriterTest.class);
		Files.createDirectories(testDir);
		final Path testFile = Files.createTempFile(testDir, "tst", ".json");
		final Map<String,Object> map = newSampleMap();
		JsnUtils.writer().write(testFile, map);
		final byte[] bytes = Files.readAllBytes(testFile);
		assertFalse(new String(bytes, StandardCharsets.UTF_8).contains("\n"));
		try (InputStream in = new ByteArrayInputStream(bytes);
			 JsonReader jr = Json.createReader(in)) {
			final JsonObject jo = jr.readObject();
			validateSampleMap(jo);
		}
	}

	/** Test case for {@link JsnWriter#write(JsonGenerator,Object)}
	 * @throws Exception The test fails.
	 */
	@Test
	public void testWriteJsonGenerator() throws Exception {
		final Path testDir = Tests.requireTestDirectory(JsnWriterTest.class);
		Files.createDirectories(testDir);
		final Path testFile = Files.createTempFile(testDir, "tst", ".json");
		final Map<String,Object> map = newSampleMap();
		try (OutputStream out = Files.newOutputStream(testFile);
			 JsonGenerator jg = Json.createGenerator(out)) {
			JsnUtils.writer().write(jg, map);
		}
		final byte[] bytes = Files.readAllBytes(testFile);
		assertFalse(new String(bytes, StandardCharsets.UTF_8).contains("\n"));
		try (InputStream in = new ByteArrayInputStream(bytes);
			 JsonReader jr = Json.createReader(in)) {
			final JsonObject jo = jr.readObject();
			validateSampleMap(jo);
		}
	}

	/** Test case for {@link JsnWriter#write(java.io.File, Object)}.
	 * @throws Exception The test fails.
	 */
	@Test
	public void testWriteFile() throws Exception {
		final Path testDir = Tests.requireTestDirectory(JsnWriterTest.class);
		Files.createDirectories(testDir);
		final File testFile = Files.createTempFile(testDir, "tst", ".json").toFile();
		final Map<String,Object> map = newSampleMap();
		JsnUtils.writer().write(testFile, map);
		final byte[] bytes = Files.readAllBytes(testFile.toPath());
		assertFalse(new String(bytes, StandardCharsets.UTF_8).contains("\n"));
		try (InputStream in = new ByteArrayInputStream(bytes);
			 JsonReader jr = Json.createReader(in)) {
			final JsonObject jo = jr.readObject();
			validateSampleMap(jo);
		}
	}

	/** Test case for {@link JsnWriter#toBytes(Object)}, with pretty print.
	 * @throws Exception The test fails.
	 */
	@Test
	public void testToBytesWithPrettyPrint() throws Exception {
		final Map<String,Object> map = newSampleMap();
		final byte[] bytes = JsnUtils.writer().usingPrettyPrint().toBytes(map);
		assertTrue(new String(bytes, StandardCharsets.UTF_8).contains("\n"));
		try (InputStream in = new ByteArrayInputStream(bytes);
			 JsonReader jr = Json.createReader(in)) {
			final JsonObject jo = jr.readObject();
			validateSampleMap(jo);
		}
	}

	/** Test case for {@link JsnWriter#toBytes(Object)}, with pretty print, and ordering.
	 * @throws Exception The test fails.
	 */
	@Test
	public void testToBytesWithPrettyPrintAndOrdered() throws Exception {
		final Map<String,Object> map = newSampleMap();
		final JsnWriter jw0 = JsnUtils.writer();
		assertFalse(jw0.isUsingPrettyPrint());
		final JsnWriter jw1 = jw0.usingPrettyPrint();
		assertTrue(jw1.isUsingPrettyPrint());
		assertNotSame(jw0, jw1);
		final JsnWriter jw2 = jw0.usingPrettyPrint(false);
		assertFalse(jw2.isUsingPrettyPrint());
		assertSame(jw0, jw2);
		assertFalse(jw2.isOrdered());
		final JsnWriter jw3 = jw1.ordered();
		assertTrue(jw3.isOrdered());
		assertNotSame(jw1, jw3);
		final JsnWriter jw4 = jw1.ordered(false);
		assertFalse(jw4.isOrdered());
		assertSame(jw1, jw4);
		final byte[] bytes = jw3.toBytes(map);
		final String string = new String(bytes, StandardCharsets.UTF_8);
		assertTrue(string.contains("\n"));
		try (InputStream in = new ByteArrayInputStream(bytes);
			 JsonReader jr = Json.createReader(in)) {
			final JsonObject jo = jr.readObject();
			validateSampleMap(jo);
			assertOrdered(jo);
		}
	}

	/** Test case for {@link JsnWriter#toString(Object)}.
	 * @throws Exception The test fails.
	 */
	@Test
	public void testToString() throws Exception {
		final Map<String,Object> map = newSampleMap();
		final String string = JsnUtils.writer().toString(map);
		assertFalse(string.contains("\n"));
		try (Reader r = new StringReader(string);
			 JsonReader jr = Json.createReader(r)) {
			final JsonObject jo = jr.readObject();
			validateSampleMap(jo);
		}
	}

	/** Test case for {@link JsnWriter#toString(Object)}, with pretty print.
	 * @throws Exception The test fails.
	 */
	@Test
	public void testToStringWithPrettyPrint() throws Exception {
		final Map<String,Object> map = newSampleMap();
		final String string = JsnUtils.writer().usingPrettyPrint().toString(map);
		assertTrue(string.contains("\n"));
		try (Reader r = new StringReader(string);
			 JsonReader jr = Json.createReader(r)) {
			final JsonObject jo = jr.readObject();
			validateSampleMap(jo);
		}
	}

	/** Test case for {@link JsnWriter#toString(Object)}, with pretty print, and ordering.
	 * @throws Exception The test fails.
	 */
	@Test
	public void testToStringWithPrettyPrintAndOrdered() throws Exception {
		final Map<String,Object> map = newSampleMap();
		final String string = JsnUtils.writer().usingPrettyPrint().ordered().toString(map);
		assertTrue(string.contains("\n"));
		try (Reader r = new StringReader(string);
			 JsonReader jr = Json.createReader(r)) {
			final JsonObject jo = jr.readObject();
			validateSampleMap(jo);
			assertOrdered(jo);
		}
	}

	/** Test case for {@link JsnWriter#toString(Object)}, with ordering.
	 * @throws Exception The test fails.
	 */
	@Test
	public void testToStringOrdered() throws Exception {
		final Map<String,Object> map = newSampleMap();
		final String string = JsnUtils.writer().ordered().toString(map);
		assertFalse(string.contains("\n"));
		try (Reader r = new StringReader(string);
			 JsonReader jr = Json.createReader(r)) {
			final JsonObject jo = jr.readObject();
			validateSampleMap(jo);
			assertOrdered(jo);
		}
	}

	/** Checks, whether the given maps attributes are ordered.
	 * @param pMap The map, which is being checked- 
	 */
	protected void assertOrdered(JsonObject pMap) {
		final List<String> keys = new ArrayList<>(pMap.keySet());
		final List<String> orderedKeys = new ArrayList<>(pMap.keySet());
		orderedKeys.sort(String::compareToIgnoreCase);
		assertArrayEquals(orderedKeys.toArray(), keys.toArray());
	}

	/** Validates the Json object, which has been obtained by
	 * writing, and rereading the sample map.
	 * @param pMap The Json object, which is being checked.
	 */
	static void validateSampleMap(JsonObject pMap) {
		assertEquals(13, pMap.size());
		final JsonObject innerMapObject = pMap.getJsonObject("innerMap");
		assertNotNull(innerMapObject);
		assertEquals(1, innerMapObject.size());
		assertEquals("bar", innerMapObject.getJsonString("foo").getString());
		assertEquals(JsonValue.NULL, pMap.get("null"));
		final JsonArray listObject = pMap.getJsonArray("list");
		assertEquals(5, listObject.size());
		assertEquals(JsonValue.NULL, listObject.get(0));
		assertEquals(JsonValue.TRUE, listObject.get(1));
		assertEquals("whatever", ((JsonString) listObject.get(2)).getString());
		assertEquals(42, ((JsonNumber) listObject.get(3)).intValue());
		final JsonArray emptyArray = (JsonArray) listObject.get(4);
		assertTrue(emptyArray.isEmpty());
		assertEquals(BigDecimal.TEN, pMap.getJsonNumber("bd").bigDecimalValue());
		assertEquals(TWO, pMap.getJsonNumber("bi").bigIntegerValue());
		assertEquals(Double.valueOf(3.14159d), Double.valueOf(pMap.getJsonNumber("d").doubleValue()));
		final Double fValue = Double.valueOf(pMap.getJsonNumber("f").doubleValue());
		assertEquals(Float.valueOf(2.71f), Float.valueOf(fValue.floatValue()));
		assertEquals(Long.MAX_VALUE, pMap.getJsonNumber("l").longValue());
		assertEquals(Integer.MIN_VALUE, pMap.getJsonNumber("i").intValue());
		final Integer sValue = Integer.valueOf(pMap.getJsonNumber("s").intValue());
		assertEquals(Short.valueOf((short) 35), Short.valueOf(sValue.shortValue()));
		final Integer btValue = Integer.valueOf(pMap.getJsonNumber("bt").intValue());
		assertEquals(Byte.valueOf((byte) 7), Byte.valueOf(btValue.byteValue()));
		assertEquals(JsonValue.TRUE, pMap.get("bl"));
		assertEquals("Whatever", pMap.getJsonString("str").getString());
	}

	/** Creates a new sample map.
	 * @return The created sample map.
	 */
	static Map<String,Object> newSampleMap() {
		final Map<String,Object> map = new HashMap<>();
		map.put("innerMap", Collections.singletonMap("foo", "bar"));
		map.put("null", null);
		map.put("list", Arrays.asList(null, Boolean.TRUE, "whatever", Integer.valueOf(42), new Object[0]));
		map.put("bd", BigDecimal.TEN);
		map.put("bi", TWO);
		map.put("d", Double.valueOf(3.14159d));
		map.put("f", Float.valueOf(2.71f));
		map.put("l", Long.valueOf(Long.MAX_VALUE));
		map.put("i", Integer.valueOf(Integer.MIN_VALUE));
		map.put("s", Short.valueOf((short) 35));
		map.put("bt", Byte.valueOf((byte) 7));
		map.put("bl", Boolean.TRUE);
		map.put("str", "Whatever");
		return map;
	}

	/** Some stuff, that's been done for the sake of coverage
	 * only. Don't take this serious.
	 * @throws Exception The test has failed.
	 */
	@Test
	public void testCoverageNonsense() throws Exception { 
		/** Attempt to write an invalid object as a value.
		 */
		Functions.assertFail(IllegalArgumentException.class,
				             "Invalid value type: java.lang.Object", () -> {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (JsonGenerator jg = Json.createGenerator(baos)) {
				JsnUtils.writer().writeValue(jg, null, new Object());
			}
		});
		/** Attempt to write an invalid number value.
		 */
		Functions.assertFail(IllegalArgumentException.class,
	                         "Invalid number type: " + MutableInteger.class.getName(), () -> {
	                        	 final ByteArrayOutputStream baos = new ByteArrayOutputStream();
	                        	 try (JsonGenerator jg = Json.createGenerator(baos)) {
	                        		 JsnUtils.writer().writeValue(jg, null, new MutableInteger());
	                        	 }
	                         });
		/** Write an array, which contains various numeric values.
		 */
		{
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (JsonGenerator jg = Json.createGenerator(baos)) {
				final List<Object> list = Arrays.asList((Object) Short.valueOf((short) 3),
						                                (Object) Long.valueOf(41l),
						                                (Object) Float.valueOf(0.0f),
						                                (Object) Double.valueOf(0.1),
						                                (Object) new BigInteger("530"),
						                                (Object) new BigDecimal("0.000000004530"),
						                                (Object) Byte.valueOf((byte) 4));
				JsnUtils.writer().write(jg, list);
			}
		}
		/** I/O error, while writing to a Path object.
		 */
		{
			final Path testDir = Tests.requireTestDirectory(JsnWriterTest.class);
			Files.createDirectories(testDir);
			final Path testFile = Files.createTempFile(testDir, "tst", ".json");
			final Map<String,Object> map = newSampleMap();
			final UncheckedIOException ioe = new UncheckedIOException(new IOException("Write failed."));
			final JsnWriter jw = new JsnWriter() {
				@Override
				public void write(OutputStream pOut, Object pObject) {
					throw ioe;
				}
			};
			Functions.assertFail(ioe, () -> jw.write(testFile, map));
		}
		/** I/O error, while writing to a File object.
		 */
		{
			final Path testDir = Tests.requireTestDirectory(JsnWriterTest.class);
			Files.createDirectories(testDir);
			final Path testFile = Files.createTempFile(testDir, "tst", ".json");
			final Map<String,Object> map = newSampleMap();
			final UncheckedIOException ioe = new UncheckedIOException(new IOException("Write failed."));
			final JsnWriter jw = new JsnWriter() {
				@Override
				public void write(OutputStream pOut, Object pObject) {
					throw ioe;
				}
			};
			Functions.assertFail(ioe, () -> jw.write(testFile.toFile(), map));
		}
	}
	
}
