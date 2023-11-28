package com.github.jochenw.afw.core.io;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Test;

import com.github.jochenw.afw.core.util.MutableBoolean;

/** Test for the {@link ObservableReader}.
 */
public class ObservableReaderTest {
	/** Test case for reading a simple character array.
	 * @throws Exception The test failed.
	 */
	@Test
	public void test() throws Exception {
		final String expect = "abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz";
		final Reader in = new StringReader(expect);
		final StringWriter sw = new StringWriter();
		final MutableBoolean closed = new MutableBoolean();
		final MutableBoolean endOfFileSeen = new MutableBoolean();
		final ObservableReader.Listener listener = new ObservableReader.Listener() {
			@Override
			public void endOfFile() throws IOException {
				if (endOfFileSeen.getValue()) {
					throw new IllegalStateException("EoF already seen.");
				}
				endOfFileSeen.setValue(true);
			}

			@Override
			public void reading(int pChar) throws IOException {
				sw.write(pChar);
			}

			@Override
			public void reading(char[] pB, int pOff, int pLen) throws IOException {
				sw.write(pB, pOff, pLen);
			}

			@Override
			public void closing() throws IOException {
				if (closed.getValue()) {
					throw new IllegalStateException("EoF already seen.");
				}
				closed.setValue(true);
			}
		};
		try (final ObservableReader ois = new ObservableReader(in, listener)) {
			final char[] buffer = new char[16];
			final int res1 = ois.read(buffer);
			assertTrue(res1 > 0);
			final int res2 = ois.read(buffer, 0, 16);
			assertTrue(res2 > 0);
			for (;;) {
				final int b = ois.read();
				if (b == -1) {
					break;
				}
			}
		}
		assertTrue(endOfFileSeen.getValue());
		assertTrue(closed.getValue());
		final String got = sw.toString();
		assertEquals(expect, got);
	}

}
