package com.github.jochenw.afw.core.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.github.jochenw.afw.core.io.ObservableInputStream.Listener;
import com.github.jochenw.afw.core.util.Functions.FailableSupplier;
import com.github.jochenw.afw.core.util.Streams;


/** An {@link InputStream}, which can be read multiple times.
 */
public class RestartableInputStream {
	private final FailableSupplier<InputStream,IOException> supplier;
	private byte[] buffer;

	/** Creates a new instance. The {@link InputStream input stream's}
	 * contents are read from another {@link InputStream}, that
	 * is returned from the given {@link FailableSupplier}.
	 * @param pSupplier Provides an InputStream, which will be read
	 *   once to create a copy. The copy can be read, or reread
	 *   arbitrarily.
	 */
	public RestartableInputStream(FailableSupplier<InputStream,IOException> pSupplier) {
		supplier = pSupplier;
	}
	/** Creates a new instance. The {@link InputStream input stream's}
	 * contents are read from the given {@link InputStream}.
	 * @param pIn An InputStream, which will be read
	 *   once to create a copy. The copy can be read, or reread
	 *   arbitrarily.
	 */
	public RestartableInputStream(InputStream pIn) {
		this(() -> pIn);
	}

	/**
	 * If this is invoked for the first time, then the underlying input stream
	 * is being read, and a copy is created.
	 * For this invocation, and any following, an {@link InputStream} is
	 *   returned, that allows to read the created copy.
	 * @return An {@link InputStream}, that return the contents of the created
	 *   copy.
	 * @throws IOException Creating the copy failed.
	 */
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
