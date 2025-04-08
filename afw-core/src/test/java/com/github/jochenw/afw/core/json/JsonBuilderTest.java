package com.github.jochenw.afw.core.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;

import org.junit.Test;



/** Test suite for the {@link JsonUtils} class.
 */
public class JsonBuilderTest {
	/** Test case for {@link JsnBuilder#build(Object)}.
	 * @throws Exception The test has failed.
	 */
	@Test
	public void testBuilderBuildObject() throws Exception {
		final Map<String,Object> map = JsonWriterTest.newSampleMap();
		final JsonObject jo = JsonUtils.builder().build(map);
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonWriter jw = Json.createWriter(baos)) {
			jw.write(jo);
		}
		final byte[] bytes = baos.toByteArray();
		try (InputStream in = new ByteArrayInputStream(bytes);
			 JsonReader jr = Json.createReader(in)) {
			final JsonObject jobj = jr.readObject();
			JsonWriterTest.validateSampleMap(jobj);
		}
	}
}
