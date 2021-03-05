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

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;


/** A {@link Reader}, which maintains a buffer of upcoming
 * data, that allows peeking into the buffer.
 */
public class CircularBufferReader extends Reader {
	protected final Reader in;
	protected final CircularCharBuffer buffer;
	protected final int bufferSize;
	private boolean eofSeen;

	/**
	 * Creates a new instance, which reads from the given {@link
	 * Reader}, maintaining a buffer of the given size.
	 * @param pIn The underlying {@link Reader}, from which
	 *   to read the data.
	 * @param pBufferSize The size of the underlying buffer.
	 */
	public CircularBufferReader(Reader pIn, int pBufferSize) {
		Objects.requireNonNull(pIn, "InputStream");
		if (pBufferSize <= 0) {
			throw new IllegalArgumentException("Invalid buffer size: " + pBufferSize);
		}
		in = pIn;
		buffer = new CircularCharBuffer(pBufferSize);
		bufferSize = pBufferSize;
		eofSeen = false;
	}

	/**
	 * Creates a new instance, which reads from the given {@link
	 * Reader}, maintaining a buffer of the default size
	 * (8192 bytes).
	 * @param pIn The underlying {@link Reader}, from which
	 *   to read the data.
	 */
	public CircularBufferReader(Reader pIn) {
		this(pIn, 8192);
	}

	protected void fillBuffer() throws IOException {
		if (eofSeen) {
			return;
		}
		int space = buffer.getSpace();
		final char[] buf = new char[space];
		while (space > 0) {
			final int res = in.read(buf, 0, space);
			if (res == -1) {
				eofSeen = true;
				return;
			} else if (res > 0) {
				buffer.add(buf, 0, res);
				space -= res;
			}
		}
	}

	protected boolean haveBytes(int pNumber) throws IOException {
		if (buffer.getCurrentNumberOfChars() < pNumber) {
			fillBuffer();
		}
		return buffer.hasChars();
	}

	@Override
	public int read() throws IOException {
		if (!haveBytes(1)) {
			return -1;
		}
		return buffer.next();
	}

	@Override
	public int read(char[] pBuffer) throws IOException {
		return read(pBuffer, 0, pBuffer.length);
	}

	@Override
	public int read(char[] pBuffer, int pOffset, int pLength) throws IOException {
		Objects.requireNonNull(pBuffer, "Buffer");
		if (pOffset < 0) {
			throw new IllegalArgumentException("Offset must not be negative");
		}
		if (pLength < 0) {
			throw new IllegalArgumentException("Length must not be negative");
		}
		if (!haveBytes(pLength)) {
			return -1;
		}
		final int result = Math.min(pLength, buffer.getCurrentNumberOfChars());
		for (int i = 0;  i < result;  i++) {
			pBuffer[pOffset+i] = buffer.next();
		}
		return result;
	}

	@Override
	public void close() throws IOException {
		in.close();
		eofSeen = true;
		buffer.clear();
	}
}
