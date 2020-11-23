package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.junit.Test;

import com.github.jochenw.afw.core.util.HttpConnector.HttpConnection;


/** Test case for the {@link HttpConnector}.
 */
public class HttpConnectorTest {
	@Test
	public void testHttpRequest() throws Exception {
		final String stringToSend = "abcdefghijklmnopqrstuvwxyz\n0123456789\nABCDEFGHIJKLMNOPQRSTUVWXYZ\n";
		final InetAddress localHostAddress = InetAddress.getLoopbackAddress();
		final Holder<Throwable> error = new Holder<Throwable>();
		final HttpRequestHandler handler = new HttpRequestHandler() {
			@Override
			public void handle(HttpRequest pReq, HttpResponse pRes, HttpContext pCtx) throws HttpException, IOException {
				try {
					assertTrue(pReq instanceof HttpEntityEnclosingRequest);
					final HttpEntityEnclosingRequest req = (HttpEntityEnclosingRequest) pReq;
					assertEquals("POST", pReq.getRequestLine().getMethod());
					assertEquals("/test/resource", pReq.getRequestLine().getUri());
					final String contentType = pReq.getFirstHeader("content-type").getValue();
					final String testPropertyFoo = pReq.getFirstHeader("x-test-property-foo").getValue();
					assertEquals("text/plain; charset=UTF-8", contentType);
					assertEquals("bar", testPropertyFoo);
					assertNull(pReq.getFirstHeader("NoSuchHeader"));
					final HttpEntity entity = req.getEntity();
					final ByteArrayOutputStream baos = new ByteArrayOutputStream();
					try (InputStream in = entity.getContent()) {
						Streams.copy(in, baos);
					}
					final byte[] bytes = baos.toByteArray();
					final String s = new String(bytes, StandardCharsets.UTF_8);
					assertEquals(stringToSend, s);
					pRes.setStatusCode(200);
					pRes.setReasonPhrase("Okay, message received.");
				} catch (Throwable t) {
					error.set(t);
					throw Exceptions.show(t);
				}
			}
		};
		HttpServer server = null;
		try {
			server = ServerBootstrap
					.bootstrap()
					.setLocalAddress(localHostAddress)
					.registerHandler("*", handler)
					.create();
			server.start();
			final int portNumber = server.getLocalPort();
			final String uri = "http://" + localHostAddress.getHostAddress() + ":" + portNumber + "/test/resource";
			final URL url = new URL(uri);
			try (HttpConnection conn = new HttpConnector().connect(url)) {
				final HttpURLConnection urlConn = conn.getUrlConnection();
				urlConn.setRequestMethod("POST");
				urlConn.setDoOutput(true);
				urlConn.setDoInput(true);
				urlConn.setRequestProperty("content-type", "text/plain; charset=UTF-8");
				urlConn.setRequestProperty("x-test-property-foo", "bar");
				try (OutputStream out = urlConn.getOutputStream();
						Writer w = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
					w.write(stringToSend);
				}
				int statusCode = -1;
				String statusMessage = null;
				try {
					statusCode = urlConn.getResponseCode();
					statusMessage = urlConn.getResponseMessage();
					try (InputStream in = urlConn.getInputStream()) {
					}
				} catch (Throwable t) {
					if (error.get() == null) {
						error.set(t);
					}
				}
				if (error.get() != null) {
					throw Exceptions.show(error.get());
				}
				assertEquals(200, statusCode);
				assertEquals("Okay, message received.", statusMessage);
			}
			server.shutdown(1000, TimeUnit.MILLISECONDS);
			server = null;
		} finally {
			if (server != null) {
				server.shutdown(1000, TimeUnit.MILLISECONDS);
			}
		}
		
	}
}
