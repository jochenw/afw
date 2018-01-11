package com.github.jochenw.afw.core.io;

import java.io.IOException;
import java.io.Reader;

public class PeekableReader extends Reader {
	private final Reader parent;
	private final char[] buffer;
	private int offset, end;
	private boolean closed;

	public PeekableReader(Reader pParent, int pBufferSize) {
		parent = pParent;
		buffer = new char[pBufferSize];
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

	@Override
	public int read(char[] pBuffer) throws IOException {
		return read(pBuffer, 0, pBuffer.length);
	}

	@Override
	public int read(char[] pBuffer, int pOffset, int pLength) throws IOException {
		if (offset < end) {
			int off = pOffset;
			int len = pLength;
			int result = 0;
			while(len > 0  &&  offset < end) {
				pBuffer[off++] = buffer[offset++];
				++result;
				--len;
			}
			return result;
		} else {
			assertOpen();
			return parent.read(pBuffer, pOffset, pLength);
		}
	}
	
	protected void assertOpen() throws IOException {
		if (closed) {
			throw new IOException("Already closed, no more data available.");
		}
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
	public void pushback(int pChar) throws IOException {
		if (pChar == -1) {
			throw new IllegalArgumentException("Unable to convert -1 into a character.");
		}
		final char[] buff = new char[1];
		buff[0] = (char) pChar;
		pushback(buff);
	}

	/**
	 * Pushes the given characters back into the input stream.
	 * Further read operations will return these bytes.
	 * This may trigger an {@link IOException}, due to
	 * insufficient space in the internal buffer. To avoid
	 * this, you may use {@link #getAvailableBufferSpace()}.
	 */
	public void pushback(char[] pBuffer) throws IOException {
		pushback(pBuffer, 0, pBuffer.length);
	}

	/**
	 * Pushes the given characters back into the input stream.
	 * Further read operations will return these bytes.
	 * This may trigger an {@link IOException}, due to
	 * insufficient space in the internal buffer. To avoid
	 * this, you may use {@link #getAvailableBufferSpace()}.
	 */
	public void pushback(CharSequence pBuffer) throws IOException {
		pushback(pBuffer, 0, pBuffer.length());
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
	public void pushback(char[] pBuffer, int pOffset, int pLength) throws IOException {
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
	 * Pushes the given characters back into the input stream.
	 * Further read operations will return these bytes.
	 * This may trigger an {@link IOException}, due to
	 * insufficient space in the internal buffer. To avoid
	 * this, you may use {@link #getAvailableBufferSpace()}.
	 * @see #pushback(byte[])
	 * @see #pushback(int)
	 * @see #getAvailableBufferSpace()
	 */
	public void pushback(CharSequence pChars, int pOffset, int pLength) throws IOException {
		if (offset > 0) {
			moveToBegin();
		}
		if (pLength+end > buffer.length) {
			throw new IOException("Internal buffer length of " + buffer.length
					+ " characters exceeded.");
		}
		for (int i = 0;  i < pLength;  i++) {
			buffer[end++] = pChars.charAt(pOffset+i);
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
	 * Returns, whether the internal buffer begins with the given characters.
	 * If necessary, the internal buffer will be filled until it has
	 * the necessary number of bytes, and this may trigger an
	 * {@link IOException}.
	 */
	public boolean startsWith(CharSequence pChars) throws IOException {
		return startsWith(pChars, 0, pChars.length());
	}

	/**
	 * Returns, whether the internal buffer begins with the given characters.
	 * If necessary, the internal buffer will be filled until it has
	 * the necessary number of bytes, and this may trigger an
	 * {@link IOException}.
	 */
	public boolean startsWith(char[] pChars) throws IOException {
		return startsWith(pChars, 0, pChars.length);
	}

	/**
	 * Returns, whether the internal buffer begins with the given characters.
	 * If necessary, the internal buffer will be filled until it has
	 * the necessary number of bytes, and this may trigger an
	 * {@link IOException}.
	 */
	public boolean startsWith(CharSequence pChars, int pOffset, int pLength) throws IOException {
		if (pLength > buffer.length) {
			throw new IllegalArgumentException("Buffer length of "
					      + pLength + " exceeds length of internal buffer (" + buffer.length + ")");
		}
		if (offset > 0) {
			moveToBegin();
		}
		while (end < pLength) {
			final int res = parent.read(buffer, end, buffer.length-end);
			if (res == -1) {
				return false;
			} else {
				end += res;
			}
		}
		for (int i = 0;  i < pLength;  i++) {
			if (buffer[i] != pChars.charAt(pOffset+i)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns, whether the internal buffer begins with the given characters.
	 * If necessary, the internal buffer will be filled until it has
	 * the necessary number of bytes, and this may trigger an
	 * {@link IOException}.
	 */
	public boolean startsWith(char[] pChars, int pOffset, int pLength) throws IOException {
		if (pLength > buffer.length) {
			throw new IllegalArgumentException("Buffer length of "
					      + pLength + " exceeds length of internal buffer (" + buffer.length + ")");
		}
		if (offset > 0) {
			moveToBegin();
		}
		while (end < pLength) {
			final int res = parent.read(buffer, end, buffer.length-end);
			if (res == -1) {
				return false;
			} else {
				end += res;
			}
		}
		for (int i = 0;  i < pLength;  i++) {
			if (buffer[i] != pChars[pOffset+i]) {
				return false;
			}
		}
		return true;
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
}
