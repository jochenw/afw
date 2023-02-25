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
	/** The actual {@link Reader}, from which to read characters.
	 */
	protected final Reader in;
	/** The buffer, which is used to enable peeking.
	 */
	protected final CircularCharBuffer buffer;
	/** The buffers size.
	 */
	protected final int bufferSize;
	/** Whether the {@link #in actual InputStream has already reported EOF.
	 */
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

	/** Called to fill the buffer by reading from the actual {@link Reader}.
	 * @throws IOException Filling the buffer has failed.
	 */
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

	/** Returns, whether the buffer currently contains the given number of
	 * characters. If that is not the case, an attempt will be made to fill
	 * the buffer.
	 * @param pNumber The requested number of bytes.
	 * @return True, if the requested number of bytes is available in the buffer.
	 *   Otherwise false.
	 * @throws IOException Filling the buffer has failed.
	 */
	protected boolean haveChars(int pNumber) throws IOException {
		if (buffer.getCurrentNumberOfChars() < pNumber) {
			fillBuffer();
		}
		return buffer.hasChars();
	}

	@Override
	public int read() throws IOException {
		if (!haveChars(1)) {
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
		if (!haveChars(pLength)) {
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
