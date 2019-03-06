package com.github.jochenw.afw.core.io;

import java.io.IOException;
import java.io.Reader;

public class ObservableReader extends Reader {
	public interface Listener {
		void endOfFile() throws IOException;
		void reading(int pChar) throws IOException;
		void reading(char[] pB, int pOff, int pLen) throws IOException;
		void closing() throws IOException;
		
	}

	private final Reader in;
	private final Listener listener;

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
		final int res = in.read();
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
