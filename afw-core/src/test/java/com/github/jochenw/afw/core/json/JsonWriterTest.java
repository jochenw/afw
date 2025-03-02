package com.github.jochenw.afw.core.json;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.junit.Test;

import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.core.function.Functions.FailableConsumer;

/** Test suite for the {@link JsonWriter}.
 */
public class JsonWriterTest {
	/** Test for writing a single Json object with a few, atomic attributes.
	 */
	@Test
	public void testSimpleObject() {
		final StringWriter sw = new StringWriter();
		try (final JsonWriter jw = JsonWriter.of(sw)) {
			jw.object(null, "answer", Integer.valueOf(42), "foo", "bar", "status", Boolean.TRUE);
		}
		final JsonObject actual;
		try (JsonReader jr = Json.createReader(new StringReader(sw.toString()))) {
			actual = jr.readObject();
		}
		assertNotNull(actual);
		assertEquals(3, actual.size());
		assertEquals(42, ((JsonNumber) actual.get("answer")).intValueExact());
		assertEquals("bar", ((JsonString) actual.get("foo")).getString());
		assertEquals(JsonValue.TRUE, actual.get("status"));
	}

	/** Test for writing the various atomic number types.
	 */
	@Test
	public void testAtomicTypes() {
		final String bigDecimalString = String.valueOf(Long.MAX_VALUE) + "0." + String.valueOf(Long.MAX_VALUE);
		final String bigIntegerString = String.valueOf(Long.MAX_VALUE)+"0";
		final Consumer<JsonObject> validator = (actual) -> {
			assertNotNull(actual);
			assertEquals(9, actual.size());
			// BigDecimal.equals() is not necessarily a numeric comparison. So, we use compareTo.
			assertEquals(0, new BigDecimal(bigDecimalString).compareTo(actual.getJsonNumber("bd").bigDecimalValue()));
			assertEquals(0, new BigInteger(bigIntegerString).compareTo(actual.getJsonNumber("bi").bigIntegerValueExact()));
			assertEquals(Double.valueOf(3.14159d), Double.valueOf(actual.getJsonNumber("d").doubleValue()));
			final float f = (float) actual.getJsonNumber("f").doubleValue();
			assertEquals(Float.valueOf(1.23f), Float.valueOf(f));
			assertEquals(Long.MIN_VALUE, actual.getJsonNumber("l").longValueExact());
			assertEquals(69, actual.getJsonNumber("i").intValueExact());
			assertEquals(Short.valueOf((short) 70), Short.valueOf((short) actual.getJsonNumber("s").intValueExact()));
			assertEquals(Byte.valueOf((byte) 71), Byte.valueOf((byte) actual.getJsonNumber("b").intValueExact()));
			assertEquals(JsonValue.NULL, actual.get("nl"));
		};
		final Consumer<JsonWriter> creator = (jw) -> {
			jw.object(null, "bd", new BigDecimal(bigDecimalString),
					  "bi", new BigInteger(bigIntegerString),
					  "d", Double.valueOf(3.14159d),
					  "f", Float.valueOf(1.23f),
					  "l", Long.valueOf(Long.MIN_VALUE),
					  "i", Integer.valueOf(69),
					  "s", Short.valueOf((short) 70),
					  "b", Byte.valueOf((byte) 71),
					  "nl", null);
		};
		{ // Test 1: Use a Writer. 
			final StringWriter sw = new StringWriter();
			try (final JsonWriter jw = JsonWriter.of(sw)) {
				creator.accept(jw);
			}
			final JsonObject actual;
			try (JsonReader jr = Json.createReader(new StringReader(sw.toString()))) {
				actual = jr.readObject();
			}
			validator.accept(actual);
		}
		{ // Test 2: Use an OutputStream 
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (final JsonWriter jw = JsonWriter.of(baos)) {
				creator.accept(jw);
			}
			final JsonObject actual;
			try (JsonReader jr = Json.createReader(new ByteArrayInputStream(baos.toByteArray()))) {
				actual = jr.readObject();
			}
			validator.accept(actual);
		}
	}

	private String arrayAsString(FailableConsumer<JsonWriter,?> pConsumer) {
		return asString((jw) -> {
			jw.object(null, () -> {
				pConsumer.accept(jw);
			});
		});
	}

