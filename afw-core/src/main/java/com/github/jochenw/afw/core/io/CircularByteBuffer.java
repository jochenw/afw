package com.github.jochenw.afw.core.io;

import java.util.Objects;

public class CircularByteBuffer {
	private final byte[] buffer;
	private int startOffset, endOffset, currentNumberOfBytes;

	public CircularByteBuffer(int pSize) {
		buffer = new byte[pSize];
		startOffset = 0;
		endOffset = 0;
		currentNumberOfBytes = 0;
	}

	public CircularByteBuffer() {
		this(8192);
	}

	public byte next() {
		if (currentNumberOfBytes <= 0) {
			throw new IllegalStateException("No bytes available.");
		}
		final byte b = buffer[startOffset];
		--currentNumberOfBytes;
		if (++startOffset == buffer.length) {
			startOffset = 0;
		}
		return b;
	}

	public void add(byte pByte) {
		if (currentNumberOfBytes >= buffer.length) {
			throw new IllegalStateException("No space available");
		}
		buffer[endOffset] = pByte;
		++currentNumberOfBytes;
		if (++endOffset == buffer.length) {
			endOffset = 0;
		}
	}

	public boolean peek(byte[] pBuffer, int pOffset, int pLength) {
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
	
	public void add(byte[] pBuffer, int pOffset, int pLength) {
		Objects.requireNonNull(pBuffer, "Buffer");
		if (pOffset < 0  ||  pOffset >= pBuffer.length) {
			throw new IllegalArgumentException("Invalid offset: " + pOffset);
		}
		if (pLength < 0) {
			throw new IllegalArgumentException("Invalid length: " + pLength);
		}
		if (currentNumberOfBytes+pLength > buffer.length) {
			throw new IllegalStateException("No space available");
		}
		for (int i = 0;  i < pLength;  i++) {
			buffer[endOffset] = pBuffer[pOffset+i];
			if (++endOffset == buffer.length) {
				endOffset = 0;
			}
		}
		currentNumberOfBytes += pLength;
	}

	public boolean hasSpace() {
		return currentNumberOfBytes < buffer.length;
	}

	public boolean hasBytes() {
		return currentNumberOfBytes > 0;
	}

	public int getSpace() {
		return buffer.length - currentNumberOfBytes;
	}

	public int getCurrentNumberOfBytes() {
		return currentNumberOfBytes;
	}

	public void clear() {
		startOffset = 0;
		endOffset = 0;
		currentNumberOfBytes = 0;
	}
}
