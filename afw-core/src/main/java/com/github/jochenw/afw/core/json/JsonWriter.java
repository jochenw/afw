package com.github.jochenw.afw.core.json;

import java.io.OutputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableRunnable;
import com.github.jochenw.afw.core.util.Objects;

/**
 * A {@link JsonWriter} is a quick, and easy, way to convert native Java objects
 * into Json documents.
 */
public class JsonWriter implements AutoCloseable {
	private final JsonGenerator jg;
	private final boolean prettyPrintEnabled;
	private final OutputStream out;
	private final Writer w;

	private JsonWriter(JsonGenerator pJg, boolean pPrettyPrintEnabled, OutputStream pOut, Writer pWrt) {
		jg = pJg;
		prettyPrintEnabled = pPrettyPrintEnabled;
		out = pOut;
		w = pWrt;
	}

	/**
	 * Creates a new instance, which writes to the given {@link OutputStream}.
	 * 
	 * @param pOut The {@link OutputStream}, to which Json should be written.
	 *             Closing the {@link JsonWriter} will also close the output stream;
	 * @return The created {@link JsonWriter}.
	 */
	public static JsonWriter of(OutputStream pOut) {
		return new JsonWriter(Json.createGenerator(pOut), false, pOut, null);
	}

	/**
	 * Creates a new instance, which writes to the given {@link Writer}.
	 * 
	 * @param pWriter The {@link Writer}, to which Json should be written. Closing
	 *                the {@link JsonWriter} will also close the output stream;
	 * @return The created {@link JsonWriter}.
	 */
	public static JsonWriter of(Writer pWriter) {
		return new JsonWriter(Json.createGenerator(pWriter), false, null, pWriter);
	}

	/** Returns, whether pretty printing is enabled for this
	 * {@link JsonWriter json writer}.
	 * @return True, if pretty print is enabled, otherwise false.
	 */
	public boolean isPrettyPrintEnabled() {
		return prettyPrintEnabled;
	}

