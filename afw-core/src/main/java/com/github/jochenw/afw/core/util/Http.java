package com.github.jochenw.afw.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.core.function.Functions.FailableBiConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableFunction;
import com.github.jochenw.afw.core.function.Functions.FailableSupplier;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.util.HttpConnector.HttpConnection;


/** A utility class for sending HTTP requests, and processing the response.
 * The general idea is to create an instance of {@link Http.Request} in
 * a builder-like manner. The request is being sent by using either of
 * the methods {@link Http.Request#call(Functions.FailableFunction)},
 * or {@link Http.Request#run(Functions.FailableConsumer)}.
 */
public class Http {
	/** Private constructor, because this class has only static methods.
	 */
	private Http() {}
	/** A simple logger implementation, which can be used to log Http requests,
	 * and the response.
	 */
	public static interface TrafficLogger {
		/** Logs the given message string.
		 * @param pMsg The message string, which is being logged.
		 */
		public void log(String pMsg);
		/** Logs the given byte array as a message string..
		 * @param pBytes The byte array, which is being logged.
		 */
		public void log(byte @NonNull [] pBytes);
		/** Logs the given error message string.
		 * @param pMsg The message string, which is being logged.
		 */
		public void logErr(String pMsg);
		/** Terminates a message line by logging a line terminator
		 * string. The default implementation invokes
		 * {@link #log(String)} with the argument
		 * {@link Streams#LINE_SEPARATOR}.
		 */
		public default void newLine() {
			log(Streams.LINE_SEPARATOR);
		}
		/** Terminates an error message line by logging a
		 * line terminator string. The default implementation
		 * invokes {@link #logErr(String)} with the argument
		 * {@link Streams#LINE_SEPARATOR}.
		 */
		public default void newLineErr() {
			logErr(Streams.LINE_SEPARATOR);
		}
		/** Logs the given byte array as an error message string..
		 * @param pBytes The byte array, which is being logged.
		 */
		public void logErr(byte @NonNull [] pBytes);
		/** Logs the given message string, followed by a line terminator.
		 * @param pMsg The message string, which is being logged.
		 */
		public default void logLn(String pMsg) {
			log(pMsg);
			newLine();
		}
		/** Logs the given error message string, followed by a line terminator.
		 * @param pMsg The error message string, which is being logged.
		 */
		public default void logErrLn(String pMsg) {
			logErr(pMsg);
			newLineErr();
		}
		/** Logs the given byte array as a message string,
		 * followed by an invocation to {@link #newLine()}.
		 * @param pBytes The byte array, which is being logged.
		 */
		public default void logLn(byte @NonNull[] pBytes) {
			log(pBytes);
			newLine();
		}
		/** Logs the given byte array as an error message
		 * string, followed by an invocation to {@link #newLineErr()}.
		 * @param pBytes The byte array, which is being logged.
		 */
		public default void logErrLn(byte @NonNull[] pBytes) {
			logErr(pBytes);
			newLineErr();
		}
		/** Logs the given message format string, with the given
		 * arguments. Message string, and arguments will be
		 * formatted into a string by invoking
		 * {@link Strings#formatCb(String, Object...)}, and
		 * the result will be passed to {@link #log(String)}.
		 * @param pMsg The message format string, which is being logged.
		 * @param pArgs The message arguments.
		 */
		public default void log(@NonNull String pMsg, @Nullable Object... pArgs) {
			log(Strings.formatCb(pMsg, pArgs));
		}
		/** Logs the given error message format string, with
		 * the given arguments. Message string, and arguments
		 * will be formatted into a string by invoking
		 * {@link Strings#formatCb(String, Object...)}, and
		 * the result will be passed to {@link #logErr(String)}.
		 * @param pMsg The message format string, which is being logged.
		 * @param pArgs The message arguments.
		 */
		public default void logErr(@NonNull String pMsg, @Nullable Object... pArgs) {
			logErr(Strings.formatCb(pMsg, pArgs));
		}
		/** Logs the contents of the given {@link InputStream} by
		 * invoking {@link #log(byte[])} repeatedly, until the
		 * {@link InputStream} has been consumed. Typically, this will
		 * be used to log a request, or response body.
		 * @param pIn The input stream, which is being logged.
		 */
		public default void log(InputStream pIn) {
			log(Streams.read(pIn)); }
		/** Logs the contents of the given {@link InputStream} by
		 * invoking {@link #logErr(byte[])} repeatedly, until the
		 * {@link InputStream} has been consumed. Typically, this will
		 * be used to log the body of an error response.
		 * @param pIn The input stream, which is being logged.
		 */
		public default void logErr(InputStream pIn) { logErr(Streams.read(pIn)); }
		/** Logs the contents of the given {@link InputStream} by
		 * invoking {@link #log(byte[])} repeatedly, until the
		 * {@link InputStream} has been consumed. Typically, this will
		 * be used to log a request, or response body. Finally,
		 * {@link #newLine()} will be invoked.
		 * @param pIn The input stream, which is being logged.
		 */
		public default void logLn(InputStream pIn) { logLn(Streams.read(pIn)); }
		/** Logs the contents of the given {@link InputStream} by
		 * invoking {@link #logErr(byte[])} repeatedly, until the
		 * {@link InputStream} has been consumed. Typically, this will
		 * be used to log an error response body. Finally,
		 * {@link #newLineErr()} will be invoked.
		 * @param pIn The input stream, which is being logged.
		 */
		public default void logErrLn(InputStream pIn) { logErrLn(Streams.read(pIn)); }
		/** Returns, whether this logger is actually logging. This can be
		 * used to avoid really verbose logging output. The default implementation
		 * returns true, only the {@link TrafficLogger#NULL_LOGGER} returns false. 
		 * @return True, if this logger is actually logging.
		 */
		public default boolean isLogging() { return true; }

