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


/**
 * A filtering {@link InputStream} with some buffering capabilities,
 * that allow to peek into the upcoming data.
 */
public class PeekableInputStream extends CircularBufferInputStream {
	/**
	 * Creates a new instance, which filters the given underlying
	 * input stream, maintaining a buffer with the given size.
	 * @param pIn The underlying input stream.
	 * @param pBufferSize The internal buffers size.
	 */
	public PeekableInputStream(InputStream pIn, int pBufferSize) {
		super(pIn, pBufferSize);
	}

	/**
	 * Creates a new instance, which filters the given underlying
	 * input stream, maintaining a buffer with the default size
	 * (8192).
	 * @param pIn The underlying input stream.
	 */
	public PeekableInputStream(InputStream pIn) {
		super(pIn);
	}

	/**
	 * Checks, whether the given bytes are currently at the beginning
	 * of the buffer.
	 * @param pBuffer A byte array, which is being compared with the
	 *   beginning of the buffer.
	 * @return True, if the buffer currently starts with the given bytes.
	 *   Otherwise false. If necessary, the buffer will be filled.
	 * @throws IOException Filling the buffer failed.
	 */
	public boolean peek(byte[] pBuffer) throws IOException {
		Objects.requireNonNull(pBuffer, "Buffer");
		if (pBuffer.length > bufferSize) {
			throw new IllegalArgumentException("Peek request size of " + pBuffer.length
					                           + " bytes exceeds buffer size of " + bufferSize + " bytes");
		}
		if (buffer.getCurrentNumberOfBytes() < pBuffer.length) {
			fillBuffer();
		}
		return buffer.peek(pBuffer, 0, pBuffer.length);
	}
}
