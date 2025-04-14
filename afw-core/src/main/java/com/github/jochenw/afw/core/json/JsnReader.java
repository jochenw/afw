package com.github.jochenw.afw.core.json;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.json.Json;
import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.function.Functions.FailableSupplier;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Numbers;

/** An instance of this class reads a Json document, converting it into
 * native Java objects, like {@link Map maps}, {@link List lists}, etc.
 */
public class JsnReader {
	/** Private constructor, because this class contains only static methods.
	 */
	JsnReader() {}

	/** If the JsnReader throws an exception, then it tries hard to use
	 * this class, which includes location information.
	 */
	public static class JsonParseException extends RuntimeException implements JsonLocation {
		private static final long serialVersionUID = 7592950485871454407L;
		/** URI of the file, which is being parsed, or null.
		 */
		private final String uri;
		/** Line number, part of the location information, as specified by
		 * {@link JsonLocation#getLineNumber()}.
		 */
		private final long lineNumber;
		/** Line number, part of the location information, as specified by
		 * {@link JsonLocation#getColumnNumber()}.
		 */
		private final long columnNumber;
		/** Line number, part of the location information, as specified by
		 * {@link JsonLocation#getStreamOffset()}.
		 */
		private final long streamOffset;
		
		/** Creates a new instance.
		 * @param pLocation The errors location, if available, or null.
		 * @param pUri Uri of the file, which is being parsed. (Or null,
		 *   if no URI is available.)
		 * @param pMsg The error message (Without location information.)
		 */
		public JsonParseException(JsonLocation pLocation, String pUri, String pMsg) {
			super(asMsg(pUri, pLocation, pMsg));
			uri = pUri;
			if (pLocation == null) {
				lineNumber = -1l;
				columnNumber = -1l;
				streamOffset = -1l;
			} else {
				lineNumber = pLocation.getLineNumber();
				columnNumber = pLocation.getColumnNumber();
				streamOffset = pLocation.getStreamOffset();
			}
		}

		/** Returns the Uri of the file, which is being parsed.
		 * @return The Uri of the file, which is being parsed.
		 */
		public String getUri() { return uri; }
		@Override public long getColumnNumber() { return columnNumber; }
		@Override public long getLineNumber() { return lineNumber; }
		@Override public long getStreamOffset() { return streamOffset; }

		/** Converts the given location, and the given error message
		 * into a single message string, which contains both.
		 * @param pUri The Uri of the file, which is being parsed.
		 * @param pLocation The location of the error.
		 * @param pMsg The error message.
		 * @return An extended error message, whivh includes the location (if available).
		 */
		public static String asMsg(String pUri, JsonLocation pLocation, String pMsg) {
			final StringBuilder sb = new StringBuilder();
			String sep = "At ";
			if (pUri != null) {
				sb.append(sep);
				sep = ", ";
				sb.append("file ");
				sb.append(pUri);
			}
			final long lineNumber = pLocation == null ? -1l : pLocation.getLineNumber();
			if (lineNumber != -1l) {
				sb.append(sep);
				sep = ", ";
				sb.append("line ");
				sb.append(lineNumber);
			}
			final long columnNumber = pLocation == null ? -1l : pLocation.getColumnNumber();
			if (columnNumber != -1) {
				sb.append(sep);
				sep = ", ";
				sb.append("column ");
				sb.append(columnNumber);
			}
			final long streamOffset = pLocation == null ? -1l : pLocation.getStreamOffset();
			if (streamOffset != -1) {
				sb.append(sep);
				sep = ", ";
				sb.append("offset ");
				sb.append(streamOffset);
			}
			if (sb.length() == 0) {
				return pMsg;
			} else {
				sb.append(": ");
				sb.append(pMsg);
				return sb.toString();
			}
		}
	}

	/** The Json readers state. An instance of this object is created, when
	 * the parser starts. This instance is then handed down through all the
	 * method calls.
	 */
	public static class Context {
		private final @NonNull FailableSupplier<@NonNull JsonParser,?> parserSupplier;
		private final String uri;
		private JsonParser jp;

		/** Creates a new instance.
		 * @param pParserSupplier A supplier for the {@link JsonParser}, which is being
		 *   used internally. Note, that the {@link JsonParser} is tightly linked to
		 *   the byte, or character stream, which is being parsed. So, in practice,
		 *   this supplier is mainly responsible for providing that stream.
		 * @param pUri URI of the file, which is being parsed. Null, if no such URI
		 * is available.
		 */
		public Context(@NonNull FailableSupplier<@NonNull JsonParser,?> pParserSupplier, String pUri) {
			parserSupplier = pParserSupplier;
			uri = pUri;
		}


		/** Returns the URI of the file, which is being parsed.
		 *@return The URI of the file, which is being parsed. Null, if no such URI
		 * is available.
		 */
		public String getUri() {
			return uri;
		}