		/** Implementation of {@link TrafficLogger}, which does nothing.
		 */
		public static final @NonNull TrafficLogger NULL_LOGGER = new TrafficLogger() {
			@Override public void logErr(byte @NonNull[] pBytes) { /* Do nothing */ }
			@Override public void logErr(String pMsg) { /* Do nothing */ }
			@Override public void log(byte @NonNull[] pBytes) { /* Do nothing */ }
			@Override public void log(String pMsg) { /* Do nothing */ }
			@Override public void logLn(String pMsg) { /* Do nothing */ }
			@Override public void logErrLn(String pMsg) { /* Do nothing */ }
			@Override public void logLn(byte @NonNull[] pBytes) { /* Do nothing */ }
			@Override public void logErrLn(byte @NonNull[] pBytes) { /* Do nothing */ }
			@Override public void log(InputStream pIn) { /* Do nothing */ }
			@Override public void logErr(InputStream pIn) { /* Do nothing */ }
			@Override public void logLn(InputStream pIn) { /* Do nothing */ }
			@Override public void logErrLn(InputStream pIn) { /* Do nothing */ }
			@Override public boolean isLogging() { return false; }
		};

		/** Implementation of {@link TrafficLogger}, which uses {@link System#out}
		 * for logging non-error messages, and {@link System#err}.
		 */
		public static final @NonNull TrafficLogger SYSTEM_OUT_LOGGER = new TrafficLogger() {
			@Override public void logErr(byte @NonNull[] pBytes) {
				write(Objects.requireNonNull(System.err, "System.err"), pBytes);
			}
			@Override public void logErr(String pMsg) { System.err.print(pMsg); }
			@Override public void log(byte @NonNull[] pBytes) {
				write(Objects.requireNonNull(System.out, "System.out"), pBytes);
			}
			@Override public void log(String pMsg) { System.out.print(pMsg); }
		};

		/** Writes the given byte array, to the given {@link OutputStream},
		 * catching, and hiding, {@link IOException I/O exceptions}.
		 * @param pOut The destination output stream
		 * @param pBytes The byte array, which is being written.
		 */
		public static void write(@NonNull OutputStream pOut, byte @NonNull [] pBytes) {
			try {
				pOut.write(pBytes);
			} catch (IOException ioe) {
				throw Exceptions.show(ioe);
			}
		}

		/** Creates a new instance, which logs messages to the given {@link OutputStream}
		 * using the {@link StandardCharsets#UTF_8 UTF-8 charset} for conversion of
		 * strings into byte array.
		 * @param pOut The destination stream.
		 * @return The created instance.
		 * @see #of(OutputStream, Charset)
		 */
		public static @NonNull TrafficLogger of(OutputStream pOut) { return of(pOut, Streams.UTF_8); }
		/** Creates a new instance, which logs messages to the given {@link OutputStream},
		 * using the given {@code pCharset} for conversion of strings into byte array.
		 * @param pOut The destination stream.
		 * @param pCharset The character set to use.
		 * @return The created instance.
		 * @see #of(OutputStream)
		 */
		public static @NonNull TrafficLogger of(OutputStream pOut, Charset pCharset) {
			final @NonNull OutputStream out = Objects.requireNonNull(pOut, "OutputStream");
			final @NonNull Charset cs = Objects.requireNonNull(pCharset, "Charset");
			return new TrafficLogger() {
				@Override public void log(String pMsg) {
					log(Objects.requireNonNull(pMsg.getBytes(cs)));
				}
				@Override public void log(byte @NonNull[] pBytes) { TrafficLogger.write(out, pBytes); }
				@Override public void logErr(String pMsg) {
					logErr(Objects.requireNonNull(pMsg.getBytes(cs))); }
				@Override public void logErr(byte @NonNull[] pBytes) { TrafficLogger.write(out, pBytes); }
			};
		}

		/** Creates a new instance, which works by using the given
		 * {@link ILog} internally.
		 * @param pLog The logger, which is being used internally.
		 * @return The created instance.
		 */
		public static TrafficLogger of(ILog pLog) {
			final @NonNull ILog log = Objects.requireNonNull(pLog, "ILog");
			return new TrafficLogger() {
				@Override public void logErr(byte @NonNull[] pBytes) { logErr(Strings.toHexString(pBytes)); }
				@Override public void logErr(String pMsg) { log.error("TrafficLogger.logErr", pMsg); }
				@Override public void log(byte @NonNull[] pBytes) { log(Strings.toHexString(pBytes)); }
				@Override public void log(String pMsg) { log.trace("TrafficLogger.log", pMsg); }
				@Override public void logLn(byte @NonNull[] pBytes) { log.trace("TrafficLogger.logLn", Strings.toHexString(pBytes)); }
				@Override public void logErrLn(byte @NonNull[] pBytes) { log.error("TrafficLogger.logErrLn", Strings.toHexString(pBytes)); }
				@Override
				public void logLn(InputStream pIn) { log.trace("TrafficLogger.logLn", Strings.toHexString(Streams.read(pIn))); }
				@Override
				public void logErrLn(InputStream pIn) { log.error("TrafficLogger.logErrLn", Strings.toHexString(Streams.read(pIn))); }
			};
		}
	}
	/** An object, which holds a set of key/value pairs, like HTTP headers,
	 * or parameters.
	 */
	public static class KvPairs {
		private final boolean caseSensitive;
		private @Nullable Map<@NonNull String,List<String>> map;

