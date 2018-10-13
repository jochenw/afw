package com.github.jochenw.afw.jsgen.api;

public interface JSGField extends IAnnotatable, IProtectable {
	JSGQName getType();
	String getName();
	boolean isStatic();
}
