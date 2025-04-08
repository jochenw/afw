package com.github.jochenw.afw.core.json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.jochenw.afw.core.util.Numbers;


/** This class provides utilities for dealing with Json
 * documents.
 */
public class JsonUtils {
	/** Private constructor, because this class has only static methods.
	 */
	private JsonUtils() {}
	/** Creates, and returns a new writer instance with default values
	 * (ordered=false, and prettyPrint=false).
	 * You may wish to configure this instance by invoking
	 * {@link JsnWriter#ordered(boolean)}, or
	 * {@link JsnWriter#usingPrettyPrint(boolean)}
	 * on it.
	 * @return The created instance.
	 */
	public static JsnWriter writer() { return new JsnWriter(); }

	/** Creates, and returns a new builder instance with default values.
	 * @return The created instance.
	 */
	public static JsnBuilder builder() { return new JsnBuilder(); }

	/** Creates, and returns a new reader instance with default values.
	 * @return The created instance.
	 */
	public static JsnReader reader() { return new JsnReader(); }
	
	/** 
	/** Converts the given Json object to a native Map. The Json
	 * objects keys are used as the map keys, and the Json objects
	 * values are being converted recursively.
	 * @param pJsonObject The Json object, which is being converted.
	 * @return The converted Json object. Never null, but the
	 *   converted map may be empty.
	 */
	public static @NonNull Map<String, Object> toMap(JsonObject pJsonObject) {
		if (pJsonObject == null) {
			return new HashMap<>();
		}
		final Map<String,Object> map = new HashMap<String,Object>();
		pJsonObject.forEach((k,jv) -> {
			final @NonNull JsonValue jsonValue = Objects.requireNonNull(jv);
			final Object v = toObject(jsonValue);
			map.put(k, v);
		} );
		return map;
	}
	/** Converts the given Json array to a native array.
	 * The Json arrays values are being converted recursively.
	 * @param pJsonArray The Json array, which is being converted.
	 * @return The converted Json array. Never null, but the
	 * array may be empty.
	 */
	public static Object @NonNull[] toArray(JsonArray pJsonArray) {
		if (pJsonArray == null) {
			return new Object[0];
		}
		final Object @NonNull[] array = new Object[pJsonArray.size()];
		for (int i = 0;  i < pJsonArray.size();  i++) {
			array[i] = toObject(Objects.requireNonNull(pJsonArray.get(i)));
		}
		return array;
	}
	/** Converts the given Json value to a native value.
	 * Supported object types are
	 *   {@link Number numbers}, {@link Boolean booleans},
	 *   {@link String strings}, null values,
	 *   {@link Map maps} (Json objects),
	 *   {@link List lists}, and arrays (Json array)
	 * @param pJsonValue The Json value, which is being converted.
	 * @return The converted Json value. May be null, if the
	 * given Json value is {@link JsonValue#NULL}.
	 */
	public static @Nullable Object toObject(@NonNull JsonValue pJsonValue) {
		if (pJsonValue == JsonValue.NULL) {
			return null;
		} else if (pJsonValue == JsonValue.TRUE) {
			return Boolean.TRUE;
		} else if (pJsonValue == JsonValue.FALSE) {
			return Boolean.FALSE;
		} else if (pJsonValue == JsonValue.EMPTY_JSON_ARRAY) {
			return toArray(null);
		} else if (pJsonValue == JsonValue.EMPTY_JSON_OBJECT) {
			return toMap(null);
		} else if (pJsonValue instanceof JsonObject) {
			return toMap((JsonObject) pJsonValue);
		} else if (pJsonValue instanceof JsonArray) {
			return toArray((JsonArray) pJsonValue);
		} else if (pJsonValue instanceof JsonString) {
			return ((JsonString) pJsonValue).getString();
		} else if (pJsonValue instanceof JsonNumber) {
			final JsonNumber jn = (JsonNumber) pJsonValue;
			return Numbers.toNumberPreferringInteger(jn.bigDecimalValue());
		} else {
			throw new IllegalStateException("Invalid type of JsonValue: " + pJsonValue.getClass().getName());
		}
	}
}