		/** Creates a new instance.
		 * @param pCaseSensitive True, if the keys should be handled
		 *   case sensitive.
		 */
		public KvPairs(boolean pCaseSensitive) {
			caseSensitive = pCaseSensitive;
		}
		/** Creates a new instance with case sensitive handling of
		 * keys. In other words, this is equivalent to
		 * <pre>KvPairs(true)</pre>.
		 */
		public KvPairs() {
			this(true);
		}
		/** Returns the map, which is being used internally.
	     * If this object doesn't have case sensitive handling
	     * enabled, then all keys will be lowercased.
		 * @return The map, which is being used internally.
		 */
		protected @NonNull Map<@NonNull String,List<String>> getMap() {
			if (map == null) {
				final Map<@NonNull String,List<String>> mp = new HashMap<>();
				map = mp;
			}
			return Objects.requireNonNull(map);
		}
		/** Adds a new key/value pair.
		 * @param pKey The key.
		 * @param pValue The value.
		 */
		public void add(@NonNull String pKey, String pValue) {
			final @NonNull String key = asKey(pKey);
			getMap().computeIfAbsent(key, (k) -> new ArrayList<String>()).add(pValue);
		}
		/** Called to iterate over all keys, and their respective values.
		 * @param pConsumer The consumer, which is being invoked for all
		 * keys. The consumer will receive the respective key as the first
		 * argument, and the associated value list as the second argument.
		 * If this object doesn't have case-sensitive handling enabled, then
		 * all keys will be lowercased.
		 */
		public void forEach(FailableBiConsumer<@NonNull String,@NonNull List<String>,?> pConsumer) {
			if (map != null) {
				getMap().forEach((k,l) -> {
					final @NonNull List<String> list = Objects.requireNonNull(l);
					try {
						pConsumer.accept(k, list);
					} catch (Throwable t) {
						throw Exceptions.show(t);
					}
				});
			}
		}
		/** Returns the given keys value list, if available, or null.
		 * @param pKey The key, for which the values are being queried.
		 * @return The requested list of values, of available, or null.
		 */
		public List<String> get(@NonNull String pKey) {
			final @NonNull String key = asKey(pKey);
			if (map == null) {
				return null;
			}
			final Map<@NonNull String,List<String>> mp = Objects.requireNonNull(map);
			return mp.get(key);
		}
		/** Iterates over the values of the given key by invoking the given
		 * consumer.
		 * @param pKey The key, for which the values are being queried.
		 * @param pConsumer The consumer, which is being invoked for every value
		 *   of the given key.
		 */
		public void forEach(@NonNull String pKey, FailableConsumer<String,?> pConsumer) {
			final @NonNull String key = asKey(pKey);
			final List<String> list = get(key);
			if (list != null) {
				list.forEach((s) -> {
					try {
						pConsumer.accept(s);
					} catch (Throwable t) {
						throw Exceptions.show(t);
					}
				});
			}
		}
		/** Returns the actual key, which is used for storing the
		 * given keys values.
		 * @param pKey The actual key. If case-sensitive handling
		 *   of keys is enabled, then this will be zhe argument
		 *   {@code pKey} itself, otherwise the argument will be
		 *   lowercased.
		 * @return The actual key.
		 */
		protected @NonNull String asKey(@NonNull String pKey) {
			if (caseSensitive) {
				return pKey;
			} else {
				@SuppressWarnings("null")
				final @NonNull String key = pKey.toLowerCase();
				return key;
			}
		}
	}
	/** A builder-like object, which is being used to configure the
	 * outgoing HTTP request. To obtain such an object, use
	 * {@link Http#request()}. Some configuration values are mandatory,
	 * for example, you must invoke {@link #url(URL)}, or {@link #url(String)},
	 * and either of {@link #get()}, {@link #post()}, {@link #put()},
	 * {@link #delete()}, and {@link #method(String)}, but most configuration
	 * methods are optional.
	 */
	public static class Request {
		private @NonNull TrafficLogger logger = TrafficLogger.NULL_LOGGER;
		private @Nullable KvPairs headers;
		private @Nullable KvPairs parameters;
		private HttpConnector connector;
		private @Nullable String method;
		private Charset charset;
		private FailableConsumer<OutputStream,?> body;
		private URL url;
		private String restResource, restResourceId;

		/** Package protected constructor, because you
		 * are supposed to use {@link Http#request()}.
		 */
		Request() {}

		/** Returns the complete URL, which is being requested.
		 * The URL is built from the following components, if
		 * available:
		 * <ol>
		 *   <li>The {@link #url(URL) servers URL}</li>
		 *   <li>The {@link #restResource REST resource}</li>
		 *   <li>The {@link #restResourceId REST resource id}</li>
		 *   <li>The {@link #parameter(String, String) HTTP parameters}</li>
		 * </ol>
		 * @return The complete URL, including REST resource, REST resource id,
		 *   and HTTP parameters.
		 * @throws NullPointerException No URL has been configuted. In other words:
		 *   Neither of {@link #url(URL)}, nor {@link #url(String)} have been
		 *   invoked.
		 */
		public URL getUrl() {
			final StringBuilder sb = new StringBuilder();
			sb.append(Objects.requireNonNull(url, "URL").toExternalForm());
			final Consumer<String> appender = (s) -> {
				if (s != null  &&  s.length() > 0) {
					final char lastChar;
					if (sb.length() == 0) {
						lastChar = '.';
					} else {
						lastChar = sb.charAt(sb.length()-1);
					}
					if (lastChar != '/') {
						sb.append('/');
					}
					while (s.startsWith("/")) {
						s = s.substring(1);
					}
					sb.append(s);
				}
			};
			appender.accept(restResource);
			appender.accept(restResourceId);
			if (parameters != null) {
				@SuppressWarnings("null")
				final @NonNull KvPairs params = (@NonNull KvPairs) parameters;
				params.forEach((k,l) -> {
					l.forEach((v) -> {
						if (sb.indexOf("?") >= 0) {
							sb.append("&");
						} else {
							sb.append("?");
						}
						urlEncode(sb, k);
						sb.append("=");
						urlEncode(sb, v);
					});
				});
			}
			@SuppressWarnings("null")
			final @NonNull String urlStr = (@NonNull String) sb.toString();
			try {
				return Strings.asUrl(urlStr);
			} catch (IOException ioe) {
				throw Exceptions.show(ioe);
			}
		}

