package com.github.jochenw.afw.core.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableSupplier;
import com.github.jochenw.afw.core.io.IReadable;
import com.github.jochenw.afw.core.util.HttpConnector.HttpConnection;

import groovyjarjarantlr4.v4.runtime.misc.NotNull;

public class Rest {
	private @Inject HttpConnector httpConnector;

	public class Request {
		private final @NotNull URL url;
		private final String method, resource, resourceId;
		private final @Nullable Map<String,String> parameters, headers;
		private final FailableConsumer<OutputStream,?> body;
		private final FailableConsumer<InputStream,?> consumer, errorConsumer;

		public Request(URL pUrl, String pMethod, String pResource, String pResourceId, Map<String, String> pParameters,
				Map<String, String> pHeaders, FailableConsumer<OutputStream, ?> pBody,
				FailableConsumer<InputStream, ?> pConsumer, FailableConsumer<InputStream, ?> pErrorConsumer) {
			url = pUrl;
			method = pMethod;
			resource = pResource;
			resourceId = pResourceId;
			parameters = pParameters;
			headers = pHeaders;
			body = pBody;
			consumer = pConsumer;
			errorConsumer = pErrorConsumer;
		}

		public URL getUrl() { return url; }
		public String getMethod() { return method; }
		public String getResource() { return resource; }
		public String getResourceId() { return resourceId; }
		public Map<String, String> getParameters() { return parameters; }
		public Map<String, String> getHeaders() { return headers; }
		public FailableConsumer<OutputStream, ?> getBody() { return body; }
		public FailableConsumer<InputStream, ?> getConsumer() { return consumer; }
		public FailableConsumer<InputStream, ?> getErrorConsumer() { return errorConsumer; }
		public void send() {
			Rest.this.send(this);
		}
	}
   
	public class Builder extends AbstractBuilder<Request,Builder> {
		private final @NotNull URL url;
		private @Nullable String method, resource, resourceId;
		private @Nullable Map<String,String> parameters, headers;
		private FailableConsumer<OutputStream,?> body;
		private FailableConsumer<InputStream,?> consumer, errorConsumer;
		Builder(@NotNull URL pUrl) {
			url = pUrl;
		}
	
		public @NotNull URL getUrl() { return url; }
		public @NotNull String getMethod() { return Objects.notNull(method, "GET"); }
		public @Nullable String getResource() { return resource; }
		public @Nullable String getResourceId() { return resourceId; }
		public @NotNull Map<String,String> getParameters() { return parameters == null ? Collections.emptyMap() : parameters; }
		public @NotNull Map<String,String> getHeaders() { return headers == null ? Collections.emptyMap() : headers; }
		public Builder method(String pMethod) {
			assertMutable();
			method = pMethod;
			return this;
		}
		public Builder resource(String pResource) {
			assertMutable();
			resource = pResource;
			return this;
		}
		public Builder resourceId(String pResourceId) {
			assertMutable();
			resourceId = pResourceId;
			return this;
		}
		public FailableConsumer<OutputStream,?> getBody() { return body; }
		public Builder body(FailableConsumer<OutputStream,?> pBody) {
			assertMutable();
			body = pBody;
			return this;
		}
		public Builder body(IReadable pBody) {
			assertMutable();
			if (pBody == null) {
				body = null;
			} else {
				body = (out) -> {
					pBody.read((in) -> {
						Streams.copy(in, out);
					});
				};
			}
			return this;
		}
		public Builder body(Path pPath) { return body(IReadable.of(pPath)); }
		public Builder body(File pFile) { return body(IReadable.of(pFile)); }
		public Builder body(URL pUrl) { return body(IReadable.of(pUrl)); }
		public Builder body(FailableSupplier<InputStream,?> pIn, String pUri) { return body(IReadable.of(pUri, pIn)); }
		public Builder post(FailableConsumer<OutputStream,?> pBody) {
			body(pBody);
			return method("POST");
		}
		public Builder post(IReadable pReadable) {
			body(pReadable);
			return method("POST");
		}
		public Builder post(Path pPath) { return post(IReadable.of(pPath)); }
		public Builder post(File pFile) { return post(IReadable.of(pFile)); }
		public Builder post(URL pUrl) { return post(IReadable.of(pUrl)); }
		public Builder post(FailableSupplier<InputStream,?> pIn, String pUri) { return post(IReadable.of(pUri, pIn)); }
		public Builder put(FailableConsumer<OutputStream,?> pBody) {
			body(pBody);
			return method("PUT");
		}
		public Builder put(IReadable pReadable) {
			body(pReadable);
			return method("PUT");
		}
		public Builder put(Path pPath) { return put(IReadable.of(pPath)); }
		public Builder put(File pFile) { return put(IReadable.of(pFile)); }
		public Builder put(URL pUrl) { return put(IReadable.of(pUrl)); }
		public Builder put(FailableSupplier<InputStream,?> pIn, String pUri) { return put(IReadable.of(pUri, pIn)); }
		public Builder header(@NotNull String pName, String pValue) {
			final @NotNull String name = Objects.requireNonNull(pName, "Name");
			assertMutable();
			if (pValue == null) {
				if (headers != null) {
					headers.remove(name);
				}
			} else {
				if (headers == null) {
					headers = new HashMap<>();
				}
				headers.put(name, pValue);
			}
			return this;
		}
		public Builder headers(String... pNameValuePairs) {
			if (pNameValuePairs == null  ||  pNameValuePairs.length == 0) {
				headers = null;
			} else {
				for (int i = 0;  i < pNameValuePairs.length;  i += 2) {
					headers(pNameValuePairs[i], pNameValuePairs[i+1]);
				}
			}
			return this;
		}
		public Builder basicAuth(@NotNull String pUserName, @NotNull String pPassword) {
			final String userName = Objects.requireNonNull(pUserName, "User name");
			final String password = Objects.requireNonNull(pUserName, "Password");
			final byte[] authBytes = (userName + ":" + password).getBytes(StandardCharsets.UTF_8);
			final String authHeader = "Basic " +
					Base64.getMimeEncoder(-1, null).encodeToString(authBytes);
			return header("Authorize", authHeader);
		}
		public Builder parameter(@NotNull String pName, String pValue) {
			final @NotNull String name = Objects.requireNonNull(pName, "Name");
			assertMutable();
			if (pValue == null) {
				if (parameters != null) {
					parameters.remove(name);
				}
			} else {
				if (parameters == null) {
					parameters = new HashMap<>();
				}
				parameters.put(name, pValue);
			}
			return this;
		}
		public Builder parameters(String... pNameValuePairs) {
			if (pNameValuePairs == null  ||  pNameValuePairs.length == 0) {
				parameters = null;
			} else {
				for (int i = 0;  i < pNameValuePairs.length;  i += 2) {
					parameters(pNameValuePairs[i], pNameValuePairs[i+1]);
				}
			}
			return this;
		}
		public FailableConsumer<InputStream,?> getConsumer() { return consumer; }
		public FailableConsumer<InputStream,?> getErrorConsumer() { return errorConsumer; }
		public Builder consumer(FailableConsumer<InputStream,?> pConsumer) {
			assertMutable();
			consumer = pConsumer;
			return this;
		}
		public Builder errorConsumer(FailableConsumer<InputStream,?> pConsumer) {
			assertMutable();
			errorConsumer = pConsumer;
			return this;
		}
		public Request newInstance() {
			return new Request(getUrl(), getMethod(),
					           getResource(), getResourceId(),
					           getParameters(), getHeaders(),
					           getBody(), getConsumer(), getErrorConsumer());
		}
		public void send() {
			Rest.this.send(build());
		}
	}