		/** Returns the {@link JsonParser}, which is being used internally. The parser
		 * is being created, when this method is being invoked for the first time.
		 * @return The {@link JsonParser}, which is being used internally. The parser
		 * is being created, when this method is being invoked for the first time.
		 */
		public @NonNull JsonParser getParser() {
			if (jp == null) {
				try {
					final @NonNull JsonParser jprs = Objects.requireNonNull(parserSupplier.get());
					jp = jprs;
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
			}
			return Objects.requireNonNull(jp);
		}

		/** Returns an exception, which can be thrown, including location
		 * information.
		 * @param pMsg The error message.
		 * @return The created exception.
		 */
		public JsonParseException error(String pMsg) {
			return new JsonParseException(getParser().getLocation(), uri, pMsg);
		}
	}

	/** Called to read a map, or a list from the Json document, as provided
	 * by the given {@link InputStream}.
	 * @param <O> Type of the result object.
	 * @param pIn The {@link InputStream}, which provides the Json
	 *   document.
	 * @param pUri The Json documents URI.
	 * @return The created object, a representation of the Json
	 *   document.
	 */
	public <O> O read(InputStream pIn, String pUri) {
		final FailableSupplier<@NonNull JsonParser,?> parserSupplier = () -> {
			@SuppressWarnings("null")
			final @NonNull JsonParser jp = Json.createParser(pIn);
			return jp;
		};
		final @NonNull Context ctx = new Context(parserSupplier, pUri);
		return read(ctx);
	}

	/** Called to read a map, or a list from the Json document, as provided
	 * by the {@link JsonParser} in the given context object.
	 * @param <O> Type of the result object.
	 * @param pCtx The context object, which provides access to the Json
	 *   parser.
	 * @return The created object, a representation of the Json
	 *   document.
	 */
	protected <O> O read(@NonNull Context pCtx) {
		final JsonParser jp = pCtx.getParser();
		Object obj = null;
		while (obj == null &&  jp.hasNext()) {
			final Event ev = jp.next();
			switch (ev) {
			case START_OBJECT:
				final Map<String,Object> map = readObject(pCtx);
				obj = map;
				break;
			case START_ARRAY:
				final List<Object> list = readArray(pCtx);
				obj = list;
				break;
			case VALUE_FALSE:
			case VALUE_TRUE:
			case VALUE_NULL:
			case VALUE_NUMBER:
			case VALUE_STRING:
				obj = readValue(pCtx, ev);
				break;
			case END_ARRAY:
			case END_OBJECT:
			case KEY_NAME:
				throw pCtx.error("Unexpected Json event: " + ev.name());
			}
		}
		if (obj == null) {
			throw pCtx.error("No Json content has been found.");
		}
		@SuppressWarnings("unchecked")
		final O o = (O) obj;
		return o;
	}

	/** Called to parse a Json array from the Json document.
	 * It is assumed, that the {@link Context#getParser() Json parser}
	 * has just consumed the {@link Event#START_ARRAY} event. This
	 * method will consume all following events, until, and including
	 * the associated {@link Event#END_ARRAY} event, but not more.
	 * @param pCtx The context object
	 * @return A list, which represents the contents of the Json
	 *   array, that has been parsed.
	 */
	protected List<Object> readArray(Context pCtx) {
		final JsonParser jp = pCtx.getParser();
		final List<Object> list = new ArrayList<>();
		while (jp.hasNext()) {
			final Event ev = jp.next();
			switch(ev) {
			    case KEY_NAME:
				    throw pCtx.error("Unexpected attribute name within an array.");
			    case START_OBJECT: {
			    	final Map<String,Object> mp = readObject(pCtx);
			    	if (mp == null) {
			    		throw pCtx.error("Expected non-null object element.");
			    	}
			    	list.add(mp);
			    	break;
			    }
			    case START_ARRAY:
			    	final List<Object> lst = readArray(pCtx);
			    	if (lst == null) {
			    		throw pCtx.error("Expected non-null array element.");
			    	}
			    	list.add(lst);
			    	break;
			    case VALUE_FALSE:
			    case VALUE_TRUE:
			    case VALUE_NULL:
			    case VALUE_NUMBER:
			    case VALUE_STRING: {
			    	final Object value = readValue(pCtx, ev);
			    	if (value == null  &&  ev != Event.VALUE_NULL) {
			    		throw pCtx.error("Expected non-null value element.");
			    	}
			    	list.add(value);
			    	break;
			    }
			    case END_OBJECT:
			    	throw pCtx.error("Unexpected END_OBJECT, while waiting for END_ARRAY.");
			    case END_ARRAY: {
			    	return list;
			    }
			}
		}
		throw pCtx.error("Unexpected end of file, while waiting for END_ARRAY");
	}

