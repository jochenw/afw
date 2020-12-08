package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.ssl.SSLContextBuilder;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.TBSCertificate;
import org.bouncycastle.asn1.x509.TBSCertificateStructure;
import org.bouncycastle.asn1.x509.Time;
import org.bouncycastle.asn1.x509.V3TBSCertificateGenerator;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.x509.extension.X509ExtensionUtil;
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
		final HttpRequestHandler handler = newHttpRequestHandler(stringToSend, error);
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

	private HttpRequestHandler newHttpRequestHandler(final String stringToSend, final Holder<Throwable> error) {
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
		return handler;
	}

	@Test
	public void testHttpsRequest() throws Exception {
		final String stringToSend = "abcdefghijklmnopqrstuvwxyz\n0123456789\nABCDEFGHIJKLMNOPQRSTUVWXYZ\n";
		final InetAddress localHostAddress = InetAddress.getLoopbackAddress();
		final Holder<Throwable> error = new Holder<Throwable>();
		final TBSCertificate certificate = createCertificate(localHostAddress.getHostName());
		final SSLContext sslc = SSLContext.getInstance("TLS");
		sslc.init(null, null, null);
	}

	protected TBSCertificate createCertificate(String pCn) {
		try {
			final KeyPair keyPair = createKeyPair();
			final Calendar startTime = Calendar.getInstance();
			final Calendar expiry = (Calendar) startTime.clone();
			expiry.add(Calendar.DAY_OF_YEAR, 5);
			final X500Name name = new X500Name("CN=" + pCn); 
			final V3TBSCertificateGenerator certGen = new V3TBSCertificateGenerator();
			certGen.setSerialNumber(new ASN1Integer(System.currentTimeMillis()));
			certGen.setIssuer(name);
			certGen.setSubject(name);
			final Class<?> x509UtilClass = Class.forName("org.bouncycastle.x509.X509Util");
			final Method getAlgorithOIDMethod = x509UtilClass.getDeclaredMethod("getAlgorithmOID", String.class);
			getAlgorithOIDMethod.setAccessible(true);
			ASN1ObjectIdentifier sha1WithRsaEncryption = (ASN1ObjectIdentifier) getAlgorithOIDMethod.invoke(null, "SHA1WithRSAEncryption");
			AlgorithmIdentifier sigAlgId = new AlgorithmIdentifier(sha1WithRsaEncryption, DERNull.INSTANCE);
			certGen.setSignature(sigAlgId);
			final SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(new ASN1InputStream(new ByteArrayInputStream(keyPair.getPublic().getEncoded())).readObject());
			certGen.setSubjectPublicKeyInfo(publicKeyInfo);
			certGen.setStartDate(new Time(startTime.getTime()));
			certGen.setEndDate(new Time(expiry.getTime()));
			return certGen.generateTBSCertificate();
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	protected KeyPair createKeyPair() {
		try {
			final KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(1024, new SecureRandom());
			return kpg.generateKeyPair();
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}
}
