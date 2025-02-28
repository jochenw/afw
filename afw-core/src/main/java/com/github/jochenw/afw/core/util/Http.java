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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.inject.Inject;

import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.core.function.Functions.FailableBiConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableBiFunction;
import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableSupplier;
import com.github.jochenw.afw.core.util.HttpConnector.HttpConnection;


/** Utility class for working with REST requests. Suggested use:
 * <pre>
 *   final RestAccess restAccess = getComponentFactory().requireInstance(Rest.class);
 *   final RestAccess.Response = rest.send(rest.request()
 *       .method("POST")
 *       .body("{ some json string }")
 *       .resource("/rest/MyResource")
 *       .resourceId(myResourceId)
 *       .basicAuth("MyUsername", "Mypassword")
 *       .parameter("MyParam", "Value")
 *       .send();
 * </pre>
 */
public class Http {
	/** Creates a new instance.
	 */
	public Http() {}

	/** Interface of the response object.
	 */
	public static interface Response {
		/** Returns the HTTP status code.
		 * @return The HTTP status code.
		 */
		int getStatusCode();
		/** Returns the HTTP status message,
		 * @return The HTTP status message,
		 */
		String getStatusMessage();
		/** Returns the value of the header field with the name given by {@code pKey}.
		 * If the header field is specified more than once, returns the last value.
		 * @param pKey Name of the header field, which is being requested.
		 * @return Value of the requested header field, if present, or null.
		 */
		String getHeaderField(String pKey);
		/** Returns all values of the header field with the name given by {@code pKey}.
		 * @param pKey Name of the header field, which is being requested.
		 * @return Values of the requested header field, if present, or null.
		 */
		List<String> getHeaderFields(String pKey);
		/** Returns the response body, or null. For performance reasons, the
		 * body will only available, if no response body consumer has been
		 * specified. To specify a response body consumer, use the 
		 * method {@link Http#send(Request, Functions.FailableBiConsumer)}, or
		 * {@link Http#send(Request, Functions.FailableBiConsumer, Functions.FailableBiConsumer)}.
		 * @return The response body, if available, or null.
		 */
		public byte[] getResponseBody();
		/** Sets the response body, as a byte array. This will only be done,
		 * if no response body consumer has been specified. In other words,
		 * if you are using {@link Http#send(Request)}, or
		 * {@link Http#send(Request, Functions.FailableConsumer)}.
		 * @param pResponseBody The response body.
		 */
		public void setResponseBody(byte[] pResponseBody);
		/** Sets the response body.
		/** Returns the request object, which has been used to create this
		 * response object.
		 * @return The request object, which has been used to create this
		 *   response object.
		 */
		public Request getRequest();
	}
	/** A request object is used to describe the details of the request, which
	 * is being sent to the remote HTTP server.
	 */
	public static class Request {
		private final URL baseUrl;
		private String resource, resourceId;
		private String method, contentType;
		private FailableConsumer<OutputStream,?> body;
		private Map<String,List<String>> headers;
		private Map<String,List<String>> parameters;

		/** Creates a new instance with the given base URL.
		 */
		Request(URL pBaseUrl) {
			baseUrl = pBaseUrl;
		}

		/** Sets the HTTP method, like "POST", or "GET". By
		 * default, "GET" will be used, if no body has been
		 * specified, otherwise "POST".
		 * @param pMethod The HTTP method, which is being used.
		 *   Null is a valid value, and restores the default
		 *   behavior. ("GET", or "POST", depending on the
		 *   presence of a body.)
		 * @return This request object.
		 */
		public Request method(String pMethod) {
			method = pMethod;
			return this;
		}

		/** Returns the base URL.
		 * @return The base URL.
		 */
		public URL getBaseUrl() {
			return baseUrl;
		}

		/** Returns the request body creator, if any, or null.
		 * @return The request body creator, if any, or null.
		 */
		public FailableConsumer<OutputStream,?> getBody() {
			return body;
		}


