package com.github.jochenw.afw.jsgen.api;

import javax.annotation.Nonnull;


public interface JSGMethod extends JSGSubroutine {
	@Nonnull JSGQName getReturnType();
	@Nonnull String getName();
	boolean isAbstract();
	boolean isStatic();
	boolean isFinal();
	boolean isSynchronized();
	boolean isOverriding();
}
