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
