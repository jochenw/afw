package com.github.jochenw.afw.bootstrap.log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;


public interface Logger {
	public static enum Level {
		trace, debug, info, warn, error, fatal;
	}
	public interface OsConsumer {
		public void accept(OutputStream pOs) throws IOException;
	}

	public Charset getCharset();

	public boolean isEnabled(Level pLevel);
	public void log(Level pLevel, OsConsumer pConsumer);

	public default void log(Level pLevel, String pMsg) {
		if (isEnabled(pLevel)) {
			final OsConsumer consumer = (out) -> {
				final byte[] bytes = (pMsg + System.lineSeparator()).getBytes(getCharset());
				out.write(bytes, 0, bytes.length);
			};
			log(pLevel, consumer);
		}
	}
	public default void log(Level pLevel, Throwable pTh) {
		if (isEnabled(pLevel)) {
			final OsConsumer consumer = (out) -> {
				final PrintStream ps = new PrintStream(out);
				pTh.printStackTrace(ps);
				ps.flush();
			};
			log(pLevel, consumer);
		}
	}

	public default void trace(String pMsg) {
		if (isEnabled(Level.trace)) {
			log(Level.trace, pMsg);
		}
	}
	public default void debug(String pMsg) {
		if (isEnabled(Level.debug)) {
			log(Level.debug, pMsg);
		}
	}
	public default void info(String pMsg) {
		if (isEnabled(Level.info)) {
			log(Level.info, pMsg);
		}
	}
	public default void warn(String pMsg) {
		if (isEnabled(Level.warn)) {
			log(Level.warn, pMsg);
		}
	}
	public default void warn(Throwable pTh) {
		if (isEnabled(Level.warn)) {
			log(Level.warn, pTh);
		}
	}
	public default void error(String pMsg) {
		if (isEnabled(Level.error)) {
			log(Level.error, pMsg);
		}
	}
	public default void error(Throwable pTh) {
		if (isEnabled(Level.error)) {
			log(Level.error, pTh);
		}
	}
	public default void fatal(String pMsg) {
		if (isEnabled(Level.fatal)) {
			log(Level.fatal, pMsg);
		}
	}
	public default void fatal(Throwable pTh) {
		if (isEnabled(Level.fatal)) {
			log(Level.fatal, pTh);
		}
	}
}
