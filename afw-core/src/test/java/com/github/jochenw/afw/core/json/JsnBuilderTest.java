package com.github.jochenw.afw.core.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.JsonWriter;

import org.junit.Test;

import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.core.util.MutableInteger;



/** Test suite for the {@link JsnUtils} class.
 */
public class JsnBuilderTest {
	/** Test case for {@link JsnBuilder#build(Object)}.
	 * @throws Exception The test has failed.
	 */
	@Test
	public void testBuilderBuildObject() throws Exception {
		{
			final Map<String,Object> map = JsnWriterTest.newSampleMap();
			final JsonObject jo = JsnUtils.builder().build(map);
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (JsonWriter jw = Json.createWriter(baos)) {
				jw.write(jo);
			}
			final byte[] bytes = baos.toByteArray();
			try (InputStream in = new ByteArrayInputStream(bytes);
				 JsonReader jr = Json.createReader(in)) {
				final JsonObject jobj = jr.readObject();
				JsnWriterTest.validateSampleMap(jobj);
			}
		}
		{ // Same test, but using a {@link Stream} value.
			final Map<String,Object> map = JsnWriterTest.newSampleMap();
			@SuppressWarnings("unchecked")
			final List<Object> list = (List<Object>) map.get("list");
			map.put("list", list.stream());
			
			final JsonObject jo = JsnUtils.builder().build(map);
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (JsonWriter jw = Json.createWriter(baos)) {
				jw.write(jo);
			}
			final byte[] bytes = baos.toByteArray();
			try (InputStream in = new ByteArrayInputStream(bytes);
				 JsonReader jr = Json.createReader(in)) {
				final JsonObject jobj = jr.readObject();
				JsnWriterTest.validateSampleMap(jobj);
			}
		}
	}

	/** 
	 * Various small stuff, just for coverage completion.
	 */
	@Test
	public void testCoverageNonsense() {
		final List<Object> list = Arrays.asList((Object) Boolean.TRUE, Boolean.FALSE);
		{ // Test both boolean values.
			final JsonArray ja = JsnUtils.builder().build(list);
			assertNotNull(ja);
			assertEquals(2, ja.size());
			assertEquals(JsonValue.TRUE, ja.get(0));
			assertEquals(JsonValue.FALSE, ja.get(1));
		}
		{ // Test for an invalid number type.
			final List<Object> list2 = new ArrayList<>(list);
			list2.add(new MutableInteger());
			final String msg = "Invalid number type: " + MutableInteger.class.getName();
			Functions.assertFail(IllegalArgumentException.class, msg,
					() -> JsnUtils.builder().build(list2));
		}
		{ // Test for an invalid object type.
			final List<Object> list2 = new ArrayList<>(list);
			list2.add(new JsnUtilsTest.JsnValue());
			final String msg = "Invalid value type: " + JsnUtilsTest.JsnValue.class.getName();
			Functions.assertFail(IllegalArgumentException.class, msg,
					() -> JsnUtils.builder().build(list2));
		}
		{ // Test for JsnBuilder.clone().
		  final JsnBuilder bld1 = JsnUtils.builder();
		  final JsnBuilder bld2 = bld1.clone();
		  assertNotNull(bld2);
		  assertNotSame(bld1, bld2);
		}
	}
}
