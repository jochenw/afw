package com.github.jochenw.afw.rm.impl;

import com.github.jochenw.afw.rm.api.RmLogger;

public class SimpleRmLogger implements RmLogger {
	@Override
	public void warning(String pMessage) {
		System.out.println("WARNING: " + pMessage);
	}

	@Override
	public void error(String pMessage) {
		System.out.println("ERROR: " + pMessage);
	}
}
