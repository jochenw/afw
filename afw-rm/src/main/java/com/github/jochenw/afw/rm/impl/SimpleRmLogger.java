package com.github.jochenw.afw.rm.impl;

import com.github.jochenw.afw.rm.api.RmLogger;

public class SimpleRmLogger implements RmLogger {
	@Override
	public void warning(String pMessage) {
		System.err.println("WARNING: " + pMessage);
	}

	@Override
	public void debug(String pMessage) {
		System.err.println("DEBUG: " + pMessage);
	}

	@Override
	public void error(String pMessage) {
		System.err.println("ERROR: " + pMessage);
	}


	@Override
	public void error(String pMessage, Throwable pThrowable) {
		System.err.println("ERROR: " + pMessage);
		pThrowable.printStackTrace(System.err);
	}
}
