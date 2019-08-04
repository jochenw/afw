package com.github.jochenw.afw.core.io;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.github.jochenw.afw.core.util.MutableBoolean;

public class ObservableInputStreamTest {
	@Test
	public void test() throws Exception {
		final String expect = "abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz";
		final InputStream in = new ByteArrayInputStream(expect.getBytes("US-ASCII"));
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final MutableBoolean closed = new MutableBoolean();
		final MutableBoolean endOfFileSeen = new MutableBoolean();
		final ObservableInputStream.Listener listener = new ObservableInputStream.Listener() {
			@Override
			public void endOfFile() throws IOException {
				if (endOfFileSeen.getValue()) {
					throw new IllegalStateException("End of file is indicated twice.");
				}
				endOfFileSeen.setValue(true);
			}

			@Override
			public void reading(int pRes) throws IOException {
				baos.write(pRes);
			}

			@Override
			public void reading(byte[] pB, int pI, int pRes) throws IOException {
				baos.write(pB, pI, pRes);
			}

			@Override
			public void closing() throws IOException {
				if (closed.getValue()) {
					throw new IllegalStateException("End of file is indicated twice.");
				}
				closed.setValue(true);
			}
		};
		try (final ObservableInputStream ois = new ObservableInputStream(in, listener)) {
			final byte[] buffer = new byte[16];
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
		final String got = baos.toString("US-ASCII");
		assertEquals(expect, got);
	}

}
