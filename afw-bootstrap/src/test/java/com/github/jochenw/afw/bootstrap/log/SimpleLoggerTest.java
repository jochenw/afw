package com.github.jochenw.afw.bootstrap.log;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UncheckedIOException;

import org.junit.Test;

public class SimpleLoggerTest {
	@Test
	public void testSimpleLog() throws Exception {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final Logger log = new Logger(Logger.Level.INFO) {
			@Override
			protected OutputStream getOutputStream() {
				return baos;
			}

			@Override
			public void close() {
				try {
					baos.close();
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}
		};
		assertSame(Logger.Level.INFO, log.getLevel());
		assertFalse(log.isEnabled(Logger.Level.TRACE));
		assertFalse(log.isEnabled(Logger.Level.DEBUG));
		assertTrue(log.isEnabled(Logger.Level.INFO));
		assertTrue(log.isEnabled(Logger.Level.WARN));
		assertTrue(log.isEnabled(Logger.Level.ERROR));
		assertTrue(log.isEnabled(Logger.Level.FATAL));
		log.setLevel(Logger.Level.DEBUG);
		assertFalse(log.isEnabled(Logger.Level.TRACE));
		assertTrue(log.isEnabled(Logger.Level.DEBUG));
		assertTrue(log.isEnabled(Logger.Level.INFO));
		assertTrue(log.isEnabled(Logger.Level.WARN));
		assertTrue(log.isEnabled(Logger.Level.ERROR));
		assertTrue(log.isEnabled(Logger.Level.FATAL));
		log.debug("This is the first line.");
		log.info("This is the second line.");
		log.trace("This line isn't logged.");
		log.error("This is the third line.");
		log.close();
		final String output = baos.toString("UTF-8");
		try (StringReader sr = new StringReader(output);
			 BufferedReader br = new BufferedReader(sr)) {
			final String line0 = br.readLine();
			assertTrue(line0.contains("DEBUG: This is the first line."));
			final String line1 = br.readLine();
			assertTrue(line1.contains(" INFO: This is the second line."));
			final String line2 = br.readLine();
			assertTrue(line2.contains("ERROR: This is the third line."));
		}
	}

}
