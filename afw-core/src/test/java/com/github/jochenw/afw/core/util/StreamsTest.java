package com.github.jochenw.afw.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import org.junit.Assert;
import org.junit.Test;

public class StreamsTest {
    public static class CloseableInputStream extends ByteArrayInputStream {
        private final byte[] contents;
        private boolean closed;

        private CloseableInputStream(byte[] pContents) {
            super(pContents);
            contents = pContents;
        }
        
        public boolean isClosed() {
            return closed;
        }

        public byte[] getContents() {
            return contents;
        }

        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }

        public static CloseableInputStream newInstance(String pContents) {
            try {
                final byte[] contents = pContents.getBytes("UTF-8");
                return new CloseableInputStream(contents);
            } catch (UnsupportedEncodingException e) {
                throw Exceptions.newUncheckedIOException(e);
            }
        }
    }
    public static class CloseableReader extends StringReader {
        private final String contents;
        private boolean closed;

        public CloseableReader(String pContents) {
            super(pContents);
            contents = pContents;
        }
        
        public boolean isClosed() {
            return closed;
        }

        public String getContents() {
            return contents;
        }

        @Override
        public void close() {
            closed = true;
            super.close();
        }
    }

    @Test
    public void testReadInputStream() {
        final CloseableInputStream cis = CloseableInputStream.newInstance("SomeString");
        Assert.assertFalse(cis.isClosed());
        final byte[] bytes = Streams.read(cis);
        Assert.assertTrue(cis.isClosed());
        Assert.assertArrayEquals(cis.getContents(), bytes);
    }

    @Test
    public void testCopyInputStreamOutputStream() {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final CloseableInputStream cis = CloseableInputStream.newInstance("SomeString");
        Assert.assertFalse(cis.isClosed());
        Streams.copy(cis, baos);
        Assert.assertFalse(cis.isClosed());
        Assert.assertArrayEquals(cis.getContents(), baos.toByteArray());
    }

    @Test
    public void testCopyInputStreamOutputStreamInt() {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final CloseableInputStream cis = CloseableInputStream.newInstance("SomeString");
        Assert.assertFalse(cis.isClosed());
        Streams.copy(cis, baos, 2);
        Assert.assertFalse(cis.isClosed());
        Assert.assertArrayEquals(cis.getContents(), baos.toByteArray());
    }

    @Test
    public void testReadReader() {
        final CloseableReader cr = new CloseableReader("Some String");
        Assert.assertFalse(cr.isClosed());
        final String got = Streams.read(cr);
        Assert.assertTrue(cr.isClosed());
        Assert.assertEquals(cr.getContents(), got);
    }

    @Test
    public void testCopyReaderWriter() {
        final StringWriter sw = new StringWriter();
        final CloseableReader cr = new CloseableReader("SomeString");
        Assert.assertFalse(cr.isClosed());
        Streams.copy(cr, sw);
        Assert.assertFalse(cr.isClosed());
        Assert.assertEquals(cr.getContents(), sw.toString());
    }

    @Test
    public void testCopyReaderWriterInt() {
        final StringWriter sw = new StringWriter();
        final CloseableReader cr = new CloseableReader("SomeString");
        Assert.assertFalse(cr.isClosed());
        Streams.copy(cr, sw, 2);
        Assert.assertFalse(cr.isClosed());
        Assert.assertEquals(cr.getContents(), sw.toString());
    }

    @Test
    public void testUncloseableStream() {
        final CloseableInputStream cis = CloseableInputStream.newInstance("SomeString");
        Assert.assertFalse(cis.isClosed());
        final byte[] bytes = Streams.read(Streams.uncloseableStream(cis));
        Assert.assertFalse(cis.isClosed());
        Assert.assertArrayEquals(cis.getContents(), bytes);
    }

}
