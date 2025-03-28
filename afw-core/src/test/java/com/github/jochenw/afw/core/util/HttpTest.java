package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.CloseMode;
import org.jspecify.annotations.NonNull;
import org.junit.Test;

import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.json.JsonUtils;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.log.ILog.Level;
import com.github.jochenw.afw.core.log.simple.SimpleLogFactory;
import com.github.jochenw.afw.core.util.Http.Request;
import com.github.jochenw.afw.core.util.Http.TrafficLogger;

/** Test suite for the {@link Http} class.
 */
public class HttpTest {
	@Test
	public void testSimpleGet() {
		run(Http.request().get(), (map) -> {
			assertNotNull(map);
			assertEquals("GET", map.get("method"));
			@SuppressWarnings("unchecked")
			final Map<String,Object> headers = (Map<String,Object>) map.get("headers");
			assertNotNull(headers);
			assertNotNull(headers.get("user-agent"));
			assertNull(headers.get("content-type"));
			assertNull(headers.get("x-foo"));
		});
	}
	@Test
	public void testSimpleGetWithHeader() {
		run(Http.request().get().header("X-Foo", "Bar"), (map) -> {
			assertNotNull(map);
			assertEquals("GET", map.get("method"));
			@SuppressWarnings("unchecked")
			final Map<String,Object> headers = (Map<String,Object>) map.get("headers");
			assertNotNull(headers);
			assertNotNull(headers.get("user-agent"));
			assertNull(headers.get("content-type"));
			assertArrayEquals(new Object[] {"Bar"}, (Object[]) headers.get("x-foo"));
		});
	}
	@Test
	public void testSimpleGetWithTwoHeaders() {
		run(Http.request().get().header("X-Foo", "Bar").contentType("None"), (map) -> {
			assertNotNull(map);
			assertEquals("GET", map.get("method"));
			@SuppressWarnings("unchecked")
			final Map<String,Object> headers = (Map<String,Object>) map.get("headers");
			assertNotNull(headers);
			assertNotNull(headers.get("user-agent"));
			assertArrayEquals(new Object[] {"Bar"}, (Object[]) headers.get("x-foo"));
			assertArrayEquals(new Object[] {"None"}, (Object[]) headers.get("content-type"));
		});
	}

	private byte[] bytesOf(String pString) {
		return pString.getBytes(Streams.UTF_8);
	}
	private InputStream streamOf(String pString) {
		return new ByteArrayInputStream(bytesOf(pString));
	}
	@Test
	public void testTrafficLogger() {
		final ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
		final TrafficLogger tl1 = TrafficLogger.of(baos1);
		testTrafficLogger(tl1);
		testTrafficLogger(TrafficLogger.NULL_LOGGER);
		final String expect = "foo bar" + Streams.LINE_SEPARATOR
				+ "fErr bErr" + Streams.LINE_SEPARATOR
		        + "Doctor Who" + Streams.LINE_SEPARATOR
		        + "The Master" + Streams.LINE_SEPARATOR
		        + "Counting: One, 2, 3 " + Streams.LINE_SEPARATOR
		        + "Counting down: Zero, Go!" + Streams.LINE_SEPARATOR
		        + "Too smart to fail." + Streams.LINE_SEPARATOR
		        + "We all live in a yellow submarine." + Streams.LINE_SEPARATOR
		        + "Made my day." + Streams.LINE_SEPARATOR
		        + "Revolution" + Streams.LINE_SEPARATOR;
		assertEquals(expect, new String(baos1.toByteArray(), Streams.UTF_8));
	}

	@Test
	public void testSystemOutTrafficLogger() {
		final PrintStream oldSystemOut = System.out;
		final PrintStream oldSystemErr = System.err;
		try {
			final ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
			final ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
			System.setOut(new PrintStream(baosOut));
			System.setErr(new PrintStream(baosErr));
			testTrafficLogger(TrafficLogger.SYSTEM_OUT_LOGGER);
			final String expectOut = "foo bar" + Streams.LINE_SEPARATOR
			        + "Doctor Who" + Streams.LINE_SEPARATOR
			        + "Counting: One, 2, 3 " + Streams.LINE_SEPARATOR
			        + "Too smart to fail." + Streams.LINE_SEPARATOR
			        + "Made my day." + Streams.LINE_SEPARATOR;
			final String expectErr =
					"fErr bErr" + Streams.LINE_SEPARATOR
			        + "The Master" + Streams.LINE_SEPARATOR
			        + "Counting down: Zero, Go!" + Streams.LINE_SEPARATOR
			        + "We all live in a yellow submarine." + Streams.LINE_SEPARATOR
			        + "Revolution" + Streams.LINE_SEPARATOR;
			assertEquals(expectOut, new String(baosOut.toByteArray(), Streams.UTF_8));
			assertEquals(expectErr, new String(baosErr.toByteArray(), Streams.UTF_8));
		} finally {
			System.setOut(oldSystemOut);
			System.setErr(oldSystemErr);
		}
	}

	@Test
	public void testILogTrafficLogger() {
		final PrintStream oldSystemOut = System.out;
		final PrintStream oldSystemErr = System.err;
		try {
			final ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
			final ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
			System.setOut(new PrintStream(baosOut));
			System.setErr(new PrintStream(baosErr));
			final SimpleLogFactory slf = SimpleLogFactory.ofSystemOut(Level.TRACE);
			final ILog iLog = slf.getLog(HttpTest.class);
			final TrafficLogger tl = TrafficLogger.of(iLog);
			testTrafficLogger(tl);
		} finally {
			System.setOut(oldSystemOut);
			System.setErr(oldSystemErr);
		}
	}

