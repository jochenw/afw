package com.github.jochenw.afw.core.json;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.json.Json;
import javax.json.stream.JsonGenerator;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.util.Exceptions;

/** An object, which has the ability to create Json
	 * documents from native objects, like {@link Map maps},
	 * {@link List lists}. The created Json documents will
	 * be written to an {@link OutputStream output stream},
	 * or {@link Writer writer}.
	 */
	public class JsnWriter implements Cloneable {
		/** Protected constructor, because you are supposed
		 * to use {@link JsnUtils#writer()}.
		 */
		protected JsnWriter() {}
		private boolean usingPrettyPrint;
		private boolean ordered;

		/** Converts this object into an equivalent
		 * object, except that the converted object
		 * will have {@link #isUsingPrettyPrint() prettyprint=true}.
		 * In other words: This method a shortcut for
		 * <pre>usingPrettyPrint(true)</pre>.
		 * @return The converted object.
		 * @see #usingPrettyPrint(boolean)
		 * @see #isUsingPrettyPrint()
		 */
		public JsnWriter usingPrettyPrint() {
			return usingPrettyPrint(true);
		}
		/** Converts this object into an equivalent
		 * object, except that the converted object
		 * will have the given value for {@link #isUsingPrettyPrint()}.
		 * In other words: This method is equivalent to
		 * <pre>usingPrettyPrint(true)</pre>.
		 * @param pPrettyPrint The converted objects value
		 * for {@link #isUsingPrettyPrint()}.
		 * @return The converted object.
		 * @see #usingPrettyPrint()
		 * @see #isUsingPrettyPrint()
		 */
		public JsnWriter usingPrettyPrint(boolean pPrettyPrint) {
			if (pPrettyPrint == usingPrettyPrint) {
				return this;
			} else {
				final JsnWriter jsnWriter = clone();
				jsnWriter.usingPrettyPrint = pPrettyPrint;
				return jsnWriter;
			}
		}
		/** Converts this object into an equivalent
		 * object, except that the converted object
		 * will have {@link #isOrdered() ordered=true}.
		 * In other words: This method a shortcut for
		 * <pre>ordered(true)</pre>.
		 * @return The converted object.
		 * @see #ordered(boolean)
		 * @see #isOrdered()
		 */
		public JsnWriter ordered() {
			return ordered(true);
		}
		/** Converts this object into an equivalent
		 * object, except that the converted object
		 * will have the given value for {@link #isOrdered()}.
		 * @param pOrdered The converted objects value
		 * for {@link #isUsingPrettyPrint()}.
		 * @return The converted object.
		 * @see #ordered()
		 * @see #isOrdered()
		 */
		public JsnWriter ordered(boolean pOrdered) {
			if (ordered == pOrdered) {
				return this;
			} else {
				final JsnWriter jsnWriter = clone();
				jsnWriter.ordered = pOrdered;
				return jsnWriter;
			}
		}
		/** Returns true, if object keys are being
		 * sorted alphabetically. Using this property
		 * will make the output predictable. By default,
		 * the order of Json object keys depends on the
		 * underlying {@link Map} implementation, which
		 * appears to be random.
		 * @return True, if ordered keys are enabled. The
		 *   default value is false.
		 */
		public boolean isOrdered() { return ordered; }
		/** Returns true, if indentation is enabled
		 * for the created Json document. By default,
		 * indentation is disabled.
		 * @return True, if indentation is enabled. The
		 *   default value is false.
		 */
		public boolean isUsingPrettyPrint() { return usingPrettyPrint; }

		@Override public JsnWriter clone() {
			final JsnWriter jsw = new JsnWriter();
			jsw.usingPrettyPrint = usingPrettyPrint;
			jsw.ordered = ordered;
			return jsw;
		}

		/** Writes the given object as a Json document to the
		 * given file.
		 * @param pFile The destination file. It will be created,
		 *   if necessary, but the files directory is supposed to
		 *   exist.
		 * @param pObject The object, which is being converted.
		 *   Supported object types are {@link Map maps} (Json objects),
		 *   {@link List lists}, or arrays (Json array),
		 *   {@link Number numbers}, {@link Boolean booleans},
		 *   {@link String strings}, and null values.
		 */
		public void write(Path pFile, Object pObject) {
			try (OutputStream os = Files.newOutputStream(pFile)) {
				write(os, pObject);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		}

		/** Writes the given object as a Json document to the
		 * given file.
		 * @param pFile The destination file. It will be created,
		 *   if necessary, but the files directory is supposed to
		 *   exist.
		 * @param pObject The object, which is being converted.
		 *   Supported object types are {@link Map maps} (Json objects),
		 *   {@link List lists}, or arrays (Json array),
		 *   {@link Number numbers}, {@link Boolean booleans},
		 *   {@link String strings}, and null values.
		 */
		public void write(File pFile, Object pObject) {
			try (OutputStream os = new FileOutputStream(pFile)) {
				write(os, pObject);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		}

		/** Converts the given object to a byte
		 * array, which contains a Json document.
		 * @param pObject The object, which is being converted.
		 *   Supported object types are {@link Map maps} (Json objects),
		 *   {@link List lists}, or arrays (Json array),
		 *   {@link Number numbers}, {@link Boolean booleans},
		 *   {@link String strings}, and null values.
		 * @return The byte array, which contains the created
		 *   Json document.
		 */
		public byte[] toBytes(Object pObject) {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			write(baos, pObject);
			return baos.toByteArray();
		}

		/** Converts the given object to a string, which
		 * contains a Json document.
		 * @param pObject The object, which is being converted.
		 *   Supported object types are {@link Map maps} (Json objects),
		 *   {@link List lists}, or arrays (Json array),
		 *   {@link Number numbers}, {@link Boolean booleans},
		 *   {@link String strings}, and null values.
		 * @return The string, which contains the created
		 *   Json document.
		 */
		public String toString(Object pObject) {
			final StringWriter sw = new StringWriter();
			write(sw, pObject);
			return sw.toString();
		}

		/** Converts the given object to a Json document,
		 * which is being written to the given
		 * {@link OutputStream output stream}.
		 * @param pOut The output stream, which will
		 *   receive the created Json document.
		 * @param pObject The object, which is being converted.
		 *   Supported object types are {@link Map maps} (Json objects),
		 *   {@link List lists}, or arrays (Json array),
		 *   {@link Number numbers}, {@link Boolean booleans},
		 *   {@link String strings}, and null values.
		 */
		public void write(OutputStream pOut, Object pObject) {
			final JsonGenerator jg;
			if (usingPrettyPrint) {
				final Map<String,?> config = Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, "true");
				jg = Json.createGeneratorFactory(config).createGenerator(pOut);
			} else {
				jg = Json.createGenerator(pOut);
			}
			write(jg, null, pObject);
			jg.flush();
		}

		/** Converts the given object to a Json document,
		 * which is being written to the given
		 * {@link Writer writer}.
		 * @param pWriter The writer, which will
		 *   receive the created Json document.
		 * @param pObject The object, which is being converted.
		 *   Supported object types are {@link Map maps} (Json objects),
		 *   {@link List lists}, or arrays (Json array),
		 *   {@link Number numbers}, {@link Boolean booleans},
		 *   {@link String strings}, and null values.
		 */
		public void write(Writer pWriter, Object pObject) {
			final JsonGenerator jg;
			if (usingPrettyPrint) {
				final Map<String,?> config = Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, "true");
				jg = Json.createGeneratorFactory(config).createGenerator(pWriter);
			} else {
				jg = Json.createGenerator(pWriter);
			}
			write(jg, null, pObject);
			jg.flush();
		}

		/** Converts the given object to a Json document,
		 * which is being written by using the given
		 * {@link JsonGenerator Json generator}. 
		 * @param pJg The Json generator, which will
		 *   receive the created Json document.
		 * @param pObject The object, which is being converted.
		 *   Supported object types are {@link Map maps} (Json objects),
		 *   {@link List lists}, or arrays (Json array),
		 *   {@link Number numbers}, {@link Boolean booleans},
		 *   {@link String strings}, and null values.
		 *   Support for additional object types can be
		 *   implemented by overwriting this method.
		 */
		public void write(JsonGenerator pJg, Object pObject) {
			write(pJg, null, pObject);
		}

		/** Converts the given object to a Json document,
		 * which is being written by using the given
		 * {@link JsonGenerator Json generator}. 
		 * @param pJg The Json generator, which will
		 *   receive the created Json document.
		 * @param pName The objects name within a Json object,
		 *   if available, or null.
		 * @param pObject The object, which is being converted.
		 *   Supported object types are {@link Map maps} (Json objects),
		 *   {@link List lists}, or arrays (Json array),
		 *   {@link Number numbers}, {@link Boolean booleans},
		 *   {@link String strings}, and null values.
		 *   Support for additional object types can be
		 *   implemented by overwriting this method.
		 */
		public void write(JsonGenerator pJg, String pName, Object pObject) {
			if (pObject == null) {
				if (pName == null) {
					pJg.writeNull();
				} else {
					pJg.writeNull(pName);
				}
			} else if (pObject instanceof Map) {
				@SuppressWarnings("unchecked")
				final Map<String,?> map = (Map<String,?>) pObject;
				writeMap(pJg, pName, map);
			} else if (pObject instanceof List) {
				final List<?> list = (List<?>) pObject;
				writeList(pJg, pName, list);
			} else if (pObject.getClass().isArray()) {
				final Object[] array = (Object[]) pObject;
				writeList(pJg, pName, Arrays.asList(array));
			} else {
				writeValue(pJg, pName, pObject);
			}
		}

		/** Converts the given {@link Map map} to a Json object,
		 * which is being written by using the given
		 * {@link JsonGenerator Json generator}. 
		 * @param pJg The Json generator, which will
		 *   receive the created Json document.
		 * @param pName The objects name within a Json object,
		 *   if available, or null.
		 * @param pMap The map, which is being converted.
		 *   The map keys are strings, which are being used as
		 *   the Json objects keys. Supported object types
		 *   for the values are {@link Map maps} (Json objects),
		 *   {@link List lists}, or arrays (Json array),
0		 *   {@link Number numbers}, {@link Boolean booleans},
		 *   {@link String strings}, and null values.
		 *   Support for additional object types can be
		 *   implemented by overwriting {@link #write(JsonGenerator, String, Object)}.
		 */
		protected void writeMap(JsonGenerator pJg, String pName, Map<String,?> pMap) {
			final @NonNull Map<String,?> map = Objects.requireNonNull(pMap, "Map");
			if (pName == null) {
				pJg.writeStartObject();
			} else {
				pJg.writeStartObject(pName);
			}
			if (ordered) {
				final List<String> keys = new ArrayList<>(map.keySet());
				keys.sort(String::compareToIgnoreCase);
				for (String k : keys) {
					final Object v = map.get(k);
					write(pJg, k, v);
				}
			} else {
				map.forEach((k,v) -> {
					write(pJg, k, v);
				});
			}
			pJg.writeEnd();
		}

		/** Converts the given {@link Iterable iterable} to a Json array,
		 * which is being written by using the given
		 * {@link JsonGenerator Json generator}. 
		 * @param pJg The Json generator, which will
		 *   receive the created Json document.
		 * @param pName The objects name within a Json object,
		 *   if available, or null.
		 * @param pIterable The collection, which is being converted.
		 *   The map keys are strings, which are being used as
		 *   the Json objects keys. Supported object types
		 *   for the values are {@link Map maps} (Json objects),
		 *   {@link List lists}, or arrays (Json array),
		 *   {@link Number numbers}, {@link Boolean booleans},
		 *   {@link String strings}, and null values.
		 *   Support for additional object types can be
		 *   implemented by overwriting {@link #write(JsonGenerator, String, Object)}.
		 */
		protected void writeList(JsonGenerator pJg, String pName, Iterable<?> pIterable) {
			final @NonNull Iterable<?> iterable = Objects.requireNonNull(pIterable, "List");
			if (pName == null) {
				pJg.writeStartArray();
			} else {
				pJg.writeStartArray(pName);
			}
			iterable.forEach((v) -> write(pJg, null, v));
			pJg.writeEnd();
		}

		/** Converts the given atomic value to a Json value,
		 * which is being written by using the given
		 * {@link JsonGenerator Json generator}. 
		 * @param pJg The Json generator, which will
		 *   receive the created Json document.
		 * @param pName The objects name within a Json object,
		 *   if available, or null.
		 * @param pValue The object, which is being converted.
		 *   Supported object types are
		 *   {@link Number numbers}, {@link Boolean booleans},
		 *   {@link String strings}, and null values.
		 *   Support for additional object types can be
		 *   implemented by overwriting {@link #write(JsonGenerator, String, Object)}.
		 */
		protected void writeValue(JsonGenerator pJg, String pName, Object pValue) {
			final @NonNull Object value = Objects.requireNonNull(pValue, "Value");
			if (value instanceof String) {
				final @NonNull String s = (String) value;
				if (pName == null) {
					pJg.write(s);
				} else {
					pJg.write(pName, s);
				}
				return;
			} else if (value instanceof Boolean) {
				final @NonNull Boolean b = (Boolean) value;
				if (pName == null) {
					pJg.write(b.booleanValue());
				} else {
					pJg.write(pName, b.booleanValue());
				}
				return;
			} else if (value instanceof Number) {
				final @NonNull Number n = (Number) value;
				if (n instanceof BigDecimal) {
					final @NonNull BigDecimal bd = (BigDecimal) n;
					if (pName == null) {
						pJg.write(bd);
					} else {
						pJg.write(pName, bd);
					}
				} else if (n instanceof BigInteger) {
					final @NonNull BigInteger bi = (BigInteger) n;
					if (pName == null) {
						pJg.write(bi);
					} else {
						pJg.write(pName, bi);
					}
				} else if (n instanceof Double) {
					final @NonNull Double d = (Double) n;
					if (pName == null) {
						pJg.write(d.doubleValue());
					} else {
						pJg.write(pName, d.doubleValue());
					}
				} else if (n instanceof Float) {
					final @NonNull Float f = (Float) n;
					if (pName == null) {
						pJg.write(f.doubleValue());
					} else {
						pJg.write(pName, f.doubleValue());
					}
				} else if (n instanceof Long) {
					final @NonNull Long l = (Long) n;
					if (pName == null) {
						pJg.write(l.longValue());
					} else {
						pJg.write(pName, l.longValue());
					}
				} else if (n instanceof Integer) {
					final @NonNull Integer i = (Integer) n;
					if (pName == null) {
						pJg.write(i.intValue());
					} else {
						pJg.write(pName, i.intValue());
					}
				} else if (n instanceof Short  ||  n instanceof Byte) {
					if (pName == null) {
						pJg.write(n.intValue());
					} else {
						pJg.write(pName, n.intValue());
					}
				} else {
					throw new IllegalArgumentException("Invalid number type: " + n.getClass().getName());
				}
				return;
			}
			throw new IllegalArgumentException("Invalid value type: " + value.getClass().getName());
		}
	}