		/** Writes an URL encoded representation of the string
		 * {@code pValue} to the given {@link StringBuilder}.
		 * @param pSb The {@link StringBuilder}, to which the
		 *   URL encoded value is being written.
		 * @param pValue The value, which is being URL
		 *   encoded.
		 */
		protected void urlEncode(StringBuilder pSb, String pValue) {
			final String encodedValue;
			try {
				encodedValue = URLEncoder.encode(pValue, getCharset().name());
			} catch (UnsupportedEncodingException e) {
				throw Exceptions.show(e);
			}
			pSb.append(encodedValue);
		}

		/** Sets the REST resource. If present, the REST resource is being
		 * appended to the base URL in order to build the complete URL.
		 * @param pRestResource The REST resource, if any, or null.
		 * @return This request object, for builder-like programming,
		 */
		public @NonNull Request restResource(String pRestResource) {
			restResource = pRestResource;
			return this;
		}

		/** Returns the REST resource. If present, the REST resource is being
		 * appended to the base URL in order to build the complete URL.
		 * @return The REST resource, if any, or null.
		 */
		public @Nullable String getRestResource() {
			return restResource;
		}

		/** Sets the REST resource id. If present, the REST resource id is
		 * being appended to the base URL, and the REST resource, in order
		 * to build the complete URL.
		 * @param pRestResourceId The REST resource id, if any, or null.
		 * @return This request object, for builder-like programming,
		 */
		public @NonNull Request restResourceId(String pRestResourceId) {
			restResourceId = pRestResourceId;
			return this;
		}

		/** Returns the REST resource id. If present, the REST resource id
		 * is being appended to the base URL, and the REST resource, in
		 * order to build the complete URL.
		 * @return  The REST resource id, if any, or null.
		 */
		public @Nullable String getRestResourceId() {
			return restResourceId;
		}

		/** Sets the base URL, as opposed to the complete URL. The complete
		 * URL is built from this value, but also from the {@link #getRestResource()},
		 * the {@link #getRestResourceId()}, and the HTTP parameters. If no
		 * parameters, and no resource strings are configured, then the base
		 * URL is the complete URL.
		 * @param pUrl The base URL.
		 * @return This request object, for builder-like programming.
		 * @see #url(String)
		 * @see #getUrl()
		 */
		public Request url(@NonNull URL pUrl) {
			url = Objects.requireNonNull(pUrl, "URL");
			return this;
		}

		/** Sets the base URL, as opposed to the complete URL. The complete
		 * URL is built from this value, but also from the {@link #getRestResource()},
		 * the {@link #getRestResourceId()}, and the HTTP parameters. If no
		 * parameters, and no resource strings are configured, then the base
		 * URL is the complete URL.
		 * @param pUrl The base URL.
		 * @return This request object, for builder-like programming.
		 * @see #url(URL)
		 * @see #getUrl()
		 */
		public Request url(@NonNull String pUrl) {
			final @NonNull String urlStr = Objects.requireNonNull(pUrl, "URL");
			final URL u;
			try {
				u = Strings.asUrl(urlStr);
			} catch (MalformedURLException mue) {
				throw new IllegalArgumentException("Invalid URL string: " + urlStr, mue);
			}
			return url(u);
		}

		/** Sets the logger, for displaying traffic details.
		 * @param pLogger The traffic logger.
		 * @return This request object, for builder-like programming.
		 * @see #systemOutLogger()
		 */
		public Request logger(@NonNull TrafficLogger pLogger) {
			logger = Objects.requireNonNull(pLogger, "Logger");
			return this;
		}

		/** Sets the logger to {@link TrafficLogger#SYSTEM_OUT_LOGGER}, for displaying
		 * traffic details.
		 * @return This request object, for builder-like programming.
		 * @see #logger(TrafficLogger)
		 */
		public Request systemOutLogger() {
			return logger(TrafficLogger.SYSTEM_OUT_LOGGER);
		}

		/** Returns the current traffic logger. By default, this will be
		 * {@link TrafficLogger#NULL_LOGGER}.
		 * @return The current traffic logger.
		 */
		public @NonNull TrafficLogger getLogger() {
			return logger;
		}

		/** Adds an HTTP header to the request.
		 * @param pKey The HTTP headers key.
		 * @param pValue The HTTP headers value.
		 * @return This request object, for builder-like programming.
		 */
		public @NonNull Request header(@NonNull String pKey, @NonNull String pValue) {
			getHeaders().add(pKey, pValue);
			return this;
		}

		/** Adds an HTTP parameter to the request.
		 * @param pKey The HTTP parameters key.
		 * @param pValue The HTTP parameters value.
		 * @return This request object, for builder-like programming.
		 */
		public @NonNull Request parameter(@NonNull String pKey, @NonNull String pValue) {
			getParameters().add(pKey, pValue);
			return this;
		}