		/** Sets the REST resource, which will be appended to
		 * the {@link #getBaseUrl() base URL}.
		 * @param pResource The REST resource.
		 * @return This request object.
		 */
		public Request resource(String pResource) {
			resource = pResource;
			return this;
		}

		/** Returns the REST resource, which will be appended to
		 * the {@link #getBaseUrl() base URL}.
		 * @return The REST resource
		 */
		public String getResource() {
			return resource;
		}

		/** Sets the REST resource id, which will be appended to
		 * the {@link #getBaseUrl() base URL}, and the
		 * {@link #getResource()}.
		 * @param pResourceId The REST resource id.
		 * @return This request object.
		 */
		public Request resourceId(String pResourceId) {
			resourceId = pResourceId;
			return this;
		}

		/** Returns the REST resource id, which will be appended to
		 * the {@link #getBaseUrl() base URL}, and the
		 * {@link #getResource()}.
		 * @return The REST resource id, if any, or null.
		 */
		public String getResourceId() {
			return resourceId;
		}

		/** Returns the HTTP method, like "POST", or "GET". By
		 * default the value is null. ("GET" will be used, if
		 * no body has been specified, otherwise "POST".)
		 * @return The HTTP method, which is being used, or
		 * null, if the default behavior is active.
		 * ("GET", or "POST", depending on the
		 *   presence of a body.)
		 */
		public String getMethod() {
			return method;
		}

		/** Specifies the body to be the given string with the
		 * UTF-8 character set.
		 * @param pContent The body contents, as a string.
		 * @return This request object.
		 */
		public Request body(String pContent) {
			return body(pContent, StandardCharsets.UTF_8);
		}
	
		/** Specifies the body to be the given string with the
		 * UTF-8 character set.
		 * @param pContent The body contents, as a string.
		 * @param pCharset The character set, which is being used
		 *     to convert the string into a byte stream.
		 * @return This request object.
		 * @throws NullPointerException Either of the parameters is null.
		 */
		public Request body(String pContent, Charset pCharset) {
			final String content = Objects.requireNonNull(pContent, "Content string");
			final Charset charset = Objects.requireNonNull(pCharset, "Charset");
			body = (os) -> {
				final byte[] bytes = content.getBytes(charset);
				os.write(bytes);
			};
			return this;
		}

		/** Specifies the body to be the contents of the given
		 * reader with the UTF-8 character set.
		 * @param pContent The body contents, as a string.
		 * @return This request object
		 * @throws NullPointerException The parameter is null.
		 */
		public Request body(Reader pContent) {
			return body(pContent, StandardCharsets.UTF_8);
		}

		/** Specifies the body to be the given string with the
		 * UTF-8 character set.
		 * @param pContent The body contents, as a string.
		 * @param pCharset The character set, which is being used
		 *     to convert the reader contents into a byte stream.
		 * @return This request object.
		 * @throws NullPointerException Either of the parameters is null.
		 */
		public Request body(Reader pContent, Charset pCharset) {
			final Reader content = Objects.requireNonNull(pContent, "Content reader");
			final Charset charset = Objects.requireNonNull(pCharset, "Charset");
			body = (os) -> {
				final OutputStreamWriter osw = new OutputStreamWriter(os, charset);
				Streams.copy(content, osw);
				osw.flush();
			};
			return this;
		}

		/** Specifies the body to be the contents of the given
		 * file. Assumes, that no character conversion is necessary.
		 * @param pContent The file with the body contents.
		 * @return This request object.
		 * @throws NullPointerException The parameter is null.
		 */
		public Request body(File pContent) {
			final File content = Objects.requireNonNull(pContent, "Content reader");
			return body((FailableSupplier<InputStream, ?>) () -> new FileInputStream(content));
		}

		/** Specifies the body to be the contents of the given
		 * file. Assumes, that no character conversion is necessary.
		 * @param pContent The file with the body contents.
		 * @return This request object.
		 * @throws NullPointerException The parameter is null.
		 */
		public Request body(Path pContent) {
			final Path content = Objects.requireNonNull(pContent, "Content reader");
			return body((FailableSupplier<InputStream, ?>) () -> Files.newInputStream(content));
		}
	
