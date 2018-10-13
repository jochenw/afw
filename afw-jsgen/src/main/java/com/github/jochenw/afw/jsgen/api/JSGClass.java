package com.github.jochenw.afw.jsgen.api;

import java.util.List;

import javax.annotation.Nonnull;

public interface JSGClass extends IAnnotatable, IProtectable {
	JSGQName getType();
	@Nonnull List<Object> getContent();
	@Nonnull List<JSGQName> getExtendedClasses();
	@Nonnull List<JSGQName> getImplementedInterfaces();
	boolean isInterface();
}
