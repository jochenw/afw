package com.github.jochenw.afw.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class PeekableInputStream extends CircularBufferInputStream {
	public PeekableInputStream(InputStream pIn, int pBufferSize) {
		super(pIn, pBufferSize);
	}

	public PeekableInputStream(InputStream pIn) {
		super(pIn);
	}

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
