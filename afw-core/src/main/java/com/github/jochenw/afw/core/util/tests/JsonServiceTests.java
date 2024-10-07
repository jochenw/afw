package com.github.jochenw.afw.core.util.tests;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.github.jochenw.afw.core.util.HttpConnector;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.core.util.Strings;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IComponentFactoryAware;

public class JsonServiceTests {
	/** Request specification, suitable for
	 * {@link JsonServiceTests#runServiceTest(Path)}
	 */
	public static class JsonServiceTestSpecification implements Serializable {
		private static final long serialVersionUID = -64038754447555476L;

		private final URL url;
		private final String method;
		private Map<String,List<String>> queryParameters, headers;

		public JsonServiceTestSpecification(URL pUrl, String pMethod) {
			url = pUrl;
			method = pMethod;
		}

		protected void addValueToMap(final Map<String,List<String>> pMap, String pKey, String pValue) {
			final String key = Objects.requireNonNull(pKey, "Key").toLowerCase();
			final String value = Objects.requireNonNull(pKey, "Value");
			pMap.computeIfAbsent(key, (k) -> new ArrayList<>()).add(value);
		}
	
		public void addQueryParameter(String pKey, String pValue) {
			if (queryParameters == null) {
				queryParameters = new HashMap<>();
			}
			addValueToMap(queryParameters, pKey, pValue);
		}

		public void addHeader(String pKey, String pValue) {
			if (headers == null) {
				headers = new HashMap<>();
			}
			addValueToMap(headers, pKey, pValue);
		}
	}

	public static class JsonServiceTest implements IComponentFactoryAware {
		private final Path dir;
		private final Path requestJsonFile;
		private static final HttpConnector DEFAULT_HTTP_CONNECTOR = new HttpConnector();
		private HttpConnector httpConnector;

		public JsonServiceTest(Path pDir) {
			dir = pDir;
			if (!Files.isDirectory(pDir)) {
				throw new IllegalStateException("Directory not found: " + pDir);
			}
			requestJsonFile = pDir.resolve("request.json");
			if (!Files.isRegularFile(requestJsonFile)) {
				throw new IllegalStateException("RFequest file not found: " + requestJsonFile);
			}
		}

		@Override
		public void init(IComponentFactory pFactory) throws Exception {
			httpConnector = Objects.notNull(pFactory.getInstance(HttpConnector.class), DEFAULT_HTTP_CONNECTOR);
		}

		public void run() {
			try (InputStream is = Files.newInputStream(requestJsonFile);
				 JsonReader jsonReader = Json.createReader(is)) {
				run(jsonReader.readObject());
			} catch (IOException ioe) {
				throw new UncheckedIOException(ioe);
			}
		}

		public JsonServiceTestSpecification run(JsonObject pServiceSpecification) {
			final String urlStr = pServiceSpecification.getString("url");
			if (urlStr == null) {
				throw new NullPointerException("Attribute url is missing in service test specification");
			}
			if (urlStr.trim().length() == 0) {
				throw new IllegalArgumentException("Attribute url is empty in service test specification");
			}
			final URL url;
			try {
				url = Strings.asUrl(urlStr);
			} catch (MalformedURLException e) {
				throw new IllegalArgumentException("Attribute url is invalid in service test specification: " + urlStr, e);
			}
			String method = validateMethod(pServiceSpecification.getString("method"));
			if (method == null) {
				throw new IllegalArgumentException("Attribute method is invalid in service test specification: "
							+ pServiceSpecification.getString("method"));
			}
			
			return new JsonServiceTestSpecification(url, method);
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

	public static void runServiceTest(Path pDir) {
		final JsonServiceTest jst = new JsonServiceTest(pDir);
		final JsonServiceTestSpecification jsts;
		try (InputStream is = Files.newInputStream(jst.requestJsonFile);
			 JsonReader jr = Json.createReader(is)) {
			final JsonObject jo = jr.readObject();
			jsts = jst.run(jo);
		} catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}

	public static boolean isServiceTestDirectory(Path pDir) {
		try {
			new JsonServiceTest(pDir);
		} catch (Throwable t) {
			return false;
		}
		return true;
	}
}
