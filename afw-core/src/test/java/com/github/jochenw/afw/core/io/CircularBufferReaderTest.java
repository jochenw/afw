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

import java.io.StringReader;
import java.util.Random;

import org.junit.Test;

/** Test for the {@link CircularBufferInputStream}.
 */
public class CircularBufferReaderTest {
	private final Random rnd = new Random(1530960934483l); // System.currentTimeMillis(), when this test was written.
	                                                       // Always using the same seed should ensure a reproducable test.

	/** Standard test for small to medium-sized files.
	 * @throws Exception The test failed.
	 */
	@Test
	public void test() throws Exception {
		final char[] inputBuffer = newInputBuffer();
		final char[] bufferCopy = new char[inputBuffer.length];
		final StringReader bais = new StringReader(new String(inputBuffer));
		@SuppressWarnings("resource")
		final CircularBufferReader cbis = new CircularBufferReader(bais, 253);
		int offset = 0;
		final char[] readBuffer = new char[256];
		while (offset < bufferCopy.length) {
			switch (rnd.nextInt(2)) {
			case 0:
			{
				final int res = cbis.read();
				if (res == -1) {
					throw new IllegalStateException("Unexpected EOF at offset " + offset);
				}
				if (inputBuffer[offset] != res) {
					throw new IllegalStateException("Expected " + inputBuffer[offset] + " at offset " + offset + ", got " + res);
				}
				++offset;
				break;
			}
			case 1:
			{
				final int res = cbis.read(readBuffer, 0, rnd.nextInt(readBuffer.length+1));
				if (res == -1) {
					throw new IllegalStateException("Unexpected EOF at offset " + offset);
				} else if (res == 0) {
					throw new IllegalStateException("Unexpected zero-byte-result at offset " + offset);
				} else {
					for (int i = 0;  i < res;  i++) {
						if (inputBuffer[offset] != readBuffer[i]) {
							throw new IllegalStateException("Expected " + inputBuffer[offset] + " at offset " + offset + ", got " + readBuffer[i]);
						}
						++offset;
					}
				}
				break;
			}
			default:
				throw new IllegalStateException("Unexpected random choice value");
			}
		}
	}

	/**
	 * Create a large, but random input buffer.
	 * @return The created input buffer.
	 */
	private char[] newInputBuffer() {
		final char[] buffer = new char[16*512+rnd.nextInt(512)];
		for (int i = 0;  i < buffer.length;  i++) {
			buffer[i] = (char) rnd.nextInt();
		}
		return buffer;
	}
}
