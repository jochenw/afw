package com.github.jochenw.afw.core.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Random;

import org.junit.Assume;
import org.junit.Test;

public class CircularBufferInputStreamTest {
	private final Random rnd = new Random(1530960934483l); // System.currentTimeMillis(), when this test was written.
	                                                       // Always using the same seed should ensure a reproducable test.

	@Test
	public void test() throws Exception {
		final byte[] inputBuffer = newInputBuffer();
		final byte[] bufferCopy = new byte[inputBuffer.length];
		final ByteArrayInputStream bais = new ByteArrayInputStream(inputBuffer);
		@SuppressWarnings("resource")
		final CircularBufferInputStream cbis = new CircularBufferInputStream(bais, 253);
		int offset = 0;
		final byte[] readBuffer = new byte[256];
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


	@Test
	public void testLargeFiles() throws Exception {
		final String slowTests = System.getenv("COMMONS_SLOW_TESTS");
		final boolean slowTestsEnabled = slowTests != null  &&  Boolean.parseBoolean(slowTests);
		Assume.assumeTrue("Slow tests not enabled", slowTestsEnabled);
		final Path p = Paths.get("target/unit-tests/LargeFiles/input.bin");
		final Runnable writer = new Runnable() {
			@Override
			public void run() {
				try {
					Files.deleteIfExists(p);
					final Path dir = p.getParent();
					Files.createDirectories(dir);
					final byte[] buffer = new byte[1024];
					for (int i = 0;  i < 1024;  i++) {
						buffer[i] = (byte) i;
					}
					try (OutputStream os = Files.newOutputStream(p)) {
						for (int i = 0;  i < 100000;  i++) {
							os.write(buffer);
						}
					}
				} catch (IOException e) {
					throw new UndeclaredThrowableException(e);
				}
			}
		};
		
		final Runnable bufferedInputStreamReader = new Runnable() {
			@Override
			public void run() {
				try (InputStream is = Files.newInputStream(p);
					 BufferedInputStream bis = new BufferedInputStream(is)) {
					read(bis);
				} catch (IOException e) {
					throw new UndeclaredThrowableException(e);
				}
			}
			
		};

		final Runnable circularBufferInputStreamReader = new Runnable() {
			@Override
			public void run() {
				try (InputStream is = Files.newInputStream(p);
					 CircularBufferInputStream cbis = new CircularBufferInputStream(is)) {
					read(cbis);
				} catch (IOException e) {
					throw new UndeclaredThrowableException(e);
				}
			}
		};

		run(writer, "Writer");
		run(bufferedInputStreamReader, "BufferedInputStream");
		run(circularBufferInputStreamReader, "CircularBufferInputStream");
	}

	private void run(Runnable pRunnable, String pName) {
		final long startTime = System.nanoTime();
		pRunnable.run();
		final long runTime = System.nanoTime()-startTime;
		System.out.println(String.format(Locale.US, "%30s: %d", pName, Long.valueOf(runTime))); 
	}
	private void read(InputStream pIn) throws IOException {
		final byte[] buffer = new byte[1024];
		for (;;) {
			final int res = pIn.read(buffer);
			if (res == -1) {
				break;
			}
		}
	}

	/**
	 * Create a large, but random input buffer.
	 */
	private byte[] newInputBuffer() {
		final byte[] buffer = new byte[16*512+rnd.nextInt(512)];
		rnd.nextBytes(buffer);
		return buffer;
	}
}