		/** Returns the set of HTTP headers, which have been configured
		 * through {@link #header(String, String)}.
		 * @return The set of HTTP headers. Never null, but possibly
		 *   an empty set.
		 */
		public @NonNull KvPairs getHeaders() {
			if (headers == null) {
				final @NonNull KvPairs hdrs = new KvPairs();
				headers = hdrs;
				return hdrs;
			} else {
				return Objects.requireNonNull(headers);
			}
		}

		/** Returns the set of HTTP parameters, which have been configured
		 * through {@link #parameter(String, String)}.
		 * @return The set of HTTP parameters. Never null, but possibly
		 *   an empty set.
		 */
		public @NonNull KvPairs getParameters() {
			if (parameters == null) {
				final @NonNull KvPairs params = new KvPairs(false);
				parameters = params;
				return params;
			} else {
				return Objects.requireNonNull(parameters);
			}
		}

		/** Sets the HTTP connector, which is being used to open
		 * the actual HTTP connection. This is mainly useful, if
		 * you need to configure the connectors handling of SSL
		 * certificates. By default, an unconfigured default
		 * instance will be used, which matches the JVM's
		 * default SSL handling.
		 * @param pConnector The connector object, which will
		 *   be used.
		 * @return This request object, for builder-like programming.
		 * @see #getConnector()
		 */
		public @NonNull Request connector(HttpConnector pConnector) {
			connector = pConnector;
			return this;
		}

		/** Returns the HTTP connector, which is being used to open
		 * the actual HTTP connection. This is mainly useful, if
		 * you need to configure the connectors handling of SSL
		 * certificates. By default, an unconfigured default
		 * instance will be used, which matches the JVM's
		 * default SSL handling.
		 * @return The connector object, which will
		 * @see #connector(HttpConnector)
		 */
		public @NonNull HttpConnector getConnector() {
			if (connector == null) {
				final @NonNull HttpConnector hc = new HttpConnector();
				connector = hc;
				return hc;
			} else {
				return Objects.requireNonNull(connector);
			}
		}

		/** Sets the HTTP method, which is being used.
		 * This is a required method. However, you may use
		 * {@link #get()}, {@link #post()}, {@link #put()}, or
		 * {@link #delete()} instead.
		 * @param pMethod The HTTP method to use,
		 * @return This request object, for builder-like programming.
		 * @see #get()
		 * @see #post()
		 * @see #put()
		 * @see #delete()
		 * @see #getMethod()
		 */
		public @NonNull Request method(@NonNull String pMethod) {
			method = Objects.requireNonNull(pMethod, "Method");
			return this;
		}

		/** Sets the HTTP method to "GET". In other words: This is
		 * equivalent to invoking <pre>method("GET")</pre>.
		 * @return This request object, for builder-like programming.
		 * @see #method(String)
		 * @see #post()
		 * @see #put()
		 * @see #delete()
		 * @see #getMethod()
		 */
		public @NonNull Request get() { return method("GET"); }

		/** Sets the HTTP method to "POST". In other words: This is
		 * equivalent to invoking <pre>method("POST")</pre>.
		 * @return This request object, for builder-like programming.
		 * @see #method(String)
		 * @see #get()
		 * @see #put()
		 * @see #delete()
		 * @see #getMethod()
		 */
		public @NonNull Request post() { return method("POST"); }

		/** Sets the HTTP method to "PUT". In other words: This is
		 * equivalent to invoking <pre>method("PUT")</pre>.
		 * @return This request object, for builder-like programming.
		 * @see #method(String)
		 * @see #get()
		 * @see #post()
		 * @see #delete()
		 * @see #getMethod()
		 */
		public @NonNull Request put() { return method("PUT"); }

		/** Sets the HTTP method to "DELETE". In other words: This is
		 * equivalent to invoking <pre>method("DELETE")</pre>.
		 * @return This request object, for builder-like programming.
		 * @see #method(String)
		 * @see #get()
		 * @see #post()
		 * @see #put()
		 * @see #getMethod()
		 */
		public @NonNull Request delete() { return method("DELETE"); }

		/** Returns the HTTP method, which is being used.
		 * @return The HTTP method, which is being used.
		 * @see #method(String)
		 * @see #get()
		 * @see #post()
		 * @see #put()
		 * @see #delete()
		 */
		public String getMethod() { return method; }

		/** Sets the content type by adding a "content-type" header. In
		 * other words: This is equivalent to
		 * <pre>header("content-type", pContentType)</pre>
		 * @param pContentType The value of the "content-type" header.
		 * @return This request object, for builder-like programming.
		 */
		public @NonNull Request contentType(@NonNull String pContentType) {
			return header("content-type", pContentType);
		}

		/** Sets the character set, which is being used for conversion
		 * of strings to byte arrays, and vice versa.
		 * @param pCharset The character set. May be null, in which case
		 *   the {@link StandardCharsets#UTF_8} will be used.
		 * @return This request object, for builder-like programming.
		 * @see #charset(String)
		 * @see #getCharset()
		 */
		public @NonNull Request charset(@NonNull Charset pCharset) {
			charset = pCharset;
			return this;
		}

		/** Sets the character set, which is being used for conversion
		 * of strings to byte arrays, and vice versa.
		 * @param pCharset Name of the character set. May be null, in which case
		 *   the {@link StandardCharsets#UTF_8} will be used.
		 * @return This request object, for builder-like programming.
		 * @see #charset(Charset)
		 * @see #getCharset()
		 * @see Charset#forName(String)
		 */
		public @NonNull Request charset(@NonNull String pCharset) {
			final Charset cs = Charset.forName(pCharset);
			if (cs == null) {
				throw new IllegalArgumentException("Invalid character set: " + pCharset);
			}
			return charset(cs);
		}

