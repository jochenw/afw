package com.github.jochenw.afw.core.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;

import org.junit.Test;

import org.junit.Assert;

public class PeekableInputStreamTest {

	@Test
	public void test() throws Exception {
		final Random rnd = new Random(System.currentTimeMillis());
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// Create a suitable input stream.
		for (int i = 0;  i < 8192;  i++) {
			baos.write(rnd.nextInt(256));
		}
		final byte[] input = baos.toByteArray();
		final PeekableInputStream pis = new PeekableInputStream(new ByteArrayInputStream(input), 4096);
		int offset = 0;
		while(offset < input.length) {
			final int action = rnd.nextInt(3);
			switch (action) {
				case 0:  // Read operation.
				{
					final int maxBytes = rnd.nextInt(Math.min(8, input.length-offset-1));
					if (maxBytes < 0) {
						throw new IllegalStateException("MaxBytes is lower than 0");
					}
					final int numBytes = rnd.nextInt(maxBytes+1);
					final byte[] buffer = new byte[numBytes];
					final int numRead = pis.read(buffer);
					if (numRead > 0) {
						for (int i = 0;  i < numRead;  i++) {
							Assert.assertEquals(input[offset++], buffer[i]);
						}
					}
					break;
				}
				case 1:
					break;
				case 4:  // Peek action, returning true
				{
					final int maxBytes = rnd.nextInt(Math.min(8, input.length-offset));
					if (maxBytes == 0) {
						break;
					}
					final int numBytes = rnd.nextInt(maxBytes+1);
					if (numBytes == 0) {
						break;
					}
					final byte[] buffer = new byte[numBytes];
					for (int i = 0;  i < numBytes;  i++) {
						buffer[i] = input[offset+i];
					}
					Assert.assertTrue(pis.startsWith(buffer));
					break;
				}
				case 2:  // Peek action, returning false
				{
					final int maxBytes = rnd.nextInt(Math.min(8, input.length-offset));
					if (maxBytes == 0) {
						break;
					}
					final int numBytes = rnd.nextInt(maxBytes+1);
					if (numBytes == 0) {
						break;
					}
					final byte[] buffer = new byte[numBytes];
					for (int i = 0;  i < numBytes;  i++) {
						buffer[i] = input[offset+1+i];
					}
					final int b = buffer[numBytes-1];
					if (b == 255) {
						buffer[numBytes-1] = 0;
					} else {
						buffer[numBytes-1] = (byte) (b+1);
					}
					Assert.assertFalse(pis.startsWith(buffer));
					break;
				}
				default:
					throw new IllegalStateException("Invalid action: " + action);
			}
		}
	}

}
