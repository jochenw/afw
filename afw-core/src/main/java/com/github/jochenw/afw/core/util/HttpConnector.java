package com.github.jochenw.afw.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
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

/**
 * A component, which provides HTTP, or HTTPS connections.
 */
public class HttpConnector {
	/** A {@link X509TrustManager}, which accepts all certificates.
	 */
	public static final X509TrustManager TRUST_ALL_MANAGER = new X509TrustManager() {
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
		
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}
		
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}
	};

	/**
	 * A wrapper for an {@link HttpURLConnection}, which implements
	 * {@link AutoCloseable}.
	 */
	public static class HttpConnection implements AutoCloseable {
		private final HttpURLConnection urlConn;

		/** Creates a new instance, which wraps the given
		 * {@link HttpURLConnection}. Closing this object will
		 * {@link HttpURLConnection#disconnect() disconnect} the
		 * HTTP connection.
		 * @param pConn The HTTP connection object. Typically, the actual
		 *   network connection hasn't yet been established, and the
		 *   HTTP connection is just ready for configuration.
		 */
		public HttpConnection(HttpURLConnection pConn) {
			urlConn = pConn;
		}

		@Override
		public void close() {
			urlConn.disconnect();
		}

		/** Returns the wrapped {@link HttpURLConnection}.
		 * @return The wrapped {@link HttpURLConnection}
		 */
		public HttpURLConnection getUrlConnection() {
			return urlConn;
		}
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
	 * @param pTrustingAllCertificates True, if all SSL certificates are trusted. The default is false.
	 */
	public void setTrustingAllCertificates(boolean pTrustingAllCertificates) {
		trustingAllCertificates = pTrustingAllCertificates;
	}
	/**
	 * Returns the trust store to use for verification of SSL certificates.
	 * @return The trust store to use for verification of SSL certificates.
	 *   The default is null (Use the system's default trust store).
	 * @see #setTruststore(Path, String)
	 * @see #getTruststorePassword()
	 */
	public Path getTruststore() {
		return trustStore;
	}
	/**
	 * Returns the password to use for reading the truststore.
	 * @return The password to use for reading the truststore.
	 *   Ignored, if the system's default truststore is used
	 *   (If the configured truststore is null.)
	 * @see #setTruststore(Path, String)
	 * @see #getTruststore()
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
		trustStorePassword = pTruststorePassword;
	}

	/**
	 * Returns the proxy host.
	 * @return The proxy host.
	 */
	public @Nullable String getProxyHost() {
		return proxy == null ? null : proxy.getHostString();
	}
	/**
	 * Returns the proxy port.
	 * @return The proxy port.
	 */
	public @Nullable Integer getProxyPort() {
		return proxy == null ? null : Integer.valueOf(proxy.getPort());
	}

	/** Sets the proxy server to use.
	 * 
	 * @param pProxyDefinition A proxy definition in the form "host:port".
	 * @see #setProxy(String, int)
	 */
	public void setProxy(@NonNull String pProxyDefinition) {
		final @NonNull String proxy = Objects.requireNonNull(pProxyDefinition, "Proxy Definition");
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
		@SuppressWarnings("null")
		final @NonNull String host = proxy.substring(0, offset);
		setProxy(host, port);
	}

	/** Sets the proxy server to use.
	 * 
	 * @param pProxyHost The proxy servers host name, or ip address.
	 * @param pProxyPort The proxy servers port number.
	 * @see #setProxy(String)
	 */
	public void setProxy(@NonNull String pProxyHost, int pProxyPort) {
		final String proxyHost = Objects.requireNonNull(pProxyHost, "Proxy Host");
		try {
			proxy = new InetSocketAddress(proxyHost, pProxyPort);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid port number: Expected 0 <= PortNumber < 65536, got " + pProxyPort);
		}
	}
	

	/**
	 * Creates an {@link HttpConnection} for the given URL.
	 * @param pUrl The URL, to which a connection is being created.
	 * @return The established connection.
	 * @throws IOException Creating the connection failed.
	 */
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

	/** Creates an {@link AutoCloseable}, that provides a connection to the given
	 * URL.
	 * @param pUrl The URL, to which a connection shall be made.
	 * @return The created {@link AutoCloseable}.
	 * @throws IOException Creating the connection has failed.
	 */
	protected HttpConnection asHttpConnection(URL pUrl) throws IOException {
		final HttpURLConnection urlConn;
		if (proxy != null) {
			final Proxy prox = new Proxy(Proxy.Type.HTTP, proxy);
			urlConn = (HttpURLConnection) pUrl.openConnection(prox);
		} else {
			urlConn = (HttpURLConnection) pUrl.openConnection();
		}
		return new HttpConnection(urlConn);
	}

	/** Creates an {@link AutoCloseable}, that provides a connection to the given
	 * https URL.
	 * @param pUrl The URL, to which a connection shall be made.
	 * @return The created {@link AutoCloseable}.
	 * @throws IOException Creating the connection has failed.
	 */
	protected HttpConnection asHttpsConnection(URL pUrl) throws IOException {
		final HttpsURLConnection urlConn;
		if (proxy != null) {
			final Proxy prox = new Proxy(Proxy.Type.HTTP, proxy);
			urlConn = (HttpsURLConnection) pUrl.openConnection(prox);
		} else {
			urlConn = (HttpsURLConnection) pUrl.openConnection();
		}
		urlConn.setSSLSocketFactory(getSSLSocketFactory());
		return new HttpConnection(urlConn);
	}

	/** Creates an {@link SSLSocketFactory SSL socket factory},
	 * that can be used to create HTTPS connections.
	 * @return The created {@link SSLSocketFactory SSL socket factory}.
	 */
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
