package com.github.jochenw.afw.core.csv;

import java.util.ArrayList;
import java.util.List;

public class CsvParser {
	private final String lineSeparator;
	private final String quoteString;
	private final String columnSeparator;
	private final int lineSeparatorLength;
	private final int quoteStringLength;
	private final int columnSeparatorLength;

	public CsvParser(String pLineSeparator, String pQuoteString, String pColumnSeparator) {
		lineSeparator = pLineSeparator;
		quoteString = pQuoteString;
		columnSeparator = pColumnSeparator;
		lineSeparatorLength = lineSeparator.length();
		quoteStringLength = quoteString.length();
		columnSeparatorLength = columnSeparator.length();
	}

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
