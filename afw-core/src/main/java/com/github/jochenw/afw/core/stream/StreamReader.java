package com.github.jochenw.afw.core.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public interface StreamReader {
	public <O> O read(InputStream pIn, Class<O> pType) throws IOException;
	public <O> O read(Reader pReader, Class<O> pType) throws IOException;
}
