package com.github.jochenw.afw.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
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
	public static interface TrafficLogger {
		public void log(String pMsg);
		public void log(byte[] pBytes);
		public void logErr(String pMsg);
		public void logErr(byte[] pBytes);
		public default void logLn(String pMsg) {
			log(pMsg);
			log(Streams.LINE_SEPARATOR);
		}
		public default void logErrLn(String pMsg) {
			logErr(pMsg);
			logErr(Streams.LINE_SEPARATOR);
		}
		public default void logLn(byte[] pBytes) {
			log(pBytes);
			log(Streams.LINE_SEPARATOR_BYTES);
		}
		public default void logErrLn(byte[] pBytes) {
			logErr(pBytes);
			logErr(Streams.LINE_SEPARATOR_BYTES);
		}
		public default void log(String pMsg, Object... pArgs) {
			log(Strings.formatCb(pMsg, pArgs));
		}
		public default void logErr(String pMsg, Object... pArgs) {
			logErr(Strings.formatCb(pMsg, pArgs));
		}
		public default void log(InputStream pIn) { log(Streams.read(pIn)); }
		public default void logErr(InputStream pIn) { logErr(Streams.read(pIn)); }
		public default void logLn(InputStream pIn) { logLn(Streams.read(pIn)); }
		public default void logErrLn(InputStream pIn) { logErrLn(Streams.read(pIn)); }
		public default boolean isLogging() { return true; }

		public static final @NonNull TrafficLogger NULL_LOGGER = new TrafficLogger() {
			@Override public void logErr(byte[] pBytes) { /* Do nothing */ }
			@Override public void logErr(String pMsg) { /* Do nothing */ }
			@Override public void log(byte[] pBytes) { /* Do nothing */ }
			@Override public void log(String pMsg) { /* Do nothing */ }
			@Override public void logLn(String pMsg) { /* Do nothing */ }
			@Override public void logErrLn(String pMsg) { /* Do nothing */ }
			@Override public void logLn(byte[] pBytes) { /* Do nothing */ }
			@Override public void logErrLn(byte[] pBytes) { /* Do nothing */ }
			@Override public void log(InputStream pIn) { /* Do nothing */ }
			@Override public void logErr(InputStream pIn) { /* Do nothing */ }
			@Override public void logLn(InputStream pIn) { /* Do nothing */ }
			@Override public void logErrLn(InputStream pIn) { /* Do nothing */ }
			@Override public boolean isLogging() { return false; }
		};

		public static final @NonNull TrafficLogger SYSTEM_OUT_LOGGER = new TrafficLogger() {
			@Override public void logErr(byte[] pBytes) { write(System.err, pBytes); }
			@Override public void logErr(String pMsg) { System.err.print(pMsg); }
			@Override public void log(byte[] pBytes) { write(System.out, pBytes); }
			@Override public void log(String pMsg) { System.out.print(pMsg); }
		};

		public static void write(@NonNull OutputStream pOut, byte @NonNull [] pBytes) {
			try {
				pOut.write(pBytes);
			} catch (IOException ioe) {
				throw Exceptions.show(ioe);
			}
		}

		public static @NonNull TrafficLogger of(OutputStream pOut) { return of(pOut, StandardCharsets.UTF_8); }
		public static @NonNull TrafficLogger of(OutputStream pOut, Charset pCharset) {
			final @NonNull OutputStream out = Objects.requireNonNull(pOut, "OutputStream");
			final @NonNull Charset cs = Objects.requireNonNull(pCharset, "Charset");
			return new TrafficLogger() {
				@Override public void log(String pMsg) { log(pMsg.getBytes(cs)); }
				@Override public void log(byte[] pBytes) { TrafficLogger.write(out, pBytes); }
				@Override public void logErr(String pMsg) { logErr(pMsg.getBytes(cs)); }
				@Override public void logErr(byte[] pBytes) { TrafficLogger.write(out, pBytes); }
			};
		}

		public static TrafficLogger of(ILog pLog) {
			final @NonNull ILog log = Objects.requireNonNull(pLog, "ILog");
			return new TrafficLogger() {
				@Override public void logErr(byte[] pBytes) { logErr(Strings.toHexString(pBytes)); }
				@Override public void logErr(String pMsg) { log.error("TrafficLogger.logErr", pMsg); }
				@Override public void log(byte[] pBytes) { log(Strings.toHexString(pBytes)); }
				@Override public void log(String pMsg) { log.trace("TrafficLogger.log", pMsg); }
				@Override public void logLn(byte[] pBytes) { log.trace("TrafficLogger.logLn", Strings.toHexString(pBytes)); }
				@Override public void logErrLn(byte[] pBytes) { log.error("TrafficLogger.logErrLn", Strings.toHexString(pBytes)); }
				@Override
				public void logLn(InputStream pIn) { log.trace("TrafficLogger.logLn", Strings.toHexString(Streams.read(pIn))); }
				@Override
				public void logErrLn(InputStream pIn) { log.error("TrafficLogger.logErrLn", Strings.toHexString(Streams.read(pIn))); }
			};
		}
	}
	public static class KvPairs {
		private final boolean caseSensitive;
		private @Nullable Map<@NonNull String,List<String>> map;
	
		public KvPairs(boolean pCaseSensitive) {
			caseSensitive = pCaseSensitive;
		}
		public KvPairs() {
			this(true);
		}
		protected @NonNull Map<@NonNull String,List<String>> getMap() {
			if (map == null) {
				final Map<@NonNull String,List<String>> mp = new HashMap<>();
				map = mp;
			}
			return Objects.requireNonNull(map);
		}
		public void add(@NonNull String pKey, String pValue) {
			final @NonNull String key = asKey(pKey);
			getMap().computeIfAbsent(key, (k) -> new ArrayList<String>()).add(pValue);
		}
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
		public List<String> get(@NonNull String pKey) {
			if (map == null) {
				return null;
			}
			final Map<@NonNull String,List<String>> mp = Objects.requireNonNull(map);
			return mp.get(pKey);
		}
		public void forEach(@NonNull String pKey, FailableConsumer<String,?> pConsumer) {
			final List<String> list = get(pKey);
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
		protected @NonNull String asKey(@NonNull String pKey) {
			if (caseSensitive) {
				return pKey;
			} else {
				return pKey.toLowerCase();
			}
		}
	}
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

		protected void urlEncode(StringBuilder pSb, String pValue) {
			pSb.append(URLEncoder.encode(pValue, getCharset()));
		}

		public Request url(@NonNull URL pUrl) {
			url = Objects.requireNonNull(pUrl, "URL");
			return this;
		}

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

		public Request logger(@NonNull TrafficLogger pLogger) {
			logger = Objects.requireNonNull(pLogger, "Logger");
			return this;
		}

		public Request systemOutLogger() {
			return logger(TrafficLogger.SYSTEM_OUT_LOGGER);
		}

		public @NonNull TrafficLogger getLogger() {
			return logger;
		}
		public Request header(@NonNull String pKey, @NonNull String pValue) {
			getHeaders().add(pKey, pValue);
			return this;
		}

		public Request parameter(@NonNull String pKey, @NonNull String pValue) {
			getParameters().add(pKey, pValue);
			return this;
		}

		public @NonNull KvPairs getHeaders() {
			if (headers == null) {
				final @NonNull KvPairs hdrs = new KvPairs();
				headers = hdrs;
				return hdrs;
			} else {
				return Objects.requireNonNull(headers);
			}
		}

		public @NonNull KvPairs getParameters() {
			if (parameters == null) {
				final @NonNull KvPairs params = new KvPairs(false);
				parameters = params;
				return params;
			} else {
				return Objects.requireNonNull(parameters);
			}
		}

		public @NonNull Request connector(HttpConnector pConnector) {
			connector = pConnector;
			return this;
		}

		public @NonNull HttpConnector getConnector() {
			if (connector == null) {
				return HttpConnector.DEFAULT_CONNECTOR;
			} else {
				return Objects.requireNonNull(connector);
			}
		}

		public @NonNull Request method(@NonNull String pMethod) {
			method = pMethod;
			return this;
		}

		public @NonNull Request get() { return method("GET"); }

		public @NonNull Request post() { return method("POST"); }

		public @NonNull Request put() { return method("PUT"); }

		public @NonNull Request delete() { return method("DELETE"); }

		public String getMethod() { return method; }

		public @NonNull Request contentType(@NonNull String pContentType) {
			return header("content-type", pContentType);
		}

		public @NonNull Request charset(@NonNull Charset pCharset) {
			charset = pCharset;
			return this;
		}

		public @NonNull Request charset(@NonNull String pCharset) {
			final Charset cs = Charset.forName(pCharset);
			if (cs == null) {
				throw new IllegalArgumentException("Invalid character set: " + pCharset);
			}
			return charset(cs);
		}

		public @NonNull Charset getCharset() {
			return Objects.notNull(charset, Streams.UTF_8);
		}

		public @NonNull Request basicAuth(@NonNull String pUserName, @NonNull String pPassword) {
			final String authStr = pUserName + ":" + pPassword;
			final byte[] authBytes = authStr.getBytes(getCharset());
			final String basicAuthStr = Base64.getMimeEncoder(0, Streams.LINE_SEPARATOR_BYTES).encodeToString(authBytes);
			return header("authorization", "Basic " + basicAuthStr);
		}

		public @NonNull Request body(FailableConsumer<OutputStream,?> pWriter) {
			body = pWriter;
			return this;
		}

		public @NonNull Request body(Consumer<OutputStream> pWriter) {
			final @NonNull Consumer<OutputStream> writer = Objects.requireNonNull(pWriter, "Writer");
			final FailableConsumer<OutputStream,?> failableWriter = (os) -> writer.accept(os);
			return body(failableWriter);
		}

		public @NonNull Request body(byte[] pBytes) {
			final  byte @NonNull[] bytes = Objects.requireNonNull(pBytes, "Bytes");
			final FailableConsumer<OutputStream,?> writer = (os) -> os.write(bytes);
			return body(writer);
		}

		public @NonNull Request body(String pText) {
			return body(new StringReader(pText));
		}

		public @NonNull Request body(InputStream pIs) {
			final @NonNull InputStream is = Objects.requireNonNull(pIs, "InputStream");
			final FailableConsumer<OutputStream,?> writer = (os) -> Streams.copy(is, os);
			return body(writer);
		}

		public @NonNull Request body(Reader pReader) {
			final @NonNull Reader r = Objects.requireNonNull(pReader, "Reader");
			final FailableConsumer<OutputStream,?> writer = (os) -> {
				final OutputStreamWriter osw = new OutputStreamWriter(os, getCharset());
				Streams.copy(r, osw);
			};
			return body(writer);
		}


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
						final byte[] bytes = baos.toByteArray();
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

		public void run(@NonNull FailableConsumer<Response,?> pRunnable) {
			final @NonNull FailableConsumer<Response,?> consumer = Objects.requireNonNull(pRunnable, "Consumer");
			final FailableFunction<Response,Void,?> callable = (res) -> {
				consumer.accept(res);
				return null;
			};
			call(callable);
		}

	}

	public static Request request() {
		return new Request();
	}
	
	public static class Response {
		private final int statusCode;
		private final String statusMsg;
		private FailableSupplier<InputStream,?> inputStreamSupplier;
		private byte[] inputBytes;
		private FailableSupplier<InputStream,?> errorStreamSupplier;
		private byte[] errorBytes;
		private final TrafficLogger logger;
		private final Charset charset;
		private final Supplier<KvPairs> headerSupplier;
		private KvPairs headers;

		public Response(int pStatusCode, String pStatusMsg, FailableSupplier<InputStream,?> pInputStreamSupplier,
				        FailableSupplier<InputStream,?> pErrorStreamSupplier, TrafficLogger pLogger,
				        Charset pCharset, Supplier<KvPairs> pHeaderSupplier) {
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

		public void input(@NonNull FailableConsumer<InputStream,?> pConsumer, boolean pRepeatable) {
			inputBytes = provide(pConsumer, inputBytes, inputStreamSupplier, (bytes) -> logger.logLn(bytes), pRepeatable);
			inputStreamSupplier = null;
		}
		public void errorInput(@NonNull FailableConsumer<InputStream,?> pConsumer, boolean pRepeatable) {
			errorBytes = provide(pConsumer, errorBytes, errorStreamSupplier, (bytes) -> logger.logErrLn(bytes), pRepeatable);
			errorStreamSupplier = null;
		}
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

		public boolean isOkay() { return statusCode >= 200  &&  statusCode < 300; }
		public int getStatusCode() { return statusCode; }
		public String getStatusMsg() { return statusMsg; }
	}
}