		/** Returns the character set, which is being used for conversion
		 * of strings to byte arrays, and vice versa.
		 * @return The character set. Never null, by default
		 *   {@link StandardCharsets#UTF_8} will be used.
		 * @see #charset(Charset)
		 * @see #charset(String)
		 */
		public @NonNull Charset getCharset() {
			return Objects.notNull(charset, Streams.UTF_8);
		}

		/** Sets the value of the "Authorization" header to basic authentication
		 * with the given user name, and password. 
		 * <em>Note:</em> Setting this value implies the use of preemptive
		 * authentication (The user name, and password will be sent in any
		 * case.) Reactive authentication (First attempt without user name,
		 * and password, if necessary a second attempt without.) is not
		 * supported.
		 * @param pUserName The user name, for basic authentication.
		 * @param pPassword password, for basic authentication.
		 * @return This request object, for builder-like programming.
		 */
		public @NonNull Request basicAuth(@NonNull String pUserName, @NonNull String pPassword) {
			final String authStr = pUserName + ":" + pPassword;
			final byte[] authBytes = authStr.getBytes(getCharset());
			final String basicAuthStr = Base64.getMimeEncoder(0, Streams.LINE_SEPARATOR_BYTES).encodeToString(authBytes);
			return header("authorization", "Basic " + basicAuthStr);
		}

		/** Sets the value of the HTTP request body, which will be
		 * created by invoking
		 * the producer {@code pWriter}.
		 * @param pWriter The HTTP body's producer.
		 * @return This request object, for builder-like programming.
		 */
		public @NonNull Request body(FailableConsumer<OutputStream,?> pWriter) {
			body = pWriter;
			return this;
		}

		/** Sets the value of the HTTP request body to the given byte array.
		 * @param pBytes The byte array, which is being sent as the HTTP
		 * request body.
		 * @return This request object, for builder-like programming.
		 */
		public @NonNull Request body(byte[] pBytes) {
			final  byte @NonNull[] bytes = Objects.requireNonNull(pBytes, "Bytes");
			final FailableConsumer<OutputStream,?> writer = (os) -> os.write(bytes);
			return body(writer);
		}

		/** Sets the value of the HTTP request body to the given byte array.
		 * The configured {@link #getCharset()} will be used to convert
		 * the string into a byte array.
		 * @param pText The text string, which is being sent as the HTTP
		 * request body.
		 * @return This request object, for builder-like programming.
		 */
		public @NonNull Request body(String pText) {
			return body(new StringReader(pText));
		}

		/** Sets the value of the HTTP request body to the contents
		 * of the given {@link InputStream}.
		 * If possible, the streams contents are read on-the-fly,
		 * without conversion into an internal byte array.) In other
		 * words: This is preferrable over {@link #body(byte[])}.
		 * @param pIs The {@link InputStream}, which will be consumed.
		 * @return This request object, for builder-like programming.
		 */
		public @NonNull Request body(InputStream pIs) {
			final @NonNull InputStream is = Objects.requireNonNull(pIs, "InputStream");
			final FailableConsumer<OutputStream,?> writer = (os) -> Streams.copy(is, os);
			return body(writer);
		}

		/** Sets the value of the HTTP request body to the contents
		 * of the given {@link Reader}.
		 * The configured {@link #getCharset()} will be used to convert
		 * characters into bytes.
		 * If possible, the streams contents are read on-the-fly,
		 * without conversion into an internal byte array.) In other
		 * words: This is preferrable over {@link #body(String)}.
		 * @param pReader The {@link Reader}, which will be consumed.
		 * @return This request object, for builder-like programming.
		 */
		public @NonNull Request body(Reader pReader) {
			final @NonNull Reader r = Objects.requireNonNull(pReader, "Reader");
			final FailableConsumer<OutputStream,?> writer = (os) -> {
				final OutputStreamWriter osw = new OutputStreamWriter(os, getCharset());
				Streams.copy(r, osw);
			};
			return body(writer);
		}


		/** Sends the request, creates a {@link Response} object, and invokes the
		 * given {@code pCallable} for processing the response, and
		 * producing a result object.
		 * @param <O> Type of the result object.
		 * @param pCallable The function, which converts the response into the
		 *   result object.
		 * @return The created result object. (Obtained by invoking {@code pCallable}.
		 * @see #run(FailableConsumer)
		 */
		public <O> O call(FailableFunction<Response,O,?> pCallable) {
			if (url == null) {
				throw new IllegalStateException("The URL is null. Did you invoke url(URL), or url(String)?");
			}
			if (method == null) {
				throw new IllegalStateException("The HTTP method is null. Did you invoke get(), post(), put(), delete(), or method(String)?");
			}
			final URL u = getUrl();
			logger.logLn(method + " " + u.toExternalForm());
			try (HttpConnection httpConn = getConnector().connect(u)) {
				final HttpURLConnection urlConn = httpConn.getUrlConnection();
				urlConn.setRequestMethod(getMethod());
				getHeaders().forEach((k,l) -> {
					l.forEach((v) -> {
						logger.logLn(k + ": " + v);
						urlConn.addRequestProperty(k,  v);
					});
				});
				urlConn.setDoInput(true);
				if (body != null) {
					urlConn.setDoOutput(true);
					if (logger.isLogging()) {
						final ByteArrayOutputStream baos = new ByteArrayOutputStream();
						body.accept(baos);
						@SuppressWarnings("null")
						final byte @NonNull[] bytes = baos.toByteArray();
						logger.logLn(bytes);
						try (OutputStream os = urlConn.getOutputStream()) {
							os.write(bytes);
						}
					} else {
						try (OutputStream os = urlConn.getOutputStream()) {
							body.accept(os);
						}
					}
				}
				final Response response = Response.of(this, urlConn);
				return pCallable.apply(response);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		}

		/** Sends the request, creates a {@link Response} object, and invokes the
		 * given {@code pRunnable} for processing the response. No result object
		 * is produces.
		 * @param pRunnable The consumer, which processes the response.
		 * @see #call(FailableFunction)
		 */
		public void run(@NonNull FailableConsumer<Response,?> pRunnable) {
			final @NonNull FailableConsumer<Response,?> consumer = Objects.requireNonNull(pRunnable, "Consumer");
			final FailableFunction<Response,Void,?> callable = (res) -> {
				consumer.accept(res);
				return null;
			};
			call(callable);
		}

	}