		/** Specifies the body to be the contents of the given
		 * InputStream. Assumes, that no character conversion is necessary.
		 * @param pContentSupplier The input stream with the body contents.
		 * @return This request object.
		 * @throws NullPointerException The parameter is null.
		 */
		public Request body(FailableSupplier<InputStream,?> pContentSupplier) {
			final FailableSupplier<InputStream,?> contentSupplier = Objects.requireNonNull(pContentSupplier, "Content supplier");
			return body((os) -> {
				final InputStream is = contentSupplier.get();
				if (is == null) {
					final String msg = "The content supplier, that was given to "
							+ getClass().getSimpleName()
							+ ", returned a null InputStream.";
					throw new NullPointerException(msg);
				}
				Streams.copy(is, os);
			});
		}

		/** Specifies the body to be the contents of the given
		 * InputStream. Assumes, that no character conversion is necessary.
		 * @param pContent The input stream with the body contents.
		 * @return This request object.
		 * @throws NullPointerException The parameter is null.
		 */
		public Request body(InputStream pContent) {
			final InputStream content = Objects.requireNonNull(pContent, "Content stream");
			return body(() -> content);
		}

		/** Specifies the body to be written by the given consumer.
		 * @param pConsumer A consumer, which creates the body content by
		 *     writing it to the given {@link OutputStream}.
		 * @return This request object.
		 * @throws NullPointerException The parameter is null.
		 */
		public Request body(FailableConsumer<OutputStream,?> pConsumer) {
			body = Objects.requireNonNull(pConsumer, "Consumer");
			return this;
		}

		/** Adds a header with the given name, and value to the
		 * request object. It is permissible to call this method
		 * more than once with the same header name, in which case
		 * multiple header lines will be generated.
		 * @param pName The header name
		 * @param pValue The header value.
		 * @return This request object.
		 * @throws NullPointerException Either of the parameters is null.
		 */
		public Request header(String pName, String pValue) {
			final String name = Objects.requireNonNull(pName, "Name").toLowerCase();
			final String value = Objects.requireNonNull(pValue, "Value");
			if ("content-type".equalsIgnoreCase(pName)) {
				return contentType(value);
			} else {
				if (headers == null) {
					headers = new HashMap<>();
				}
				headers.computeIfAbsent(name, (n) -> new ArrayList<String>()).add(value);
				return this;
			}
		}

		/** Sets the requests content type.
		 * @param pContentType The requests content type, like
		 *   "application/json", "text/xml", "image/png", etc.
		 *   See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types">
		 *   the Mozilla documentation on the subject</a>
		 *   for details.
		 * @return This request object.
		 * @throws NullPointerException The parameter is null.
		 */
		public Request contentType(String pContentType) {
			contentType = Objects.requireNonNull(pContentType, "Content Type");
			return this;
		}

		/** Returns the requests content type.
		 * @return The requests content type, if any, or null.
		 */
		public String getContentType() {
			return contentType;
		}

		/** Returns an immutable map with the headers, that have been
		 * specified so far, by invoking {@link #header(String, String)}.
		 * @return An immutable map with the headers, that have been
		 *     specified, so far.
		 */
		public Map<String,List<String>> getHeaders() {
			if (headers == null) {
				return Collections.emptyMap();
			} else {
				return Collections.unmodifiableMap(headers);
			}
		}

		/** Adds a parameter with the given name, and value to the
		 * request object. It is permissible to call this method
		 * more than once with the same parameter name, in which case
		 * multiple parameter instances will be generated.
		 * @param pName The parameter name
		 * @param pValue The parameter value.
		 * @return This request object.
		 * @throws NullPointerException Either of the parameters is null.
		 */
		public Request parameter(String pName, String pValue) {
			final String name = Objects.requireNonNull(pName, "Name").toLowerCase();
			final String value = Objects.requireNonNull(pValue, "Value");
			if (parameters == null) {
				parameters = new HashMap<>();
			}
			parameters.computeIfAbsent(name, (n) -> new ArrayList<String>()).add(value);
			return this;
		}

