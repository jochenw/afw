/*
 * Copyright 2018 Jochen Wiedmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jochenw.afw.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableFunction;
import com.github.jochenw.afw.core.function.Functions.FailableSupplier;
import com.github.jochenw.afw.core.io.IReadable;
import com.github.jochenw.afw.core.util.HttpConnector.HttpConnection;

import groovyjarjarantlr4.v4.runtime.misc.NotNull;


/** Utility class for working with REST requests. Suggested use:
 * <pre>
 *   final Rest rest = getComponentFactory().requireInstance(Rest.class);
 *   rest.builder(url)
 *       .method("POST")
 *       .resource("/rest/MyResource")
 *       .resourceId(myResourceId)
 *       .basicAuth("MyUsername", "Mypassword")
 *       .parameter("MyParam", "Value")
 *       .send()
 * </pre>
 * 
 * If your request is supposed to create a result, replace {@link Builder#send()}
 * with {@link Builder#call(FailableFunction)}.
 *       
 * </pre>
 */
public class Rest {
	private @Inject HttpConnector httpConnector;

	/** This class is a container for the REST requests configuration.
	 * To create an instance, use a {@link Rest#builder(URL)}.
	 */
	public class Request {
		private final @NotNull URL url;
		private final String method, resource, resourceId;
		private final @Nullable Map<String,String> parameters, headers;
		private final FailableConsumer<OutputStream,?> body;
		private final FailableConsumer<InputStream,?> consumer, errorConsumer;

		/** Creates a new instance. You are not supposed to use this constructor
		 * directly. Instead, use {@link Rest#builder(URL)}.
		 * @param pUrl The REST API's URL.
		 * @param pMethod The HTTP method, which is being used.
		 * @param pResource The REST resource.
		 * @param pResourceId The REST resource id.
		 * @param pParameters The map of request parameters.
		 * @param pHeaders The map of request headers.
		 * @param pBody An optional producer for the request body.
		 * @param pConsumer An optional consumer for the response body.
		 * @param pErrorConsumer An optional consumer for the error response body.
		 */
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

		/** Returns the REST API's URL. The actual URL is built by concatenating this URL,
		 * the {@link #getResource() resource URI}, and the {@link #getResourceId() resource id}.
		 * @return The REST API's URL
		 * @see #getResource()
		 * @see #getResourceId()
		 */
		public URL getUrl() { return url; }
		/** Returns the HTTP request method, like POST, PUT, DELETE, or GET.
		 * May be null, in which case GET is used as the default.
		 * @return The HTTP request method, or null.
		 * @see Rest.Builder#post(FailableConsumer)
		 * @see Rest.Builder#put(FailableConsumer)
		 */
		public String getMethod() { return method; }
		/** Returns the REST resource URI, which will be appended to the {@link #getUrl() REST API URL}.
		 * @return The REST resource URI
		 * @see #getUrl()
		 * @see #getResourceId()
		 */
		public String getResource() { return resource; }
		/** Returns the optional REST resource id, which will be appended to the {@link #getUrl() REST API URL},
		 * and the {@link #getResource() REST resource URI}.
		 * @return The REST resource id
		 * @see #getUrl()
		 * @see #getResource()
		 */
		public String getResourceId() { return resourceId; }
		/** Returns the map of HTTP request parameters.
		 * <em>Note:</em> As of this writing, multiple values per parameter are unsupported.
		 * @return The map of HTTP request parameters.
		 * @see Rest.Builder#parameter(String, String)
		 * @see #getHeaders()
		 */
		public Map<String, String> getParameters() { return parameters; }
		/** Returns the map of HTTP request parameters.
		 * <em>Note:</em> As of this writing, multiple values per header are unsupported.
		 * @return The map of HTTP request parameters.
		 * @see Rest.Builder#header(String, String)
		 * @see #getParameters()
		 */
		public Map<String, String> getHeaders() { return headers; }
		/** Returns a producer for the request body.
		 * <em>Note:</em> Keep in mind, that the {@link #getMethod() HTTP request method}
		 * must be "POST", or "PUT", if you have a request body. The best way to ensure this
		 * is to use the methods {@link Rest.Builder#post(FailableConsumer)}, or
		 * {@link Rest.Builder#put(FailableConsumer)}, rather than
		 * {@link Rest.Builder#body(FailableConsumer)}.
		 * @return The producer for the request body, or null.
		 */
		public FailableConsumer<OutputStream, ?> getBody() { return body; }
		/** Returns a consumer for the response body.
		 * <em>Note:</em> If your consumer is supposed to produce a result,
		 * consider using a {@link Rest#call(Request, FailableFunction) result
		 * function} instead.
		 * @return The consumer for the response body, or null.
		 */
		public FailableConsumer<InputStream, ?> getConsumer() { return consumer; }
		/** Returns a consumer for the error response body.
		 * @return The consumer for the error response body, or null.
		 */
		public FailableConsumer<InputStream, ?> getErrorConsumer() { return errorConsumer; }
		/** Performs the request by invoking {@link Rest#send(Request)}. Invokes the
		 * {@link #getBody() body producer}, {@link #getConsumer() response consumer},
		 * and the {@link #getErrorConsumer() error response consumer}, as necessary.
		 * If your response is supposed to produce a result, consider using
		 * {@link #call(FailableFunction)} instead.
		 */
		public void send() {
			Rest.this.send(this);
		}
		/** Performs the request by invoking {@link Rest#send(Request)}. Invokes the
		 * {@link #getBody() body producer}, the {@code pCallable result producer},
		 * the {@link #getConsumer() response consumer},
		 * and the {@link #getErrorConsumer() error response consumer}, as necessary.
		 */
		public <O> O call(FailableFunction<InputStream,O,?> pCallable) {
			return Rest.this.call(this, pCallable);
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

		public <O> O call(FailableFunction<InputStream, O, ?> pCallable) {
			return Rest.this.call(build(), pCallable);
		}
	}

	public <O> O call(Request pRequest, FailableFunction<InputStream,O,?> pCallable) {
		final Request request = Objects.requireNonNull(pRequest, "Request");
		final FailableFunction<InputStream,O,?> callable = Objects.requireNonNull(pCallable, "Callable");
		final FailableConsumer<InputStream,?> consumer = request.getConsumer();
		final Holder<O> holder = new Holder<>();
		if (consumer == null) {
			final FailableConsumer<InputStream,?> reader = (in) -> {
				holder.set(callable.apply(in));
			};
			send(request, reader);
			return holder.get();
		} else {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final FailableConsumer<InputStream,?> reader = (in) -> Streams.copy(in, baos);
			send(request, reader);
			final byte[] bytes = baos.toByteArray();
			try {
				consumer.accept(new ByteArrayInputStream(bytes));
				holder.set(callable.apply(new ByteArrayInputStream(bytes)));
				return holder.get();
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		}
	}

	public void send(Request pRequest) {
		send(pRequest, pRequest.getConsumer());
	}

	protected void send(Request pRequest, FailableConsumer<InputStream,?> pReader) {
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
				if (pReader != null) {
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
				if (pReader != null) {
					try (InputStream in = urlConn.getInputStream()) {
						pReader.accept(in);
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
