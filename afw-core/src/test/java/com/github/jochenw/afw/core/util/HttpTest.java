package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.jspecify.annotations.NonNull;
import org.junit.Test;

import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.json.JsonUtils;
import com.github.jochenw.afw.core.util.Http.KvPairs;
import com.github.jochenw.afw.core.util.Http.Request;

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
						if (hdrs != null) {
							for (Header hdr : hdrs) {
								final @NonNull String key = Objects.requireNonNull(hdr.getName(), "Name");
								final @NonNull String value = Objects.requireNonNull(hdr.getValue(), "Value");
								headerMap.computeIfAbsent(key.toLowerCase(), (k) -> new ArrayList<>()).add(value);
							}
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
						final byte[] jsonBytes = JsonUtils.writer().toBytes(requestMap);
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
				final JsonReader jr = Json.createReader(is);
				final JsonObject jo = jr.readObject();
				final Map<String,Object> map = JsonUtils.toMap(jo);
				if (pValidator != null) {
					pValidator.accept(map);
					valid.set();
				}
			}, false);
			checked.set();
		});
		httpServer.close();
		assertTrue(checked.isSet());
		assertTrue(valid.isSet());
	}

}
