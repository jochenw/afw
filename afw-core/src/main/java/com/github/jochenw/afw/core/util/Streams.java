package com.github.jochenw.afw.core.util;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
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
     * is closed.
     */
    public static void copy(InputStream pIn, OutputStream pOut) {
        copy(pIn, pOut, 8192);
    }

    /** Copies the contents of the given {@link InputStream} to the given
     * {@link OutputStream}, using a buffer of {@code pBufSize} bytes.
     * Neither stream is closed.
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
     * stream is closed.
     */
    public static void copy(Reader pIn, Writer pOut) {
        copy(pIn, pOut, 8192);
    }

    /** Copies the contents of the given {@link Reader} to the given
     * {@link Writer}, using a buffer of {@code pBufSize} characters. Neither
     * stream is closed.
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
     */
    public static InputStream uncloseableStream(final InputStream pIn) {
        return new FilterInputStream(pIn) {
            @Override
            public void close() throws IOException {
                // Does nothing.
            }
        };
    }
}
