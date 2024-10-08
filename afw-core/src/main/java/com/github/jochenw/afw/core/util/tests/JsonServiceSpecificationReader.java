package com.github.jochenw.afw.core.util.tests;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.BiConsumer;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.util.Strings;
import com.github.jochenw.afw.core.util.tests.JsonServiceTests.JsonServiceTestSpecification;

/**
 * An object, which converts a "request.json" file into a service test specification.
 */
public class JsonServiceSpecificationReader {
	/**
	 * Parses the contents of a "request.json" file, and converts it into a service test specification.
	 * @param pServiceSpecification A Json object, which supplies the details of the created
	 *   {@link JsonServiceTestSpecification service test specification}.
	 * @return A {@link JsonServiceTestSpecification service test specification}, which has been
	 *   created from the contents of the given object.
	 */
	public JsonServiceTestSpecification parse(@NonNull JsonObject pServiceSpecification) {
		final JsonString urlJStr = pServiceSpecification.getJsonString("url"); 
		if (urlJStr == null) {
			throw new IllegalStateException("Attribute url is missing in service test specification");
		}
		final String urlStr = urlJStr.getString();
		if (urlStr.trim().length() == 0) {
			throw new IllegalStateException("Attribute url is empty in service test specification");
		}
		final @NonNull URL url;
		try {
			url = Strings.asUrl(urlStr);
		} catch (MalformedURLException e) {
			throw new IllegalStateException("Attribute url is invalid in service test specification: " + urlStr, e);
		}
		final JsonString methodJStr = pServiceSpecification.getJsonString("method");
		if (methodJStr == null) {
			throw new IllegalStateException("Attribute method is missing in service test specification");
		}
		String method = validateMethod(methodJStr.getString());
		if (method == null) {
			throw new IllegalStateException("Attribute method is invalid in service test specification: "
						+ pServiceSpecification.getString("method"));
		}
		
		
		final JsonServiceTestSpecification jstSpec = new JsonServiceTestSpecification(url, method);
		readValueList(pServiceSpecification, "queryParameters", jstSpec::addQueryParameter);
		readValueList(pServiceSpecification, "headers", jstSpec::addHeader);
		return jstSpec;
	}

	/** Called to read a list of key/value pairs, like query parameters, or HTTP parameters, from the
	 * given JSON objects attribute {@code pName}.
	 * @param pServiceSpecification The JSON object, from which to read the service test specification.
	 * @param pName The JSON objects attribute name, which specifies the value list.
	 * @param pConsumer A consumer, which is being called for every key/value pair, which has
	 *   been read.
	 * @throws IllegalStateException The list of key/value pairs is invalid.
	 */
	protected void readValueList(@NonNull JsonObject pServiceSpecification, @NonNull String pName, @NonNull BiConsumer<@NonNull String,@NonNull String> pConsumer) {
		final JsonValue jv = pServiceSpecification.get(pName);
		if (jv != null) {
			if (jv instanceof JsonArray) {
				final JsonArray ja = (JsonArray) jv;
				for (int i = 0;  i < ja.size();  i++) {
					final JsonValue jsv = ja.get(i);
					if (jsv != null) {
						if (jsv instanceof JsonObject) {
							final JsonObject jo = (JsonObject) jsv;
							String key = jo.getString("key");
							if (key == null) {
								key = jo.getString("name");
							}
							if (key == null) {
								throw new IllegalStateException("Missing attribute 'key', or 'name', in JsonObject " + i + " at value list " + pName);
							}
							final String value = jo.getString("value");
							if (value == null) {
								throw new IllegalStateException("Missing attribute 'value', in JsonObject " + i + " at value list " + pName);
							}
							pConsumer.accept(key, value);
						} else if (jsv instanceof JsonString) {
							final JsonString js = (JsonString) jsv;
							@SuppressWarnings("null")
							final @NonNull String kvPair = js.getString();
							final int offset = kvPair.indexOf('=');
							if (offset == -1) {
								throw new IllegalStateException("Invalid string element " + i + " of value list " + pName
										+ ": Expected key=value, got " + kvPair);
							} else {
								@SuppressWarnings("null")
								final @NonNull String key = kvPair.substring(0, offset);
								@SuppressWarnings("null")
								final @NonNull String value = kvPair.substring(offset+1);
								pConsumer.accept(key, value);
							}
						} else {
							throw new IllegalStateException("Invalid element " + i + " in value list " + pName
									+ ": Expected string (key=value), or object (key: 'key', value: 'value'), got " + jsv.getClass().getSimpleName());
						}
					}
				}
			} else if (jv instanceof JsonObject) {
				final JsonObject jo = (JsonObject) jv;
				jo.forEach((k,v) -> {
					if (k == null) {
						throw new IllegalStateException("Null key detected in JSON object for value list " + pName);
					}
					final @NonNull String key = k;
					if (v == null) {
						throw new IllegalStateException("Null value detected in JSON object for value list " + pName);
					}
					if (v instanceof JsonString) {
						final JsonString jsv = (JsonString) v;
						@SuppressWarnings("null")
						final @NonNull String val = jsv.getString();
						pConsumer.accept(key, val);
					} else if (v instanceof JsonArray) {
						final JsonArray ja = (JsonArray) v;
						for (int i = 0;  i < ja.size();  i++) {
							final JsonString js = ja.getJsonString(i);
							if (js == null) {
								throw new IllegalStateException("Null value detected in JSON string list for value list " + pName);
							}
							@SuppressWarnings("null")
							final @NonNull String str = js.getString();
							pConsumer.accept(key, str);
						}
					} else {
						throw new IllegalStateException("Invalid value detected in attribute " + key
								+ " of JSON object for value list " + pName);
					}
				});
			}
		}
	}

	/** Called to validate (and , possibly, adjust the value of the HTTP method verb.
	 * @param pMethod The specified value of the HTTP method verb.
	 * @return A non-null value indicates, that the HTTP method verb is valid,
	 * possibly with adjustments applied. The caller should replace the input
	 * value with the return value. (For example, the returned value is uppercased.)
	 * Null, if the input value was found to be invalid.
	 */
	protected String validateMethod(String pMethod) {
		if (pMethod == null) {
			return "GET"; // Okay
		} else {
			final String method = pMethod.toUpperCase();
			switch (method) {
			  case "GET":
			  case "POST":
			  case "PUT":
			  case "DELETE":
			  case "PATCH":
				  return method; // Okay.
			  default:
				  return null;
			}
		}
	}
}
