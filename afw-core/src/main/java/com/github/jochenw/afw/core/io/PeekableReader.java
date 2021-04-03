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

/**
 * A filtering {@link Reader} with some buffering capabilities,
 * that allow to peek into the upcoming data.
 */
public class PeekableReader extends CircularBufferReader {
	/**
	 * Creates a new instance, which filters the given underlying
	 * reader, maintaining a buffer with the given size.
	 * @param pIn The underlying reader.
	 * @param pBufferSize The internal buffers size.
	 */
	public PeekableReader(Reader pIn, int pBufferSize) {
		super(pIn, pBufferSize);
	}

	/**
	 * Creates a new instance, which filters the given underlying
	 * reader, maintaining a buffer with the default size
	 * (8192).
	 * @param pIn The underlying reader.
	 */
	public PeekableReader(Reader pIn) {
		super(pIn);
	}

	/**
	 * Checks, whether the given characters are currently at the beginning
	 * of the buffer.
	 * @param pBuffer A character array, which is being compared with the
	 *   beginning of the buffer.
	 * @return True, if the buffer currently starts with the given characters.
	 *   Otherwise false. If necessary, the buffer will be filled.
	 * @throws IOException Filling the buffer failed.
	 */
	public boolean peek(char[] pBuffer) throws IOException {
		return peek(pBuffer, 0, pBuffer.length);
	}

	/**
	 * Checks, whether the given characters are currently at the beginning
	 * of the buffer.
	 * @param pBuffer A character array, which is being compared with the
	 *   beginning of the buffer.
	 * @param pOffset The offset of the first character in the array, that
	 *   is being compared.
	 * @param pLength The number of characters, that are being compared.
	 * @return True, if the buffer currently starts with the given characters.
	 *   Otherwise false. If necessary, the buffer will be filled.
	 * @throws IOException Filling the buffer failed.
	 */
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

	/**
	 * Checks, whether the given character sequence is currently at
	 * the beginning of the buffer.
	 * @param pSequence A string, which is being compared with the
	 *   beginning of the buffer.
	 * @return True, if the buffer currently starts with the given characters.
	 *   Otherwise false. If necessary, the buffer will be filled.
	 * @throws IOException Filling the buffer failed.
	 */
	public boolean peek(CharSequence pSequence) throws IOException {
		return peek(pSequence, 0, pSequence.length());
	}

	/**
	 * Checks, whether the given character sequence is currently at the beginning
	 * of the buffer.
	 * @param pSequence A character array, which is being compared with the
	 *   beginning of the buffer.
	 * @param pOffset The offset of the first character in the array, that
	 *   is being compared.
	 * @param pLength The number of characters, that are being compared.
	 * @return True, if the buffer currently starts with the given characters.
	 *   Otherwise false. If necessary, the buffer will be filled.
	 * @throws IOException Filling the buffer failed.
	 */
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
