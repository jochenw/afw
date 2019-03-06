package com.github.jochenw.afw.core.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.github.jochenw.afw.core.io.ObservableInputStream.Listener;
import com.github.jochenw.afw.core.util.Functions.FailableSupplier;
import com.github.jochenw.afw.core.util.Streams;

public class RestartableInputStream {
	private final FailableSupplier<InputStream,IOException> supplier;
	private byte[] buffer;

	public RestartableInputStream(FailableSupplier<InputStream,IOException> pSupplier) {
		supplier = pSupplier;
	}
	public RestartableInputStream(InputStream pIn) {
		this(() -> pIn);
	}

	public InputStream open() throws IOException {
		if (buffer == null) {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final InputStream istream = supplier.get();
			return new ObservableInputStream(istream, new Listener() {
				private boolean endOfFileSeen;

				@Override
				public void endOfFile() throws IOException {
					endOfFileSeen = true;
				}

				@Override
				public void reading(int pByte) throws IOException {
					baos.write(pByte);
				}

				@Override
				public void reading(byte[] pBytes, int pOff, int pLen) throws IOException {
					baos.write(pBytes, pOff, pLen);
				}

				@Override
				public void closing() throws IOException {
					if (!endOfFileSeen) {
						Streams.copy(istream, baos);
						baos.close();
						buffer = baos.toByteArray();
						endOfFileSeen = true;
					}
				}
			});
		} else {
			return new ByteArrayInputStream(buffer);
		}
	}
}
