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
import java.io.InputStream;
import java.util.Objects;


/** An {@link InputStream}, which maintains a buffer of upcoming
 * data, that allows peeking into the buffer.
 */
public class CircularBufferInputStream extends InputStream {
	/** The actual {@link InputStream}, which is being buffered by this {@link InputStream}.
	 */
	protected final InputStream in;
	/** The buffer, which is used to enable peeking.
	 */
	protected final CircularByteBuffer buffer;
	/** The buffers size.
	 */
	protected final int bufferSize;
	/** Whether the {@link #in actual InputStream}
	 * has already reported EOF.
	 */
	private boolean eofSeen;

	/**
	 * Creates a new instance, which reads from the given {@link
	 * InputStream}, maintaining a buffer of the given size.
	 * @param pIn The underlying {@link InputStream}, from which
	 *   to read the data.
	 * @param pBufferSize The size of the underlying buffer.
	 */
	public CircularBufferInputStream(InputStream pIn, int pBufferSize) {
		Objects.requireNonNull(pIn, "InputStream");
		if (pBufferSize <= 0) {
			throw new IllegalArgumentException("Invalid buffer size: " + pBufferSize);
		}
		in = pIn;
		buffer = new CircularByteBuffer(pBufferSize);
		bufferSize = pBufferSize;
		eofSeen = false;
	}

	/**
	 * Creates a new instance, which reads from the given {@link
	 * InputStream}, maintaining a buffer of the default size
	 * (8192 bytes).
	 * @param pIn The underlying {@link InputStream}, from which
	 *   to read the data.
	 */
	public CircularBufferInputStream(InputStream pIn) {
		this(pIn, 8192);
	}

	/** Called to fill the buffer by reading from the actual {@link InputStream}.
	 * @throws IOException Filling the buffer failed.
	 */
	protected void fillBuffer() throws IOException {
		if (eofSeen) {
			return;
		}
		int space = buffer.getSpace();
		final byte[] buf = new byte[space];
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

	/** Returns, whether the buffer currently contains the given number of bytes.
	 * If that is not the case, an attempt will be made to fill the buffer.
	 * @param pNumber The requested number of bytes.
	 * @return True, if the requested number of bytes is available in the buffer.
	 *   Otherwise false.
	 * @throws IOException Filling the buffer has failed.
	 */
	protected boolean haveBytes(int pNumber) throws IOException {
		if (buffer.getCurrentNumberOfBytes() < pNumber) {
			fillBuffer();
		}
		return buffer.hasBytes();
	}

	@Override
	public int read() throws IOException {
		if (!haveBytes(1)) {
			return -1;
		}
		return buffer.read();
	}

	@Override
	public int read(byte[] pBuffer) throws IOException {
		return read(pBuffer, 0, pBuffer.length);
	}

	@Override
	public int read(byte[] pBuffer, int pOffset, int pLength) throws IOException {
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
		final int result = Math.min(pLength, buffer.getCurrentNumberOfBytes());
		for (int i = 0;  i < result;  i++) {
			pBuffer[pOffset+i] = buffer.read();
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
