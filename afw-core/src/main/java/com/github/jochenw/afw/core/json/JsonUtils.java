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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.jochenw.afw.core.util.Numbers;
import com.github.jochenw.afw.di.util.Exceptions;

public class JsonUtils {
	public static class JsnWriter implements Cloneable {
		private boolean usingPrettyPrint;
		private boolean ordered;

		public JsnWriter usingPrettyPrint() {
			return usingPrettyPrint(true);
		}
		public JsnWriter usingPrettyPrint(boolean pPrettyPrint) {
			if (pPrettyPrint == usingPrettyPrint) {
				return this;
			} else {
				final JsnWriter jsnWriter = clone();
				jsnWriter.usingPrettyPrint = pPrettyPrint;
				return jsnWriter;
			}
		}
		public JsnWriter ordered() {
			return ordered(true);
		}
		public JsnWriter ordered(boolean pOrdered) {
			if (ordered == pOrdered) {
				return this;
			} else {
				final JsnWriter jsnWriter = clone();
				jsnWriter.ordered = pOrdered;
				return jsnWriter;
			}
		}
		public boolean isOrdered() { return ordered; }
		public boolean isUsingPrettyPrint() { return usingPrettyPrint; }

		@Override public JsnWriter clone() {
			final JsnWriter jsw = new JsnWriter();
			jsw.usingPrettyPrint = usingPrettyPrint;
			jsw.ordered = ordered;
			return jsw;
		}
	
		public void write(Path pFile, Object pObject) {
			try (OutputStream os = Files.newOutputStream(pFile)) {
				write(os, pObject);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		}

		public void write(File pFile, Object pObject) {
			try (OutputStream os = new FileOutputStream(pFile)) {
				write(os, pObject);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		}

		public byte[] toBytes(Object pObject) {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			write(baos, pObject);
			return baos.toByteArray();
		}

		public String toString(Object pObject) {
			final StringWriter sw = new StringWriter();
			write(sw, pObject);
			return sw.toString();
		}

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

		public void write(JsonGenerator pJg, Object pObject) {
			write(pJg, null, pObject);
		}

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

		protected void writeList(JsonGenerator pJg, String pName, List<?> pList) {
			final @NonNull List<?> list = Objects.requireNonNull(pList, "List");
			if (pName == null) {
				pJg.writeStartArray();
			} else {
				pJg.writeStartArray(pName);
			}
			list.forEach((v) -> write(pJg, null, v));
			pJg.writeEnd();
		}

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

	public static JsnWriter writer() { return new JsnWriter(); }

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
	public static @NonNull Object[] toArray(JsonArray pJsonArray) {
		if (pJsonArray == null) {
			return new Object[0];
		}
		final Object[] array = new Object[pJsonArray.size()];
		for (int i = 0;  i < pJsonArray.size();  i++) {
			array[i] = toObject(Objects.requireNonNull(pJsonArray.get(i)));
		}
		return array;
	}
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
