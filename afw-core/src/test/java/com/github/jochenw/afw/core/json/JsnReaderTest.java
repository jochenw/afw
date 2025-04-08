package com.github.jochenw.afw.core.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser;

import org.jspecify.annotations.NonNull;
import org.junit.Test;

import com.github.jochenw.afw.core.function.Functions.FailableSupplier;
import com.github.jochenw.afw.core.json.JsnReader.JsonParseException;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.NotImplementedException;
import com.github.jochenw.afw.core.util.Objects;


/** Test suite for the {@link JsnReader}.
 */
public class JsnReaderTest {
	/** Test for writing, and reading the sample map.
	 */
	@Test
	public void testReadSampleMap() {
		final Map<String,Object> expect = JsonWriterTest.newSampleMap();
		final byte[] bytes = JsonUtils.writer().toBytes(expect);
		final Map<String,Object> actual = JsonUtils.reader().read(new ByteArrayInputStream(bytes), null);
		assertEqual(expect, actual);
	}

	/** Asserts, that two maps are identical in the sense, that they have
	 * the same keys, and values.
	 * @param pExpect The expected map.
	 * @param pActual The actual map.
	 */
	public static void assertEqual(Map<String,Object> pExpect, Map<String,Object> pActual) {
		assertEquals(pExpect.size(), pActual.size());
		for (String key : pExpect.keySet()) {
			final Object eVal = pExpect.get(key);
			final Object aVal = pActual.get(key);
			assertEqual(eVal, aVal);
		}
	}

	/** Asserts, that two lists are identical in the sense, that the respective
	 * elements are identical.
	 * @param pExpect The expected list.
	 * @param pActual The actual list.
	 */
	public static void assertEqual(List<Object> pExpect, List<Object> pActual) {
		assertEquals(pExpect.size(), pActual.size());
		for (int i = 0;  i < pExpect.size();  i++) {
			assertEqual(pExpect.get(i), pActual.get(i));
		}
	}

	/** Asserts, that two objects are identical in the sense, that they have
	 * the same attributes, or elements, or are equal in the sense of
	 * {@link Object#equals(Object)}.
	 * @param pExpect The expected object.
	 * @param pActual The actual object.
	 */
	public static void assertEqual(Object pExpect, Object pActual) {
		if (pExpect == null) {
			assertNull(pActual);
		} else if (pExpect instanceof Map) {
			@SuppressWarnings("unchecked")
			final Map<String,Object> eMap = (Map<String,Object>) pExpect;
			@SuppressWarnings("unchecked")
			final Map<String,Object> aMap = (Map<String,Object>) pActual;
			assertEqual(eMap, aMap);
		} else if (pExpect instanceof List) {
			@SuppressWarnings("unchecked")
			final List<Object> eList = (List<Object>) pExpect;
			@SuppressWarnings("unchecked")
			final List<Object> aList = (List<Object>) pActual;
			assertEqual(eList, aList);
		} else if (pExpect.getClass().isArray()) {
			final Object[] expectArray = (Object[]) pExpect;
			final List<Object> eList = Arrays.asList(expectArray);
			@SuppressWarnings("unchecked")
			final List<Object> aList = (List<Object>) pActual;
			assertEqual(eList, aList);
		} else if (pExpect instanceof Boolean  ||  pExpect instanceof String) {
			assertEquals(pExpect, pActual);
		} else if (pExpect instanceof Number) {
			final Number expectNmbr = (Number) pExpect;
			final Number actualNmbr = (Number) pActual;
			if (expectNmbr instanceof Byte  ||  actualNmbr instanceof Byte
					||  expectNmbr instanceof Short  ||  actualNmbr instanceof Short
					||  expectNmbr instanceof Integer  ||  actualNmbr instanceof Integer) {
				assertEquals(expectNmbr.intValue(), actualNmbr.intValue());
			} else if (expectNmbr instanceof Long  ||  actualNmbr instanceof Long) {
				assertEquals(expectNmbr.longValue(), actualNmbr.longValue());
			} else if (expectNmbr instanceof Float  ||  actualNmbr instanceof Float
					   ||  expectNmbr instanceof Double  ||  actualNmbr instanceof Double) {
				assertEquals(expectNmbr.doubleValue(), actualNmbr.doubleValue(), 0.0000001d);
			} else if (expectNmbr instanceof BigInteger  ||  actualNmbr instanceof BigInteger) {
				final BigInteger expect = (expectNmbr instanceof BigInteger) ? (BigInteger) expectNmbr : ((BigDecimal) expectNmbr).toBigIntegerExact();
				final BigInteger actual = (actualNmbr instanceof BigInteger) ? (BigInteger) actualNmbr : ((BigDecimal) actualNmbr).toBigIntegerExact();
				assertEquals(0, expect.compareTo(actual));
			} else {
				final BigDecimal expect = (BigDecimal) pExpect;
				final BigDecimal actual = (BigDecimal) pActual;
				assertEquals(0, expect.compareTo(actual));
			}
		} else {
			throw new IllegalStateException("Invalid type for expected object: " + pExpect.getClass().getName());
		}
	}

