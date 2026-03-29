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

import java.io.IOException;

import com.github.jochenw.afw.core.io.WritableCharacterStream;


/** An entity, which can write CSV files, applying minimal formatting.
 */
public class CsvFormatter implements AutoCloseable {
	private final String lineSeparator;
	private final String quoteString;
	private final String columnSeparator;

	private final WritableCharacterStream wcs;

	/** Creates a new instance, which writes CSV data to the given character stream, using
	 * the given line separator, the given quote string, and the given column separator.
	 * @param pWcs The character stream, to which data is being written.
	 * @param pLineSeparator The line separator.
	 * @param pQuoteString The quote string
	 * @param pColumnSeparator The column separator.
	 */
	public CsvFormatter(WritableCharacterStream pWcs,
			            String pLineSeparator, String pQuoteString, String pColumnSeparator) {
		wcs = pWcs;
		lineSeparator = pLineSeparator;
		quoteString = pQuoteString;
		columnSeparator = pColumnSeparator;
	}

	/** Writes the given row to the target stream.
	 * @param pRow The data row to write.
	 * @param pForceQuoteString Whether to force the prsence of the quote string. By
	 *   default, the need of quote strings is determined automatically, and per cell.
	 * @throws IOException Writing the data failed, due to an I/O error.
	 */
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

	/** Returns, whether the use of quote strings is required for the given cell value.
	 * @param pValue The value, which is being checked.
	 * @return True, if writing a CSV cell with the given value requires the use of
	 *   quote strings. Otherwise false.
	 * @throws IllegalArgumentException Writing this cell value would require escaping,
	 *   which is not supported.
	 */
	private boolean isQuoteStringRequired(String pValue) {
		if (pValue.indexOf(quoteString) != -1) {
			throw new IllegalArgumentException("Quote string inside column value is not supported.");
		}
		if (pValue.indexOf(columnSeparator) != -1) {
			throw new IllegalArgumentException("Column separator inside column value is not supported.");
		}
		return false;
	}
	
	@Override
	public void close() throws Exception {
		wcs.close();
	}

}