	/** Creates a new request object, which needs configuration by invoking
	 * it's builder-like setters. The configured request object can then be
	 * used to send the request by invoking {@link Request#call(FailableFunction)},
	 * or {@link Request#run(FailableConsumer)}.
	 * @return The created request object.
	 */
	public static Request request() {
		return new Request();
	}

	/** This object provides access to the details of the HTTP servers response.
	 */
	public static class Response {
		private final int statusCode;
		private final String statusMsg;
		private FailableSupplier<InputStream,?> inputStreamSupplier;
		private byte[] inputBytes;
		private FailableSupplier<InputStream,?> errorStreamSupplier;
		private byte[] errorBytes;
		private final TrafficLogger logger;
		private @NonNull Charset charset;
		private final Supplier<KvPairs> headerSupplier;
		private KvPairs headers;

		/** Creates a new instance.
		 * @param pStatusCode The servers HTTP status code.
		 * @param pStatusMsg The servers HTTP response message.
		 * @param pInputStreamSupplier A supplier for the servers response body.
		 * @param pErrorStreamSupplier A supplier for the servers error response body.
		 * @param pLogger The traffic logger, which is being used.
		 * @param pCharset The character set, which is being used for conversion of
		 *   characters into strings, and vice versa.
		 * @param pHeaderSupplier A supplier for the set of HTTP response headers.
		 */
		public Response(int pStatusCode, String pStatusMsg, FailableSupplier<InputStream,?> pInputStreamSupplier,
				        FailableSupplier<InputStream,?> pErrorStreamSupplier, TrafficLogger pLogger,
				        @NonNull Charset pCharset, Supplier<KvPairs> pHeaderSupplier) {
			statusCode = pStatusCode;
			statusMsg = pStatusMsg;
			inputStreamSupplier = pInputStreamSupplier;
			inputBytes = null;
			errorStreamSupplier = pErrorStreamSupplier;
			errorBytes = null;
			logger = pLogger;
			charset = pCharset;
			headerSupplier = pHeaderSupplier;
			headers = null;
		}

		/** Returns the character set, which is being used to process the response object.
		 * By default, the character set will be inherited from the
		 * {@link Request#getCharset() request object}.
		 * @return The character set, which is being used to process the response
		 * object. Never null, the default value is inherited from the request
		 * object.
		 * @see #charSet(String)
		 * @see #charSet(Charset)
		 * @see Request#getCharset()
		 */
		public @NonNull Charset getCharSet() {
			return charset;
		}

		/** Sets the character set, which is being used to process the response object.
		 * By default, the character set will be inherited from the
		 * {@link Request#getCharset() request object}.
		 * @param pCharset The character set, which is being used to process the response
		 * object. Must not be null.
		 * @return This response object, for builder-like programming.
		 * @see #charSet(String)
		 */
		public @NonNull Response charSet(@NonNull Charset pCharset) {
			charset = Objects.requireNonNull(pCharset, "Charset");
			return this;
		}

		/** Sets the character set, which is being used to process the response object.
		 * By default, the character set will be inherited from the
		 * {@link Request#getCharset() request object}.
		 * @param pCharset The character set, which is being used to process the response
		 * object. Must not be null.
		 * @return This response object, for builder-like programming.
		 * @see #charSet(String)
		 */
		public @NonNull Response charSet(@NonNull String pCharset) {
			final @NonNull String charSetName = Objects.requireNonNull(pCharset, "Charset");
			final @NonNull Charset charSet;
			try {
				charSet = Objects.requireNonNull(Charset.forName(charSetName), "Charset (after lookup)");
			} catch (IllegalArgumentException iae) {
				throw new IllegalArgumentException("Invalid character set name: " + charSetName);
			}
			return charSet(charSet);
		}

		/** Creates a new instance by reading the necessary details from the HTTP request
		 * object, and the open HTTP connection.
		 * @param pRequest The request object, which provides the
		 * {@link Request#getCharset() character set}, and the {@link Request#getLogger() traffic logger}.
		 * @param pUrlConnection The open HTTP connection.
		 * @return The created response object.
		 */
		public static Response of(Request pRequest, HttpURLConnection pUrlConnection) {
			final Supplier<KvPairs> headerSupplier = () -> {
				final KvPairs kvPairs = new KvPairs(false);
				final Map<String,List<String>> headers = pUrlConnection.getHeaderFields();
				headers.forEach((k,l) -> {
					final @NonNull String key = Objects.requireNonNull(k, "Key");
					if (l != null) {
						l.forEach((v) -> kvPairs.add(key, v));
					}
				});
				return kvPairs;
			};
			try {
				return new Response(pUrlConnection.getResponseCode(), pUrlConnection.getResponseMessage(),
						            () -> pUrlConnection.getInputStream(), () -> pUrlConnection.getErrorStream(),
						            pRequest.getLogger(), pRequest.getCharset(), headerSupplier);
			} catch (IOException e) {
				throw Exceptions.show(e);
			}
		}

