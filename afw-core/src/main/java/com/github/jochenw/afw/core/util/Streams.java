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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.FilterReader;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.jochenw.afw.core.io.ReaderInputStream;
import com.github.jochenw.afw.core.util.Functions.FailableConsumer;
import com.github.jochenw.afw.core.util.Functions.FailableFunction;


/**
 * Utility class for working with byte, and character streams.
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
     * array. The {@link InputStream} isn't closed.
     * @param pIn The {@link InputStream} to read from. Will be closed.
     * @return The contents, which have been read from {@code pIn}.
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

    /**
     * Returns the contents of the given {@link InputStream}, as a
     * string. The {@link InputStream} isn't closed.
     * @param pIn The {@link InputStream} to read from. Will be closed.
     * @param pCharset The character set to use for byte[]->string
     *   conversion. May be null, in which case
     *   {@link StandardCharsets#UTF_8 UTF-8} will be used.
     * @return The contents, which have been read from {@code pIn}.
     */
    public static @Nonnull String read(@Nonnull InputStream pIn, @Nullable Charset pCharset) {
    	final Charset charSet = Objects.notNull(pCharset, StandardCharsets.UTF_8);
    	return read(new InputStreamReader(pIn, charSet));
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
     * @param pOut The {@link OutputStream} to write to.
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
     * {@link Reader} is <em>not</em> closed, after invoking this method.
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
     * @see #copy(Reader, Writer)
     * @see #copy(InputStream, OutputStream)
     * @see #copy(InputStream, OutputStream, int)
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

    /** _Returns an {@link InputStream}, which returns the same
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

    /** Returns an {@link Reader}, which returns the same
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

    /** Returns an {@link OutputStream}, which copies its output to the given, but
     * prevents the latter from being closed.
     * @param pOut The {@link OutputStream} to write to.
     * @return Another {@link OutputStream}, which copies all output to {@code pOut},
     * but cannot be closed. (The {@link OutputStream#close()}
     * method may be invoked, but it does nothing.)
     */
    public static OutputStream uncloseableStream(final OutputStream pOut) {
        return new FilterOutputStream(pOut) {
			@Override
			public void write(byte[] pBuffer) throws IOException {
				out.write(pBuffer);
			}

			@Override
			public void write(byte[] pBuffer, int pOff, int pLen) throws IOException {
				out.write(pBuffer, pOff, pLen);
			}

			@Override
			public void close() throws IOException {
				// Does nothing
			}
        };
    }

    /** Returns a {@link Writer}, which copies its output to the given, but
     * prevents the latter from being closed.
     * @param pOut The {@link Writer} to write to.
     * @return Another {@link Writer}, which copies all output to {@code pOut},
     * but cannot be closed. (The {@link Writer#close()}
     * method may be invoked, but it does nothing.)
     */
    public static Writer uncloseableWriter(final Writer pOut) {
        return new FilterWriter(pOut) {
            @Override
            public void close() throws IOException {
                // Does nothing.
            }
            @Override
            public void write(char[] pBuffer) throws IOException {
                out.write(pBuffer);
            }
            @Override
            public void write(char[] pBuffer, int pOffset, int pLength) throws IOException {
                out.write(pBuffer, pOffset, pLength);
            }
            @Override
            public void write(int pChar) throws IOException {
                out.write(pChar);
            }
			@Override
			public void write(String pStr, int pOff, int pLen) throws IOException {
				out.write(pStr, pOff, pLen);
			}
			@Override
			public void write(String pStr) throws IOException {
				out.write(pStr);
			}
        };
    }

    /** Opens an {@link InputStream} for reading the given {@link URL},
     * and passes that {@link InputStream} to the given {@link Consumer}.
     * @param pUrl The {@link URL} to read.
     * @param pConsumer A {@link FailableConsumer}, which is being invoked with
     *   the opened {@link InputStream} as the parameter. The {@link
     *   InputStream} will be closed automatically afterwards.
     * @throws UncheckedIOException An error occurred, while opening
     *   the {@link URL}, or reading the {@link InputStream}.
     */
    public static void accept(@Nonnull URL pUrl, @Nonnull FailableConsumer<InputStream,IOException> pConsumer) {
    	try (InputStream in = pUrl.openStream();
    		 BufferedInputStream bin = new BufferedInputStream(in)) {
    		pConsumer.accept(in);
    	} catch (IOException e) {
    		throw new UncheckedIOException(e);
    	}
    }

    /** Opens an {@link InputStream} for reading the given {@link Path file},
     * and passes that {@link InputStream} to the given {@link Consumer}.
     * @param pFile The {@link Path file} to read.
     * @param pConsumer A {@link FailableConsumer}, which is being invoked with
     *   the opened {@link InputStream} as the parameter. The {@link
     *   InputStream} will be closed automatically afterwards.
     * @throws UncheckedIOException An error occurred, while opening
     *   the {@link URL}, or reading the {@link InputStream}.
     */
    public static void accept(@Nonnull Path pFile, @Nonnull FailableConsumer<InputStream,IOException> pConsumer) {
    	try (InputStream in = Files.newInputStream(pFile);
    		 BufferedInputStream bin = new BufferedInputStream(in)) {
    		pConsumer.accept(bin);
    	} catch (IOException e) {
    		throw new UncheckedIOException(e);
    	}
    }

    /** Opens an {@link InputStream} for reading the given {@link File file},
     * and passes that {@link InputStream} to the given {@link Consumer}.
     * @param pFile The {@link File file} to read.
     * @param pConsumer A {@link FailableConsumer}, which is being invoked with
     *   the opened {@link InputStream} as the parameter. The {@link
     *   InputStream} will be closed automatically afterwards.
     * @throws UncheckedIOException An error occurred, while opening
     *   the {@link URL}, or reading the {@link InputStream}.
     */
    public static void accept(@Nonnull File pFile, @Nonnull FailableConsumer<InputStream,IOException> pConsumer) {
    	try (InputStream in = new FileInputStream(pFile);
    		 BufferedInputStream bin = new BufferedInputStream(in)) {
    		pConsumer.accept(bin);
    	} catch (IOException e) {
    		throw new UncheckedIOException(e);
    	}
    }

    /** Opens an {@link InputStream} for reading the given {@link URL},
     * and passes that {@link InputStream} to the given {@link FailableFunction}.
     * @param pUrl The {@link URL} to read.
     * @param pFunction A {@link FailableFunction}, which is being invoked with
     *   the opened {@link InputStream} as the parameter. The {@link
     *   InputStream} will be closed automatically afterwards. The functions
     *   result is returned as the method result.
     * @throws UncheckedIOException An error occurred, while opening
     *   the {@link URL}, or reading the {@link InputStream}.
     * @return The result of invoking {@code pFunction}.
     */
    public static <O> O apply(@Nonnull URL pUrl, @Nonnull FailableFunction<InputStream,O,IOException> pFunction) {
    	try (InputStream in = pUrl.openStream();
    		 BufferedInputStream bin = new BufferedInputStream(in)) {
    		return pFunction.apply(in);
    	} catch (IOException e) {
    		throw new UncheckedIOException(e);
    	}
    }

    /** Opens an {@link InputStream} for reading the given {@link Path},
     * and passes that {@link InputStream} to the given {@link FailableFunction}.
     * @param pPath The {@link Path file} to read.
     * @param pFunction A {@link FailableFunction}, which is being invoked with
     *   the opened {@link InputStream} as the parameter. The {@link
     *   InputStream} will be closed automatically afterwards. The functions
     *   result is returned as the method result.
     * @throws UncheckedIOException An error occurred, while opening
     *   the {@link URL}, or reading the {@link InputStream}.
     * @return The result of invoking {@code pFunction}.
     */
    public static <O> O apply(@Nonnull Path pPath, @Nonnull FailableFunction<InputStream,O,IOException> pFunction) {
    	try (InputStream in = Files.newInputStream(pPath);
    		 BufferedInputStream bin = new BufferedInputStream(in)) {
    		return pFunction.apply(in);
    	} catch (IOException e) {
    		throw new UncheckedIOException(e);
    	}
    }

    /** Opens an {@link InputStream} for reading the given {@link File file},
     * and passes that {@link InputStream} to the given {@link FailableFunction}.
     * @param pFile The {@link File file} to read.
     * @param pFunction A {@link FailableFunction}, which is being invoked with
     *   the opened {@link InputStream} as the parameter. The {@link
     *   InputStream} will be closed automatically afterwards. The functions
     *   result is returned as the method result.
     * @throws UncheckedIOException An error occurred, while opening
     *   the {@link URL}, or reading the {@link InputStream}.
     * @return The result of invoking {@code pFunction}.
     */
    public static <O> O apply(@Nonnull File pFile, @Nonnull FailableFunction<InputStream,O,IOException> pFunction) {
    	return apply(pFile.toPath(), pFunction);
    }

    /** Reads the given input stream, and returns it as an instance of
     * {@link java.util.Properties}.
     * @param pIn The input stream, which is being read.
     * @param pUri The input streams URI.
     * @return The contents of the given file, as a {@link java.util.Properties}
     *   object.
     * @throws IOException Reading the input stream has failed.
     */
    public static Properties load(InputStream pIn, String pUri) throws IOException {
    	final Properties props = new Properties();
    	if (pUri.endsWith(".xml")) {
    		props.loadFromXML(pIn);
    	} else {
    		props.load(pIn);
    	}
    	return props;
    }

    /** Loads the given property file, and returns it as an instance of
     * {@link java.util.Properties}.
     * @param pPath The property file, which is being loaded.
     * @return The contents of the given file, as a {@link java.util.Properties}
     *   object.
     */
    public static Properties load(Path pPath) {
    	return Streams.apply(pPath, (in) -> { return load(in, pPath.getFileName().toString()); });
    }

    /** Loads the given property file, and returns it as an instance of
     * {@link java.util.Properties}.
     * @param pFile The property file, which is being loaded.
     * @return The contents of the given file, as a {@link java.util.Properties}
     *   object.
     */
    public static java.util.Properties load(File pFile) {
    	return Streams.apply(pFile, (in) -> { return load(in, pFile.getName()); });
    }

    /** Reads the given property URL, and returns it as an instance of
     * {@link java.util.Properties}.
     * @param pUrl The URL, which is being read.
     * @return The contents of the given URL, as a {@link java.util.Properties}
     *   object.
     */
    public static java.util.Properties load(URL pUrl) {
    	return Streams.apply(pUrl, (in) -> { return load(in, pUrl.toExternalForm()); });
    }

    /** Converts the given {@link Reader} into an {@link InputStream}, using the
     * given {@link Charset character set} to encode characters as bytes.
     *
     * @param pReader The {@link Reader}, which is being converted.
     * @param pCharset The {@link Charset character set}, which is being
     *     used to convert characters into bytes. May be null, in which
     *     case {@link StandardCharsets#UTF_8} will be used.
     * @return An {@link InputStream}, which is internally reading characters
     *     from {@code pReader}.
     * @throws NullPointerException The parameter {@code pReader} is null.
     */
    public static @Nonnull ReaderInputStream asInputStream(@Nonnull Reader pReader, @Nullable Charset pCharset) {
    	final @Nonnull Reader reader = Objects.requireNonNull(pReader, "Reader");
    	final @Nonnull Charset charset = Objects.notNull(pCharset, StandardCharsets.UTF_8);
    	return new ReaderInputStream(reader, charset);
    }

    /**
     * Creates a {@link ByteArrayInputStream}, returning the given byte array.
     * @param pBytes The byte array, which ought to constitute the contents
     *   of the returned stream.
     * @return A {@link ByteArrayInputStream}, returning the given byte array.
     * @throws NullPointerException The parameter {@code pBytes} is null.
     */
    public static @Nonnull ByteArrayInputStream of(@Nonnull byte[] pBytes) {
    	return new ByteArrayInputStream(Objects.requireNonNull(pBytes, "Bytes"));
    }


    /**
     * Creates a {@link StringReader}, returning the given string.
     * @param pString The string, which ought to constitute the contents
     *   of the returned stream.
     * @return A {@link StringReader}, returning the given string.
     * @throws NullPointerException The parameter {@code pString} is null.
     */
    public static @Nonnull StringReader of(@Nonnull String pString) {
    	return new StringReader(Objects.requireNonNull(pString, "String"));
    }

    /**
     * Creates a {@link ByteArrayInputStream}, returning the given string.
     * @param pString The string, which ought to constitute the contents
     *   of the returned stream.
     * @param pCharset The character set, which should be used to convert
     *   the string into a byte array. (May be null, in which case
     *   {@link StandardCharsets#UTF_8} will be used as the default.
     * @return A {@link ByteArrayInputStream}, returning the given byte array.
     * @throws NullPointerException The parameter {@code pBytes} is null.
     */
    public static @Nonnull ByteArrayInputStream of(@Nonnull String pString, @Nullable Charset pCharset) {
    	final @Nonnull byte[] bytes = Objects.requireNonNull(pString, "String")
    			.getBytes(Objects.notNull(pCharset, StandardCharsets.UTF_8));
    	return of(bytes);
    }
}
