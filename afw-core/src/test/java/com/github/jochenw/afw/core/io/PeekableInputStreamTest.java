package com.github.jochenw.afw.core.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;




public class PeekableInputStreamTest {
	@Test
	public void test() throws Exception {
		// Create a random input stream.
		final byte[] input = new byte[8192*8];
		long seed = 1519674873762l;
		final Random rnd = new Random(seed);
		rnd.nextBytes(input);
		final ByteArrayInputStream bais = new ByteArrayInputStream(input);
		final CircularBufferInputStream pis = new CircularBufferInputStream(bais);
		read(pis, rnd, seed, input);
	}

	private void read(final CircularBufferInputStream pPis, final Random pRnd, long pSeed, final byte[] pInput) throws IOException {
		final byte[] buffer = new byte[16];
		int inputOffset = 0;
		while (inputOffset < pInput.length) {
			final int action = pRnd.nextInt(2);
			switch(action) {
			  case 0:  // Read a few bytes.
			  {
				final int num = Math.min(16, pInput.length-inputOffset);
				final int res = pPis.read(buffer, 0, num);
				for (int i = 0;  i < res;  i++) {
					Assert.assertEquals("Seed=" + pSeed + ", pos=" + inputOffset, pInput[inputOffset++], buffer[i]);
				}
				break;
			  }
			  case 1:  // Read a single byte.
			  {
				final int res = pPis.read();
				Assert.assertEquals("Seed=" + pSeed + ", pos=" + inputOffset, (int) pInput[inputOffset++], res);
				break;
			  }
			  case 2:  // Read a few bytes.
			  {
				final int max = Math.min(16, pInput.length-inputOffset);
				final int num = pRnd.nextInt(max);
				final int res = pPis.read(buffer, 0, num);
				for (int i = 0;  i < res;  i++) {
					Assert.assertEquals("Seed=" + pSeed + ", pos=" + inputOffset, pInput[inputOffset++], buffer[i]);
				}
				break;
			  }
			}
		}
	}
}


