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
package com.github.jochenw.afw.core.io;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.github.jochenw.afw.core.util.Streams;


/** A buffered {@link Reader}, which filters an underlying
 * {@link InputStream}, or {@link Reader}.
 */
public class ReadableCharacterStream implements AutoCloseable {
	private Charset charSet;
	private BufferedReader br;

	/**
	 * Creates a new instance, which filters the given {@link BufferedReader}.
	 * @param pReader The underlying {@link BufferedReader}, that is being
	 *   filtered.
	 */
	public ReadableCharacterStream(BufferedReader pReader) {
		br = pReader;
		charSet = null;
	}

	/**
	 * Creates a new instance, which filters the given {@link BufferedInputStream},
	 * using the given character set for conversion of bytes into characters.
	 * @param pIn The underlying {@link BufferedInputStream}, that is being
	 *   filtered.
	 * @param pCharSet The character set, that is being used for conversion of
	 *   bytes into characters
	 */
	public ReadableCharacterStream(BufferedInputStream pIn, Charset pCharSet) {
		this(new BufferedReader(new InputStreamReader(pIn, pCharSet)));
		charSet = pCharSet;
	}

	@Override
	public void close() throws IOException {
		br.close();
	}

	/**
	 * Returns the character set, that is being used for conversion of
	 * bytes into characters. Null, if no conversion is necessary.
	 * @return The character set, that is being used for conversion of
	 * bytes into characters. Null, if no conversion is necessary.
	 */
	public Charset getCharSet() {
		return charSet;
	}

    /**
     * Reads characters into an array.  This method will block until some input
     * is available, an I/O error occurs, or the end of the stream is reached.
     *
     * @param       pBuffer  Destination buffer
     *
     * @return      The number of characters read, or -1
     *              if the end of the stream
     *              has been reached
     *
     * @exception   IOException  If an I/O error occurs
     */
	public int read(char[] pBuffer) throws IOException {
		return br.read(pBuffer);
	}

    /**
     * Reads a single character.
     *
     * @return The character read, as an integer in the range
     *         0 to 65535 (0x00-0xffff), or -1 if the
     *         end of the stream has been reached
     * @exception  IOException  If an I/O error occurs
     */
	public int read() throws IOException {
		return br.read();
	}

    /**
     * Reads characters into a portion of an array.
     *
     * <p> This method implements the general contract of the corresponding
     * <code>{@link Reader#read(char[], int, int) read}</code> method of the
     * <code>{@link Reader}</code> class.  As an additional convenience, it
     * attempts to read as many characters as possible by repeatedly invoking
     * the <code>read</code> method of the underlying stream.  This iterated
     * <code>read</code> continues until one of the following conditions becomes
     * true: <ul>
     *
     *   <li> The specified number of characters have been read,
     *
     *   <li> The <code>read</code> method of the underlying stream returns
     *   <code>-1</code>, indicating end-of-file, or
     *
     *   <li> The <code>ready</code> method of the underlying stream
     *   returns <code>false</code>, indicating that further input requests
     *   would block.
     *
     * </ul> If the first <code>read</code> on the underlying stream returns
     * <code>-1</code> to indicate end-of-file then this method returns
     * <code>-1</code>.  Otherwise this method returns the number of characters
     * actually read.
     *
     * <p> Subclasses of this class are encouraged, but not required, to
     * attempt to read as many characters as possible in the same fashion.
     *
     * <p> Ordinarily this method takes characters from this stream's character
     * buffer, filling it from the underlying stream as necessary.  If,
     * however, the buffer is empty, the mark is not valid, and the requested
     * length is at least as large as the buffer, then this method will read
     * characters directly from the underlying stream into the given array.
     * Thus redundant <code>BufferedReader</code>s will not copy data
     * unnecessarily.
     *
     * @param      pBuffer  Destination buffer
     * @param      pOff   Offset at which to start storing characters
     * @param      pLen   Maximum number of characters to read
     *
     * @return     The number of characters read, or -1 if the end of the
     *             stream has been reached
     *
     * @exception  IOException  If an I/O error occurs
     */
	public int read(char[] pBuffer, int pOff, int pLen) throws IOException {
		return br.read(pBuffer, pOff, pLen);
	}