	@Test
	public void testSimpleGetWithTrafficLogger() {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final TrafficLogger tLog = TrafficLogger.of(baos);
		run(Http.request().logger(tLog).get(), (map) -> {});
		final String loggedTraffic = new String(baos.toByteArray(), Streams.UTF_8);
		assertTrue(loggedTraffic.startsWith("GET http://localhost:"));
		final int jsonStart = loggedTraffic.indexOf('{');
		assertTrue(jsonStart >= 0);
		final int jsonEnd = loggedTraffic.lastIndexOf('}');
		assertTrue(jsonEnd > jsonStart);
		final String jsonString = loggedTraffic.substring(jsonStart, jsonEnd+1);
		final JsonReader jr = Json.createReader(new StringReader(jsonString));
		final JsonObject jo = jr.readObject();
		assertEquals("GET", jo.getJsonString("method").getString());
		final String uri = jo.getJsonString("uri").getString();
		assertTrue(uri.startsWith("http://localhost:"));
	}

	@Test
	public void testTrafficLoggerFailing() {
		final IOException ioe = new IOException("foo");
		final OutputStream failingOs = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				throw ioe;
			}
		};
		final TrafficLogger tl = TrafficLogger.of(failingOs);
		final @NonNull BiFunction<@NonNull UncheckedIOException,String,String> errorValidator = (t,msg) -> {
			assertSame(ioe, t.getCause());
			assertEquals(IOException.class.getName()+ ": foo", msg);
			return null;
		};
		Functions.assertFail(UncheckedIOException.class, errorValidator, () -> tl.log(" "));
	}
	private void testTrafficLogger(final TrafficLogger pTl) {
		pTl.log("foo ");
		pTl.logLn("bar");
		pTl.logErr("fErr ");
		pTl.logErrLn("bErr");
		pTl.log(bytesOf("Doctor "));
		pTl.logLn(bytesOf("Who"));
		pTl.logErr(bytesOf("The "));
		pTl.logErrLn(bytesOf("Master"));
		pTl.log("Counting: {}, {}, {} "+ Streams.LINE_SEPARATOR, "One", Integer.valueOf(2), Long.valueOf(3));
		pTl.logErr("Counting down: {}, {}" + Streams.LINE_SEPARATOR, "Zero", "Go!");
		pTl.log(streamOf("Too smart to fail."+ Streams.LINE_SEPARATOR));
		pTl.logErr(streamOf("We all live in a yellow submarine." + Streams.LINE_SEPARATOR));
		pTl.logLn(streamOf("Made my day."));
		pTl.logErrLn(streamOf("Revolution"));
	}
	public void run(Request pRequest, Consumer<Map<String,Object>> pValidator) {
		final MutableBoolean valid = new MutableBoolean();
		final HttpServer httpServer = ServerBootstrap.bootstrap()
				.setListenerPort(0)
				.setCanonicalHostName("localhost")
				.setLocalAddress(InetAddress.getLoopbackAddress())
				.register("*", new HttpRequestHandler() {
					@Override
					public void handle(ClassicHttpRequest pReq, ClassicHttpResponse pRes, HttpContext pCtx) throws HttpException, IOException {
						final Map<String,Object> requestMap = new HashMap<>();
						final Map<String,List<String>> headerMap = new HashMap<>();
						final Header[] hdrs = pReq.getHeaders();
						for (Header hdr : hdrs) {
							final @NonNull String key = Objects.requireNonNull(hdr.getName(), "Name");
							final @NonNull String value = Objects.requireNonNull(hdr.getValue(), "Value");
							headerMap.computeIfAbsent(key.toLowerCase(), (k) -> new ArrayList<>()).add(value);
						}
						requestMap.put("headers", headerMap);
						requestMap.put("method", pReq.getMethod());
						final URI uri;
						try {
							uri = pReq.getUri();
						} catch (URISyntaxException e) {
							throw Exceptions.show(e);
						}
						requestMap.put("uri", uri.toASCIIString());
						
						final HttpEntity entity = pReq.getEntity();
						if (entity != null) {
							final byte[] bytes = Streams.read(entity.getContent());
							requestMap.put("body", bytes);
						}
						final byte[] jsonBytes = JsonUtils.writer().usingPrettyPrint().toBytes(requestMap);
						pRes.setCode(200);
						pRes.setReasonPhrase("Ok");
						pRes.setEntity(new ByteArrayEntity(jsonBytes, ContentType.APPLICATION_JSON));
						valid.set();
						pRes.close();
					}
				})
				.create();
		try {
			httpServer.start();
		} catch (IOException ioe) {
			throw Exceptions.show(ioe);
		}
		final int portNumber = httpServer.getLocalPort();
		final String urlStr = "http://localhost:" + portNumber + "/test";
		final URL url;
		try {
			url = Strings.asUrl(urlStr);
		} catch (MalformedURLException e) {
			throw Exceptions.show(e);
		}
		MutableBoolean checked = MutableBoolean.of(false);
		pRequest.url(url).run((response) -> {
			assertNotNull(response);
			assertTrue(response.getStatusCode() + ": " + response.getStatusMsg(),  response.isOkay());
			response.input((FailableConsumer<InputStream, ?>) (is) -> {
				checked.set();
				if (is == null) {
					pValidator.accept(null);
				} else {
					final JsonReader jr = Json.createReader(is);
					final JsonObject jo = jr.readObject();
					final Map<String,Object> map = JsonUtils.toMap(jo);
					if (pValidator != null) {
						pValidator.accept(map);
						valid.set();
					}
				}
			}, false);
			checked.set();
		});
		httpServer.close(CloseMode.IMMEDIATE);
		assertTrue(checked.isSet());
		assertTrue(valid.isSet());
	}

}
