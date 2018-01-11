package com.github.jochenw.afw.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class PeekableInputStream extends InputStream {
	private final byte[] buffer;
	private int offset, end;
	private final InputStream parent;
	private boolean closed;

	public PeekableInputStream(InputStream pParent, int pBufferSize) {
		parent = pParent;
		buffer = new byte[pBufferSize];
		offset = 0;
		end = 0;
	}

	@Override
	public int read() throws IOException {
		if (offset < end) {
			return buffer[offset++];
		} else {
			assertOpen();
			return parent.read();
		}
	}

	protected void assertOpen() throws IOException {
		if (closed) {
			throw new IOException("Already closed, no more data available.");
		}
	}


	@Override
	public int read(byte[] pBuffer) throws IOException {
		return read(pBuffer, 0, pBuffer.length);
	}

	@Override
	public int read(byte[] pBuffer, int pOffset, int pLen) throws IOException {
		if (offset < end) {
			int off = pOffset;
			int len = pLen;
			int result = 0;
			while(len > 0  &&  offset < end) {
				pBuffer[off++] = buffer[offset++];
				++result;
				--len;
			}
			return result;
		} else {
			assertOpen();
			return parent.read(pBuffer, pOffset, pLen);
		}
	}

	// Move the data in the buffer to the buffers beginning.
	protected void moveToBegin() {
		if (offset == 0) {
			// Check for offset > 0 should preceed any invocation of this method.
			throw new IllegalStateException("No need to invoke me.");
		}
		System.arraycopy(buffer, offset, buffer, 0, end-offset);
		end -= offset;
		offset = 0;
	}

	@Override
	public void close() throws IOException {
		parent.close();
		closed = true;
	}

	/**
	 * Pushes the given byte back into the input stream.
	 * Further read operations will return this byte.
	 * This may trigger an {@link IOException}, due to
	 * insufficient space in the internal buffer. To avoid
	 * this, you may use {@link #getAvailableBufferSpace()}.
	 */
	public void pushback(int pByte) throws IOException {
		if (pByte == -1) {
			throw new IllegalArgumentException("Unable to convert -1 into a byte.");
		}
		final byte[] buff = new byte[1];
		buff[0] = (byte) pByte;
		pushback(buff);
	}

	/**
	 * Pushes the given bytes back into the input stream.
	 * Further read operations will return these bytes.
	 * This may trigger an {@link IOException}, due to
	 * insufficient space in the internal buffer. To avoid
	 * this, you may use {@link #getAvailableBufferSpace()}.
	 */
	public void pushback(byte[] pBuffer) throws IOException {
		pushback(pBuffer, 0, pBuffer.length);
	}

	/**
	 * Pushes the given bytes back into the input stream.
	 * Further read operations will return these bytes.
	 * This may trigger an {@link IOException}, due to
	 * insufficient space in the internal buffer. To avoid
	 * this, you may use {@link #getAvailableBufferSpace()}.
	 * @see #pushback(byte[])
	 * @see #pushback(int)
	 * @see #getAvailableBufferSpace()
	 */
	public void pushback(byte[] pBuffer, int pOffset, int pLength) throws IOException {
		if (offset > 0) {
			moveToBegin();
		}
		if (pLength+end > buffer.length) {
			throw new IOException("Internal buffer length of " + buffer.length
					+ " characters exceeded.");
		}
		for (int i = 0;  i < pLength;  i++) {
			
		}
	}

	/**
	 * Returns the number of bytes, which are currently available in the internal buffer.
	 * This may be used to determine, whether a pushback operation will succeed.
	 */
	public int getAvailableBufferSpace() {
		return buffer.length-end;
	}

	/**
	 * Returns, whether the internal buffer begins with the given bytes.
	 * If necessary, the internal buffer will be filled until it has
	 * the necessary number of bytes, and this may trigger an
	 * {@link IOException}.
	 */
	public boolean startsWith(byte[] pBuffer) throws IOException {
		if (pBuffer.length > buffer.length) {
			throw new IllegalArgumentException("Buffer length of "
					      + pBuffer.length + " exceeds length of internal buffer (" + buffer.length + ")");
		}
		if (offset > 0) {
			moveToBegin();
		}
		while (end < pBuffer.length) {
			final int res = parent.read(buffer, end, buffer.length-end);
			if (res == -1) {
				return false;
			} else {
				end += res;
			}
		}
		for (int i = 0;  i < pBuffer.length;  i++) {
			if (buffer[i] != pBuffer[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns, whether the internal buffer begins with the given string.
	 * The string will first be converted into a byte array, using the
	 * given character set.
	 * If necessary, the internal buffer will be filled until it has
	 * the necessary number of bytes, and this may trigger an
	 * {@link IOException}.
	 */
	public boolean startsWith(String pValue, Charset pCharset) throws IOException {
		return startsWith(pValue.getBytes(pCharset));
	}
}