		/** Returns an immutable map with the headers, that have been
		 * specified so far, by invoking {@link #header(String, String)}.
		 * @return An immutable map with the headers, that have been
		 *     specified, so far.
		 */
		public Map<String,List<String>> getParameters() {
			if (parameters == null) {
				return Collections.emptyMap();
			} else {
				return Collections.unmodifiableMap(parameters);
			}
		}

		/** Adds a basic authentication header by invoking
		 * {@link #header(String, String)} with suitable
		 * values to the HTTP request. (Preemptive authentication)
		 * <em>Note:</em> The UTF-8 character set is used for
		 * conversion of user id, and password into bytes.
		 * @param pUserId The user id, that is being given to the
		 *   remote HTTP server for authentication.
		 * @param pPassword The password, that is being given to the
		 *   remote HTTP server for authentication.
		 * @return This request object.
		 * @throws NullPointerException Either of the parameters is null.
		 */
		public Request basicAuth(String pUserId, String pPassword) {
			return basicAuth(pUserId, pPassword, StandardCharsets.UTF_8);
		}
	
		/** Adds a basic authentication header by invoking
		 * {@link #header(String, String)} with suitable
		 * values to the HTTP request. (Preemptive authentication)
		 * @param pUserId The user id, that is being given to the
		 *   remote HTTP server for authentication.
		 * @param pPassword The password, that is being given to the
		 *   remote HTTP server for authentication.
		 * @param pCharset The character set, which is being used
		 *   for conversion of user id, and password into bytes.
		 * @return This request object.
		 * @throws NullPointerException Either of the parameters is null.
		 */
		public Request basicAuth(String pUserId, String pPassword, Charset pCharset) {
			final String userId = Objects.requireNonNull(pUserId, "UserId");
			final String password = Objects.requireNonNull(pPassword, "Password");
			final Charset charset = Objects.requireNonNull(pCharset, "Charset");
			final String authStr = userId + ":" + password;
			final byte[] authBytes = authStr.getBytes(charset);
			final Base64.Encoder base64Encoder = Base64.getMimeEncoder(0, new byte[] {(byte) 10});
			final String authBase64Str = base64Encoder.encodeToString(authBytes);
			return header("Authorization", "Basic " + authBase64Str);
		}
	}

	private @Inject HttpConnector httpConnector;

	/** Creates a new request object with the given base URL.
	 * @param pBaseUrl The {@link Request#getBaseUrl() base URL} of
	 *   the new request object.
	 * @return A new request object with the given base URL.
	 * @throws NullPointerException The parameter is null.
	 */
	public Request request(URL pBaseUrl) {
		final URL url = Objects.requireNonNull(pBaseUrl, "Base URL");
		return new Request(url);
	}

	/** Creates a new request object with the given base URL.
	 * @param pBaseUrl The {@link Request#getBaseUrl() base URL} of
	 *   the new request object.
	 * @return A new request object with the given base URL.
	 * @throws NullPointerException The parameter is null.
	 */
	public Request request(String pBaseUrl) {
		final String urlStr = Objects.requireNonNull(pBaseUrl, "Base URL");
		final URL url;
		try {
			url = Strings.asUrl(urlStr);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Invalid base URL: " + urlStr, e);
		}
		return request(url);
	}

	/** Sends the HTTP request. If a successful response is returned, returns
	 * the response object, with the HTTP response body.
	 * @param pRequest The request object.
	 * @return The response object, in case of success. Otherwise,
	 *   the Exception is being thrown.
	 * @throws NullPointerException The parameter is null.
	 */
	public Response send(Request pRequest) {
		Objects.requireNonNull(pRequest, "Request");
		final Holder<Response> holder = new Holder<>();
		send(pRequest, holder::set);
		return holder.get();
	}

