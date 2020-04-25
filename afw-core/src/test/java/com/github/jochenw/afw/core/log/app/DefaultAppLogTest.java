/**
 * 
 */
package com.github.jochenw.afw.core.log.app;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Test;

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
}
