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

public class PeekableReader extends CircularBufferReader {
	public PeekableReader(Reader pIn, int pBufferSize) {
		super(pIn, pBufferSize);
	}

	public PeekableReader(Reader pIn) {
		super(pIn);
	}

	public boolean peek(char[] pBuffer) throws IOException {
		return peek(pBuffer, 0, pBuffer.length);
	}

	public boolean peek(char[] pBuffer, int pOffset, int pLength) throws IOException {
		Objects.requireNonNull(pBuffer, "Buffer");
		if (pLength > bufferSize) {
			throw new IllegalArgumentException("Peek request size of " + pLength
					                           + " bytes exceeds buffer size of " + bufferSize + " bytes");
		}
		if (buffer.getCurrentNumberOfChars() < pBuffer.length) {
			fillBuffer();
		}
		return buffer.peek(pBuffer, pOffset, pLength);
	}

	public boolean peek(CharSequence pSequence) throws IOException {
		return peek(pSequence, 0, pSequence.length());
	}

	public boolean peek(CharSequence pSequence, int pOffset, int pLength) throws IOException {
		Objects.requireNonNull(pSequence, "Sequence");
		if (pLength > bufferSize) {
			throw new IllegalArgumentException("Peek request size of " + pLength
					                           + " bytes exceeds sequence size of " + bufferSize + " bytes");
		}
		if (buffer.getCurrentNumberOfChars() < pSequence.length()) {
			fillBuffer();
		}
		return buffer.peek(pSequence, pOffset, pLength);
	}

}