	/** Sends the HTTP request. If a successful response is returned, invokes
	 * the {@code pResponseConsumer}. Otherwise, throws an Exception.
	 * @param pRequest The request configuration.
	 * @param pResponseConsumer A consumer, which will be invoked in case of a
	 *   successful response. The consumers parameter is the response object,
	 *   with the HTTP servers response body. The response consumer may be
	 *   null, in which case the servers response will be ignored.
     * @throws NullPointerException The request parameter is null.
	 */
	public void send(Request pRequest,
			         FailableConsumer<Response,?> pResponseConsumer) {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		send(pRequest, (res, is) -> {
			Streams.copy(is, baos);
			res.setResponseBody(baos.toByteArray());
			if (pResponseConsumer != null) {
				pResponseConsumer.accept(res);
			}
		});
	}

	/** Sends the HTTP request. If a successful response is returned, invokes
	 * the {@code pResponseConsumer}. Otherwise, throws an Exception.
	 * @param pRequest The request configuration.
	 * @param pResponseConsumer A consumer, which will be invoked in case of a
	 *   successful response. The consumers parameters are the response object,
	 *   and an {@link InputStream} with the HTTP servers response body. The
	 *   response consumer may be null, in which case the servers response will
	 *   be ignored.
	 * @throws NullPointerException The request parameter is null.
	 */
	public void send(Request pRequest, FailableBiConsumer<Response,InputStream,?> pResponseConsumer) {
		send(pRequest, pResponseConsumer, null);
	}

	/** Sends the HTTP request. If a successful response is returned, invokes
	 * the {@code pResponseConsumer}. Otherwise, invokes the
	 * {@code pErrorResponseConsumer}.
	 * @param pRequest The request configuration.
	 * @param pResponseConsumer A consumer, which will be invoked in case of a
	 *   successful response. The consumers parameters are the response object,
	 *   and an {@link InputStream} with the HTTP servers response body. The
	 *   response consumer may be null, in which case the servers response will
	 *   be ignored.
	 * @param pErrorResponseConsumer A consumer, which will be invoked in case of an
	 *   error response. The consumers parameters are the response object,
	 *   and an {@link InputStream} with the HTTP servers response body. The
	 *   error consumer may be null, in which case the servers response will
	 *   be ignored. Instead, an Exception will be throws.
	 * @throws NullPointerException The request parameter is null.
	 */
	public void send(Request pRequest,
			         FailableBiConsumer<Response,InputStream,?> pResponseConsumer,
			         FailableBiConsumer<Response,InputStream,?> pErrorResponseConsumer) {
		final FailableBiFunction<Response,InputStream,Object,?> responseCallable =
				(res, in) -> {
					if (pResponseConsumer != null) {
						pResponseConsumer.accept(res, in);
					}
					return null;
				};
		call(pRequest, responseCallable, pErrorResponseConsumer);
	}

	/** Sends the HTTP request. If a successful response is returned, invokes
	 * the {@code pResponseConsumer}, and returns the response consumers
	 * output object. Otherwise, invokes the {@code pErrorResponseConsumer},
	 * and returns null.
	 * @param <O> Type of the output object.
	 * @param pRequest The request configuration.
	 * @param pResponseConsumer A consumer, which will be invoked in case of a
	 *   successful response. The consumers parameters are the response object,
	 *   and an {@link InputStream} with the HTTP servers response body. The
	 *   response consumer may be null, in which case the servers response will
	 *   be ignored.
	 * @param pErrorResponseConsumer A consumer, which will be invoked in case of an
	 *   error response. The consumers parameters are the response object,
	 *   and an {@link InputStream} with the HTTP servers response body. The
	 *   error consumer may be null, in which case the servers response will
	 *   be ignored. Instead, an Exception will be thrown.
	 * @return The output object, that was returned by the response consumer in
	 *   case of success. Null, if the request wasn't successful, and the error
	 *   response consumer was invoked. If you need to distinguish between the
	 *   case, that the response consumer has returned null, and a failure, use
	 *   an error response consumer, that throws an exception.
	 * @throws NullPointerException The request parameter is null.
	 */
	public <O> O call(Request pRequest,
			          FailableBiFunction<Response,InputStream,O,?> pResponseConsumer,
			          FailableBiConsumer<Response,InputStream,?> pErrorResponseConsumer) {
		Objects.requireNonNull(pRequest, "Request");
		return executeRequest(pRequest, prepareRequest(pRequest), pResponseConsumer,
				pErrorResponseConsumer);
	}


