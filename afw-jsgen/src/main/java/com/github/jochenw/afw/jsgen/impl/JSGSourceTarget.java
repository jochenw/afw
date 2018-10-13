package com.github.jochenw.afw.jsgen.impl;

import java.io.IOException;

import javax.annotation.Nonnull;

public interface JSGSourceTarget {
	void write(@Nonnull Object pObject) throws IOException;
	void newLine() throws IOException;
	void close() throws IOException;
}
