package com.github.jochenw.afw.core.util;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.apache.hc.core5.http.io.HttpRequestHandler;
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
						}
						
					})
					.create();
				server.start();
				final int portNumber = server.getLocalPort();
			final Http ra = new Http();
			ra.setHttpConnector(new HttpConnector());
			final String url = "http://127.0.0.1:" + portNumber;
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