	/** This object provides the data, that has been assembled by
	 * {@link #prepareRequest(Request)}, and is being used by
	 * {@link #executeRequest(Request, RequestData, Functions.FailableBiFunction,
	 *                        Functions.FailableBiConsumer)}.
	 */
	protected static class RequestData {
		private final URL url;
		private final FailableConsumer<OutputStream,?> body;
		private final String method, contentType;
		private final boolean hasRequestBody;
		/** Creates a new instance with all the collected data.
		 * @param pUrl The actual URL, possibly including parameters.
		 * @param pBody The request body creator, if any, or null.
		 * @param pMethod The request method. 
		 * @param pContentType The content type, if any, or null.
		 */
		public RequestData(URL pUrl, FailableConsumer<OutputStream, ?> pBody,
				           String pMethod, String pContentType) {
			url = pUrl;
			body = pBody;
			method = pMethod;
			contentType = pContentType;
			hasRequestBody = pBody != null;
		}
		/** Returns the URL, which is actually being invoked.
		 * Request parameters might be included in the URL, if they
		 * have been configured in the request configuration object.
		 * @return The actual request URL, possibly including request parameters.
		 */
		public URL getUrl() {
			return url;
		}
		/** Returns the request body creator, if any, or null.
		 * @return The request body creator, or null.
		 */
		public FailableConsumer<OutputStream, ?> getBody() {
			return body;
		}
		/** Returns the requests content type, if any, or null.
		 * @return the requests content type, if any, or null.
		 */
		public String getContentType() {
			return contentType;
		}
		/** Returns the request method, never null.
		 * @return the request method, never null.
		 */
		public String getMethod() {
			return method;
		}
		/** Returns true, if a request body is available,
		 * Otherwise, returns false.
		 * @return True, if a request body is available, otherwise
		 *   false.
		 */
		public boolean hasRequestBody() {
			return hasRequestBody;
		}
	}

