package com.github.jochenw.afw.core.util;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Utility class for working with Exceptions. Provides static utility methods.
 */
public class Streams {
    /**
     * Private constructor, to prevent accidental instantiation.
     */
    private Streams() {
        // Does nothing.
    }

    /**
     * Returns the contents of the given {@link InputStream}, as a byte
     * array. The {@link InputStream} is closed.
     * @param pIn The {@link InputStream} to read from. Will be closed.
     * @return The contents, which have been read from {@link pIn}.
     */
    public static byte[] read(InputStream pIn) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream in = pIn) {
            copy(pIn, baos);
        } catch (IOException e) {
            throw Exceptions.newUncheckedIOException(e);
        }
        return baos.toByteArray();
    }
    
    /** Copies the contents of the given {@link InputStream} to the given
     * {@link OutputStream}, using a buffer of 8192 bytes. Neither stream
     * is closed. Equivalent to invoking
     * {@link #copy(java.io.InputStream, java.io.OutputStream, int)}
     * with a buffer size of 8192 bytes.
     * @param pIn The {@link InputStream} to read from.
     * @param pOut The {@link OutputStream} to write to.
     * @see #copy(java.io.InputStream, java.io.OutputStream, int) 
     * @see #copy(java.io.Reader, java.io.Writer)
     * @see #copy(java.io.InputStream, java.io.OutputStream, int)
     */
    public static void copy(InputStream pIn, OutputStream pOut) {
        copy(pIn, pOut, 8192);
    }

    /** Copies the contents of the given {@link InputStream} to the given
     * {@link OutputStream}, using a buffer of {@code pBufSize} bytes.
     * Neither stream is closed.
     * @param pIn The {@link InputStream} to read from.
     * @param pOut The {@link OutputStream0 to write to.
     * @param pBufSize The buffer size to use, in bytes (performance).
     * @see #copy(java.io.InputStream, java.io.OutputStream) 
     * @see #copy(java.io.Reader, java.io.Writer)
     * @see #copy(java.io.InputStream, java.io.OutputStream, int)
     */
    public static void copy (InputStream pIn, OutputStream pOut, int pBufSize) {
        try {
            final byte[] buffer = new byte[pBufSize];
            for (;;) {
                final int res = pIn.read(buffer);
                if (res == -1) {
                    return;
                } else if (res > 0) {
                    pOut.write(buffer, 0, res);
                }
            }
        } catch (IOException e) {
            throw Exceptions.newUncheckedIOException(e);
        }
    }

    /**
     * Returns the contents of the given {@link Reader}, as a string.
     * The {@link Reader} is closed.
     * @param pIn The {@link Reader} to read from. This
     * {@link Reader will be closed, after invoking this method.
     * @return The contents, which have been read from {@code pIn}.
     * @see #read(java.io.InputStream) 
     */
    public static String read(Reader pIn) {
        final StringWriter sw = new StringWriter();
        try (Reader in = pIn) {
            copy(pIn, sw);
        } catch (IOException e) {
            throw Exceptions.newUncheckedIOException(e);
        }
        return sw.toString();
    }

    /** Copies the contents of the given {@link Reader} to the given
     * {@link Writer}, using a buffer of 8192 characters. Neither
     * stream is closed. Equivalent to invoking
     * {@link #copy(java.io.Reader, java.io.Writer, int)} with a buffer
     * size of 8192 characters.
     * @param pIn The {@link Reader} to read from.
     * @param pOut The {@link Writer} to write to.
     * @see #copy(java.io.Reader, java.io.Writer, int) 
     * @see #copy(java.io.InputStream, java.io.OutputStream) 
     * @see #copy(java.io.InputStream, java.io.OutputStream, int)
     */
    public static void copy(Reader pIn, Writer pOut) {
        copy(pIn, pOut, 8192);
    }

    /** Copies the contents of the given {@link Reader} to the given
     * {@link Writer}, using a buffer of {@code pBufSize} characters. Neither
     * stream is closed.
     * @param pIn The {@link Reader} to read from.
     * @param pOut The {@link Writer} to write to.
     * @param pBufSize The buffer size to use.
     * @see copy(Reader, Writer)
     * @see copy(InputStream, OutputStream)
     * @see copy(InputStream, OutputStream, int)
     */
    public static void copy(Reader pIn, Writer pOut, int pBufSize) {
        try {
            final char[] buffer = new char[pBufSize];
            for (;;) {
                final int res = pIn.read(buffer);
                if (res == -1) {
                    return;
                } else if (res > 0) {
                    pOut.write(buffer, 0, res);
                }
            }
        } catch (IOException e) {
            throw Exceptions.newUncheckedIOException(e);
        }
    }

    /** _Returns an {@link InputSteam}, which returns the same
     * contents than the given, but prevents the latter from being
     * closed.
     * @param pIn The {@link InputStream} to read.
     * @return Another {@link InputStream}, which is actually reading
     * {@code pIn}, but cannot be closed. (The {@link InputStream#close()}
     * method may be invoked, but it does nothing.)
     */
    public static InputStream uncloseableStream(final InputStream pIn) {
        return new FilterInputStream(pIn) {
            @Override
            public void close() throws IOException {
                // Does nothing.
            }
            @Override
            public int read(byte[] pBuffer) throws IOException {
                return pIn.read(pBuffer);
            }
            @Override
            public int read(byte[] pBuffer, int pOffset, int pLength) throws IOException {
                return pIn.read(pBuffer, pOffset, pLength);
            }
            @Override
            public int read() throws IOException {
                return pIn.read();
            }
        };
    }

    /** _Returns an {@link Reader}, which returns the same
     * contents than the given, but prevents the latter from being
     * closed.
     * @param pIn The {@link Reader} to read.
     * @return Another {@link Reader}, which is actually reading
     * {@code pIn}, but cannot be closed. (The {@link Reader#close()}
     * method may be invoked, but it does nothing.)
     */
    public static Reader uncloseableReader(final Reader pIn) {
        return new FilterReader(pIn) {
            @Override
            public void close() throws IOException {
                // Does nothing.
            }
            @Override
            public int read(char[] pBuffer) throws IOException {
                return pIn.read(pBuffer);
            }
            @Override
            public int read(char[] pBuffer, int pOffset, int pLength) throws IOException {
                return pIn.read(pBuffer, pOffset, pLength);
            }
            @Override
            public int read() throws IOException {
                return pIn.read();
            }
        };
    }
}