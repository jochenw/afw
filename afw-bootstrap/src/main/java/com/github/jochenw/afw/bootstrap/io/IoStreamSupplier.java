package com.github.jochenw.afw.bootstrap.io;

import java.io.IOException;
import java.io.InputStream;

public interface IoStreamSupplier {
	public InputStream get() throws IOException;
}
