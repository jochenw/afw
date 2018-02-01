package com.github.jochenw.afw.rm.api;

import java.io.BufferedReader;
import java.io.IOException;


public interface SqlReader {
	boolean hasNextLine(BufferedReader pReader) throws IOException;
	String nextLine(BufferedReader pReader) throws IOException;
}