	private String asString(FailableConsumer<JsonWriter,?> pConsumer) {
		final StringWriter sw = new StringWriter();
		try (JsonWriter jw = JsonWriter.of(sw)) {
			Functions.accept(pConsumer, jw);
		}
		return sw.toString();
	}

	/** Test case for {@link JsonWriter#array(String, List)}, etc.
	 */
	@Test
	public void testArrayListEtc() {
	     final List<Integer> list = getIntegerList();
	     assertEquals("{\"array\":[0,1,2,3]}", arrayAsString((FailableConsumer<JsonWriter, Throwable>) jw -> jw.array("array", list)));
	     assertEquals("{\"array\":[0,1,2,3]}", arrayAsString((FailableConsumer<JsonWriter, Throwable>) jw -> jw.array("array", list.stream())));
	     assertEquals("{\"array\":[0,1,2,3]}", arrayAsString((FailableConsumer<JsonWriter, Throwable>) jw -> jw.array("array", list.toArray())));
	}

	private List<Integer> getIntegerList() {
		final List<Integer> list = new ArrayList<>();
	     for (int i = 0;  i <= 3;  i++) {
	    	 list.add(Integer.valueOf(i));
	     }
		return list;
	}

	/** Test case for {@link JsonWriter#array(String, Functions.FailableConsumer)}.
	 */
	@Test
	public void testArrayFailableConsumer() {
		final List<Integer> list = getIntegerList();
		{ // Test without name.
			final StringWriter sw = new StringWriter();
			try (JsonWriter jw = JsonWriter.of(sw)) {
				jw.array(null, (jw2) -> list.forEach((i) -> jw2.writeValue(i)));
			}
			assertEquals("[0,1,2,3]", sw.toString());
		}
		{ // Test with name.
			final StringWriter sw = new StringWriter();
			try (JsonWriter jw = JsonWriter.of(sw)) {
				jw.object(null, (jw2) -> {
					jw2.array("arr", (jw3) -> list.forEach((i) -> jw3.writeValue(i)));
				});
			}
			assertEquals("{\"arr\":[0,1,2,3]}", sw.toString());
		}
	}

	/** Test case for {@link JsonWriter#array(String, Functions.FailableRunnable)}.
	 */
	@Test
	public void testArrayFailableRunnable() {
		final List<Integer> list = getIntegerList();
		{ // Test without name.
			final StringWriter sw = new StringWriter();
			try (JsonWriter jw = JsonWriter.of(sw)) {
				jw.array(null, () -> list.forEach((i) -> jw.writeValue(i)));
			}
			assertEquals("[0,1,2,3]", sw.toString());
		}
		{ // Test with name.
			final StringWriter sw = new StringWriter();
			try (JsonWriter jw = JsonWriter.of(sw)) {
				jw.object(null, (jw2) -> {
					jw2.array("arr", () -> list.forEach((i) -> jw2.writeValue(i)));
				});
			}
			assertEquals("{\"arr\":[0,1,2,3]}", sw.toString());
		}
	}

	/** Test case for {@link JsonWriter#object(String, FailableConsumer)}.
	 */
	@Test
	public void testObjectFailableConsumer() {
		final StringWriter sw = new StringWriter();
		try (JsonWriter jw = JsonWriter.of(sw)) {
			jw.object(null, (jw2) -> {
				jw2.object("obj", (jw3) -> {
					jw3.writeValue("i", Integer.valueOf(42));
					jw3.writeValue("b", Boolean.TRUE);
				});
			});
		}
		final String actual = sw.toString();
		try (JsonReader jr = Json.createReader(new StringReader(actual))) {
			final JsonObject jo = jr.readObject();
			assertNotNull(jo);
			assertEquals(1, jo.size());
			final JsonObject obj = jo.getJsonObject("obj");
			assertNotNull(obj);
			assertEquals(42, obj.getJsonNumber("i").intValue());
			assertTrue(obj.getBoolean("b"));
		}
	};
	/** Some stupid stuff, just for coverage completion.	 * 
	 */
	@Test
	public void testCoverageNonsense() {
	     assertEquals("[]", asString((jw) -> jw.array(null, (List<?>) null)));
	     assertEquals("[]", asString((jw) -> jw.array(null, (Object[]) null)));
	     assertEquals("[]", asString((jw) -> jw.array(null, (Stream<?>) null)));
	     assertEquals("{}", asString((jw) -> jw.object(null, (Object[]) null)));
	}
}
