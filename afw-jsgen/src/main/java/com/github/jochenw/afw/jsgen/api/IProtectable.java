package com.github.jochenw.afw.jsgen.api;

public interface IProtectable {
	public static enum Protection {
		PUBLIC,
		PROTECTED,
		PACKAGE_PROTECTED,
		PRIVATE
	}

	IProtectable.Protection getProtection();
}
