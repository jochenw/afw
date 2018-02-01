package com.github.jochenw.afw.rm.impl;

import java.io.BufferedReader;
import java.io.IOException;

import com.github.jochenw.afw.rm.api.SqlReader;

public class DefaultSqlReader implements SqlReader {
	private String statement;
	@Override
	public boolean hasNextLine(BufferedReader pReader) throws IOException {
		if (statement == null) {
			final StringBuilder sb = new StringBuilder();
			for (;;) {
				final String line = pReader.readLine();
				if (line == null) {
					if (sb.length() > 0) {
						statement = sb.toString();
						return true;
					} else {
						return false;
					}
				}
				if (line.trim().startsWith("--")) {
					continue;
				} else {
					final int offset = line.indexOf(';');
					if (sb.length() > 0) {
						sb.append('\n');
					}
					if (offset == -1) {
						sb.append(line);
					} else {
						sb.append(line.subSequence(0, offset));
						statement = sb.toString();
						return true;
					}
				}
			}
		} else {
			return true;
		}
	}

	@Override
	public String nextLine(BufferedReader pReader) throws IOException {
		final String s = statement;
		statement = null;
		return s;
	}
}
