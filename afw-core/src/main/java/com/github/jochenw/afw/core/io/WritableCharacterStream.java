/**
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
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import com.github.jochenw.afw.core.util.Streams;

public class WritableCharacterStream implements AutoCloseable {
	private Charset charSet;
	private BufferedWriter bw;

	public WritableCharacterStream(BufferedWriter pWriter) {
		bw = pWriter;
		charSet = null;
	}

	public WritableCharacterStream(Writer pWriter) {
		this(new BufferedWriter(pWriter));
	}

	public WritableCharacterStream(BufferedOutputStream pOs, Charset pCharSet) {
		this(new OutputStreamWriter(pOs, pCharSet));
		charSet = pCharSet;
	}

	public WritableCharacterStream(OutputStream pOut, Charset pCharSet) {
		this(new BufferedOutputStream(pOut), pCharSet);
		charSet = pCharSet;
	}

	public Charset getCharSet() {
		return charSet;
	}
	
	public void write(CharSequence pValue) throws IOException {
		write(pValue, 0, pValue.length());
	}

	public void write(char[] pBuffer) throws IOException {
		bw.write(pBuffer);
	}

	public void write(int pChar) throws IOException {
		bw.write(pChar);
	}

	public void write(char[] pBuffer, int pOffset, int pLength) throws IOException {
		bw.write(pBuffer, pOffset, pLength);
	}

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

	public void flush() throws IOException {
		bw.flush();
	}

	public void close() throws IOException {
		bw.close();
	}

	public static WritableCharacterStream newInstance(BufferedWriter pWriter, boolean pClose) {
		Objects.requireNonNull(pWriter, "Writer");
		if (pClose) {
			return new WritableCharacterStream(pWriter);
		} else {
			return new WritableCharacterStream(Streams.uncloseableWriter(pWriter));
		}
	}

	public static WritableCharacterStream newInstance(Writer pWriter, boolean pClose) {
		Objects.requireNonNull(pWriter, "Writer");
		final BufferedWriter bw;
		if (pClose) {
			if (pWriter instanceof BufferedWriter) {
				bw = (BufferedWriter) pWriter;
			} else {
				bw = new BufferedWriter(pWriter);
			}
		} else {
			bw = new BufferedWriter(Streams.uncloseableWriter(pWriter));
		}
		return new WritableCharacterStream(bw);
	}

	public static WritableCharacterStream newInstance(BufferedOutputStream pOut, Charset pCharSet, boolean pClose) {
		Objects.requireNonNull(pOut, "Out");
		Objects.requireNonNull(pCharSet, "CharSet");
		if (pClose) {
			return new WritableCharacterStream(pOut, pCharSet);
		} else {
			return new WritableCharacterStream(Streams.uncloseableStream(pOut), pCharSet);
		}
	}

	public static WritableCharacterStream newInstance(BufferedOutputStream pOut, boolean pClose) {
		return newInstance(pOut, StandardCharsets.UTF_8, pClose);
	}

	public static WritableCharacterStream newInstance(OutputStream pOut, Charset pCharSet, boolean pClose) {
		Objects.requireNonNull(pOut, "Out");
		Objects.requireNonNull(pCharSet, "CharSet");
		if (pClose) {
			return new WritableCharacterStream(pOut, pCharSet);
		} else {
			return new WritableCharacterStream(Streams.uncloseableStream(pOut), pCharSet);
		}
	}

	public static WritableCharacterStream newInstance(OutputStream pOut, boolean pClose) {
		return newInstance(pOut, StandardCharsets.UTF_8, pClose);
	}
}