	/** Test case for the {@link JsnReader.JsonParseException}.
	 */
	@Test
	public void testJsonParseException() {
		final JsonParseException jpe1 = new JsonParseException(null,  null, "Exception 1");
		assertEquals(null, jpe1.getUri());
		assertEquals(-1l, jpe1.getLineNumber());
		assertEquals(-1l, jpe1.getColumnNumber());
		assertEquals(-1l, jpe1.getStreamOffset());
		assertEquals("Exception 1", jpe1.getMessage());
		assertFalse(Exceptions.hasCause(jpe1));

		final JsonLocation jl = new JsonLocation() {
			@Override
			public long getStreamOffset() {
				return 423l;
			}
			
			@Override
			public long getLineNumber() {
				return 12l;
			}
			
			@Override
			public long getColumnNumber() {
				return 17l;
			}
		};
		final JsonParseException jpe2 = new JsonParseException(jl, "Test file", "Exception 2");
		assertFalse(Exceptions.hasCause(jpe2));
		assertEquals("Test file", jpe2.getUri());
		assertEquals(12l, jpe2.getLineNumber());
		assertEquals(17l, jpe2.getColumnNumber());
		assertEquals(423l, jpe2.getStreamOffset());
		assertEquals("At file Test file, line 12, column 17, offset 423: Exception 2", jpe2.getMessage());
	}

	/** Test case for a failing parser supplier.
	 */
	@Test
	public void testFailingParserSupplier() {
        final NotImplementedException nie = new NotImplementedException();
		final FailableSupplier<JsonParser,?> parserSupplier = () -> {
			throw nie;
		};
		final JsnReader.Context ctx = new JsnReader.Context(parserSupplier, "myUri");
		assertEquals("myUri", ctx.getUri());
		try {
			ctx.getParser();
			fail("Expected Exception");
		} catch (Throwable t) {
			assertSame(nie, t);
		}
	}

	/** Test case for a working parser supplier.
	 */
	@Test
	public void testWorkingParserSupplier() {
		final @NonNull FailableSupplier<@NonNull JsonParser,?> parserSupplier = () -> {
			final byte[] bytes = "{}".getBytes(StandardCharsets.UTF_8);
			return Objects.requireNonNull(Json.createParser(new ByteArrayInputStream(bytes)));
		};
		final JsnReader.Context ctx = new JsnReader.Context(parserSupplier, "myUri");
		assertEquals("myUri", ctx.getUri());
		final JsonParser jp = ctx.getParser();
		assertNotNull(jp);
		final JsonLocation jl = jp.getLocation();
		assertNotNull(jl);
		assertEquals(1l, jl.getLineNumber());
		assertEqual(1l, jl.getColumnNumber());
		assertEqual(0l, jl.getStreamOffset());
		final JsonParseException jpe3 = ctx.error("Exception 3");
		assertEquals("At file myUri, line 1, column 1, offset 0: Exception 3", jpe3.getMessage());
	}
}
