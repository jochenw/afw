package com.github.jochenw.afw.jsgen.api;

public interface IJSGSource extends IAnnotatable, IProtectable {
	JSGQName getName();
	boolean isStatic();
	boolean isAbstract();
	boolean isFinal();
}