	/** Actual execution of the HTTP request, using the essential
	 * data, that has been collected by {@link #prepareRequest(Request)}.
	 * @param pRequest The request configuration object.
	 * @param pRequestData The essential request data, that has been prepared.
	 * @param pResponseConsumer A consumer, which will be invoked in case of a
	 *   successful response. The consumers parameters are the response object,
	 *   and an {@link InputStream} with the HTTP servers response body. The
	 *   response consumer may be null, in which case the servers response will
	 *   be ignored.
	 * @param pErrorResponseConsumer A consumer, which will be invoked in case of an
	 *   error response. The consumers parameters are the response object,
	 *   and an {@link InputStream} with the HTTP servers response body. The
	 *   error consumer may be null, in which case the servers response will
	 *   be ignored. Instead, an Exception will be thrown.
	 * @param <O> Type of the output object.
	 * @return The output object, that was returned by the response consumer in
	 *   case of success. Null, if the request wasn't successful, and the error
	 *   response consumer was invoked. If you need to distinguish between the
	 *   case, that the response consumer has returned null, and a failure, use
	 *   an error response consumer, that throws an exception.
	 */
	@SuppressWarnings("null")
	protected <O> O executeRequest(Request pRequest, RequestData pRequestData,
			  FailableBiFunction<Response,InputStream,O,?> pResponseConsumer,
	          FailableBiConsumer<Response,InputStream,?> pErrorResponseConsumer) {
		try (HttpConnection hConn = httpConnector.connect(pRequestData.getUrl())) {
			HttpURLConnection conn = hConn.getUrlConnection();
			conn.setRequestMethod(pRequestData.getMethod());
			if (pRequestData.getContentType() != null) {
				conn.setRequestProperty("content-type", pRequestData.getContentType());
			}
			final Map<String,List<String>> headers = pRequest.getHeaders();
			if (headers != null) {
				headers.forEach((k,l) -> l.forEach((v) -> conn.addRequestProperty(k, v)));
			}
			
			if (pRequestData.getBody() != null) {
				conn.setDoOutput(true);
			}
			conn.setDoInput(pResponseConsumer != null);
			int statusCode;
			String statusMessage;
			boolean useErrorResponse;
			try {
				statusCode = conn.getResponseCode();
				statusMessage = conn.getResponseMessage();
				useErrorResponse = false;
			} catch (IOException e) {
				statusCode = 9999;
				statusMessage = "IOException; " + e.getMessage();
				useErrorResponse = true;
			}
			if (useErrorResponse) {
				if (pErrorResponseConsumer != null) {
					try (InputStream es = conn.getErrorStream()) {
						final Http.Response response =
								newResponse(pRequest, statusCode, statusMessage,
											conn, null);
						pErrorResponseConsumer.accept(response, es);
					}
				}
				return null;
			} else {
				if (pResponseConsumer != null) {
					try (InputStream is = conn.getInputStream()) {
						final Http.Response response =
								newResponse(pRequest, statusCode, statusMessage,
											conn, null);
						return pResponseConsumer.apply(response, is);
					}
				} else {
					return null;
				}
			}
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}
	/** Prepares the HTTP request by collecting the essential data,
	 * and returning it as an {@link RequestData} object.
	 * @param pRequest The request configuration object.
	 * @return The essential data object.
	 */
	protected RequestData prepareRequest(Request pRequest) {
		final URL url;
		FailableConsumer<OutputStream,?> body = pRequest.getBody();
		String contentType = pRequest.getContentType();
		final boolean hasRequestBody = pRequest.getBody() != null;
		final String method = Objects.notNull(pRequest.getMethod(), (FailableSupplier<String, ?>) () -> {
			if (hasRequestBody) {
				return "POST";
			} else {
				return "GET";
			}
		});
		try {
			URL tmpUrl = getUrl(pRequest);
			Map<String,List<String>> parameters = pRequest.getParameters();
			if (parameters != null  &&  !parameters.isEmpty()) {
				boolean useBodyForParameters;
				if ("GET".equalsIgnoreCase(method)) {
					useBodyForParameters = false;
				} else if (body == null) {
					useBodyForParameters = true;
				} else {
					useBodyForParameters = false;
				}
				if (useBodyForParameters) {
					final String formUrlEncodedContentType = "application/x-www-form-urlencoded; charset=UTF-8";
					if (contentType == null) {
						contentType = formUrlEncodedContentType;
					} else if (formUrlEncodedContentType.equalsIgnoreCase(contentType)) {
						// Okay, as it is.
					} else if ("application/x-www-form-urlencoded".equalsIgnoreCase(contentType)) {
						// Okay to modify the content type by adding the character set.
						contentType = formUrlEncodedContentType;
					} else {
						final String msg = "The request object specifies parameters,"
								+ " but I cannot set the content type to"
								+ " application/x-www-form-urlencoded, because"
								+ " it was specified as " + contentType;
						throw new IllegalStateException(msg);
					}
				} else {
					final StringBuilder sb = new StringBuilder(tmpUrl.toExternalForm());
					final String utf8Enc = StandardCharsets.UTF_8.name();
					final Function<String,String> urlEncoder = (s) -> {
						try {
							return URLEncoder.encode(s, utf8Enc);
						} catch (UnsupportedEncodingException e) {
							throw Exceptions.show(e);
						}
					};
					parameters.forEach((k,l) -> l.forEach((v) -> {
						if (sb.indexOf("?") == -1) {
							sb.append("?");
						} else {
							sb.append("&");
						}
						sb.append(urlEncoder.apply(k));
						sb.append("=");
						sb.append(urlEncoder.apply(v));
					}));
					try {
						tmpUrl = new URL(sb.toString());
					} catch (MalformedURLException e) {
						throw new IllegalStateException("Remote URL is invalid after adding parameters: " + sb);
					}
				}
			}
			url = tmpUrl;
		} catch (IOException e) {
			throw Exceptions.show(e);
		}
		return new RequestData(url, body, method, contentType);
	}

	/** Creates a new response object.
	 * @param pRequest The Request object.
	 * @param pStatusCode The requests status code, if available, or
	 *   the number 9999 (no status code available).
	 * @param pStatusMessage The requests status message, if available,
	 *   or an error message.
	 * @param pConn The open HTTP connection object.
	 * @param pBodyContents The HTTP response body, as a byte array.
	 * @return The created response object.
	 */
	public Response newResponse(Request pRequest, int pStatusCode, String pStatusMessage,
			                    HttpURLConnection pConn, byte[] pBodyContents) {
		String statusMessage;
		int statusCode;
		try {
			statusCode = pConn.getResponseCode();
			statusMessage = pConn.getResponseMessage();
		} catch (IOException e) {
			statusCode = 500;
			statusMessage = "IOException: " + e.getMessage();
		}
		final int stCode = statusCode;
		final String stMessage = statusMessage;
		return new Response() {
			private byte[] responseBody;

			@Override
			public int getStatusCode() {
				return stCode;
			}
			@Override
			public String getStatusMessage() {
				return stMessage;
			}
			@Override
			public String getHeaderField(String pKey) {
				return pConn.getHeaderField(pKey);
			}

			@Override
			public List<String> getHeaderFields(String pKey) {
				return pConn.getHeaderFields().get(pKey);
			}

			@Override
			public byte[] getResponseBody() {
				if (responseBody == null) {
					throw new IllegalStateException("Use the supplied InputStream"
							+ " to read the HTTP response body. This method is only"
							+ " available, if you didn't specify a response consumer.");
				} else {
					return responseBody;
				}
			}
			
			@Override
			public void setResponseBody(byte[] pResponseBody) {
				responseBody = pResponseBody;
			}

			@Override
			public Request getRequest() {
				return pRequest;
			}
			
		};
	}
	
	/** Assemble the actual request URL by combining
	 * {@link Request#getBaseUrl() base Url}, {@link Request#getResource() resource},
	 * and {@link Request#getResourceId() resource id}. If you need complete control
	 * over the actual URL, set the base Url, and leave resource, and resource id
	 * to be null.
	 * @param pRequest The request object, which provides base Url, resource,
	 *   and resource id.
	 * @return The URL, to which the request is actually being sent. If resource,
	 * @throws MalformedURLException The assembly resulted in an invalid URL.
	 */
	protected URL getUrl(Request pRequest) throws MalformedURLException {
		URL url = pRequest.getBaseUrl();
		final String resource = pRequest.getResource();
		if (resource != null) {
			url = new URL(url, resource);
		}
		final String resourceId = pRequest.getResourceId();
		if (resourceId != null) {
			url = new URL(url, resourceId);
		}
		return url;
	}

	/** Sets the {@link HttpConnector}, which is being used
	 * to open HTTP connections.
	 * @param pConnector The Http connector, which is being used.
	 * @throws NullPointerException The parameter is null.
	 */
	public void setHttpConnector(HttpConnector pConnector) {
		httpConnector = Objects.requireNonNull(pConnector, "HttpConnector");
	}

	/** Returns the {@link HttpConnector}, which is being used
	 * to open HTTP connections.
	 * @return The Http connector, which is being used.
	 */
	public HttpConnector getHttpConnector() {
		return httpConnector;
	}
}
