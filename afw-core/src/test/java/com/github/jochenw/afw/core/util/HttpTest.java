package com.github.jochenw.afw.core.util;

import static org.junit.Assert.assertNotNull;

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
import java.util.Base64;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

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

import com.github.jochenw.afw.core.io.IReadable;

/** Test suite for the {@link Http} class.
 */
public class HttpTest {
	@Test
	public void testSimpleRequest() throws Exception {
		Consumer<Http.Request> requestPreparer = (rb) -> {};
		run(requestPreparer, null, null);
	}

	private void run(Consumer<Http.Request> pPreparer, Object pBody,
			         Consumer<Http.Response> pValidator) {
		try {
			final HttpServer server = ServerBootstrap
					.bootstrap()
					.setListenerPort(0)
					.setLocalAddress(InetAddress.getByName("0.0.0.0"))
					.register("/*", new HttpRequestHandler() {
						@Override
						public void handle(ClassicHttpRequest pReq, ClassicHttpResponse pRes,
								HttpContext pCtx) throws HttpException, IOException {
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
							try (JsonWriter jw = Json.createWriter(baos)) {
								jw.writeObject(job.build());
							}
							final ContentType ct = ContentType.APPLICATION_JSON;
							final HttpEntity outputEntity = new BasicHttpEntity(new ByteArrayInputStream(baos.toByteArray()), ct);
							pRes.setEntity(outputEntity);
							baos.writeTo(System.out);
							System.out.println();
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
			final Http.Response response = ra.send(rb);
			assertNotNull(response);
			if (pValidator != null) {
				pValidator.accept(response);
			}
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}
}
