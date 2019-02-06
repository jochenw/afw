/*
 * Copyright 2018 Jochen Wiedmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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


