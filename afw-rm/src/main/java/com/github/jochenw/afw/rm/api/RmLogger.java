package com.github.jochenw.afw.rm.api;

public interface RmLogger {
	void warning(String pMessage);
	void error(String pMessage);
	void error(String string, Throwable t);
	void debug(String string);
}
