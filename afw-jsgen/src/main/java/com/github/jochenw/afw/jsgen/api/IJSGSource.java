package com.github.jochenw.afw.jsgen.api;

import java.util.List;

public interface IJSGSource extends IAnnotatable, IProtectable {
	JSGQName getName();
	boolean isStatic();
	boolean isAbstract();
	boolean isFinal();
	List<Object> getMembers();
	List<IJSGMethod> getMethods();
}
