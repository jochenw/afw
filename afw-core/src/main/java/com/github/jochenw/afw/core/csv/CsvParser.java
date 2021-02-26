/*
 * Copyright 2018 Jochen Wiedmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jochenw.afw.core.csv;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple parser for CSV files.
 */
public class CsvParser {
	private final String lineSeparator;
	private final String quoteString;
	private final String columnSeparator;
	private final int lineSeparatorLength;
	private final int quoteStringLength;
	private final int columnSeparatorLength;

	/**
	 * Creates a new line with the given line separator, quote string, and column separator.
	 * @param pLineSeparator The line separator, typically "\n", or "\r\n".
	 * @param pQuoteString The quite string, which is being used as a prefix, and suffix for
	 *   CSV cell values. Typically "\"", or "'".
	 * @param pColumnSeparator The column separator, typically ",", or ";".
	 */
	public CsvParser(String pLineSeparator, String pQuoteString, String pColumnSeparator) {
		lineSeparator = pLineSeparator;
		quoteString = pQuoteString;
		columnSeparator = pColumnSeparator;
		lineSeparatorLength = lineSeparator.length();
		quoteStringLength = quoteString.length();
		columnSeparatorLength = columnSeparator.length();
	}


	/**
	 * Parses the given line
	 * @param pLine The line, which is being parsed.
	 * @param pLineNumber The line number, for use in error messages.
	 * @return The array of cell values, that have been found in the
	 *   given line.
	 */
	public String[] asArray(final String pLine, int pLineNumber) {
		final int length = pLine.length();
		int state = 0;
		int offset = 0;
		boolean sbUsed = false;
		final StringBuilder sb = new StringBuilder();
		final List<String> list = new ArrayList<>();
		for (;;) {
			switch (state) {
			case 0:
			    if (length == offset  ||  (length == offset + lineSeparatorLength  &&  pLine.indexOf(lineSeparator, offset) == offset)) {
			    	final String v = sbUsed ? sb.toString() : "";
			    	list.add(v);
			    	offset = pLine.length();
					return list.toArray(new String[list.size()]);
			    } else if (pLine.length() >= offset + quoteStringLength  &&  pLine.indexOf(quoteString, offset) == offset) {
					state = 1;
					offset += quoteStringLength;
				} else if (pLine.length() >= offset + columnSeparatorLength  &&  pLine.indexOf(columnSeparator, offset) == offset) {
					final String v = sbUsed ? sb.toString() : "";
					list.add(v);
					sb.setLength(0);
					sbUsed = false;
					offset += columnSeparatorLength;
				} else {
					sb.append(pLine.charAt(offset++));
					sbUsed = true;
				}
				break;
			case 1:
			    if (pLine.length() == offset  ||  (length == offset + lineSeparatorLength  &&  pLine.indexOf(lineSeparator, offset) == offset)) {
					throw new IllegalStateException("Unexpected end of column at offset " + offset + " in line " + pLineNumber);
				}
				if (pLine.length() >= offset + quoteStringLength  &&  pLine.indexOf(quoteString, offset) == offset) {
					state = 0;
					offset += quoteStringLength;
				} else {
					sb.append(pLine.charAt(offset++));
					sbUsed = true;
				}
				break;
			default:
				throw new IllegalStateException("Invalid state: " + state);
			}
		}
	}
}