	/** Creates a clone of this JsonWriter, which has pretty printing enabled.
	 * @return Another JsonWriter, with {@link #isPrettyPrintEnabled()} = true.
	 */
	public JsonWriter withPrettyPrint() {
		if (prettyPrintEnabled) {
			return this;
		} else {
			final Map<String,?> config = Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, "true");
			final JsonGeneratorFactory jgf = Json.createGeneratorFactory(config);
			final JsonGenerator jg;
			if (out == null) {
				jg = jgf.createGenerator(w);
			} else {
				jg = jgf.createGenerator(out);
			}
			return new JsonWriter(jg, true, out, w);
		}
	}
	
	/** Writes a named, or unnamed, Json array. Named values are typically
	 * used for writing object attributes, unnamed values are typically
	 * used for writing the outermost element, or for array values.
	 * @param pName Name of the array, if any, or null.
	 * @param pValues The array values, each of which is being written
	 *   by invoking {@link #writeValue(Object)}.
	 */
	public void array(String pName, List<?> pValues) {
		array(pName, () -> {
			if (pValues != null) {
				pValues.forEach(v -> writeValue(v));
			}
		});
	}

	/** Writes a named, or unnamed, Json array. Named values are typically
	 * used for writing object attributes, unnamed values are typically
	 * used for writing the outermost element, or for array values.
	 * @param pName Name of the array, if any, or null.
	 * @param pValues The array values, each of which is being written
	 *   by invoking {@link #writeValue(Object)}.
	 */
	public void array(String pName, Object... pValues) {
		array(pName, () -> {
			if (pValues != null) {
				for (Object v : pValues) {
					writeValue(v);
				}
			}
		});
	}

	/** Writes a named, or unnamed, Json array. Named values are typically
	 * used for writing object attributes, unnamed values are typically
	 * used for writing the outermost element, or for array values.
	 * @param pName Name of the array, if any, or null.
	 * @param pValues The array values, each of which is being written
	 *   by invoking {@link #writeValue(Object)}.
	 */
	public void array(String pName, Stream<?> pValues) {
		array(pName, () -> {
			if (pValues != null) {
				pValues.forEach(v -> writeValue(v));
			}
		});
	}

	/** Writes a named, or unnamed, Json array. Named values are typically
	 * used for writing object attributes, unnamed values are typically
	 * used for writing the outermost element, or for array values.
	 * This is convenient when using a Lambda, and this
	 * {@link JsonWriter json writer} is already available
	 * in the Lambda's scope.
	 * @param pName Name of the object, if any, or null.
	 * @param pElementWriter An object, which writes the array elements
	 *   by invoking {@link #writeValue(Object)} for each element.
	 */
	public void array(String pName, FailableRunnable<?> pElementWriter) {
		if (pName == null) {
			jg.writeStartArray();
		} else {
			jg.writeStartArray(pName);
		}
		Functions.run(pElementWriter);
		jg.writeEnd();
	}

	/** Writes a named, or unnamed, Json array. Named values are typically
	 * used for writing object attributes, unnamed values are typically
	 * used for writing the outermost element, or for array values.
	 * Compared to
	 * {@link #array(String, FailableRunnable)}, this is more convenient, if this
	 * {@link JsonWriter json writer} is not available in your current
	 * scope.
	 * @param pName Name of the array, if any, or null.
	 * @param pElementWriter An object, which writes the array elements
	 *   by invoking {@link #writeValue(Object)} for each element.
	 */
	public void array(String pName, FailableConsumer<JsonWriter,?> pElementWriter) {
		if (pName == null) {
			jg.writeStartArray();
		} else {
			jg.writeStartArray(pName);
		}
		Functions.accept(pElementWriter, this);
		jg.writeEnd();
	}

	/**
	 * Writes a named, or unnamed, Json object. Unnamed values are
	 * typically used when writing the outermost object, or when writing
	 * array elements. The object contents are
	 * supposed to be created by the given {@link FailableConsumer}, which
	 * receives this {@link JsonWriter} as argument. Compared to
	 * {@link #object(String, FailableRunnable)}, this is more convenient, if this
	 * {@link JsonWriter json writer} is not available in your current
	 * scope.
	 * @param pObjectWriter This {@link JsonWriter json writer}.
	 * @param pName Name of the object, if any, or null.
	 * @see #object(String, FailableRunnable)
	 */
	public void object(@Nullable String pName, @NonNull FailableConsumer<JsonWriter, ?> pObjectWriter) {
		final FailableConsumer<JsonWriter, ?> objectWriter = Objects.requireNonNull(pObjectWriter, "ObjectWriter");
		if (pName == null) {
			jg.writeStartObject();
		} else {
			jg.writeStartObject(pName);
		}
		Functions.accept(objectWriter, this);
		jg.writeEnd();
	}

	/**
	 * Writes a named, or unnamed, Json object. Unnamed values are
	 * typically used when writing the outermost object, or when
	 * writing array elements. The object contents are supposed to
	 * be created by the given {@link FailableRunnable}, which
	 * receives no argument. This is convenient when using a Lambda, and this
	 * {@link JsonWriter json writer} is already available in the Lambda's scope.
	 * @param pName Name of the object, if any, or null.
	 * @param pObjectWriter This {@link JsonWriter json writer}.
	 * @see #object(String, FailableConsumer)
	 */
	public void object(@Nullable String pName, @NonNull FailableRunnable<?> pObjectWriter) {
		jg.writeStartObject();
		Functions.run(pObjectWriter);
		jg.writeEnd();
	}

	
	/**
	 * Writes a Json object with the given set of attributes.
	 * @param pAttributes A list of key/value pairs: The keys are
	 * giving the attribute names, and the values may be strings,
	 * numbers, or other objects, that may be converted into
	 * {@link JsonValue json values}.
	 * @param pName Name of the object, if any, or null.
	 * @see #writeValue(String, Object)
	 * @see #writeValue(Object)
	 */
	public void object(String pName, Object... pAttributes) {
		object(pName, () -> {
			if (pAttributes != null) {
				for (int i = 0;  i < pAttributes.length;  i += 2) {
					final String key = (String) pAttributes[i];
					final Object value = pAttributes[i+1];
					writeValue(key, value);
				}
			}
		});
	}

	/** Writes a single, named, or unnamed, object as a value.
	 * @param pName The value's name. May be null, in which case
	 *   the value is unnamed.
	 * @param pValue The actual value, may be either of the following types:
	 *   <ul>
	 *     <li>A {@link String string}, will be written as a string literal.</li>
	 *     <li>A {@link Number number}, will be written as a number constant.</li>
	 *     <li>A {@link Boolean boolean}, will be written as a boolean constant.</li>
	 *     <li>A {@link Map}, will be written as an object</li>.
	 *     <li>A {@link List}, or an array, will be written as a Json array.</li>
	 *   </ul>
	 */
	public void writeValue(String pName, Object pValue) {
		if (pValue == null) {
			if (pName == null) {
				jg.writeNull();
			} else {
				jg.writeNull(pName);
			}
		} else if (pValue instanceof String) {
			final String s = (String) pValue;
			if (pName == null) {
				jg.write(s);
			} else {
				jg.write(pName, s);
			}
		} else if (pValue instanceof Boolean) {
			final boolean b = ((Boolean) pValue).booleanValue();
			if (pName == null) {
				jg.write(b);
			} else {
				jg.write(pName, b);
			}
		} else if (pValue instanceof Number) {
			final Number n = (Number) pValue;
			if (n instanceof BigDecimal) {
				final BigDecimal bd = (BigDecimal) n;
				if (pName == null) {
					jg.write(bd);
				} else {
					jg.write(pName, bd);
				}
			} else if (n instanceof BigInteger) {
				writeValue(pName, new BigDecimal(((BigInteger) pValue)));
			} else if (n instanceof Double  ||  n instanceof Float) {
				final double d = n.doubleValue();
				if (pName == null) {
					jg.write(d);
				} else {
					jg.write(pName, d);
				}
			} else {
				final long l = n.longValue();
				if (pName == null) {
					jg.write(l);
				} else {
					jg.write(pName, l);
				}
			}
		} else if (pValue instanceof Map) {
			object(pName, () -> {
				((Map<?,?>) pValue).forEach((k,v) -> {
					if (k == null) {
						throw new NullPointerException("An object attribute's name must not be null.");
					}
					final String name = k.toString();
					if (v != null) {
						writeValue(name, v);
					}
				});
			});
		} else if (pValue instanceof List) {
			array(pName, (List<?>) pValue);
		} else if (pValue.getClass().isArray()) {
			array(pName, (Object[]) pValue);
		} else {
			throw new IllegalArgumentException("Unable to convert an instance of " + pValue.getClass().getName()
					+ " to a Json value.");
		}
	}

	/** Writes a single, unnamed object as a value, using
	 * {@link #writeValue(String, Object)}. In other words,
	 * this is equivalent to
	 * <pre>
	 *   write(null, pValue);
	 * </pre>
	 * @param pValue The actual value, which will be written.
	 */
	public void writeValue(Object pValue) {
		writeValue(null, pValue);
	}
	
	@Override
	public void close() {
		jg.close();
	}
}
