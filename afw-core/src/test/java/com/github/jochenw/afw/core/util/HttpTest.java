package com.github.jochenw.afw.core.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.entity.BasicHttpEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.junit.Test;

import com.github.jochenw.afw.core.data.Data;
import com.github.jochenw.afw.core.io.IReadable;

/** Test suite for the {@link Http} class.
 */
public class HttpTest {
	/** Test for an extremely simple case.
	 * @throws Exception The test fails.
	 */
	@Test
	public void testSimpleRequest() throws Exception {
		Consumer<Http.Request> requestPreparer = (rb) -> {};
		run(requestPreparer, null, (mp) -> {
			final Object[] headers = (Object[]) mp.get("headers");
			Object userAgent = null;
			for (Object header : headers) {
				@SuppressWarnings("unchecked")
				final Map<String,Object> headerMap = (Map<String,Object>) header;
				if ("User-Agent".equalsIgnoreCase((String) headerMap.get("key"))) {
					userAgent = headerMap.get("value");
				}
			}
			assertNotNull(userAgent);
		});
	}

	private void run(Consumer<Http.Request> pPreparer, Object pBody,
			         Consumer<Map<String,Object>> pValidator) {
		try {
			final MutableBoolean invoked = new MutableBoolean();
			final HttpServer server = ServerBootstrap
					.bootstrap()
					.setListenerPort(0)
					.setLocalAddress(InetAddress.getByName("0.0.0.0"))
					.register("/*", new HttpRequestHandler() {
						@Override
						public void handle(ClassicHttpRequest pReq, ClassicHttpResponse pRes,
								HttpContext pCtx) throws HttpException, IOException {
							invoked.set();
							final JsonBuilderFactory jbf = Json.createBuilderFactory(null);
							final JsonObjectBuilder job = jbf.createObjectBuilder();
							job.add("method", pReq.getMethod());
							final URI uri;
							try {
								uri = pReq.getUri();
							} catch (URISyntaxException e) {
								throw new UndeclaredThrowableException(e);
							}
							job.add("host", uri.getHost());
							job.add("port", uri.getPort());
							final String query = uri.getQuery();
							if (query != null) {
								job.add("query", query);
							}
							final String fragment = uri.getFragment();
							if (fragment != null) {
								job.add("fragment", fragment);
							}
							job.add("scheme", uri.getScheme());
							job.add("protocolVersion", pCtx.getProtocolVersion().format());
							job.add("path", pReq.getPath());
							final Header[] headers = pReq.getHeaders();
							if (headers != null  &&  headers.length != 0) {
								final JsonArrayBuilder hJab = jbf.createArrayBuilder();
								for (Header h : headers) {
									final JsonObjectBuilder hJob = jbf.createObjectBuilder();
									hJob.add("key", h.getName());
									hJob.add("value", h.getValue());
									hJab.add(hJob);
								}
								job.add("headers", hJab);
							}
							final HttpEntity entity = pReq.getEntity();
							if (entity != null) {
								try (InputStream is = entity.getContent()) {
									final byte[] contentBytes = Streams.read(is);
									final String contentBase64 =
											Base64.getMimeEncoder(64, new byte[] {(byte) 13})
											      .encodeToString(contentBytes);
									job.add("content", contentBase64);
								}
							}
							final ByteArrayOutputStream baos = new ByteArrayOutputStream();
							JsonWriterFactory jwf = Json.createWriterFactory(Data.asMap(JsonGenerator.PRETTY_PRINTING, "true"));
							try (JsonWriter jw = jwf.createWriter(baos)) {
								jw.writeObject(job.build());
							}
							final ContentType ct = ContentType.APPLICATION_JSON;
							final HttpEntity outputEntity = new BasicHttpEntity(new ByteArrayInputStream(baos.toByteArray()), ct);
							pRes.setEntity(outputEntity);
							pRes.close();
						}
					})
					.create();
				server.start();
				final int portNumber = server.getLocalPort();
			final Http ra = new Http();
			ra.setHttpConnector(new HttpConnector());
			final String url = "http://127.0.0.1:" + portNumber + "/";
			final Http.Request rb = ra.request(url);
			if (pBody != null) {
				final byte[] bytes;
				if (pBody instanceof String) {
					bytes = ((String) pBody).getBytes(StandardCharsets.UTF_8);
				} else if (pBody instanceof byte[]) {
					bytes = (byte[]) pBody;
				} else {
					throw new IllegalArgumentException("Invalid body type: " + pBody.getClass().getName());
				}
				rb.body(() -> new ByteArrayInputStream(bytes));
			}
			if (pPreparer != null) {
				pPreparer.accept(rb);
			}
			assertFalse(invoked.isSet());
			final Http.Response response = ra.send(rb);
			assertNotNull(response);
			assertTrue(invoked.isSet());
			if (pValidator != null) {
				final byte[] bytes = response.getResponseBody();
				assertNotNull(bytes);
				final JsonObject jsonObject = Json.createReader(new ByteArrayInputStream(bytes)).readObject();
				pValidator.accept(asMap(jsonObject));
			}
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/** Converts the given Json object into a {@link Map}.
	 * @param pJsonObject The Json object, which is being
	 * converted.
	 * @return The created map, with lower-cased keys.
	 */
	protected Map<String,Object> asMap(JsonObject pJsonObject) {
		final Map<String,Object> map = new HashMap<>();
		if (pJsonObject != null) {
			pJsonObject.forEach((k,v) -> {
				final Object val = asValue(v);
				System.out.println(k  + ": " + v + ", " + val);
				map.put(k.toLowerCase(), val);
			});
		}
		return map;
	}

	/** Converts the given Json object into an array of
	 * objects.
	 * @param pJsonArray The Json array, which is being
	 * converted.
	 * @return The created array.
	 */
	protected Object[] asArray(JsonArray pJsonArray) {
		final List<Object> list = new ArrayList<>();
		if (pJsonArray != null  &&  !pJsonArray.isEmpty()) {
			pJsonArray.forEach((jv) -> list.add(asValue(jv)));
		}
		return list.toArray();
	}

	/** Converts the given Json value into a native Json
	 * object. (JsonObject to {@link Map}, JsonArray to
	 * an object array, etc.)
	 * @param pJsonValue The Json value, which is being
	 * converted.
	 * @return The converted value.
	 */
	protected Object asValue(JsonValue pJsonValue) {
		if (pJsonValue == null) {
			return null;
		} else if (pJsonValue instanceof JsonObject) {
			return asMap((JsonObject) pJsonValue);
		} else if (pJsonValue instanceof JsonArray) {
			return asArray((JsonArray) pJsonValue);
		} else if (pJsonValue instanceof JsonString) {
			return ((JsonString) pJsonValue).getString();
		} else if (pJsonValue instanceof JsonNumber) {
			final JsonNumber jsonNumber = (JsonNumber) pJsonValue;
			return jsonNumber.numberValue();
		} else {
			throw new IllegalStateException("Invalid value type: " + pJsonValue.getClass().getName());
		}
	}
}