		/** Called to read, and process the response body. This method may only be invoked, if the
		 * servers response indicates, that the request was processed successfully. In other words,
		 * you should only use this method, if {@link #isOkay()} returns true.
		 * @param pConsumer An object, which reads the HTTP response body by consuming the
		 *   {@link InputStream}, that it receives as the parameter of {@link FailableConsumer#accept(Object)}.
		 * @param pRepeatable True, if the caller intends to call this method again. This means,
		 *   that the response object will read the HTTP response body into an internal byte
		 *   array. Otherwise, of possible, the response body will be read on-th-fly, without
		 *   internal storage.
		 * @see #errorInput(FailableConsumer, boolean)
		 */
		public void input(@NonNull FailableConsumer<InputStream,?> pConsumer, boolean pRepeatable) {
			inputBytes = provide(pConsumer, inputBytes, inputStreamSupplier, (bytes) -> logger.logLn(bytes), pRepeatable);
			inputStreamSupplier = null;
		}
	
		/** Called to read, and process the error response body. This method may only be invoked, if the
		 * servers response indicates, that processing the request was causing an error. In other words,
		 * you should only use this method, if {@link #isOkay()} returns false.
		 * @param pConsumer An object, which reads the HTTP response body by consuming the
		 *   {@link InputStream}, that it receives as the parameter of {@link FailableConsumer#accept(Object)}.
		 * @param pRepeatable True, if the caller intends to call this method again. This means,
		 *   that the response object will read the HTTP response body into an internal byte
		 *   array. Otherwise, of possible, the response body will be read on-th-fly, without
		 *   internal storage.
		 * @see #input(FailableConsumer, boolean)
		 */
		public void errorInput(@NonNull FailableConsumer<InputStream,?> pConsumer, boolean pRepeatable) {
			errorBytes = provide(pConsumer, errorBytes, errorStreamSupplier, (bytes) -> logger.logErrLn(bytes), pRepeatable);
			errorStreamSupplier = null;
		}

		/** Returns the set of response headers.
		 * @return The set of response headers. Never null, but the set may be empty.
		 */
		public @NonNull KvPairs getHeaders() {
			if (headers == null) {
				headers = headerSupplier.get();
				if (headers == null) {
					throw new IllegalStateException("The header supplier returned a null object.");
				}
			}
			@SuppressWarnings("null")
			final @NonNull KvPairs hdrs = headers;
			return hdrs;
		}

		/** Internally called by {@link #input(FailableConsumer, boolean)}, and
		 * {@link #errorInput(FailableConsumer, boolean)}. Handles the
		 * {@code pRepeatable} parameter, and the traffic logging.
		 * @param pConsumer The response stream consumer, which was passed by the
		 *   caller.
		 * @param pBytes The response stream, as a byte array, if the stream
		 *   has been read before.
		 * @param pInputSupplier The response stream supplier, if the byte
		 *   array {@code pBytes} is null.
		 * @param pLogger The traffic logger, which is being used to log the
		 *   response stream.
		 * @param pRepeatable True, if the caller intends to read the response
		 *   stream again. If so, the stream must be preserved by reading it
		 *   into an internal byte array.
		 * @return The response stream, as a byte array. (If {@code pRepeatable}
		 *   is true, or the traffic logger requested the response stream.
		 *   Otherwise, returns null.
		 */
		protected byte[] provide(@NonNull FailableConsumer<InputStream,?> pConsumer, byte @Nullable[] pBytes,
                                 @Nullable FailableSupplier<InputStream,?> pInputSupplier,
								 Consumer<byte @NonNull[]> pLogger, boolean pRepeatable) {
			final byte[] bytes;
			if (pBytes == null) {
				if (pInputSupplier == null) {
					throw new IllegalStateException("The input has already been consumed, with repeatable=false.");
				}
				if (logger.isLogging()  ||  pRepeatable) {
					try {
						InputStream is = inputStreamSupplier.get();
						if (is == null) {
							bytes = new byte[0];
						} else {
							bytes = Streams.read(inputStreamSupplier.get());
							is.close();
						}
					} catch (Throwable t) {
						throw Exceptions.show(t);
					}
					if (logger.isLogging()) {
						@SuppressWarnings("null")
						final byte @NonNull[] bt = (byte @NonNull[]) bytes;
						pLogger.accept(bt);
					}
				} else {
					try {
						pConsumer.accept(inputStreamSupplier.get());
						inputStreamSupplier = null;
						return null;
					} catch (Throwable t) {
						throw Exceptions.show(t);
					}
				}
			} else {
				bytes = pBytes;
			}
			try {
				pConsumer.accept(new ByteArrayInputStream(bytes));
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
			return bytes;
		}

		/** Returns true, if the HTTP status code indicates success. In other
		 * words: Returns true, if the {@link #getStatusCode() HTTP status code}
		 * is &gt;= 200, and &lt; 300.
		 * @return True, if the HTTP status code indicates success.
		 */
		public boolean isOkay() { return statusCode >= 200  &&  statusCode < 300; }
		/** Returns the HTTP status code.
		 * @return The HTTP status code.
		 */
		public int getStatusCode() { return statusCode; }
		/** Returns the HTTP status message.
		 * @return The HTTP status message.
		 */
		public String getStatusMsg() { return statusMsg; }
	}
}
