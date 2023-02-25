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

import com.github.jochenw.afw.core.function.Functions;
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
 * If your request is supposed to create a result, replace {@link RestAccess.Builder#send()}
 * with {@code RestAccess.Builder#call(FailableFunction)}.
 */
public class RestAccess {
	private @Inject HttpConnector httpConnector;

	/** This class is a container for the REST requests configuration.
	 * To create an instance, use a {@link RestAccess#builder(URL)}.
	 */
	public class Request {
		private final @NotNull URL url;
		private final String method, resource, resourceId;
		private final @Nullable Map<String,String> parameters, headers;
		private final FailableConsumer<OutputStream,?> body;
		private final FailableConsumer<InputStream,?> consumer, errorConsumer;

		/** Creates a new instance. You are not supposed to use this constructor
		 * directly. Instead, use {@link RestAccess#builder(URL)}.
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
		 * @see RestAccess.Builder#post(Functions.FailableConsumer)
		 * @see RestAccess.Builder#put(FailableConsumer)
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
		 * @see RestAccess.Builder#parameter(String, String)
		 * @see #getHeaders()
		 */
		public Map<String, String> getParameters() { return parameters; }
		/** Returns the map of HTTP request parameters.
		 * <em>Note:</em> As of this writing, multiple values per header are unsupported.
		 * @return The map of HTTP request parameters.
		 * @see RestAccess.Builder#header(String, String)
		 * @see #getParameters()
		 */
		public Map<String, String> getHeaders() { return headers; }
		/** Returns a producer for the request body.
		 * <em>Note:</em> Keep in mind, that the {@link #getMethod() HTTP request method}
		 * must be "POST", or "PUT", if you have a request body. The best way to ensure this
		 * is to use the methods {@link RestAccess.Builder#post(Functions.FailableConsumer)}, or
		 * {@link RestAccess.Builder#put(Functions.FailableConsumer)}, rather than
		 * {@link RestAccess.Builder#body(Functions.FailableConsumer)}.
		 * @return The producer for the request body, or null.
		 */
		public FailableConsumer<OutputStream, ?> getBody() { return body; }
		/** Returns a consumer for the response body.
		 * <em>Note:</em> If your consumer is supposed to produce a result,
		 * consider using a {@link RestAccess#call(Request, Functions.FailableFunction) result
		 * function} instead.
		 * @return The consumer for the response body, or null.
		 */
		public FailableConsumer<InputStream, ?> getConsumer() { return consumer; }
		/** Returns a consumer for the error response body.
		 * @return The consumer for the error response body, or null.
		 */
		public FailableConsumer<InputStream, ?> getErrorConsumer() { return errorConsumer; }
		/** Performs the request by invoking {@link RestAccess#send(Request)}. Invokes the
		 * {@link #getBody() body producer}, {@link #getConsumer() response consumer},
		 * and the {@link #getErrorConsumer() error response consumer}, as necessary.
		 * If your response is supposed to produce a result, consider using
		 * {@link #call(Functions.FailableFunction)} instead.
		 */
		public void send() {
			RestAccess.this.send(this);
		}
		/** Performs the request by invoking {@link RestAccess#send(Request)}. Invokes the
		 * {@link #getBody() body producer}, the {@code pCallable result producer},
		 * the {@link #getConsumer() response consumer},
		 * and the {@link #getErrorConsumer() error response consumer}, as necessary.
		 * <em>Note:</em> 
		 * @param <O> Type of the result object, that is being produced by the
		 *   {@code pCallable result producer}, and returned by this method.
		 * @param pCallable The result producer.
		 * @return The result object, which has been created by invoking
		 *   the {@code pCallable}.
		 */
		public <O> O call(FailableFunction<InputStream,O,?> pCallable) {
			return RestAccess.this.call(this, pCallable);
		}
	}
   
	/** A builder class for creating instances of {@link RestAccess.Request}.
	 */
	public class Builder extends AbstractBuilder<Request,Builder> {
		private final @NotNull URL url;
		private @Nullable String method, resource, resourceId;
		private @Nullable Map<String,String> parameters, headers;
		private FailableConsumer<OutputStream,?> body;
		private FailableConsumer<InputStream,?> consumer, errorConsumer;
		Builder(@NotNull URL pUrl) {
			url = pUrl;
		}

		/** Returns the REST server's URL. Note, that the {@link #getResource() REST resource},
		 * and/or the {@link #getResourceId()} will be appended to the URL, if they are non-null.
		 * @return The REST server's URL
		 * @see #getResource()
		 * @see #getResourceId()
		 */
		public @NotNull URL getUrl() { return url; }
		/** Returns the HTTP request method. Defaults to "GET".
		 * @return the HTTP request method. Defaults to "GET".
		 * @see #method(String)
		 * @see #post(Functions.FailableConsumer)
		 * @see #put(Functions.FailableConsumer)
		 */
		public @NotNull String getMethod() { return Objects.notNull(method, "GET"); }
		/** Returns the REST resource, that should be appended to the REST server's URL.
		 * @return The REST resource, that should be appended to the REST server's URL.
		 * @see #getUrl()
		 */
		public @Nullable String getResource() { return resource; }
		/** Returns the REST resource id, that should be appended to the REST server's URL.
		 * @return The REST resource id, that should be appended to the REST server's URL.
		 * @see #getUrl()
		 */
		public @Nullable String getResourceId() { return resourceId; }
		/** Returns the map of HTTP request parameters.
		 * @return The map of HTTP request parameters.
		 * @see #parameter(String, String)
		 * @see #parameters(String...)
		 * @see #getHeaders()
		 */
		public @NotNull Map<String,String> getParameters() { return parameters == null ? Collections.emptyMap() : parameters; }
		/** Returns the map of HTTP request headers.
		 * @return The map of HTTP request headers.
		 * @see #header(String, String)
		 * @see #headers(String...)
		 * @see #getParameters()
		 */
		public @NotNull Map<String,String> getHeaders() { return headers == null ? Collections.emptyMap() : headers; }
		/** Sets the HTTP request method. The default value is "GET".
		 * @param pMethod The HTTP request method, or null (Default value "GET").
		 * @see #getMethod()
		 * @see #post(Functions.FailableConsumer)
		 * @see #put(Functions.FailableConsumer)
		 * @return This builder.
		 */
		public Builder method(String pMethod) {
			assertMutable();
			method = pMethod;
			return this;
		}
		/** Sets the REST resource, that should be appended to the REST server's URL.
		 * @param pResource The REST resource, that should be appended to the REST server's URL.
		 * @see #getResource()
		 * @return This builder.
		 */
		public Builder resource(String pResource) {
			assertMutable();
			resource = pResource;
			return this;
		}
		/** Sets the resource id, that is being requested from the REST server.
		 * @param pResourceId The resource id, that is being requested
		 *   from the REST server.
		 * @return This builder.
		 */
		public Builder resourceId(String pResourceId) {
			assertMutable();
			resourceId = pResourceId;
			return this;
		}
		/** Returns the request body producer.
		 * @return The request body producer.
		 * @see #body(Functions.FailableConsumer)
		 */
		public FailableConsumer<OutputStream,?> getBody() { return body; }
		/** Sets the request body producer.
		 * @param pBody The request body producer.
		 * @see #getBody()
		 * @return This builder.
		 */
		public Builder body(FailableConsumer<OutputStream,?> pBody) {
			assertMutable();
			body = pBody;
			return this;
		}
		/** Sets the request body. Equivalent to invoking
		 * {@link #body(Functions.FailableConsumer)} with an object,
		 * that copies the given {@code pBody} to the requests body.
		 * @param pBody The request body.
		 * @see #getBody()
		 * @see #body(Functions.FailableConsumer)
		 * @return This builder.
		 */
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
		/** Sets the request body. Equivalent to invoking
		 * {@link #body(Functions.FailableConsumer)} with an object,
		 * that copies the given file {@code pPath} to the requests body.
		 * @param pPath The request body.
		 * @see #getBody()
		 * @see #body(Functions.FailableConsumer)
		 * @see #body(File)
		 * @return This builder.
		 */
		public Builder body(Path pPath) { return body(IReadable.of(pPath)); }
		/** Sets the request body. Equivalent to invoking
		 * {@link #body(Functions.FailableConsumer)} with an object,
		 * that copies the given file {@code pFile} to the requests body.
		 * @param pFile The request body.
		 * @see #getBody()
		 * @see #body(Functions.FailableConsumer)
		 * @see #body(Path)
		 * @return This builder
		 */
		public Builder body(File pFile) { return body(IReadable.of(pFile)); }
		/** Sets the request body. Equivalent to invoking
		 * {@link #body(Functions.FailableConsumer)} with an object,
		 * that copies the given URL {@code pUrl} to the requests body.
		 * @param pUrl The request body.
		 * @see #getBody()
		 * @see #body(Functions.FailableConsumer)
		 * @see #body(Path)
		 * @see #body(File)
		 * @return This builder
		 */
		public Builder body(URL pUrl) { return body(IReadable.of(pUrl)); }
		/** Sets the request body. Equivalent to invoking
		 * {@link #body(Functions.FailableConsumer)} with an object,
		 * that reads the request body from an {@link InputStream}. The
		 * input stream in question is obtained by invoking the given
		 * {@link FailableSupplier supplier}.
		 * @param pIn The supplier, that returns the request body's
		 *   {@link InputStream}.
		 * @param pUri The input streams Uri, for use in error messages.
		 * @see #getBody()
		 * @see #body(Functions.FailableConsumer)
		 * @see #body(Path)
		 * @see #body(File)
		 * @return This builder
		 */
		public Builder body(FailableSupplier<InputStream,?> pIn, String pUri) { return body(IReadable.of(pUri, pIn)); }
		/** Sets the request method to "POST", and the request body
		 * generator to {@code pBody}.
		 * @param pBody The request body generator.
		 * @return This builder
		 */
		public Builder post(FailableConsumer<OutputStream,?> pBody) {
			body(pBody);
			return method("POST");
		}
		/** Sets the request method to "POST", and the request body
		 * to the contents of the given {@link IReadable readable}.
		 * @param pReadable The source for reading the request body.
		 * @return This builder
		 */
		public Builder post(IReadable pReadable) {
			body(pReadable);
			return method("POST");
		}
		/** Sets the request method to "POST", and the request body
		 * to the contents of the given {@link Path file}.
		 * @param pPath The source for reading the request body.
		 * @return This builder
		 */
		public Builder post(Path pPath) { return post(IReadable.of(pPath)); }
		/** Sets the request method to "POST", and the request body
		 * to the contents of the given {@link File file}.
		 * @param pFile The source for reading the request body.
		 * @return This builder
		 */
		public Builder post(File pFile) { return post(IReadable.of(pFile)); }
		/** Sets the request method to "POST", and the request body
		 * to the contents of the given {@link URL}.
		 * @param pUrl The source for reading the request body.
		 * @return This builder
		 */
		public Builder post(URL pUrl) { return post(IReadable.of(pUrl)); }
		/** Sets the request method to "POST", and the request body
		 * to the contents of an {@link InputStream}, that is returned
		 * by the supplier {@code pIn}.
		 * @param pIn A supplier of the source for reading the request body.
		 * @param pUri The input streams Uri, for use in error messages.
		 * @return This builder
		 */
		public Builder post(FailableSupplier<InputStream,?> pIn, String pUri) { return post(IReadable.of(pUri, pIn)); }
		/** Sets the request method to "PUT", and the request body
		 * generator to {@code pBody}.
		 * @param pBody The request body generator,
		 * @return This builder
		 */
		public Builder put(FailableConsumer<OutputStream,?> pBody) {
			body(pBody);
			return method("PUT");
		}
		/** Sets the request method to "PUT", and the request body
		 * to the contents of the given {@link IReadable readable}.
		 * @param pReadable The source for reading the request body.
		 * @return This builder
		 */
		public Builder put(IReadable pReadable) {
			body(pReadable);
			return method("PUT");
		}
		/** Sets the request method to "PUT", and the request body
		 * to the contents of the given {@link Path file}.
		 * @param pPath The source for reading the request body.
		 * @return This builder
		 */
		public Builder put(Path pPath) { return put(IReadable.of(pPath)); }
		/** Sets the request method to "PUT", and the request body
		 * to the contents of the given {@link File file}.
		 * @param pFile The source for reading the request body.
		 * @return This builder
		 */
		public Builder put(File pFile) { return put(IReadable.of(pFile)); }
		/** Sets the request method to "POST", and the request body
		 * to the contents of the given {@link URL}.
		 * @param pUrl The source for reading the request body.
		 * @return This builder
		 */
		public Builder put(URL pUrl) { return put(IReadable.of(pUrl)); }
		/** Sets the request method to "POST", and the request body
		 * to the contents of an {@link InputStream}, that is returned
		 * by the supplier {@code pIn}.
		 * @param pIn A supplier of the source for reading the request body.
		 * @param pUri The input streams Uri, for use in error messages.
		 * @return This builder
		 */
		public Builder put(FailableSupplier<InputStream,?> pIn, String pUri) { return put(IReadable.of(pUri, pIn)); }
		/** Sets the request header {@code pName} to the given value {@code pValue}.
		 * @param pName The request headers name.
		 * @param pValue The request headers value.
		 * @return This builder.
		 */
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
		/** Sets a set of request headers, as given by the name/value pairs {@code pNameValuePairs}.
		 * @param pNameValuePairs A set of request headers, as name/value pairs.
		 *   (Names have the even indexes, and values the odd indexes). 
		 * @return This builder.
		 */
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
		/** Sets the basic authentication header "Authorization" to the given
		 * user name, and password.
		 * @param pUserName The user name for basic authentication.
		 * @param pPassword The password for basic authentication.
		 * @return This builder.
		 */
		public Builder basicAuth(@NotNull String pUserName, @NotNull String pPassword) {
			final String userName = Objects.requireNonNull(pUserName, "User name");
			final String password = Objects.requireNonNull(pPassword, "Password");
			final byte[] authBytes = (userName + ":" + password).getBytes(StandardCharsets.UTF_8);
			final String authHeader = "Basic " +
					Base64.getMimeEncoder(-1, new byte[] {'\n'}).encodeToString(authBytes);
			return header("Authorization", authHeader);
		}
		/** Sets the value of the "content-type" header.
		 * @param pContentType The value of the "content-type" header.
		 * @return This builder.
		 */
		public Builder contentType(@NotNull String pContentType) {
			return header("content-type", pContentType);
		}
		/** Sets the value of the parameter {@code pName} to {@code pValue}.
		 * @param pName The parameter name.
		 * @param pValue The parameter value. (A null value will remove the
		 *   parameter.)
		 * @return This builder.
		 */
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
		/** Sets the values of a set of parameters in one go. The parameters
		 * are given as an array of name/value pairs.
		 * @param pNameValuePairs The parameter set, as an array of name/value
		 * pairs. (Names have the even indexes, values have the odd indexes,
		 * An empty array, or a null array will clear the parameters.
		 * @return This builder.
		 */
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
		/** Returns the consumer for the HTTP response body, if any, or null.
		 * In the latter case, the response body will be ignored.
		 * @return The consumer for the HTTP response body, if any, or null.
		 */
		public FailableConsumer<InputStream,?> getConsumer() { return consumer; }
		/** Returns the consumer for the HTTP error response body, if any,
		 * or null. In the latter case, the error response body will be ignored.
		 * @return The consumer for the HTTP response body, if any, or null.
		 */
		public FailableConsumer<InputStream,?> getErrorConsumer() { return errorConsumer; }
		/** Sets the consumer for the HTTP response body.
		 * A null value will cause the response body to be ignored.
		 * @param pConsumer The consumer for the HTTP response body,
		 *   if any, or null.
		 * @return This builder.
		 */
		public Builder consumer(FailableConsumer<InputStream,?> pConsumer) {
			assertMutable();
			consumer = pConsumer;
			return this;
		}
		/** Sets the consumer for the HTTP error response body.
		 * A null value will cause the error response body to be ignored.
		 * @param pConsumer The consumer for the HTTP error response body,
		 *   if any, or null.
		 * @return This builder.
		 */
		public Builder errorConsumer(FailableConsumer<InputStream,?> pConsumer) {
			assertMutable();
			errorConsumer = pConsumer;
			return this;
		}
		/** Creates the new request instance, applying the builders
		 * configuration.
		 * @return The created, and configured,
		 *   {@link RestAccess.Request request instance}.
		 */
		public Request newInstance() {
			return new Request(getUrl(), getMethod(),
					           getResource(), getResourceId(),
					           getParameters(), getHeaders(),
					           getBody(), getConsumer(), getErrorConsumer());
		}
		/** Called to invoke {@link #build()}, followed by
		 * {@link RestAccess#send(Request)}}, in one go.
		 */
		public void send() {
			RestAccess.this.send(build());
		}

		/** Called to invoke {@link #build()}, followed by
		 * {@link RestAccess#call(Request, FailableFunction)}, in one go.
	     * @param <O> Type of the result object, that is returned by the
	     * {@code pCallable}, and then this method.
		 * @param pCallable The callable, which is being supplied as a
		 *   parameter when calling {@link RestAccess#call(Request, FailableFunction)}
		 * @return The result of invoking the given {@code pCallable}.
		 */
		public <O> O call(FailableFunction<InputStream, O, ?> pCallable) {
			return RestAccess.this.call(build(), pCallable);
		}
	}

	/** Called to execute the given {@link RestAccess.Request}. The given
	 * {@code pCallable} will be invoked to read the servers response body.
	 * The callable is supposed to return a result object, which will
	 * then be returned as this methods result object.
	 * <em>Note:</em> 
	 * If the request is configured with a {@link Request#getConsumer()},
	 * then the servers response will be read into a byte array. Then both
	 * the response body consumer, and the callable will be invoked, in
	 * that order. In other words: The creation of the temporary byte array
	 * may pose an impact on the methods performance.
	 * @param <O> Type of the result object, that is returned by the
	 * {@code pCallable}, and then this method.
	 * @param pRequest The request object.
	 * @param pCallable The callable, that is creating the result object.
	 * @return The result object, that has been returned from an invocation
	 *   of the {@code pCallable}.
	 */
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

	/** Called to execute the given {@link RestAccess.Request}. The response
	 * body will be consumed by the configured
	 * {@code RestAccess.Request#getConsumer() consumer}.
	 * @param pRequest The request object.
	 */
	public void send(Request pRequest) {
		send(pRequest, pRequest.getConsumer());
	}

	/** Called to execute the given {@link RestAccess.Request}. The response
	 * body will be consumed by the given @code pReader}.
	 * @param pRequest The request object.
	 * @param pReader A consumer for the response body,
	 *   of the {@code pCallable}.
	 */
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
				String u = url.toExternalForm();
				if (u.endsWith("/")) {
					u = u.substring(0, u.length()-1);
				}
				if (spec.startsWith("/")) {
					url = new URL(u + spec);
				} else {
					url = new URL(u + "/" + spec);
				}
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
				} else {
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

	/** Creates a new {@link RestAccess.Builder}, which will be used for
	 * an HTTP request to the given URL.
	 * @param pUrl The request URL. Typically, this is the URL of the REST
	 * API. Depending on the builders configuration, the
	 * {@link RestAccess.Builder#getResource() REST resource}, and the
	 * {@link RestAccess.Builder#getResourceId() REST resource id} will
	 * be added to build the actual HTTP request URL.
	 * @return The created builder, which is not yet configured, except
	 * for the {@code pUrl}.
	 */
	public Builder builder(URL pUrl) {
		final @NotNull URL url = Objects.requireNonNull(pUrl, "URL");
		return new Builder(url);
	}

	
}
