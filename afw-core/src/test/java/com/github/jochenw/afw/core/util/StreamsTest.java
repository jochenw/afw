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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import org.jspecify.annotations.NonNull;
import org.junit.Assert;
import org.junit.Test;

import com.github.jochenw.afw.core.function.Functions.FailableBiConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.io.IReadable;
import com.github.jochenw.afw.core.io.ObservableInputStream.Listener;
import com.github.jochenw.afw.core.util.tests.Tests;

import junit.framework.AssertionFailedError;


/** Test case for the {@link Streams} class.
 */
public class StreamsTest {
	/**
	 * A version of {@link ByteArrayInputStream}, which allows to query,
	 * whether it is {@link #isClosed() closed}.
	 */
    public static class CloseableInputStream extends ByteArrayInputStream {
        private final byte[] contents;
        private boolean closed;

        private CloseableInputStream(byte[] pContents) {
            super(pContents);
            contents = pContents;
        }

        /**
         * Returns, whether the stream has been closed.
         * @return True, if this stream has already been closed.
         */
        public boolean isClosed() {
            return closed;
        }

        /**
         * Returns, whether this streams content as a single byte array.
         * @return The streams content as a single byte array
         */
        public byte[] getContents() {
            return contents;
        }

        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }

        /**
         * Creates a new instance with the given content.
         * @param pContents The content, which is being returned by this stream.
         *   The string will be converted into a byte stream using the {@code UTF-8}
         *   character set.
         * @return A new instance, which will return the given content upon reading.
         */
        public static CloseableInputStream newInstance(String pContents) {
            try {
                final byte[] contents = pContents.getBytes("UTF-8");
                return new CloseableInputStream(contents);
            } catch (UnsupportedEncodingException e) {
                throw Exceptions.newUncheckedIOException(e);
            }
        }
    }

    /**
	 * A version of {@link StringReader}, which allows to query,
	 * whether it is {@link #isClosed() closed}.
	 */
    public static class CloseableReader extends StringReader {
        private final String contents;
        private boolean closed;

        /**
         * Creates a new instance with the given content.
         * @param pContents The content, which is being returned by this stream.
         *   The string will be converted into a character stream.
         */
        public CloseableReader(String pContents) {
            super(pContents);
            contents = pContents;
        }
        
        /**
         * Returns, whether the reader has been closed.
         * @return True, if this reader has already been closed.
         */
        public boolean isClosed() {
            return closed;
        }

        /**
         * Returns, whether this streams content as a single string.
         * @return The streams content as a single string
         */
        public String getContents() {
            return contents;
        }

        @Override
        public void close() {
            closed = true;
            super.close();
        }
    }

    /**
     * Test for {@link Streams#read(java.io.InputStream)}.
     */
    @Test
    public void testReadInputStream() {
        final CloseableInputStream cis = CloseableInputStream.newInstance("SomeString");
        Assert.assertFalse(cis.isClosed());
        final byte[] bytes = Streams.read(cis);
        Assert.assertTrue(cis.isClosed());
        Assert.assertArrayEquals(cis.getContents(), bytes);
    }

    /**
     * Test for {@link Streams#copy(java.io.InputStream, java.io.OutputStream)}.
     */
    @Test
    public void testCopyInputStreamOutputStream() {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final CloseableInputStream cis = CloseableInputStream.newInstance("SomeString");
        Assert.assertFalse(cis.isClosed());
        Streams.copy(cis, baos);
        Assert.assertFalse(cis.isClosed());
        Assert.assertArrayEquals(cis.getContents(), baos.toByteArray());
    }

    /**
     * Test for {@link Streams#copy(java.io.InputStream, java.io.Writer, java.nio.charset.Charset)}.
     * @throws Exception The test failed.
     */
    @Test
    public void testCopyInputStreamWriterCharset() throws Exception {
    	final String snippet = "This is a text, which contains ascii characters, and non-ascii characters: "
				   + "\u00c4\u00e4\u00d6\u000f6\u00dc\u00fc\u00df.\n";
    	final StringWriter sw = new StringWriter();
    	Streams.copy(new ByteArrayInputStream(snippet.getBytes(StandardCharsets.UTF_8)), sw,
    			     StandardCharsets.UTF_8);
    	assertEquals(snippet, sw.toString());
    }

    /**
     * Test for {@link Streams#copy(java.io.InputStream, java.io.OutputStream, int)}.
     */
    @Test
    public void testCopyInputStreamOutputStreamInt() {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final CloseableInputStream cis = CloseableInputStream.newInstance("SomeString");
        Assert.assertFalse(cis.isClosed());
        Streams.copy(cis, baos, 2);
        Assert.assertFalse(cis.isClosed());
        Assert.assertArrayEquals(cis.getContents(), baos.toByteArray());
    }

    /**
     * Test for {@link Streams#read(java.io.Reader)}.
     */
    @Test
    public void testReadReader() {
        final CloseableReader cr = new CloseableReader("Some String");
        Assert.assertFalse(cr.isClosed());
        final String got = Streams.read(cr);
        Assert.assertTrue(cr.isClosed());
        Assert.assertEquals(cr.getContents(), got);
    }

    /**
     * Test for {@link Streams#copy(java.io.Reader, java.io.Writer)}.
     */
    @Test
    public void testCopyReaderWriter() {
        final StringWriter sw = new StringWriter();
        final CloseableReader cr = new CloseableReader("SomeString");
        Assert.assertFalse(cr.isClosed());
        Streams.copy(cr, sw);
        Assert.assertFalse(cr.isClosed());
        Assert.assertEquals(cr.getContents(), sw.toString());
    }

    /**
     * Test for {@link Streams#copy(java.io.Reader, java.io.Writer, int)}.
     */
    @Test
    public void testCopyReaderWriterInt() {
        final StringWriter sw = new StringWriter();
        final CloseableReader cr = new CloseableReader("SomeString");
        Assert.assertFalse(cr.isClosed());
        Streams.copy(cr, sw, 2);
        Assert.assertFalse(cr.isClosed());
        Assert.assertEquals(cr.getContents(), sw.toString());
    }

    /**
     * Test for {@link Streams#uncloseableStream(java.io.InputStream)}.
     */
    @Test
    public void testUncloseableStream() {
        final CloseableInputStream cis = CloseableInputStream.newInstance("SomeString");
        Assert.assertFalse(cis.isClosed());
        final byte[] bytes = Streams.read(Streams.uncloseableStream(cis));
        Assert.assertFalse(cis.isClosed());
        Assert.assertArrayEquals(cis.getContents(), bytes);
    }

    /**
     * Test for {@link Streams#accept(java.net.URL, com.github.jochenw.afw.core.function.Functions.FailableConsumer)}.
     * @throws Exception The test failed.
     */
    @Test
    public void testAcceptUrlConsumer() throws Exception {
    	final byte[] content = "MagicByteStream".getBytes(StandardCharsets.UTF_16);
       	final MutableBoolean opened = new MutableBoolean();
       	final MutableBoolean closed = new MutableBoolean();
        final URLStreamHandler ush = new URLStreamHandler() {
			@Override
			protected URLConnection openConnection(URL pUrl) throws IOException {
				assertEquals("test", pUrl.getProtocol());
				assertEquals("some.unknown.host", pUrl.getHost());
				assertEquals("some/file", pUrl.getFile());
				assertEquals(2345, pUrl.getPort());
				opened.set();
				return new URLConnection(pUrl) {					
					@Override
					public void connect() throws IOException {
						throw new NotImplementedException();
					}

					@Override
					public InputStream getInputStream() throws IOException {
						return new FilterInputStream(new ByteArrayInputStream(content)) {
							@Override
							public void close() throws IOException {
								closed.set();
								super.close();
							}
						};
					}
				};

			}
		};
		final Holder<byte[]> contentHolder = new Holder<byte[]>();
    	final URL url = new URL("test", "some.unknown.host", 2345, "some/file", ush);
    	Streams.accept(url, (in) -> {
    		final byte[] bytes = Streams.read(in);
    		contentHolder.set(bytes);
    	});
    	assertTrue(opened.isSet());
    	assertTrue(closed.isSet());
    	Tests.assertEquals(content, contentHolder.get());
    }

    /** Test for {@link Streams#accept(Path, FailableConsumer)}.
     */
    @Test
    public void testAcceptPathConsumer() {
    	@SuppressWarnings("null")
		final @NonNull Path path = Paths.get("pom.xml");
    	assertTrue(Files.isRegularFile(path));
    	Streams.accept(path, (in)-> Tests.assertSameContent(path, in));
    }

    /** Test for {@link Streams#accept(File, FailableConsumer)}.
     */
    @Test
    public void testAcceptFileConsumer() {
    	final File file = new File("pom.xml");
    	assertTrue(file.isFile());
    	Streams.accept(file, (in) -> Tests.assertSameContent(file.toPath(), in));
    }

    /** Test for {@link Streams#load(URL)}.
     * @throws Exception The test failed.
     */
    @Test
    public void testLoadUrl() throws Exception {
    	final Properties props = newTestProperties();
		final @NonNull Path path = createTestProperties(props, false);
    	final Properties got1 = Streams.load(toUrl(path));
    	assertSameProperties(props, got1);
    	final Properties got2 = Streams.load(toUrl(path), false);
    	assertSameProperties(props, got2);
    	final Path path2 = path.getParent().resolve("thisFileDoesntExist");
    	assertNull(Streams.load(toUrl(path2), true));
    	final Path xmlPath = createTestProperties(props, true);
    	final Properties xmlGot = Streams.load(toUrl(xmlPath));
    	assertSameProperties(props, xmlGot);
    }

	private @NonNull URL toUrl(final Path path) throws MalformedURLException {
		return Objects.requireNonNull(path.toUri().toURL());
	}

    /** Test for {@link Streams#load(Path)}.
     */
    @Test
    public void testLoadPath() {
    	final Properties props = newTestProperties();
    	final @NonNull Path path = createTestProperties(props, false);
    	final Properties got1 = Streams.load(path);
    	assertSameProperties(props, got1);
    	final Properties got2 = Streams.load(path, false);
    	assertSameProperties(props, got2);
    	final Properties got3 = Streams.load(false, new @NonNull Object[] {path});
    	assertSameProperties(props, got3);
    	final @NonNull Path path2 = Objects.requireNonNull(path.getParent().resolve("thisFileDoesntExist"));
    	assertNull(Streams.load(path2, true));
    	final Properties got4 = Streams.load(true, new @NonNull Object[] {path2}); 
    	assertNotNull(got4);
    	assertTrue(got4.isEmpty());
    	final Path xmlPath = createTestProperties(props, true);
    	final Properties xmlGot = Streams.load(xmlPath);
    	assertSameProperties(props, xmlGot);
    }

    /** Test for {@link Streams#load(File)}.
     */
    @Test
    public void testLoadFile() {
    	final Properties props = newTestProperties();
    	@SuppressWarnings("null")
		final @NonNull File file = createTestProperties(props, false).toFile();
    	final Properties got1 = Streams.load(file);
    	assertSameProperties(props, got1);
    	final Properties got2 = Streams.load(file, false);
    	assertSameProperties(props, got2);
    	final Properties got3 = Streams.load(false, new @NonNull Object[] {file});
    	assertSameProperties(props, got3);
    	final File file2 = new File(file.getParentFile(), "thisFileDoesntExist");
    	assertNull(Streams.load(file2, true));
    	final Properties got4 = Streams.load(true, new @NonNull Object[] {file2});
		assertNotNull(got4);
		assertTrue(got4.isEmpty());
    	final @NonNull Path xmlPath = createTestProperties(props, true);
    	final Properties xmlGot = Streams.load(Objects.requireNonNull(xmlPath.toFile()));
    	assertSameProperties(props, xmlGot);
    }

    /** Creates a basic set of properties for use in the
     * test cases.
     * @return A basic set of properties for use in the
     *   test cases.
     */
    protected Properties newTestProperties() {
    	final Properties props = new Properties();
    	props.put("answer", "42");
    	props.put("whatever", "works");
    	props.put("time", String.valueOf(System.currentTimeMillis()));
    	return props;
    }

    /**
     * Creates a basic property file for use in the test cases.
     * @param pProps The property set, that is being stored in the
     *   property file.
     * @param pXml True, if the created property file should have
     *   XML format. False for the basic property file.
     * @return Path of the created property file.
     */
    protected @NonNull Path createTestProperties(Properties pProps, boolean pXml) {
    	final Path testDir = Paths.get("target");
    	final @NonNull Path testFile;
    	if (pXml) {
    		testFile = Objects.requireNonNull(testDir.resolve("test-created.properties.xml"));
    	} else {
    		testFile = Objects.requireNonNull(testDir.resolve("test-created.properties"));
    	}
    	try {
    		Files.deleteIfExists(testFile);
    		Files.createDirectories(testDir);
    		try (OutputStream out = Files.newOutputStream(testFile)) {
    			if (pXml) {
    				pProps.storeToXML(out, null);
    			} else {
    				pProps.store(out, null);
    			}
    		}
    	} catch (IOException e) {
    		throw Exceptions.show(e);
    	}
    	return testFile;
    }

    /** Validates, that a property set has the expected content, by
     * comparing it with an expected property set.
     * @param pExpect The expected property set.
     * @param pGot The actual property set.
     * @throws AssertionFailedError The validation failed.
     */
    protected void assertSameProperties(Properties pExpect, Properties pGot) {
    	assertEquals(pExpect.size(), pGot.size());
    	for (Map.Entry<Object,Object> en : pExpect.entrySet()) {
    		final String key = (String) en.getKey();
    		final String value = (String) en.getValue();
    		assertEquals(key, value, pGot.get(key));
    	}
    }

    /** Test case for {@link Streams#of(byte[])}.
     * @throws Exception The test failed.
     */
    @SuppressWarnings("null")
	@Test
    public void testOfByteArray() throws Exception {
    	String string = "763209kfegLIZRD$";
		final byte @NonNull [] bytes = string.getBytes(StandardCharsets.UTF_8);
    	final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	Streams.copy(Streams.of(bytes), baos);
    	assertEquals(string, baos.toString(StandardCharsets.UTF_8.name()));
    	try {
    		Streams.of(getNullObject(byte[].class));
    		fail("Expected Exception");
    	} catch (NullPointerException e) {
    		assertEquals("Bytes", e.getMessage());
    	}
    }

    private <O> O getNullObject(Class<O> pType) {
    	@SuppressWarnings("null")
		final O o = (O) null;
    	return o;
    }

    /** Test case for {@link Streams#of(String)}.
     */
    @Test
    public void testOfString() {
    	String string = "763209kfegLIZRD$";
    	final StringWriter sw = new StringWriter();
    	Streams.copy(Streams.of(string), sw);
    	assertEquals(string, sw.toString());
    	try {
    		Streams.of(getNullObject(String.class));
    		fail("Expected Exception");
    	} catch (NullPointerException e) {
    		assertEquals("String", e.getMessage());
    	}
    }

    /** Test case for {@link Streams#of(String, Charset)}.
     * @throws Exception The test failed.
     */
    @Test
    public void testOfStringCharset() throws Exception {
    	String string = "763209kfegLIZRD$";
    	final ByteArrayOutputStream baos0 = new ByteArrayOutputStream();
    	Streams.copy(Streams.of(string, null), baos0);
    	assertEquals(string, baos0.toString(StandardCharsets.UTF_8.name()));
    	final ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
    	Streams.copy(Streams.of(string, StandardCharsets.ISO_8859_1), baos1);
    	assertEquals(string, baos1.toString(StandardCharsets.ISO_8859_1.name()));
    	try {
    		Streams.of(getNullObject(String.class), getNullObject(Charset.class));
    		fail("Expected Exception");
    	} catch (NullPointerException e) {
    		assertEquals("String", e.getMessage());
    	}
    }

    /** Test case for {@link Streams#read(IReadable, Charset, FailableBiConsumer)}.
     */
    @Test
    public void testReadIReadableCharsetBiConsumer() {
    	final Consumer<List<Object>> validator = (list) -> {
    		assertEquals(8, list.size());
    		while (!list.isEmpty()) {
    			final Integer lineNumber = (Integer) list.remove(0);
    			final String text = (String) list.remove(0);
    			assertNotNull(lineNumber);
    			assertNotNull(text);
    			assertEquals("This is line " + lineNumber + ".", text);
    		}
    	};
    	for (final String sep : Arrays.asList(System.lineSeparator(), "\n", "\r\n")) {
    		final String text0 = "This is line 0." + sep +
    				"This is line 1." + sep +
    				"This is line 2." + sep +
    				"This is line 3.";
    		final String text1 = text0 + sep;
    		final List<Object> list0 = new ArrayList<>();
    		final ByteArrayInputStream bais0 = new ByteArrayInputStream(text0.getBytes(StandardCharsets.UTF_8));
			Streams.read(IReadable.of("TEXT0", () -> bais0), null, (lineNumber, line) -> {
    			list0.add((Integer) lineNumber);
    			list0.add((String) line);
    		});
    		validator.accept(list0);
    		final List<Object> list1 = new ArrayList<>();
    		final ByteArrayInputStream bais1 = new ByteArrayInputStream(text1.getBytes(StandardCharsets.UTF_8));
			Streams.read(IReadable.of("TEXT0", () -> bais1), null, (lineNumber, line) -> {
    			list1.add((Integer) lineNumber);
    			list1.add((String) line);
    		});
    		validator.accept(list1);
    	}
    }

    /** Test case for {@link Streams#closeListeningStream(InputStream,BooleanSupplier)}.
     * @throws Exception The test failed.
     */
    @Test
    public void testCloseListeningStream() throws Exception {
    	final byte[] bytes = "01234567890abcdefghijklmnopqrstuvwxyz".getBytes(StandardCharsets.UTF_8);
    	{
    		final CloseableInputStream cis = new CloseableInputStream(bytes);
    		assertFalse(cis.isClosed());
    		final MutableBoolean hookInvoked = new MutableBoolean();
    		final InputStream in = Streams.closeListeningStream(cis, () -> { hookInvoked.set(); return true; });
    		final byte[] output = Streams.read(in);
    		assertArrayEquals(bytes, output);
    		assertFalse(cis.isClosed());
    		assertTrue(hookInvoked.isSet());
    		in.close();
    		assertFalse(cis.isClosed());
    		assertTrue(hookInvoked.isSet());
    	}
    	{
    		final CloseableInputStream cis = new CloseableInputStream(bytes);
    		assertFalse(cis.isClosed());
    		final MutableBoolean hookInvoked = new MutableBoolean();
    		final InputStream in = Streams.closeListeningStream(cis, () -> { hookInvoked.set(); return false; });
    		final byte[] output = Streams.read(in);
    		assertArrayEquals(bytes, output);
    		assertTrue(cis.isClosed());
    		assertTrue(hookInvoked.isSet());
    		in.close();
    		assertTrue(cis.isClosed());
    		assertTrue(hookInvoked.isSet());
    	}
    }

    /** Test case for {@link Streams#closeListeningReader(Reader,BooleanSupplier)}.
     * @throws Exception The test failed.
     */
    @Test
    public void testCloseListeningReader() throws Exception {
    	final String bytes = "01234567890abcdefghijklmnopqrstuvwxyz";
    	{
    		final CloseableReader cr = new CloseableReader(bytes);
    		assertFalse(cr.isClosed());
    		final MutableBoolean hookInvoked = new MutableBoolean();
    		final Reader in = Streams.closeListeningReader(cr, () -> { hookInvoked.set(); return true; });
    		final String output = Streams.read(in);
    		in.close();
    		assertEquals(bytes, output);
    		assertFalse(cr.isClosed());
    		assertTrue(hookInvoked.isSet());
    		in.close();
    		assertFalse(cr.isClosed());
    		assertTrue(hookInvoked.isSet());
    	}
    	{
    		final CloseableReader cr = new CloseableReader(bytes);
    		assertFalse(cr.isClosed());
    		final MutableBoolean hookInvoked = new MutableBoolean();
    		final Reader in = Streams.closeListeningReader(cr, () -> { hookInvoked.set(); return false; });
    		final String output = Streams.read(in);
    		assertEquals(bytes, output);
    		assertTrue(cr.isClosed());
    		assertTrue(hookInvoked.isSet());
    		in.close();
    		assertTrue(cr.isClosed());
    		assertTrue(hookInvoked.isSet());
    	}
    }

    /** Test for {@link Streams#uncloseableReader(Reader)}.
     * @throws IOException The test failed.
     */
    @Test
    public void testUncloseableReader() throws IOException {
    	final MutableBoolean closed = new MutableBoolean();
    	final StringReader sr = new StringReader("abcdefghijklmnopqrstuvwxyz") {
			@Override
			public void close() {
				closed.set();
				super.close();
			}
    	};
    	assertFalse(closed.isSet());
    	final Reader r = Streams.uncloseableReader(sr);
    	r.close();
    	r.close();
    	assertFalse(closed.isSet());
    }

    /** Test for {@link Streams#multiplex(OutputStream, Object...)}.
     * @throws Exception The test failed.
     */
    @Test
    public void testMultiplex() throws Exception {
    	final MutableBoolean baosClosed = new MutableBoolean();
    	final ByteArrayOutputStream baos = new ByteArrayOutputStream() {
			@Override
			public void close() throws IOException {
				baosClosed.set();
				super.close();
			}
    	};
    	assertFalse(baosClosed.isSet());
    	final String input = "abcdefghijklmnopqrstuvwxyz";

    	// Step 1: Test using read()
    	{
    		final CloseableInputStream cis = CloseableInputStream.newInstance(input);
    		try (InputStream in = Streams.multiplex(cis, Listener.of(() -> baos))) {
    			for (;;) {
    				final int res = in.read();
    				if (res == -1) {
    					break;
    				}
    			}
    		}
    		assertTrue(baosClosed.isSet());
    		assertEquals(input, baos.toString("UTF-8"));
    	}
    	
    	// Step 2: Test using read(byte[], int, int)
    	{
    		final byte[] buffer = new byte[7];
    		baos.reset();
    		baosClosed.setValue(false);
    		final CloseableInputStream cis = CloseableInputStream.newInstance(input);
    		try (InputStream in = Streams.multiplex(cis, Listener.of(() -> baos))) {
    			for (;;) {
    				final int res = in.read(buffer, 0, buffer.length);
    				if (res == -1) {
    					break;
    				}
    			}
    		}
    		assertTrue(baosClosed.isSet());
    		assertEquals(input, baos.toString("UTF-8"));
    	}

    	// Step 3: Test using read()
    	{
    		baos.reset();
    		baosClosed.setValue(false);
    		final CloseableInputStream cis = CloseableInputStream.newInstance(input);
    		try (InputStream in = Streams.multiplex(cis, Listener.of(() -> baos))) {
    			for (;;) {
    				final int res = in.read();
    				if (res == -1) {
    					break;
    				}
    			}
    		}
    		assertTrue(baosClosed.isSet());
    		assertEquals(input, baos.toString("UTF-8"));
    	}
    }
}
