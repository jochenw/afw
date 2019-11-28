package com.github.jochenw.afw.core.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public interface StreamWriter {
	public void write(OutputStream pOut, Object pStreamable) throws IOException;
	public void write(Writer pOut, Object pStreamable) throws IOException;
}
