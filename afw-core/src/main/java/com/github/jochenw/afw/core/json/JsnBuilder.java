package com.github.jochenw.afw.core.json;

import java.io.OutputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.jspecify.annotations.NonNull;

/** An object, which has the ability to create Json
 * documents from native objects, like {@link Map maps},
 * {@link List lists}. The created Json documents will
 * be written to an {@link OutputStream output stream},
 * or {@link Writer writer}.
 */
public class JsnBuilder implements Cloneable {
	/** Protected constructor, because you are supposed
	 * to use {@link JsonUtils#builder()}.
	 */
	protected JsnBuilder() {}


	@Override public JsnBuilder clone() {
		final JsnBuilder jsb = new JsnBuilder();
		return jsb;
	}

	/** Converts the given object to a Json value.
	 * {@link JsonObjectBuilder Json object builder}.
	 * @param <JV> Type of the created Json value.
	 * @param pObject The object, which is being converted.
	 *   Supported object types are {@link Map maps} (Json objects),
	 *   {@link List lists}, or arrays (Json array),
	 *   {@link Number numbers}, {@link Boolean booleans},
	 *   {@link String strings}, and null values.
	 *   Support for additional object types can be
	 *   implemented by overwriting this method.
	 * @return The created Json value.
	 */
	public <JV extends JsonValue> JV build(Object pObject) {
		final JsonValue jsonValue;
		if (pObject == null) {
			jsonValue = JsonValue.NULL;
		} else if (pObject instanceof Map) {
			@SuppressWarnings("unchecked")
			final Map<String,?> map = (Map<String,?>) pObject;
			jsonValue = buildObject(map);
		} else if (pObject instanceof Iterable) {
			final Iterable<?> iterable = (Iterable<?>) pObject;
			jsonValue = buildArray(iterable);
		} else if (pObject.getClass().isArray()) {
			final Object[] array = (Object[]) pObject;
			jsonValue = buildArray(Arrays.asList(array));
		} else if (pObject instanceof Stream) {
			final Stream<?> stream = (Stream<?>) pObject;
			jsonValue = buildArray(stream.collect(Collectors.toList()));
		} else {
			jsonValue = buildValue(pObject);
		}
		@SuppressWarnings("unchecked")
		final JV jv = (JV) jsonValue;
		return jv;
	}

	/** Converts the given {@link Map map} to a Json object.
	 * @param pMap The map, which is being converted.
	 *   The map keys are strings, which are being used as
	 *   the Json objects keys. Supported object types
	 *   for the values are {@link Map maps} (Json objects),
	 *   {@link List lists}, or arrays (Json array),
	 *   {@link Number numbers}, {@link Boolean booleans},
	 *   {@link String strings}, and null values.
	 *   Support for additional object types can be
	 *   implemented by overwriting {@link #build(Object)}.
	 * @return The created Json object.
	 */
	protected JsonObject buildObject(Map<String,?> pMap) {
		final @NonNull Map<String,?> map = Objects.requireNonNull(pMap, "Map");
		final JsonObjectBuilder job = Json.createObjectBuilder();
		map.forEach((k,v) -> {
			final JsonValue jv = build(v);
			job.add(k, jv);
		});
		return job.build();
	}

	/** Converts the given {@link Iterable iterable} to a Json array.
	 * @param pIterable The collection, which is being converted.
	 *   The Supported object types
	 *   for the elements are {@link Map maps} (Json objects),
	 *   {@link List lists}, or arrays (Json array),
	 *   {@link Number numbers}, {@link Boolean booleans},
	 *   {@link String strings}, and null values.
	 *   Support for additional object types can be
	 *   implemented by overwriting {@link #build(Object)}.
	 * @return The created Json array.
	 */
	protected JsonArray buildArray(Iterable<?> pIterable) {
		final @NonNull Iterable<?> iterable = Objects.requireNonNull(pIterable, "Iterable");
		final JsonArrayBuilder jab = Json.createArrayBuilder();
		iterable.forEach((v) -> {
			final JsonValue jv = build(v);
			jab.add(jv);
		});
		return jab.build();
	}

	/** Converts the given atomic value to a Json value.
	 * @param pValue The object, which is being converted.
	 *   Supported object types are
	 *   {@link Number numbers}, {@link Boolean booleans},
	 *   {@link String strings}, and null values.
	 *   Support for additional object types can be
	 *   implemented by overwriting {@link #build(Object)}, or this method.
	 * @return The created value.
	 */
	protected JsonValue buildValue(Object pValue) {
		final @NonNull Object value = Objects.requireNonNull(pValue, "Value");
		if (value instanceof String) {
			return Json.createValue((String) value);
		} else if (value instanceof Boolean) {
			final @NonNull Boolean b = (Boolean) value;
			return b.booleanValue() ? JsonValue.TRUE : JsonValue.FALSE;
		} else if (value instanceof Number) {
			final @NonNull Number n = (Number) value;
			if (n instanceof BigDecimal) {
				final @NonNull BigDecimal bd = (BigDecimal) n;
				return Json.createValue(bd);
			} else if (n instanceof BigInteger) {
				final @NonNull BigInteger bi = (BigInteger) n;
				return Json.createValue(bi);
			} else if (n instanceof Double) {
				final @NonNull Double d = (Double) n;
				return Json.createValue(d.doubleValue());
			} else if (n instanceof Float) {
				final @NonNull Float f = (Float) n;
				return Json.createValue(f.doubleValue());
			} else if (n instanceof Long) {
				final @NonNull Long l = (Long) n;
				return Json.createValue(l.longValue());
			} else if (n instanceof Integer) {
				final @NonNull Integer i = (Integer) n;
				return Json.createValue(i.intValue());
			} else if (n instanceof Short  ||  n instanceof Byte) {
				return Json.createValue(n.intValue());
			} else {
				throw new IllegalArgumentException("Invalid number type: " + n.getClass().getName());
			}
		}
		throw new IllegalArgumentException("Invalid value type: " + value.getClass().getName());
	}
}