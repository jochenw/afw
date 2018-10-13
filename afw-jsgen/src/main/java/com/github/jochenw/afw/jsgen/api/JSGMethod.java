package com.github.jochenw.afw.jsgen.api;

import java.util.List;

import javax.annotation.Nonnull;


public interface JSGMethod extends IAnnotatable, IProtectable, IBodyProvider {
	public interface Parameter extends IAnnotatable {
		@Nonnull String getName();
		@Nonnull JSGQName getType();
	}
	@Nonnull JSGQName getReturnType();
	@Nonnull IProtectable.Protection getProtection();
	@Nonnull String getName();
	@Nonnull List<Parameter> getParameters();
	@Nonnull List<JSGQName> getExceptions();
	boolean isAbstract();
	boolean isStatic();
	boolean isFinal();
	boolean isSynchronized();
	boolean isOverriding();
	boolean isSuppressing(String pValue);
}
