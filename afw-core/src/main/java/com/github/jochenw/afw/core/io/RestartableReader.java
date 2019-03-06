package com.github.jochenw.afw.core.io;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import com.github.jochenw.afw.core.io.ObservableReader.Listener;
import com.github.jochenw.afw.core.util.Functions.FailableSupplier;
import com.github.jochenw.afw.core.util.Streams;


public class RestartableReader {
	private final FailableSupplier<Reader,IOException> supplier;
	private String text;

	public RestartableReader(FailableSupplier<Reader,IOException> pSupplier) {
		supplier = pSupplier;
	}
	public RestartableReader(Reader pReader) {
		supplier = () -> pReader;
	}

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
