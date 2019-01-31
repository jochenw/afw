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

import java.util.Objects;

public class CircularCharBuffer {
	private final char[] buffer;
	private int startOffset, endOffset, currentNumberOfChars;

	public CircularCharBuffer(int pSize) {
		buffer = new char[pSize];
		startOffset = 0;
		endOffset = 0;
		currentNumberOfChars = 0;
	}

	public CircularCharBuffer() {
		this(8192);
	}

	public char next() {
		if (currentNumberOfChars <= 0) {
			throw new IllegalStateException("No bytes available.");
		}
		final char c = buffer[startOffset];
		--currentNumberOfChars;
		if (++startOffset == buffer.length) {
			startOffset = 0;
		}
		return c;
	}

	public void add(char pChar) {
		if (currentNumberOfChars >= buffer.length) {
			throw new IllegalStateException("No space available");
		}
		buffer[endOffset] = pChar;
		++currentNumberOfChars;
		if (++endOffset == buffer.length) {
			endOffset = 0;
		}
	}

	public boolean peek(char[] pBuffer, int pOffset, int pLength) {
		Objects.requireNonNull(pBuffer, "Buffer");
		if (pOffset < 0  ||  pOffset >= pBuffer.length) {
			throw new IllegalArgumentException("Invalid offset: " + pOffset);
		}
		if (pLength < 0  ||  pLength > buffer.length) {
			throw new IllegalArgumentException("Invalid length: " + pLength);
		}
		int offset = startOffset;
		for (int i = 0;  i < pLength;  i++) {
			if (buffer[offset] != pBuffer[i+pOffset]) {
				return false;
			}
			if (++offset == buffer.length) {
				offset = 0;
			}
		}
		return true;
	}

	public boolean peek(CharSequence pSequence, int pOffset, int pLength) {
		Objects.requireNonNull(pSequence, "Sequence");
		if (pOffset < 0  ||  pOffset >= pSequence.length()) {
			throw new IllegalArgumentException("Invalid offset: " + pOffset);
		}
		if (pLength < 0  ||  pLength > buffer.length) {
			throw new IllegalArgumentException("Invalid length: " + pLength);
		}
		int offset = startOffset;
		for (int i = 0;  i < pLength;  i++) {
			if (buffer[offset] != pSequence.charAt(i+pOffset)) {
				return false;
			}
			if (++offset == buffer.length) {
				offset = 0;
			}
		}
		return true;
	}
	
	public void add(char[] pBuffer, int pOffset, int pLength) {
		Objects.requireNonNull(pBuffer, "Buffer");
		if (pOffset < 0  ||  pOffset >= pBuffer.length) {
			throw new IllegalArgumentException("Invalid offset: " + pOffset);
		}
		if (pLength < 0) {
			throw new IllegalArgumentException("Invalid length: " + pLength);
		}
		if (currentNumberOfChars+pLength > buffer.length) {
			throw new IllegalStateException("No space available");
		}
		for (int i = 0;  i < pLength;  i++) {
			buffer[endOffset] = pBuffer[pOffset+i];
			if (++endOffset == buffer.length) {
				endOffset = 0;
			}
		}
		currentNumberOfChars += pLength;
	}

	public void add(CharSequence pSequence, int pOffset, int pLength) {
		Objects.requireNonNull(pSequence, "Sequence");
		if (pOffset < 0  ||  pOffset >= pSequence.length()) {
			throw new IllegalArgumentException("Invalid offset: " + pOffset);
		}
		if (pLength < 0) {
			throw new IllegalArgumentException("Invalid length: " + pLength);
		}
		if (currentNumberOfChars+pLength > buffer.length) {
			throw new IllegalStateException("No space available");
		}
		for (int i = 0;  i < pLength;  i++) {
			buffer[endOffset] = pSequence.charAt(pOffset+i);
			if (++endOffset == buffer.length) {
				endOffset = 0;
			}
		}
		currentNumberOfChars += pLength;
	}

	public boolean hasSpace() {
		return currentNumberOfChars < buffer.length;
	}

	public boolean hasBytes() {
		return currentNumberOfChars > 0;
	}

	public int getSpace() {
		return buffer.length - currentNumberOfChars;
	}

	public int getCurrentNumberOfChars() {
		return currentNumberOfChars;
	}

	public void clear() {
		startOffset = 0;
		endOffset = 0;
		currentNumberOfChars = 0;
	}
}
