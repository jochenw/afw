package com.github.jochenw.afw.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class CircularBufferInputStream extends InputStream {
	protected final InputStream in;
	protected final CircularByteBuffer buffer;
	protected final int bufferSize;
	private boolean eofSeen;

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

	public CircularBufferInputStream(InputStream pIn) {
		this(pIn, 8192);
	}

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