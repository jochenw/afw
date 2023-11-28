package com.github.jochenw.afw.core.io;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.github.jochenw.afw.core.util.Streams;

/**
 * Test for the {@link ReaderInputStream}.
 */
public class ReaderInputStreamTest {
	/** Test case for reading a simple byte array into a byte buffer.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testSimpleReadIntoBuffer() throws Exception {
		final String s = "01234567890abcdefghijklmnopqrstu\u00E4\u00F6\u00FC";
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final ReaderInputStream readerInputStream;
		try (final ReaderInputStream ris = Streams.asInputStream(new StringReader(s), null)) {
			Streams.copy(ris, baos);
			readerInputStream = ris;
		}
		try {
			readerInputStream.close();
		} catch (IllegalStateException e) {
			assertEquals("This stream is already closed.", e.getMessage());
		}
		assertEquals(s, baos.toString(StandardCharsets.UTF_8.name()));
	}

	/** Test case for reading a simple byte array byte by byte.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testSimpleReadSingleBytes() throws Exception {
		final String s = "01234567890abcdefghijklmnopqrstu\u00E4\u00F6\u00FC";
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final ReaderInputStream readerInputStream;
		try (final ReaderInputStream ris = Streams.asInputStream(new StringReader(s), StandardCharsets.UTF_8)) {
			for (;;) {
				final int b = ris.read();
				if (b == -1) {
					break;
				} else {
					baos.write(b);
				}
			}
			readerInputStream = ris;
		}
		try {
			readerInputStream.close();
		} catch (IllegalStateException e) {
			assertEquals("This stream is already closed.", e.getMessage());
		}
		assertEquals(s, baos.toString(StandardCharsets.UTF_8.name()));
	}

}
