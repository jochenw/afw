package com.github.jochenw.afw.core.io;

public interface IMatcher {
	public default boolean isMatchingAll() { return false; }
	public boolean matches(String pUri);
}