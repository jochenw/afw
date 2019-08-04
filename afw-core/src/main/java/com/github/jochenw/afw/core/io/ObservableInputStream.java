package com.github.jochenw.afw.core.io;

import java.io.IOException;
import java.io.InputStream;

import com.github.jochenw.afw.core.util.Exceptions;

public class ObservableInputStream extends InputStream {
	public interface Listener {
		void endOfFile() throws IOException;
		void reading(int pRes) throws IOException;
		void reading(byte[] pB, int pI, int pRes) throws IOException;
		void closing() throws IOException;
		
	}

	private final InputStream in;
	private final Listener listener;

	public ObservableInputStream(InputStream pIn, Listener pListener) {
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
	public int read(byte[] pB) throws IOException {
		final int res = in.read(pB);
		if (res == -1) {
			listener.endOfFile();
		} else {
			listener.reading(pB, 0, res);
		}
		return res;
	}

	@Override
	public int read(byte[] pB, int pOff, int pLen) throws IOException {
		final int res = in.read(pB, pOff, pLen);
		if (res == -1) {
			listener.endOfFile();
		} else {
			listener.reading(pB, pOff, res);
		}
		return res;
	}

	@Override
	public void close() throws IOException {
		Throwable th = null;
		try {
			listener.closing();
		} catch (Throwable t) {
			th = t;
		}
		try {
			in.close();
		} catch (Throwable t) {
			if (th != null) {
				th = t;
			}
		}
		if (th != null) {
			throw Exceptions.show(th, IOException.class);
		}
	}
}
