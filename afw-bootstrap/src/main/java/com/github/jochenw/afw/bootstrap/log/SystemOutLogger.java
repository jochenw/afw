package com.github.jochenw.afw.bootstrap.log;

import java.io.OutputStream;

public class SystemOutLogger extends Logger {
	public SystemOutLogger(Level pLogLevel) {
		super(pLogLevel);
	}

	@Override
	protected OutputStream getOutputStream() {
		return System.out;
	}

	@Override
	public void close() {
		// Does nothing. (We don't close System.out.)
	}
}
