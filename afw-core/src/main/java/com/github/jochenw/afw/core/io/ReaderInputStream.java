package com.github.jochenw.afw.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;


/** The opposite of a {@link InputStreamReader}: An
 * {@link InputStream}, which converts the characters,
 * that are read from an underlying {@link Reader}
 * to a byte stream.
 * 
 * In terms of performance, this class is possibly not
 * optimal, because it enforces the use of multiple
 * (possibly large) buffers. It could be faster, to
 * copy the {@link Reader readers} contents into a
 * single {@link StringWriter}, and decode the
 * resulting string into a single byte array in one
 * go, finally reading that byte array. Your mileage
 * may vary.
 */
public class ReaderInputStream extends InputStream {
	private final @Nonnull Reader reader;
	private final @Nonnull Charset charset;
	private final @Nonnull List<ByteBuffer> buffers = new ArrayList<>();
	private boolean closedSeen, closedReported;
	private final char[] chars = new char[8192];

	/**
	 * Creates a new instance, which reads characters from
	 * the given {@code pReader}. The characters are converted
	 * to bytes using the given {@code pCharset}.
	 * @param pReader
	 * @param pCharset
	 */
	public ReaderInputStream(@Nonnull Reader pReader, @Nonnull Charset pCharset) {
		reader = Objects.requireNonNull(pReader, "Reader");
		charset = Objects.requireNonNull(pCharset, "Charset");
	}

	protected void assertNotClosed() throws IOException {
		if (closedReported) {
			throw new IOException("This stream is already closed.");
		}
	}

	protected int fillBuffer(int pLength) throws IOException {
		int num = 0;
		for (ByteBuffer bb : buffers) {
			num += bb.remaining();
		}
		while (!closedSeen  &&  num < pLength) {
			final int numChars = reader.read(chars);
			if (numChars == -1) {
				closedSeen = true;
				break;
			} else {
				final CharBuffer charBuffer = CharBuffer.wrap(chars, 0, numChars);
				final ByteBuffer byteBuffer = charset.encode(charBuffer);
				buffers.add(byteBuffer);
				num += byteBuffer.remaining();
			}
		}
		if (num >= pLength) {
			return pLength;
		}
		if (num == 0  &&  closedSeen) {
			return -1;
		} else {
			return num;
		}
	}

	@Override
	public int read() throws IOException {
		assertNotClosed();
		final int size = fillBuffer(1);
		if (size <= 0) {
			closedReported = true;
			return -1;
		} else {
			final ByteBuffer byteBuffer = buffers.get(0);
			final byte b = byteBuffer.get();
			if (byteBuffer.remaining() == 0) {
				buffers.remove(0);
			}
			return b;
		}
	}

	@Override
	public int read(byte[] pBuffer) throws IOException {
		return read(pBuffer, 0, pBuffer.length);
	}

	@Override
	public int read(byte[] pBuffer, int pOffset, int pLength) throws IOException {
		assertNotClosed();
		final int size = fillBuffer(pLength);
		if (size <= 0) {
			closedReported = true;
			return -1;
		} else {
			int numBytes = 0;
			int offset = pOffset;
			while (numBytes < pLength  &&  !buffers.isEmpty()) {
				final ByteBuffer byteBuffer = buffers.get(0);
				while (byteBuffer.remaining() > 0) {
					pBuffer[offset++] = byteBuffer.get();
					if (++numBytes == pLength) {
						break;
					}
				}
				if (byteBuffer.remaining() == 0) {
					buffers.remove(0);
				}
				if (buffers.isEmpty()) {
					break;
				}
			}
			return numBytes;
		}
	}

	@Override
	public void close() throws IOException {
		reader.close();
		closedSeen = true;
		closedReported = true;
	}

	@Override
	public int available() throws IOException {
		int num = 0;
		for (ByteBuffer bb : buffers) {
			num += bb.remaining();
		}
		return num;
	}

	
}
