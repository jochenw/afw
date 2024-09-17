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

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import com.github.jochenw.afw.core.util.Streams;



/**
 * Implementation of a character stream, which is actually writing to a backing writer.
 * Guarantees buffering.
 */
public class WritableCharacterStream implements AutoCloseable, Flushable, Appendable {
	private Charset charSet;
	private BufferedWriter bw;

	/** Creates a new instance, which is actually writing to the given {@link BufferedWriter}.
	 * @param pWriter The actual target of the data, that is written to this character
	 *   stream.
	 */
	public WritableCharacterStream(BufferedWriter pWriter) {
		bw = pWriter;
		charSet = null;
	}

	/** Creates a new instance, which is actually writing to the given
	 * {@link BufferedOutputStream}, using the given character set for conversion
	 * of characters to bytes.
	 * @param pOs The actual target of the data, that is written to this character
	 *   stream.
	 * @param pCharSet The character set, that is being used for conversion
	 *   of characters to bytes
	 */
	public WritableCharacterStream(BufferedOutputStream pOs, Charset pCharSet) {
		this(new BufferedWriter(new OutputStreamWriter(pOs, pCharSet)));
		charSet = pCharSet;
	}

	/** Returns the character set, that is being used for conversion
	 *   of characters to bytes, if any. Null, if no conversion occurs.
	 * @return The character set, that is being used for conversion
	 *   of characters to bytes, if any. Null, if no conversion occurs.
	 */
	public Charset getCharSet() {
		return charSet;
	}

	/** Writes the given character sequence to the target stream.
	 * @param pValue The character sequence, which is being written.
	 * @throws IOException An I/O error occurred while writing the data.
	 */
	public void write(CharSequence pValue) throws IOException {
		write(pValue, 0, pValue.length());
	}

	/** Writes the given character array to the target stream.
	 * @param pArray The character array, which is being written.
	 * @throws IOException An I/O error occurred while writing the data.
	 */
	public void write(char[] pArray) throws IOException {
		bw.write(pArray);
	}

	/** Writes the given character to the target stream.
	 * @param pChar The character, which is being written.
	 * @throws IOException An I/O error occurred while writing the data.
	 */
	public void write(int pChar) throws IOException {
		bw.write(pChar);
	}

	/** Writes a part of the given character array to the target stream.
	 * @param pArray The character array, which is being written.
	 * @param pOffset Offset of the first character, which is being written.
	 * @param pLength The number of characters, that are being written.
	 * @throws IOException An I/O error occurred while writing the data.
	 */
	public void write(char[] pArray, int pOffset, int pLength) throws IOException {
		bw.write(pArray, pOffset, pLength);
	}

	/** Writes a part of the given character sequence to the target stream.
	 * @param pValue The character sequence, which is being written.
	 * @param pOffset Offset of the first character, which is being written.
	 * @param pLength The number of characters, that are being written.
	 * @throws IOException An I/O error occurred while writing the data.
	 */
	public void write(CharSequence pValue, int pOffset, int pLength) throws IOException {
		if (pOffset < 0) {
			throw new IllegalArgumentException("The Offset must not be lower than zero.");
		}
		if (pLength < 0) {
			throw new IllegalArgumentException("The Length must not be lower than zero.");
		}
		if (pValue instanceof String) {
			bw.write((String) pValue, pOffset, pLength);
		} else {
			final int end = Math.min(pValue.length(), pOffset+pLength);
			for (int i = pOffset;  i < end;  i++) {
				bw.write(pValue.charAt(i));
			}
		}
	}

	@Override
	public void flush() throws IOException {
		bw.flush();
	}

	@Override
	public void close() throws IOException {
		bw.close();
	}

	/**
	 * Creates a new instance, which is actually writing to the given
	 * target {@link Writer}.
	 * @param pWriter The actual data target.
	 * @param pMayCloseStream True, if closing the created instance may
	 *   close the actual data target.
	 * @return The created instance.
	 */
	public static WritableCharacterStream of(Writer pWriter, boolean pMayCloseStream) {
		Objects.requireNonNull(pWriter, "Writer");
		if (pMayCloseStream) {
			final BufferedWriter bw;
			if (pWriter instanceof BufferedWriter) {
				bw = (BufferedWriter) pWriter;
			} else {
				bw = new BufferedWriter(pWriter);
			}
			return new WritableCharacterStream(bw);
		} else {
			return new WritableCharacterStream(new BufferedWriter(Streams.uncloseableWriter(pWriter)));
		}
	}

	/**
	 * Creates a new instance, which is actually writing to the given
	 * target {@link OutputStream}.
	 * @param pOut The actual data target.
	 * @param pCharSet The character set, which is being used for conversion
	 *   of characters into bytes.
	 * @param pMayCloseStream True, if closing the created instance may
	 *   close the actual data target.
	 * @return The created instance.
	 */
	public static WritableCharacterStream of(OutputStream pOut, Charset pCharSet, boolean pMayCloseStream) {
		Objects.requireNonNull(pOut, "Out");
		Objects.requireNonNull(pCharSet, "CharSet");
		if (pMayCloseStream) {
			final BufferedOutputStream bos;
			if (pOut instanceof BufferedOutputStream) {
				bos = (BufferedOutputStream) pOut;
			} else {
				bos = new BufferedOutputStream(pOut);
			}
			return new WritableCharacterStream(bos, pCharSet);
		} else {
			return new WritableCharacterStream(new BufferedOutputStream(Streams.uncloseableStream(pOut)), pCharSet);
		}
	}

	/**
	 * Creates a new instance, which is actually writing to the given
	 * target {@link OutputStream}, using {@link StandardCharsets#UTF_8}
	 * for conversion of characters into bytes.
	 * @param pOut The actual data target.
	 * @param pMayCloseStream True, if closing the created instance may
	 *   close the actual data target.
	 * @return The created instance.
	 */
	public static WritableCharacterStream of(OutputStream pOut, boolean pMayCloseStream) {
		return of(pOut, StandardCharsets.UTF_8, pMayCloseStream);
	}

	@Override
	public Appendable append(CharSequence pCsq) throws IOException {
		return append(pCsq, 0, pCsq.length());
	}

	@Override
	public Appendable append(CharSequence pCsq, int pStart, int pEnd) throws IOException {
		final char[] ch;
		if (pCsq instanceof String) {
			ch = ((String) pCsq).toCharArray();
		} else {
			ch = new char[pCsq.length()];
			for (int i = 0;  i < ch.length;  i++) {
				ch[i] = pCsq.charAt(i);
			}
		}
		bw.write(ch, pStart, pEnd-pStart);
		return this;
	}

	@Override
	public Appendable append(char pC) throws IOException {
		bw.write((int) pC);
		return this;
	}
}
