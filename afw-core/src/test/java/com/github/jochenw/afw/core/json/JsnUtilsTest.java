package com.github.jochenw.afw.core.json;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.jspecify.annotations.NonNull;
import org.junit.Test;

/** Test suite for {@link JsnUtils}.
 */
public class JsnUtilsTest {
	/** Test case for {@link JsnUtils#toMap(JsonObject)}.
	 */
	@Test
	public void testToMap() {
		{
			final String bdValueStr = String.valueOf(Long.MAX_VALUE) + String.valueOf(Long.MAX_VALUE) + "." + String.valueOf(Long.MAX_VALUE);
			BigDecimal bdValue = new BigDecimal(bdValueStr).setScale(20, RoundingMode.UNNECESSARY);
			@SuppressWarnings("null")
			final @NonNull JsonObject jo = Json.createObjectBuilder()
			    .add("s", "Whatever")
			    .add("bd", bdValue)
			    .add("bi", BigInteger.TWO)
			    .add("map", JsonValue.EMPTY_JSON_OBJECT)
			    .add("array", JsonValue.EMPTY_JSON_ARRAY)
			    .add("true", JsonValue.TRUE)
			    .add("false", JsonValue.FALSE)
			    .build();
			final Map<String,Object> map = JsnUtils.toObject(jo);
			assertNotNull(map);
			assertEquals(7, map.size());
			assertEquals("Whatever", map.get("s"));
			final BigDecimal bd = (BigDecimal) map.get("bd");
			assertEquals(0, bdValue.compareTo(bd));
			assertEquals(Integer.valueOf(2), map.get("bi"));
			@SuppressWarnings("unchecked")
			final Map<String,Object> mapValue = (Map<String,Object>) map.get("map");
			assertNotNull(mapValue);
			assertTrue(mapValue.isEmpty());
			assertEquals(Boolean.TRUE, map.get("true"));
			assertEquals(Boolean.FALSE, map.get("false"));
			final Object[] array = (Object[]) map.get("array");
			assertNotNull(array);
			assertEquals(0, array.length);
		}
		{
			@SuppressWarnings("null")
			final @NonNull JsonValue nullMap = JsonValue.NULL;
			final Map<String,Object> map = JsnUtils.toObject(nullMap);
			assertNull(map);
		}
		{
			final Map<String,Object> map = JsnUtils.toMap((JsonObject) null);
			assertNotNull(map);
			assertTrue(map.isEmpty());
		}
	}

	/** Test case for {@link JsnUtils#toArray(JsonArray)}.
	 */
	@Test
	public void testToArray() {
		{
			final String bdValueStr = String.valueOf(Long.MAX_VALUE) + String.valueOf(Long.MAX_VALUE) + "." + String.valueOf(Long.MAX_VALUE);
			BigDecimal bdValue = new BigDecimal(bdValueStr).setScale(20, RoundingMode.UNNECESSARY);
			@SuppressWarnings("null")
			final @NonNull JsonArray ja = Json.createArrayBuilder()
					.add(bdValue)
					.add("Whatever")
					.add(BigInteger.TWO)
					.add(JsonValue.NULL)
					.build();
			final Object[] array = JsnUtils.toObject(ja);
			assertNotNull(array);
			assertEquals(4, array.length);
			final BigDecimal bd = (BigDecimal) array[0];
			assertEquals(0, bdValue.compareTo(bd));
			assertEquals("Whatever", array[1]);
			assertEquals(Integer.valueOf(2), array[2]);
			assertNull(array[3]);
		}
		{
			@SuppressWarnings("null")
			final @NonNull JsonValue nullArray = JsonValue.NULL;
			final Object[] array = JsnUtils.toObject(nullArray);
			assertNull(array);
		}
		{
			final Object[] array = JsnUtils.toArray((JsonArray) null);
			assertNotNull(array);
			assertEquals(0, array.length);
		}
	}

	private static class JsnValue implements JsonValue {
		@Override
		public ValueType getValueType() {
			return null;
		}
	}

	/** Random nonsense, for complete coverage.
	 */
	@Test
	public void testCoverageNonsense() {
		{
			@SuppressWarnings("null")
			final @NonNull JsonNumber jn = Json.createValue(Long.MIN_VALUE);
			final Long l = JsnUtils.toObject(jn);
			assertNotNull(l);
			assertEquals(Long.MIN_VALUE, l.longValue());
		}
		{
			final @NonNull JsonValue jv = new JsnValue();
			try {
				JsnUtils.toObject(jv);
				fail("Expected Exception");
			} catch (IllegalStateException iae) {
				assertEquals("Invalid type of JsonValue: " + JsnValue.class.getName(), iae.getMessage());
			}
		}
	}
}
