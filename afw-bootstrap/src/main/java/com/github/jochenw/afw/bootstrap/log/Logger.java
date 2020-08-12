package com.github.jochenw.afw.bootstrap.log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.UndeclaredThrowableException;

public abstract class Logger implements AutoCloseable {
	public enum Level {
		TRACE, DEBUG, INFO, WARN, ERROR, FATAL;
	}
	public interface LogWriter {
		public void write(OutputStream pOut) throws IOException;
	}
	private Level logLevel;
	private final long startTime;

	public boolean isEnabled(Level pLevel) {
		return logLevel.ordinal() <= pLevel.ordinal();
	}
	protected Logger(Level pLogLevel) {
		logLevel = pLogLevel;
		startTime = System.currentTimeMillis();
	}
	protected abstract OutputStream getOutputStream();
	public abstract void close();
	public void log(Level pLevel, LogWriter pLogWriter) {
		if (isEnabled(pLevel)) {
			final OutputStream out = getOutputStream();
			synchronized(out) {
				try {
					pLogWriter.write(out);
				} catch (IOException e) {
					throw new UndeclaredThrowableException(e);
				}
			}
		}
	}
	public void log(final Level pLevel, final String pMsg) {
		if (isEnabled(pLevel)) {
			final LogWriter logWriter = new LogWriter() {
				@Override
				public void write(OutputStream pOut) throws IOException {
					final StringBuilder sb = new StringBuilder();
					sb.append(System.currentTimeMillis()-startTime);
					sb.append(" ");
					sb.append(pLevel.name());
					sb.append(": ");
					sb.append(pMsg);
					sb.append("\n");
					final byte[] bytes = sb.toString().getBytes("UTF-8");
					pOut.write(bytes);
					pOut.flush();
				}
			};
			log(pLevel, logWriter);
		}
	}
	public void log(final Level pLevel, final Throwable pTh) {
		if (isEnabled(pLevel)) {
			final LogWriter logWriter = new LogWriter() {
				@Override
				public void write(OutputStream pOut) throws IOException {
					final StringBuilder sb = new StringBuilder();
					sb.append(System.currentTimeMillis()-startTime);
					sb.append(" ");
					sb.append(pLevel.name());
					sb.append(": ");
					sb.append(pTh.getClass().getName());
					if (pTh.getMessage() != null) {
						sb.append(" ");
						sb.append(pTh.getMessage());
					}
					sb.append("\n");
					final byte[] bytes = sb.toString().getBytes("UTF-8");
					pOut.write(bytes);
					final PrintStream ps = new PrintStream(pOut);
					pTh.printStackTrace(ps);
					ps.flush();
				}
			};
			log(pLevel, logWriter);
		}
	}
	public void trace(String pMsg) {
		log(Level.TRACE, pMsg);
	}
	public void debug(String pMsg) {
		log(Level.DEBUG, pMsg);
	}
	public void info(String pMsg) {
		log(Level.INFO, pMsg);
	}
	public void warn(String pMsg) {
		log(Level.WARN, pMsg);
	}
	public void error(String pMsg) {
		log(Level.ERROR, pMsg);
	}
	public void fatal(String pMsg) {
		log(Level.FATAL, pMsg);
	}
	public void warn(Throwable pTh) {
		log(Level.WARN, pTh);
	}
	public void error(Throwable pTh) {
		log(Level.ERROR, pTh);
	}
	public void fatal(Throwable pTh) {
		log(Level.FATAL, pTh);
	}
	public Object getLevel() {
		return logLevel;
	}
	public void setLevel(Level pLevel) {
		logLevel = pLevel;
	}
}