    /**
     * Reads a line of text.  A line is considered to be terminated by any one
     * of a line feed ('\n'), a carriage return ('\r'), or a carriage return
     * followed immediately by a linefeed.
     *
     * @return     A String containing the contents of the line, not including
     *             any line-termination characters, or null if the end of the
     *             stream has been reached
     *
     * @exception  IOException  If an I/O error occurs
     *
     * @see java.nio.file.Files#readAllLines
     */
	public String readLine() throws IOException {
		return br.readLine();
	}


	/**
	 * Creates a new instance, which filters the given {@link InputStream},
	 * using the {@link StandardCharsets#UTF_8 UTF-8 character set} for
	 * conversion of bytes into characters.
	 * @param pIn The underlying {@link InputStream}, that is being
	 *   filtered.
	 * @param pMayCloseStream True, if closing the created instance should
	 *   create the underlying {@link InputStream}, as well. Otherwise
	 *   false.
	 * @return The created instance.
	 */
	public static ReadableCharacterStream newInstance(InputStream pIn, boolean pMayCloseStream) {
		return newInstance(pIn, StandardCharsets.UTF_8, pMayCloseStream);
	}

	/**
	 * Creates a new instance, which filters the given {@link InputStream},
	 * using the given character set for conversion of bytes into characters.
	 * @param pIn The underlying {@link InputStream}, that is being
	 *   filtered.
	 * @param pCharSet The character stream, that is being used for conversion
	 *   of bytes into characters.
	 * @param pMayCloseStream True, if closing the created instance should
	 *   create the underlying {@link InputStream}, as well. Otherwise
	 *   false.
	 * @return The created instance.
	 */
	public static ReadableCharacterStream newInstance(InputStream pIn, Charset pCharSet, boolean pMayCloseStream) {
		final InputStream in;
		if (pMayCloseStream) {
			in = pIn;
		} else {
			in = Streams.uncloseableStream(pIn);
		}
		final BufferedInputStream bis;
		if (in instanceof BufferedInputStream) {
			bis = (BufferedInputStream) in;
		} else {
			bis = new BufferedInputStream(in);
		}
		return new ReadableCharacterStream(bis, pCharSet);
	}

	/**
	 * Creates a new instance, which filters the given {@link BufferedInputStream},
	 * using the {@link StandardCharsets#UTF_8 UTF-8 character set} for
	 * conversion of bytes into characters.
	 * @param pIn The underlying {@link BufferedInputStream}, that is being
	 *   filtered.
	 * @param pMayCloseStream True, if closing the created instance should
	 *   create the underlying {@link InputStream}, as well. Otherwise
	 *   false.
	 * @return The created instance.
	 */
	public static ReadableCharacterStream of(BufferedInputStream pIn, boolean pMayCloseStream) {
		return newInstance(pIn, StandardCharsets.UTF_8, pMayCloseStream);
	}

	/**
	 * Creates a new instance, which filters the given {@link BufferedReader}.
	 * @param pReader The underlying {@link BufferedReader}, that is being
	 *   filtered.
	 * @param pMayCloseStream True, if closing the created instance should
	 *   create the underlying {@link InputStream}, as well. Otherwise
	 *   false.
	 * @return The created instance.
	 */
	public static ReadableCharacterStream of(Reader pReader, boolean pMayCloseStream) {
		final Reader r;
		if (pMayCloseStream) {
			r = pReader;
		} else {
			r = Streams.uncloseableReader(pReader);
		}
		final BufferedReader br;
		if (r instanceof BufferedReader) {
			br = (BufferedReader) r;
		} else {
			br = new BufferedReader(r);
		}
		return new ReadableCharacterStream(br);
	}
}
