package com.github.jochenw.afw.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import com.github.jochenw.afw.core.io.ObservableReader.Listener;
import com.github.jochenw.afw.core.util.Functions.FailableSupplier;
import com.github.jochenw.afw.core.util.Streams;


/** A {@link Reader}, which can be read multiple times.
 */
public class RestartableReader {
	private final FailableSupplier<Reader,IOException> supplier;
	private String text;

	/** Creates a new instance. The {@link Reader reader's}
	 * contents are read from another {@link InputStream}, that
	 * is returned from the given {@link FailableSupplier}.
	 * @param pSupplier Provides a Reader, which will be read
	 *   once to create a copy. The copy can be read, or reread
	 *   arbitrarily.
	 */
	public RestartableReader(FailableSupplier<Reader,IOException> pSupplier) {
		supplier = pSupplier;
	}
	/** Creates a new instance. The {@link Reader reader's}
	 * contents are read from the given {@link Reader}.
	 * @param pReader A Reader, which will be read
	 *   once to create a copy. The copy can be read, or reread
	 *   arbitrarily.
	 */
	public RestartableReader(Reader pReader) {
		supplier = () -> pReader;
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
	public Reader open() throws IOException {
		if (text == null) {
			final StringWriter sw = new StringWriter();
			final Reader in = supplier.get();
			return new ObservableReader(in, new Listener() {
				private boolean endOfFileSeen;

				@Override
				public void endOfFile() throws IOException {
					endOfFileSeen = true;
				}

				@Override
				public void reading(int pChar) throws IOException {
					sw.append((char) pChar);
				}

				@Override
				public void reading(char[] pBuffer, int pOff, int pLen) throws IOException {
					sw.write(pBuffer, pOff, pLen);
				}

				@Override
				public void closing() throws IOException {
					if (!endOfFileSeen) {
						Streams.copy(in, sw);
						sw.close();
						text = sw.toString();
					}
				}
			});
		} else {
			return new StringReader(text);
		}
	}
}
