package com.github.jochenw.afw.core.util.tests;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UncheckedIOException;
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

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.util.HttpConnector;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IComponentFactoryAware;

/** A class for automated execution of REST service tests.
 */
public class JsonServiceTests {
	/** A named list of values, which is used to store query parameters, or HTTP headers.
	 */
	public static class ValueList {
		private final @NonNull String name;
		private final @NonNull List<@NonNull String> values = new ArrayList<>();

		/** Creates a new instance with the given name.
		 * @param pName The lists name (Case sensitive).
		 */
		public ValueList(@NonNull String pName) {
			name = pName;
		}

		/** Returns the lists name.
		 * @return The lists name.
		 */
		public @NonNull String getName() {
			return name;
		}

		/** Returns the lists values.
		 * @return The lists values.
		 */
		public @NonNull List<@NonNull String> getValues() {
			return values;
		}

		/** Called to iterate over the values by invoking a value consumer for every value, in order.
		 * @param pConsumer The consumer, which is being invoked for every value, in order.
		 */
		public void forEach(@NonNull Consumer<@NonNull String> pConsumer) {
			values.forEach(pConsumer);
		}
	}
	/** Request specification, suitable for
	 * {@link JsonServiceTests#runServiceTest(Path)}
	 */
	public static class JsonServiceTestSpecification implements Serializable {
		private static final long serialVersionUID = -64038754447555476L;

		/** Base URL of the REST service, which is being resred.
		 */
		private final @NonNull URL url;
		/** HTTP method of the REST service request.
		 */
		private final @NonNull String method;
		/** Map of HTTP query parameters.
		 */
		private Map<@NonNull String,@NonNull ValueList> queryParameters;
		/** Map of HTTP headers.
		 */
		private Map<@NonNull String,@NonNull ValueList> headers;

		/** Creates a new instance with the given URL, and method.
		 * @param pUrl The URL, to which a JSON request is being sent.
		 * @param pMethod The HTTP method, which is being sent, when sending the JSON request.
		 */
		public JsonServiceTestSpecification(@NonNull URL pUrl, @NonNull String pMethod) {
			url = Objects.requireNonNull(pUrl, "Url");
			method = Objects.requireNonNull(pMethod, "Method");
		}

		/**
		 * Adds a new value to a map of parameters, or headers.
		 * @param pMap The map of parameters, or headers.
		 * @param pKey The parameter, or header, name.
		 * @param pValue The parameter, or header, value.
		 */
		protected void addValueToMap(final Map<@NonNull String,@NonNull ValueList> pMap, @NonNull String pKey, @NonNull String pValue) {
			@SuppressWarnings("null")
			final @NonNull String key = Objects.requireNonNull(pKey, "Key").toLowerCase();
			final @NonNull String value = Objects.requireNonNull(pValue, "Value");
			@SuppressWarnings("null")
			final @NonNull ValueList vList = pMap.computeIfAbsent(key, (k) -> {
				return new ValueList(pKey);
			});
			vList.getValues().add(value);
		}

		/** Adds a query parameter with the given name, and value.
		 * @param pKey The query parameters name.
		 * @param pValue The query parameters value.
		 */
		public void addQueryParameter(@NonNull String pKey, @NonNull String pValue) {
			if (queryParameters == null) {
				queryParameters = new HashMap<>();
			}
			addValueToMap(queryParameters, pKey, pValue);
		}

		/** Adds a HTTP header with the given name, and value.
		 * @param pKey The HTTP headers name.
		 * @param pValue The HTTP headers value.
		 */
		public void addHeader(@NonNull String pKey, @NonNull String pValue) {
			if (headers == null) {
				headers = new HashMap<>();
			}
			addValueToMap(headers, pKey, pValue);
		}

		/** Returns the URL, to which a JSON request is being sent.
		 * @return The URL, to which a JSON request is being sent.
		 */
		public URL getUrl() { return url; }
		/** Returns the HTTP method, which is being sent, when sending the JSON request.
		 * @return The HTTP method, which is being sent, when sending the JSON request.
		 */
		public String getMethod() { return method; }

		/** Returns the map of query parameters.
		 * @return The map of query parameters.
		 */
		@NonNull Map<@NonNull String, @NonNull ValueList> getQueryParameters() { return Objects.requireNonNull(queryParameters); }
		/** Returns the map of HTTP request headers.
		 * @return The map of HTTP request headers.
		 */
		@NonNull Map<@NonNull String, @NonNull ValueList> getHeaders() { return Objects.requireNonNull(headers); }
	}

	/** Representation of a service test, which can be executed. The representation is
	 * typically read from a file "request.json".
	 */
	public static class JsonServiceTest implements IComponentFactoryAware {
		@SuppressWarnings("unused")
		private final Path dir;
		private final Path requestJsonFile;
		private static final @NonNull HttpConnector DEFAULT_HTTP_CONNECTOR = new HttpConnector();
		private static final @NonNull JsonServiceSpecificationReader DEFAULT_SERVICE_SPEC_READER = new JsonServiceSpecificationReader();
		@SuppressWarnings("unused")
		private @NonNull HttpConnector httpConnector = DEFAULT_HTTP_CONNECTOR;
		private @NonNull JsonServiceSpecificationReader reader = DEFAULT_SERVICE_SPEC_READER;

		/** Creates a new instance.
		 * @param pDir The directory from which to read the service test representation.
		 * The directory is supposed to contain a file "request.json".
		 */
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

		/** Initializes the instance by obtaining a {@link HttpConnector}.
		 * @param pFactory The component factory, which provides the {@link HttpConnector}.
		 */
		@Override
		public void init(IComponentFactory pFactory) throws Exception {
			httpConnector = Objects.notNull(pFactory.getInstance(HttpConnector.class), DEFAULT_HTTP_CONNECTOR);
			reader = Objects.notNull(pFactory.getInstance(JsonServiceSpecificationReader.class), DEFAULT_SERVICE_SPEC_READER);
		}

		/** Called to execute the test.
		 */
		public void run() {
			try (InputStream is = Files.newInputStream(requestJsonFile);
				 JsonReader jsonReader = Json.createReader(is)) {
				@SuppressWarnings("unused")
				JsonObject jo = jsonReader.readObject();
			} catch (IOException ioe) {
				throw new UncheckedIOException(ioe);
			}
		}
	}

	/** Creates a new instance. Private constructor, because all methods are static.
	 */
	private JsonServiceTests() {}
	
	/** Called to run a test, which is specified in the given directory.
	 * @param pDir The directory, where the service test specification
	 * is stored.
	 */
	public static void runServiceTest(Path pDir) {
		final JsonServiceTest jst = new JsonServiceTest(pDir);
		@SuppressWarnings("unused")
		final JsonServiceTestSpecification jsts;
		try (InputStream is = Files.newInputStream(jst.requestJsonFile);
			 JsonReader jr = Json.createReader(is)) {
			@SuppressWarnings("null")
			final @NonNull JsonObject jo = jr.readObject();
			jsts = jst.reader.parse(jo);
		} catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}

	/** Tests, whether a given directory contains a service
	 * test specification. If so, the directory is a valid
	 * argument for {@link #runServiceTest(Path)}.
	 * @param pDir The directory, which is being tested.
	 * @return True, if the directory contains a service
	 *   test specification.
	 */
	public static boolean isServiceTestDirectory(Path pDir) {
		try {
			new JsonServiceTest(pDir);
		} catch (Throwable t) {
			return false;
		}
		return true;
	}
}
