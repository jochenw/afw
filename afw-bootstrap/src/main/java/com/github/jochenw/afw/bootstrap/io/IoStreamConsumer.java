package com.github.jochenw.afw.bootstrap.io;

import java.io.IOException;
import java.io.InputStream;

public interface IoStreamConsumer {
	public void accept(InputStream pIn) throws IOException;
}
