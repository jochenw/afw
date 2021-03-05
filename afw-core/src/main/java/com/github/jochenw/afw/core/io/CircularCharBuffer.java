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

import java.util.Objects;

/**
 * A character buffer, which doesn't need reallocation of character arrays,
 * because it reuses a single character array. This works particularly well,
 * if reading from the buffer takes place at the same time than writing to.
 * Such is the case, for example, when using the buffer within a filtering
 * reader, like the {@link CircularBufferReader}.
 */
public class CircularCharBuffer {
	private final char[] buffer;
	private int startOffset, endOffset, currentNumberOfChars;

	/**
	 * Creates a new instance with the given buffer size.
	 * @param pSize The circular buffers size.
	 */
	public CircularCharBuffer(int pSize) {
		buffer = new char[pSize];
		startOffset = 0;
		endOffset = 0;
		currentNumberOfChars = 0;
	}

	/**
	 * Creates a new instance with the default buffer size
	 * (8192).
	 */
	public CircularCharBuffer() {
		this(8192);
	}

	/**
	 * Removes the next character from the buffer, and returns it.
	 * @return The next character from the buffer, which has been removed.
	 * @throws IllegalStateException The buffer is empty. Use {@link #hasChars()},
	 * or {@link #getCurrentNumberOfChars()}, to prevent this exception.
	 */
	public char next() {
		if (currentNumberOfChars <= 0) {
			throw new IllegalStateException("No characters available.");
		}
		final char c = buffer[startOffset];
		--currentNumberOfChars;
		if (++startOffset == buffer.length) {
			startOffset = 0;
		}
		return c;
	}

	/**
	 * Adds the given character to the end of the buffer.
	 * @param pChar The character, which is being added to the buffer.
	 */
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

	/**
	 * Returns, whether the buffer begins with the given characters.
	 * @param pBuffer The character array, that contains the expected characters.
	 * @param pOffset The offset in the array, where the expected characters
	 *   begin.
	 * @param pLength The length of the expected characters.
	 * @return True, if the current buffer contents begin with the given
	 *   character array, otherwise false.
	 */
	public boolean peek(char[] pBuffer, int pOffset, int pLength) {
		Objects.requireNonNull(pBuffer, "Buffer");
		if (pOffset < 0  ||  (pOffset > 0  &&  pOffset >= pBuffer.length)) {
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

	/**
	 * Returns, whether the buffer begins with the given character.
	 * @param pSequence The character sequence, that contains the expected characters.
	 * @param pOffset The offset in the sequence, where the expected characters
	 *   begin.
	 * @param pLength The length of the expected characters.
	 * @return True, if the current buffer contents begin with the given
	 *   character sequence, otherwise false.
	 */
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

	/** Adds characters to the buffers current contents.
	 * @param pBuffer The character array, from which to read the added characters.
	 * @param pOffset The offset in the character array, where the added characters are beginning.
	 * @param pLength The number of characters, that are being added.
	 */
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

	/** Adds characters to the buffers current contents.
	 * @param pSequence The character sequence, from which to read the added characters.
	 * @param pOffset The offset in the character sequence, where the added characters are beginning.
	 * @param pLength The number of characters, that are being added.
	 */
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

	/**
	 * Returns, whether it is currently possible, to add one, or more characters.
	 * @return True, if it is currently possible, to add one, or more characters.
	 *   Otherwise false.
	 */
	public boolean hasSpace() {
		return currentNumberOfChars < buffer.length;
	}

	/**
	 * Returns, whether there is currently at least one character in the buffer.
	 * @return True, if there is currently at least one character in the buffer.
	 *   False, if the buffer is empty.
	 */
	public boolean hasChars() {
		return currentNumberOfChars > 0;
	}

	/**
	 * Returns the number of characters, that can currently be added to the buffer.
	 * @return The number of characters, that can currently be added to the buffer.
	 */
	public int getSpace() {
		return buffer.length - currentNumberOfChars;
	}

	/**
	 * Returns the number of characters, that are currently in the buffer.
	 * @return The number of characters, that are currently in the buffer.
	 */
	public int getCurrentNumberOfChars() {
		return currentNumberOfChars;
	}

	/**
	 * Clears the buffer, discarding all characters that are currently in the
	 * buffer.
	 */
	public void clear() {
		startOffset = 0;
		endOffset = 0;
		currentNumberOfChars = 0;
	}

	/**
	 * Returns the buffers current content, as a string.
	 * @return The buffers current content, as a string.
	 * @see #toCharArray()
	 */
	public String toString() {
		final char[] chars = toCharArray();
		return new String(chars);
	}

	/**
	 * Returns the buffers current content, as a character array.
	 * @return The buffers current content, as a character array.
	 * @see #toString()
	 */
	public char[] toCharArray() {
		final char[] chars = new char[getCurrentNumberOfChars()];
		int offset = 0;
		for (int i=0;  i < chars.length;  i++) {
			chars[i] = buffer[offset++];
			if (offset == buffer.length) {
				offset = 0;
			}
		}
		return chars;
	}
}
