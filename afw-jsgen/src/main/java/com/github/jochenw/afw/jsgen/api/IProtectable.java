package com.github.jochenw.afw.jsgen.api;

import javax.annotation.Nonnull;

public interface IProtectable {
	enum Protection {
		PUBLIC, PROTECTED, PACKAGE, PRIVATE
	}

	@Nonnull Protection getProtection();
}
