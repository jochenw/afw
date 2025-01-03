package com.github.jochenw.afw.core.util.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonWriter;

import org.jspecify.annotations.NonNull;
import org.junit.Test;

import com.github.jochenw.afw.core.util.tests.JsonServiceTests.JsonServiceTestSpecification;
import com.github.jochenw.afw.core.util.tests.JsonServiceTests.ValueList;

/** Test case for the {@link JsonServiceSpecificationReader}.
 */
public class JsonServiceSpecificationReaderTest {
	/** Tests parsing an empty file.
	 */
	@Test
	public void testEmpty() {
		test(asJsonString(null), IllegalStateException.class, "Attribute url is missing in service test specification");
	}

	/** Tests parsing a file with an invalid URL.
	 */
	@Test
	public void testInvalidUrl() {
		test(asJsonString(null, "", null), IllegalStateException.class, "Attribute url is empty in service test specification");
		test(asJsonString(null, "http//127.0.0.1:8080/testNoMethod", null), IllegalStateException.class, "Attribute url is invalid in service test specification: http//127.0.0.1:8080/testNoMethod");
	}
	
	/** Tests parsing a file without method.
	 */
	@Test
	public void testNoMethod() {
		test(asJsonString(null, "http://127.0.0.1:8080/testNoMethod", null), IllegalStateException.class, "Attribute method is missing in service test specification");
	}

	/** Tests parsing a file with an ivalid method.
	 */
	@Test
	public void testInvalidMethod() {
		test(asJsonString(null, "http://127.0.0.1:8080/testNoMethod", "INVALID"), IllegalStateException.class, "Attribute method is invalid in service test specification: INVALID");
	}

	private @NonNull String asJsonString(Consumer<JsonObjectBuilder> pContentBuilder) {
		final JsonObjectBuilder jo = Json.createObjectBuilder();
		if (pContentBuilder != null) {
			pContentBuilder.accept(jo);
		}
		final StringWriter sw = new StringWriter();
		try (JsonWriter jw = Json.createWriter(sw)) {
			jw.writeObject(jo.build());
		}
		@SuppressWarnings("null")
		final @NonNull String string = sw.toString();
		return string;
	}

	private @NonNull String asJsonString(Consumer<JsonObjectBuilder> pContentBuilder, String pUrl, String pMethod) {
		final Consumer<JsonObjectBuilder> jobc = (job) -> {
			if (pUrl != null) {
				job.add("url", pUrl);
			}
			if (pMethod != null) {
				job.add("method", pMethod);
			}
			if (pContentBuilder != null) {
				pContentBuilder.accept(job);
			}
		};
		return asJsonString(jobc);
	}

	private void assertValueList(ValueList pActual, String pName, String... pValues) {
		assertNotNull(pActual);
		assertEquals(pName, pActual.getName());
		final @NonNull List<@NonNull String> values = pActual.getValues();
		assertNotNull(values);
		assertEquals(pValues.length, values.size());
		for (int i = 0;  i < pValues.length;  i++) {
			assertEquals(pValues[i], values.get(i));
		}
	}

	/** Tests parsing a file with query parameters.
	 */
	@Test
	public void testQueryParameters() {
		final String url = "http://127.0.0.1:8080/testNoMethod";
		final String method = "POST";
		final Consumer<JsonServiceTestSpecification> validator = (js) -> {
			assertEquals(url, js.getUrl().toExternalForm());
			assertEquals(method, js.getMethod());
			final @NonNull Map<@NonNull String, @NonNull ValueList> queryParameters = js.getQueryParameters();
			assertNotNull(queryParameters);
			assertEquals(2, queryParameters.size());
			assertValueList(queryParameters.get("foo"), "Foo", "bar", "baz");
			assertValueList(queryParameters.get("answer"), "answer", "42");
		};
		// Test entering the query parameters as an array of key/value strings.
		test(asJsonString((job) -> {
			final JsonArrayBuilder jab = Json.createArrayBuilder();
			jab.add("Foo=bar");
			jab.add("FOo=baz");
			jab.add("answer=42");
			job.add("queryParameters", jab);
		}, url, method), validator);
		// Test entering the query parameters as am object (A set of key/value pairs).
		test(asJsonString((job) -> {
			final JsonObjectBuilder jobqp = Json.createObjectBuilder();
			jobqp.add("Foo", Json.createArrayBuilder().add("bar").add("baz"));
			jobqp.add("answer", "42");
			job.add("queryParameters", jobqp);
		}, url, method), validator);
	}

	/** Tests parsing a file successfully.
	 * @param pJson The Json string, which is being parsed. (Assumed to contain valid JSON.)
	 * @param pValidator An optional object, which validates the created result object.
	 */
	protected void test(@NonNull String pJson, Consumer<JsonServiceTestSpecification> pValidator) {
		try (Reader rdr = new StringReader(pJson);
				 JsonReader jr = Json.createReader(rdr)) {
			@SuppressWarnings("null")
			final @NonNull JsonObject jo = jr.readObject();
			final JsonServiceTestSpecification jsts = new JsonServiceSpecificationReader().parse(jo);
			if (pValidator != null) {
				pValidator.accept(jsts);
			}
		} catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}
	
	
	/** Tests parsing a file with an expected Exception.
	 * @param pJson The Json string, which is being parsed. (Assumed to contain valid JSON.)
	 * @param pExceptionType Type of the expected Exception.
	 * @param pExpectedMsg Message of the expected Exception.
	 */
	protected void test(@NonNull String pJson, @NonNull Class<? extends Throwable> pExceptionType, @NonNull String pExpectedMsg) {
		try (Reader rdr = new StringReader(pJson);
			 JsonReader jr = Json.createReader(rdr)) {
			@SuppressWarnings("null")
			final @NonNull JsonObject jo = jr.readObject();
			try {
				new JsonServiceSpecificationReader().parse(jo);
			} catch (Throwable t) {
				assertSame(pExceptionType, t.getClass());
				assertEquals(pExpectedMsg, t.getMessage());
			}
		} catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}
	
}