	public void send(Request pRequest) {
		try {
			URL url = pRequest.getUrl();
			final String res = pRequest.getResource();
			final String resId = pRequest.getResourceId();
			final String spec;
			if (res == null) {
				if (resId == null) {
					spec = null;
				} else {
					spec = "/" + resId;
				}
			} else {
				if (resId == null) {
					if (res.startsWith("/")) {
						spec = res;
					} else {
						spec = "/" +res;
					}
				} else {
					if (res.startsWith("/")) {
						spec = res + "/" + resId;
					} else {
						spec = "/" + res + "/" + resId;
					}
				}
			}
			if (spec != null) {
				url = new URL(url, spec);
			}
			if (pRequest.getParameters() != null  &&  !pRequest.getParameters().isEmpty()) {
				final StringBuilder sb = new StringBuilder(url.toExternalForm());
				final BiConsumer<String,String> parameterConsumer = new BiConsumer<String,String>(){
					private boolean first = true;
					@Override
					public void accept(String pName, String pValue) {
						if (first) {
							sb.append('?');
							first = false;
						} else {
							sb.append('&');
						}
						try {
							sb.append(URLEncoder.encode(pName, "UTF-8"));
							sb.append('=');
							sb.append(URLEncoder.encode(pValue, "UTF-8"));
						} catch (IOException e) {
							throw Exceptions.show(e);
						}
					}
				};
				pRequest.getParameters().forEach(parameterConsumer);
				url = new URL(sb.toString());
			}
			try (HttpConnection conn = httpConnector.connect(url)) {
				final HttpURLConnection urlConn = conn.getUrlConnection();
				if (pRequest.getHeaders() != null) {
					pRequest.getHeaders().forEach((n,v) -> urlConn.addRequestProperty(n, v));
				}
				if (pRequest.getConsumer() != null) {
					urlConn.setDoInput(true);
				}
				if (pRequest.getBody() != null) {
					urlConn.setDoOutput(true);
					try (OutputStream out = urlConn.getOutputStream()) {
						pRequest.getBody().accept(out);
					}
				}
				final int status = urlConn.getResponseCode();
				final String msg = urlConn.getResponseMessage();
				if (status >= 200  &&  status < 300) {
					if (pRequest.getErrorConsumer() != null) {
						try (InputStream err = urlConn.getErrorStream()) {
							pRequest.getErrorConsumer().accept(err);
						}
					}
					throw new IllegalStateException("Unexepected HTTP Response: " + status + "," + msg);
				}
				if (pRequest.getConsumer() != null) {
					try (InputStream in = urlConn.getInputStream()) {
						pRequest.getConsumer().accept(in);
					}
				}
			}
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}
	
	public Builder builder(URL pUrl) {
		final @NotNull URL url = Objects.requireNonNull(pUrl, "URL");
		return new Builder(url);
	}

	
}
