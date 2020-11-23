package com.github.jochenw.afw.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class HttpConnector {
	public static final X509TrustManager TRUST_ALL_MANAGER = new X509TrustManager() {
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
		
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}
		
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}
	};
	public interface HttpConnection extends AutoCloseable {
		public HttpURLConnection getUrlConnection();
		@Override
		public void close();
	}

	private boolean trustingAllCertificates;
	private Path trustStore;
	private String trustStorePassword;
	private InetSocketAddress proxy;

	/**
	 * Returns, whether all SSL certificates are trusted. If that is the case,
	 * SSL certificate checking is effectively disabled.
	 * @return True, if all SSL certificates are trusted. The default is false.
	 */
	public boolean isTrustingAllCertificates() {
		return trustingAllCertificates;
	}
	/**
	 * Sets, whether all SSL certificates are trusted. If that is the case,
	 * SSL certificate checking is effectively disabled.
	 * @return True, if all SSL certificates are trusted. The default is false.
	 */
	public void setTrustingAllCertificates(boolean pTrustingAllCertificates) {
		trustingAllCertificates = pTrustingAllCertificates;
	}
	/**
	 * Returns the trust store to use for verification of SSL certificates.
	 * @return The trust store to use for verification of SSL certificates.
	 *   The default is null (Use the system's default trust store).
	 * @see #setTruststore(Path pPath)
	 */
	public Path getTruststore() {
		return trustStore;
	}
	/**
	 * Returns the password to use for reading the truststore.
	 * @return The password to use for reading the truststore.
	 *   Ignored, if the system's default truststore is used
	 *   (If the configured truststore is null.)
	 * @see #setTruststore(Path pPath)
	 */
	public String getTruststorePassword() {
		return trustStorePassword;
	}
	/**
	 * Sets the trust store to use for verification of SSL certificates.
	 * @param pTruststore The trust store to use for verification of SSL certificates,
	 *   or null, to use the system's default trust store.
	 * @param pTruststorePassword The password, which is being used to open the
	 *   trust store.
	 * @see #getTruststore()
	 * @see #getTruststorePassword()
	 */
	public void setTruststore(Path pTruststore, String pTruststorePassword) {
		trustStore = pTruststore;
	}

	public @Nullable String getProxyHost() {
		return proxy == null ? null : proxy.getHostString();
	}
	public @Nullable Integer getProxyPort() {
		return proxy == null ? null : Integer.valueOf(proxy.getPort());
	}

	/** Sets the proxy server to use.
	 * 
	 * @param pProxyDefinition A proxy definition in the form "host:port".
	 * @see #setProxy(String, int)
	 */
	public void setProxy(@Nonnull String pProxyDefinition) {
		final @Nonnull String proxy = Objects.requireNonNull(pProxyDefinition, "Proxy Definition");
		final int offset = proxy.indexOf(':');
		if (offset == -1) {
			throw new IllegalArgumentException("Invalid proxy definition: Expected host:port, got " + pProxyDefinition);
		}
		final int port;
		try {
			port = Integer.parseUnsignedInt(proxy.substring(offset+1));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid proxy definition: Expected host:portNumber , got " + pProxyDefinition);
		}
		setProxy(proxy.substring(0, offset), port);
	}

	/** Sets the proxy server to use.
	 * 
	 * @param pProxyHost The proxy servers host name, or ip address.
	 * @param pProxyPort The proxy servers port number.
	 * @see #setProxy(String)
	 */
	public void setProxy(@Nonnull String pProxyHost, int pProxyPort) {
		final String proxyHost = Objects.requireNonNull(pProxyHost, "Proxy Host");
		try {
			proxy = new InetSocketAddress(proxyHost, pProxyPort);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid port number: Expected 0 <= PortNumber < 65536, got " + pProxyPort);
		}
	}
	
	
	public HttpConnection connect(URL pUrl) throws IOException {
		if ("http".equals(pUrl.getProtocol())) {
			return asHttpConnection(pUrl);
		} else if ("https".equals(pUrl.getProtocol())) {
			return asHttpsConnection(pUrl);
		} else {
			throw new IllegalStateException("Invalid protocol (Expected http, or https, got "
					+ pUrl.getProtocol() + " for URL: " + pUrl);
		}
	}

	protected HttpConnection asHttpConnection(URL pUrl) throws IOException {
		final HttpURLConnection urlConn;
		if (proxy != null) {
			final Proxy prox = new Proxy(Proxy.Type.HTTP, proxy);
			urlConn = (HttpURLConnection) pUrl.openConnection(prox);
		} else {
			urlConn = (HttpURLConnection) pUrl.openConnection();
		}
		return new HttpConnection() {
			@Override
			public void close() {
				urlConn.disconnect();
			}
			
			@Override
			public HttpURLConnection getUrlConnection() {
				return urlConn;
			}
		};
	}

	protected HttpConnection asHttpsConnection(URL pUrl) throws IOException {
		final HttpsURLConnection urlConn;
		if (proxy != null) {
			final Proxy prox = new Proxy(Proxy.Type.HTTP, proxy);
			urlConn = (HttpsURLConnection) pUrl.openConnection(prox);
		} else {
			urlConn = (HttpsURLConnection) pUrl.openConnection();
		}
		urlConn.setSSLSocketFactory(getSSLSocketFactory());
		return new HttpConnection() {
			@Override
			public void close() {
				urlConn.disconnect();
			}
			
			@Override
			public HttpURLConnection getUrlConnection() {
				return urlConn;
			}
		};
	}

	protected SSLSocketFactory getSSLSocketFactory() {
		if (isTrustingAllCertificates()) {
			try {
				final SSLContext sslContext = SSLContext.getInstance("SSL");
				sslContext.init(null, new TrustManager[] { TRUST_ALL_MANAGER }, new SecureRandom());
				return  sslContext.getSocketFactory();
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		} else if (getTruststore() != null) {
			KeyStore trustStore;
			try (InputStream in = Files.newInputStream(getTruststore())) {
				trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
				final char[] pwd;
				if (getTruststorePassword() == null) {
					pwd = null;
				} else {
					pwd = getTruststorePassword().toCharArray();
				}
				trustStore.load(in, pwd);
				TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				tmf.init(trustStore);
				final SSLContext sslContext = SSLContext.getInstance("TLS");
				sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());
				return sslContext.getSocketFactory();
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		} else {
			return (SSLSocketFactory) SSLSocketFactory.getDefault();
		}
	}
}
