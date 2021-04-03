package com.github.jochenw.afw.core.io;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import org.junit.Test;

public class WritableCharacterStreamTest {
	private static final Random RND = new Random(1568496373721l); // System.currentTimeMillis() when this test was written.
	private static final String CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String CHARS;
	static {
		final StringBuilder sb = new StringBuilder();
		while (sb.length() < 512) {
			sb.append(CHARACTERS);
		}
		CHARS = sb.toString();
	}

	@Test
	public void test() throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final Writer w = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
		final WritableCharacterStream wcs = WritableCharacterStream.of(w, true);
		int offset = 0;
		while (offset < CHARS.length()) {
			int mode = RND.nextInt(2);
			if (mode == 0) {
				final int num = RND.nextInt(CHARS.length()-offset)+1;
				wcs.write(CHARS, offset, num);
				offset += num;
			} else {
				final int num = RND.nextInt(CHARS.length()-offset)+1;
				if (num == 0) {
					continue;
				}
				for (int j = 0;  j < num;  j++) {
					wcs.write(CHARS.charAt(offset++));
				}
			}
		}
		wcs.close();
		final String got = new String(baos.toByteArray(), StandardCharsets.UTF_8);
		assertEquals(CHARS, got);
	}

}
