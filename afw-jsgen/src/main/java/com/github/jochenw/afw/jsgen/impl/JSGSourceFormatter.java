package com.github.jochenw.afw.jsgen.impl;

import java.io.IOException;

import javax.annotation.Nonnull;

import com.github.jochenw.afw.jsgen.api.JSGSource;

public interface JSGSourceFormatter {
	public void write(@Nonnull JSGSource pSource, @Nonnull JSGSourceTarget pTarget) throws IOException;
}
