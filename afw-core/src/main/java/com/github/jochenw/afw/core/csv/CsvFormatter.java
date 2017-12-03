package com.github.jochenw.afw.core.csv;

import java.io.IOException;

import com.github.jochenw.afw.core.io.WritableCharacterStream;


public class CsvFormatter implements AutoCloseable {
	private final String lineSeparator;
	private final String quoteString;
	private final String columnSeparator;

	private final WritableCharacterStream wcs;

	public CsvFormatter(WritableCharacterStream pWcs,
			            String pLineSeparator, String pQuoteString, String pColumnSeparator) {
		wcs = pWcs;
		lineSeparator = pLineSeparator;
		quoteString = pQuoteString;
		columnSeparator = pColumnSeparator;
	}

	public void write(String[] pRow, boolean pForceQuoteString) throws IOException {
		for (int i = 0;  i < pRow.length;  i++) {
			if (i > 0) {
				wcs.write(columnSeparator);
			}
			final String v = pRow[i];
			final boolean useQuoteString = pForceQuoteString  ||  isQuoteStringRequired(v);
			if (useQuoteString) {
				wcs.write(quoteString);
			}
			wcs.write(v);
			if (useQuoteString) {
				wcs.write(quoteString);
			}
		}
		wcs.write(lineSeparator);
	}

	private boolean isQuoteStringRequired(String pValue) {
		if (pValue.indexOf(quoteString) != -1) {
			throw new IllegalStateException("Quote string inside column value is not supported.");
		}
		if (pValue.indexOf(columnSeparator) != -1) {
			throw new IllegalStateException("Column separator inside column value is not supported.");
		}
		return false;
	}
	
	@Override
	public void close() throws Exception {
		wcs.close();
	}

}
