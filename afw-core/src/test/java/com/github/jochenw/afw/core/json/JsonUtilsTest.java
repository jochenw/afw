package com.github.jochenw.afw.core.json;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
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
import javax.json.JsonWriter;

import org.junit.Test;



/** Test suite for the {@link JsonUtils} class.
 */
public class JsonUtilsTest {
	/** Test case for {@link JsonUtils.JsnWriter#toBytes(Object)}.
	 */
	@Test
	public void testToBytes() throws Exception {
		final Map<String,Object> map = newSampleMap();
		final byte[] bytes = JsonUtils.writer().toBytes(map);
		assertFalse(new String(bytes, StandardCharsets.UTF_8).contains("\n"));
		try (InputStream in = new ByteArrayInputStream(bytes);
			 JsonReader jr = Json.createReader(in)) {
			final JsonObject jo = jr.readObject();
			validateSampleMap(jo);
		}
	}

	/** Test case for {@link JsonUtils.JsnWriter#toBytes(Object)}, with pretty print.
	 */
	@Test
	public void testToBytesWithPrettyPrint() throws Exception {
		final Map<String,Object> map = newSampleMap();
		final byte[] bytes = JsonUtils.writer().usingPrettyPrint().toBytes(map);
		assertTrue(new String(bytes, StandardCharsets.UTF_8).contains("\n"));
		try (InputStream in = new ByteArrayInputStream(bytes);
			 JsonReader jr = Json.createReader(in)) {
			final JsonObject jo = jr.readObject();
			validateSampleMap(jo);
		}
	}

	/** Test case for {@link JsonUtils.JsnWriter#toBytes(Object)}, with pretty print, and ordering.
	 */
	@Test
	public void testToBytesWithPrettyPrintAndOrdered() throws Exception {
		final Map<String,Object> map = newSampleMap();
		final byte[] bytes = JsonUtils.writer().usingPrettyPrint().ordered().toBytes(map);
		final String string = new String(bytes, StandardCharsets.UTF_8);
		assertTrue(string.contains("\n"));
		try (InputStream in = new ByteArrayInputStream(bytes);
			 JsonReader jr = Json.createReader(in)) {
			final JsonObject jo = jr.readObject();
			validateSampleMap(jo);
			assertOrdered(jo);
		}
	}

	/** Test case for {@link JsonUtils.JsnWriter#toString(Object)}.
	 */
	@Test
	public void testToString() throws Exception {
		final Map<String,Object> map = newSampleMap();
		final String string = JsonUtils.writer().toString(map);
		assertFalse(string.contains("\n"));
		try (Reader r = new StringReader(string);
			 JsonReader jr = Json.createReader(r)) {
			final JsonObject jo = jr.readObject();
			validateSampleMap(jo);
		}
	}

	/** Test case for {@link JsonUtils.JsnWriter#toString(Object)}, with pretty print.
	 */
	@Test
	public void testToStringWithPrettyPrint() throws Exception {
		final Map<String,Object> map = newSampleMap();
		final String string = JsonUtils.writer().usingPrettyPrint().toString(map);
		assertTrue(string.contains("\n"));
		try (Reader r = new StringReader(string);
			 JsonReader jr = Json.createReader(r)) {
			final JsonObject jo = jr.readObject();
			validateSampleMap(jo);
		}
	}

	/** Test case for {@link JsonUtils.JsnWriter#toString(Object)}, with pretty print, and ordering.
	 */
	@Test
	public void testToStringWithPrettyPrintAndOrdered() throws Exception {
		final Map<String,Object> map = newSampleMap();
		final String string = JsonUtils.writer().usingPrettyPrint().ordered().toString(map);
		assertTrue(string.contains("\n"));
		try (Reader r = new StringReader(string);
			 JsonReader jr = Json.createReader(r)) {
			final JsonObject jo = jr.readObject();
			validateSampleMap(jo);
			assertOrdered(jo);
		}
	}

	/** Test case for {@link JsonUtils.JsnWriter#toString(Object)}, with ordering.
	 */
	@Test
	public void testToStringOrdered() throws Exception {
		final Map<String,Object> map = newSampleMap();
		final String string = JsonUtils.writer().ordered().toString(map);
		assertFalse(string.contains("\n"));
		try (Reader r = new StringReader(string);
			 JsonReader jr = Json.createReader(r)) {
			final JsonObject jo = jr.readObject();
			validateSampleMap(jo);
			assertOrdered(jo);
		}
	}

	protected void assertOrdered(JsonObject pMap) {
		final List<String> keys = new ArrayList<>(pMap.keySet());
		final List<String> orderedKeys = new ArrayList<>(pMap.keySet());
		orderedKeys.sort(String::compareToIgnoreCase);
		assertArrayEquals(orderedKeys.toArray(), keys.toArray());
	}

	/** Test case for {@link JsonUtils.JsnBuilder#build(Object)}.
	 * @throws Exception The test has failed.
	 */
	@Test
	public void testBuilderBuildObject() throws Exception {
		final Map<String,Object> map = newSampleMap();
		final JsonObject jo = JsonUtils.builder().build(map);
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonWriter jw = Json.createWriter(baos)) {
			jw.write(jo);
		}
		final byte[] bytes = baos.toByteArray();
		try (InputStream in = new ByteArrayInputStream(bytes);
			 JsonReader jr = Json.createReader(in)) {
			final JsonObject jobj = jr.readObject();
			validateSampleMap(jobj);
		}
	}

	protected void validateSampleMap(JsonObject pMap) {
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
		assertEquals(BigInteger.TWO, pMap.getJsonNumber("bi").bigIntegerValue());
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

	protected Map<String,Object> newSampleMap() {
		final Map<String,Object> map = new HashMap<>();
		map.put("innerMap", Collections.singletonMap("foo", "bar"));
		map.put("null", null);
		map.put("list", Arrays.asList(null, Boolean.TRUE, "whatever", Integer.valueOf(42), new Object[0]));
		map.put("bd", BigDecimal.TEN);
		map.put("bi", BigInteger.TWO);
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
}
