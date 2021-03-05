package com.github.jochenw.afw.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;


/**
 * A filtering {@link InputStream}, which allows to observe the data, that is being read.
 */
public class ObservableReader extends Reader {
	/** This listener is being notified about events regarding the observed data stream.
	 */
	public interface Listener {
		/**
		 * The data stream has been read completely, and is about to return -1 (EOF).
		 * @throws IOException The listener failed.
		 */
		void endOfFile() throws IOException;
		/**
		 * The data stream is about to return the given character.
		 * @param pChar The character, that the data stream is about to return.
		 * @throws IOException The listener failed.
		 */
		void reading(int pChar) throws IOException;
		/**
		 * The data stream is about to return the given data bytes.
		 * @param pBuffer A character array, that contains the characters that are being returned by
		 *   the data stream.
		 * @param pOffset The offset of the first character in the array, that is being returned.
		 * @param pLength The number of characters in the array, that are being returned.
		 * @throws IOException The listener failed.
		 */
		void reading(char[] pBuffer, int pOffset, int pLength) throws IOException;
		/**
		 * The data stream is about to close.
		 * @throws IOException The listener failed.
		 */
		void closing() throws IOException;
	}

	private final Reader in;
	private final Listener listener;

	/**
	 * Creates a new instance, which reads from the given {@link Reader}, and
	 * notifies the given listener, while doing so.
	 * @param pIn The input stream, which is actually being read.
	 * @param pListener The listener, which is being notified about data events.
	 */
	public ObservableReader(Reader pIn, Listener pListener) {
		in = pIn;
		listener = pListener;
	}

	@Override
	public int read() throws IOException {
		final int res = in.read();
		if (res == -1) {
			listener.endOfFile();
		} else {
			listener.reading(res);
		}
		return res;
	}

	@Override
	public int read(char[] pBuffer) throws IOException {
		final int res = in.read(pBuffer);
		if (res == -1) {
			listener.endOfFile();
		} else {
			listener.reading(pBuffer, 0, res);
		}
		return res;
	}

	@Override
	public int read(char[] pBuffer, int pOffset, int pLen) throws IOException {
		final int res = in.read(pBuffer, pOffset, pLen);
		if (res == -1) {
			listener.endOfFile();
		} else {
			listener.reading(pBuffer, pOffset, res);
		}
		return res;
	}

	@Override
	public void close() throws IOException {
		listener.closing();
		in.close();
	}
}
