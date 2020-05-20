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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import com.github.jochenw.afw.core.util.Functions.FailableConsumer;


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
     * Test for {@link Streams#accept(java.net.URL, com.github.jochenw.afw.core.util.Functions.FailableConsumer)}.
     * @throws Exception The test failed.
     */
    public void testAcceptUrlConsumer() throws Exception {
    	final byte[] content = "MagicByteStream".getBytes(StandardCharsets.UTF_16);
       	final MutableBoolean opened = new MutableBoolean();
       	final MutableBoolean closed = new MutableBoolean();
       	final MutableBoolean connectedFlag = new MutableBoolean();
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
						connectedFlag.set();
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
    	assertTrue(connectedFlag.isSet());
    	Tests.assertEquals(content, contentHolder.get());
    }

    /** Test for {@link Streams#accept(Path, FailableConsumer)}.
     */
    public void testAcceptPathConsumer() {
    	final Path path = Paths.get("pom.xml");
    	assertTrue(Files.isRegularFile(path));
    	Streams.accept(path, (in)-> Tests.assertSameContent(path, in));
    }

    /** Test for {@link Streams#accept(File, FailableConsumer)}.
     */
    public void testAcceptFileConsumer() {
    	final File file = new File("pom.xml");
    	assertTrue(file.isFile());
    	Streams.accept(file, (in) -> Tests.assertSameContent(file.toPath(), in));
    }
}
