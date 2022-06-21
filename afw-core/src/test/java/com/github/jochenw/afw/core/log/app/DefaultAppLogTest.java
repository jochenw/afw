/**
 * 
 */
package com.github.jochenw.afw.core.log.app;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.github.jochenw.afw.core.log.app.IAppLog.Level;
import com.github.jochenw.afw.core.util.Functions.FailableConsumer;

/**
 * @author jwi
 *
 */
public class DefaultAppLogTest {
	/**
	 * Test method for {@link DefaultAppLog#log(IAppLog.Level, java.lang.String)}.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testLogLevelString() throws Exception {
		{
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (DefaultAppLog dal = new DefaultAppLog(baos)) {
				dal.info("This is a log message.");
				dal.debug("This is another log message.");
				dal.error("This is the third log message.");
			}
			assertEquals("This is a log message." + System.lineSeparator() +
					"This is the third log message." + System.lineSeparator(),
					baos.toString("UTF-8"));
		}
		{
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (DefaultAppLog dal = new DefaultAppLog(baos)) {
				dal.setLevel(Level.DEBUG);
				dal.info("This is a log message.");
				dal.debug("This is another log message.");
				dal.error("This is the third log message.");
			}
			assertEquals("This is a log message." + System.lineSeparator() +
					"This is another log message." + System.lineSeparator() +
					"This is the third log message." + System.lineSeparator(),
					baos.toString("UTF-8"));
		}
		{
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (DefaultAppLog dal = new DefaultAppLog(Level.DEBUG, StandardCharsets.UTF_8, "\n", baos)) {
				dal.info("This is a log message.");
				dal.debug("This is another log message.");
				dal.error("This is the third log message.");
			}
			assertEquals("This is a log message.\n"
					+ "This is another log message.\n"
					+ "This is the third log message.\n",
					baos.toString("UTF-8"));
		}
		{
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (DefaultAppLog dal = new DefaultAppLog(Level.INFO, StandardCharsets.UTF_8, "\r\n", baos)) {
				dal.info("This is a log message.");
				dal.debug("This is another log message.");
				dal.error("This is the third log message.");
			}
			assertEquals("This is a log message.\r\n"
					+ "This is the third log message.\r\n",
					baos.toString("UTF-8"));
		}
	}

	/**
	 * Test method for {@link DefaultAppLog#log(IAppLog.Level, java.lang.String, FailableConsumer)}.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testLogLevelStringFailableConsumerOfOutputStreamIOException() throws Exception {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (DefaultAppLog dal = new DefaultAppLog(baos)) {
			dal.info(() -> "This is a log message.");
			dal.debug(() -> "This is another log message.");
			dal.error(() -> "This is the third log message.");
		}
		assertEquals("This is a log message." + System.lineSeparator() +
				     "This is the third log message." + System.lineSeparator(),
				     baos.toString("UTF-8"));
	}

	/**
	 * Test method for {@link DefaultAppLog#error(String, Throwable)}.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testLogLevelStringThrowable() throws Exception {
		final NullPointerException npe = new NullPointerException();
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		npe.printStackTrace(pw);
		pw.close();
		final String stackTrace = sw.toString();
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (DefaultAppLog dal = new DefaultAppLog(baos)) {
			dal.error("Exception occurred:", npe);
		}
		assertEquals("Exception occurred:" + System.lineSeparator() + stackTrace + System.lineSeparator(),
				     baos.toString("UTF-8"));
	}

	/**
	 * Test method for {@link DefaultAppLog#error(Throwable)}.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testLogLevelThrowable() throws Exception {
		final NullPointerException npe = new NullPointerException("NPE");
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		npe.printStackTrace(pw);
		pw.close();
		final String stackTrace = sw.toString();
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (DefaultAppLog dal = new DefaultAppLog(baos)) {
			dal.error(npe);
		}
		assertEquals(NullPointerException.class.getName() + ": NPE" + System.lineSeparator() + stackTrace + System.lineSeparator(),
				     baos.toString("UTF-8"));
	}

	/** Test method for {@link IAppLog#of(Level, Path)}, and
	 * {@link IAppLog#of(Level, String)}.
	 */
	@Test
	public void testCreate() {
		final SystemOutAppLog soal1 = (SystemOutAppLog) IAppLog.of(null, (Path) null);
		assertEquals(Level.INFO, soal1.getLevel());
		final SystemOutAppLog soal2 = (SystemOutAppLog) IAppLog.of(Level.TRACE, (Path) null);
		assertEquals(Level.TRACE, soal2.getLevel());
		final SystemOutAppLog soal3 = (SystemOutAppLog) IAppLog.of(Level.INFO, Paths.get("-"));
		assertEquals(Level.INFO, soal3.getLevel());
		final SystemOutAppLog sol4 = (SystemOutAppLog) IAppLog.of(null, (String) null);
		assertEquals(Level.INFO, sol4.getLevel());
		final SystemOutAppLog sol5 = (SystemOutAppLog) IAppLog.of(Level.TRACE, (String) null);
		assertEquals(Level.TRACE, sol5.getLevel());
		final SystemOutAppLog soal6 = (SystemOutAppLog) IAppLog.of(Level.INFO, "-");
		assertEquals(Level.INFO, soal6.getLevel());
		
		final DefaultAppLog dal1 = (DefaultAppLog) IAppLog.of(Level.DEBUG, Paths.get("foo.log"));
		assertEquals(Level.DEBUG, dal1.getLevel());
		final DefaultAppLog dal2 = (DefaultAppLog) IAppLog.of(Level.DEBUG, "foo.log");
		assertEquals(Level.DEBUG, dal2.getLevel());
				
		
	}
}
