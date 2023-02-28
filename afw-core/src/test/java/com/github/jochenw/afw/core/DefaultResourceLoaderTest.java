/**
 * 
 */
package com.github.jochenw.afw.core;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Streams;

/**
 * Test case for the {@link DefaultResourceLoader}.
 */
public class DefaultResourceLoaderTest {
	private URL URL0, URL1;

	/** Called to initialize the URL fields, before the tests are actually running.
	 * @throws Exception An error occurred.
	 */
	@Before
	public void initUrls() throws Exception {
		final URLStreamHandler ush = new URLStreamHandler() {
			@Override
			protected URLConnection openConnection(URL pUrl) throws IOException {
				return new URLConnection(pUrl) {
					@Override
					public void connect() throws IOException {
						// Do nothing.
					}

					@Override
					public InputStream getInputStream() throws IOException {
						final String content;
						if ("x/y/Foo.properties".equals(pUrl.getFile())) {
							content = "12345";
						} else if ("prod/x/y/Bar.properties".equals(pUrl.getFile())) {
							content = "012345";
						} else {
							content = null;
						}
						if (content == null) {
							throw new FileNotFoundException("File not found: " + pUrl.getFile());
						} else {
							final byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
							return new ByteArrayInputStream(bytes);
						}
					}
				};
			}
		};
		URL0 = new URL("test", "", 0, "x/y/Foo.properties", ush);
		URL1 = new URL("test", "", 0, "prod/x/y/Bar.properties", ush);
	}

	/** Test case for {@link DefaultResourceLoader#getResource(ClassLoader, String)}
	 */
	@Test
	public void testExplicitClassLoader() {
		final ClassLoader cl = newClassLoader();
		final DefaultResourceLoader drl = new DefaultResourceLoader("foo", "prod");
		assertResource(drl, cl, URL0.getFile(), "12345");
		drl.setInstanceName(null);
		assertNoResource(drl, cl, "x/y/Bar.properties");
		drl.setInstanceName("");
		assertNoResource(drl, cl, "x/y/Bar.properties");
		drl.setInstanceName("prod");
		assertResource(drl, cl, "x/y/Bar.properties", "012345");
		drl.setInstanceName(null);
		drl.setResourcePrefix("prod");
		assertResource(drl, cl, "x/y/Bar.properties", "012345");
		drl.setResourcePrefix("prod/");
		assertResource(drl, cl, "x/y/Bar.properties", "012345");
		
	}

	/** Test case for {@link DefaultResourceLoader#getResource(String)}
	 */
	@Test
	public void testImplicitClassLoader() {
		final ClassLoader cl = newClassLoader();
		ClassLoader tcl = null;
		try {
			tcl = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(cl);
			final DefaultResourceLoader drl = new DefaultResourceLoader();
			assertResource(drl, null, URL0.getFile(), "12345");
			drl.setInstanceName(null);
			assertNoResource(drl, null, "x/y/Bar.properties");
			drl.setInstanceName("");
			assertNoResource(drl, null, "x/y/Bar.properties");
			drl.setInstanceName("prod");
			assertResource(drl, null, "x/y/Bar.properties", "012345");
			drl.setInstanceName(null);
			drl.setResourcePrefix("prod");
			assertResource(drl, cl, "x/y/Bar.properties", "012345");
			drl.setResourcePrefix("prod/");
			assertResource(drl, cl, "x/y/Bar.properties", "012345");
		} finally {
			Thread.currentThread().setContextClassLoader(tcl);
		}
	}

	/** Creates a special test {@link ClassLoader}.
	 * @return The created {@link ClassLoader}.
	 */
	protected ClassLoader newClassLoader() {
		final ClassLoader cl = new ClassLoader() {
			@Override
			public URL getResource(String pName) {
				if (URL0.getFile().equals(pName)) {
					return URL0;
				} else if (URL1.getFile().equals(pName)) {
					return URL1;
				}
				return null;
			}
		};
		return cl;
	}

	/** Asserts, that a specific resource can be located using the given {@link DefaultResourceLoader},
	 * or the given {@link ClassLoader}.
	 * @param pLoader The {@link DefaultResourceLoader}, that is being tested.
	 * @param pCl The {@link ClassLoader}, that is backing the {@code pLoader}.
	 * @param pUri The URI, that is supposed to be locatable.
	 * @param pContent The expected resource contents.
	 */
	protected void assertResource(DefaultResourceLoader pLoader, ClassLoader pCl, String pUri, String pContent) {
		final URL url;
		if (pCl == null) {
			url = pLoader.getResource(pUri);
		} else {
			url = pLoader.getResource(pCl, pUri);
		}
		assertNotNull(url);
		final String got;
		try (InputStream in = url.openStream()) {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Streams.copy(in, baos);
			got = baos.toString(StandardCharsets.UTF_8.name());
		} catch (IOException e) {
			throw Exceptions.show(e);
		}
		assertEquals(pContent, got);
	}

	/** Asserts, that a specific resource can <em>not</em> be located using
	 * the given {@link DefaultResourceLoader},
	 * or the given {@link ClassLoader}.
	 * @param pLoader The {@link DefaultResourceLoader}, that is being tested.
	 * @param pCl The {@link ClassLoader}, that is backing the {@code pLoader}.
	 * @param pUri The URI, that is supposed not to be locatable.
	 */
	protected void assertNoResource(DefaultResourceLoader pLoader, ClassLoader pCl, String pUri) {
		final URL url;
		if (pCl == null) {
			url = pLoader.getResource(pUri);
		} else {
			url = pLoader.getResource(pCl, pUri);
		}
		assertNull(url);
	}
}