	/** Called to parse an atomic Json valuw from the Json document.
	 * It is assumed, that the {@link Context#getParser() Json parser}
	 * has just consumed the given event. This method will consume no
	 * events, and leave the parsers state untouched.
	 * @param pCtx The context object
	 * @param pEvent The atomic value's event, either of
	 *   {@link Event#VALUE_FALSE}, {@link Event#VALUE_TRUE},
	 *   {@link Event#VALUE_NULL}, {@link Event#VALUE_STRING}, or
	 *   {@link Event#VALUE_NUMBER}. 
	 * @return A native Java object, which represents the event's
	 *   atomic Json value.
	 */
	protected Object readValue(Context pCtx, Event pEvent) {
		final JsonParser jp = pCtx.getParser();
		switch(pEvent) {
		    case VALUE_FALSE: return Boolean.FALSE;
		    case VALUE_TRUE: return Boolean.TRUE;
		    case VALUE_NULL: return null;
		    case VALUE_STRING: return jp.getString();
		    case VALUE_NUMBER: {
		    	return Numbers.toNumberPreferringInteger(jp.getBigDecimal());
		    }
		    default:
		    	throw pCtx.error("Expected value event, got " + pEvent.name());
		}
	}

	/** Called to parse a Json object from the Json document.
	 * It is assumed, that the {@link Context#getParser() Json parser}
	 * has just consumed the {@link Event#START_OBJECT} event. This
	 * method will consume all following events, until, and including
	 * the associated {@link Event#END_OBJECT} event, but not more.
	 * @param pCtx The context object.
	 * @return A map, which represents the contents of the Json
	 *   object, that has been parsed.
	 */
	protected Map<String,Object> readObject(Context pCtx) {
		final JsonParser jp = pCtx.getParser();
		String name = null;
		final Map<String,Object> map = new HashMap<String,Object>();
		while (jp.hasNext()) {
			final Event ev = jp.next();
			switch (ev) {
				case KEY_NAME: {
					final String nm = name;
					if (nm != null) {
						throw pCtx.error("Expected content event, got KEY_START");
					}
					name = jp.getString();
					if (name == null) {
						throw pCtx.error("Expected object attribute name, got null.");
					}
					break;
				}
				case START_OBJECT: {
					readObjectAttribute(pCtx, name, map);
					name = null;
					break;
				}
				case START_ARRAY: {
					readArrayAttribute(pCtx, name, map);
					name = null;
					break;
				}
				case VALUE_FALSE:
				case VALUE_TRUE:
				case VALUE_NULL:
				case VALUE_NUMBER:
				case VALUE_STRING: {
					readAtomicAttribute(pCtx, name, map, ev);
					name = null;
					break;
				}
				case END_OBJECT: {
					final String nm = name;
					if (nm != null) {
						throw pCtx.error("Expected object attribute value, got END_OBJECT");
					}
					return map;
				}
				default:
					throw pCtx.error("Expected object attribute value, or END_OBJECT, got " + ev.name());
			}
				
		}
		throw pCtx.error("Unexpected end of file, while waiting for END_OBJECT");
	}

	private void readAtomicAttribute(Context pCtx, String pName, final Map<String, Object> pMap,
			final Event pEvnt) {
		final String nm = pName;
		if (nm == null) {
			throw pCtx.error("Expected attribute name before the actual value.");
		}
		final Object value = readValue(pCtx, pEvnt);
		if (value == null  &&  pEvnt != Event.VALUE_NULL) {
			throw pCtx.error("Expected non-null attribute value.");
		}
		final Object v = pMap.put(nm, value);
		if (v != null) {
			throw pCtx.error("Duplicate attribute name: " + nm);
		}
	}

	private void readArrayAttribute(Context pCtx, String pName, final Map<String, Object> pMap) {
		final String nm = pName;
		if (nm == null) {
			throw pCtx.error("Expected attribute name before the actual array.");
		}
		final List<Object> list = readArray(pCtx);
		if (list == null) {
			throw pCtx.error("Expected non-null array attribute.");
		}
		final Object v = pMap.put(nm,  list);
		if (v != null) {
			throw pCtx.error("Duplicate attribute name: " + pName);
		}
	}

	private void readObjectAttribute(Context pCtx, String pName, final Map<String, Object> pMap) {
		final String nm = pName;
		if (nm == null) {
			throw pCtx.error("Expected attribute name before the actual object.");
		}
		final Map<String,Object> mp = readObject(pCtx);
		if (mp == null) {
			throw pCtx.error("Expected non-null object attribute.");
		}
		final Object v = pMap.put(nm,  mp);
		if (v != null) {
			throw pCtx.error("Duplicate attribute name: " + pName);
		}
	}
}