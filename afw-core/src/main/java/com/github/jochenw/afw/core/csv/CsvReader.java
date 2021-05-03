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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.github.jochenw.afw.core.io.ReadableCharacterStream;


/**
 * The CsvReader is a light-weight, thread safe object for parsing CSV files.
 * Internally, it depends on an instance of {@link CsvParser}, which is used
 * to do the actual parsing. In other words: Details of the CSV format are
 * left to the parser. To use the parser, you've got to implement a
 * {@link Consumer} for instances of {@link CsvRow}. The consumer will be
 * invoked for each row in the CSV file.
 */
public class CsvReader {
	/** Interface of a row in the CSV file.
	 */
	public interface CsvRow {
		/** Returns the CSV files header array, which has been parsed from
		 * the first line.
		 * @return The CSV files header array
		 */
		String[] getHeaderArray();
		/** Returns the current rows array of CSV cell values, which has been
		 * parsed from the current line.
		 * @return The CSV files header array
		 */
		String[] getRowAsArray();
		/** Returns the current row as a map of key/value pairs. The map keys
		 * are the header names, as returned by {@link #getHeaderArray()}, the
		 * map values are the cell values, as returned by {@link #getRowAsArray()}.
		 * @return The current row as a map of key/value pairs.
		 */
		Map<String,String> getRowAsMap();
	}

	private final CsvParser parser;

	/** Creates a new instance with a default CSV parser: The systems line
	 * separator, a double quote as the quote string, and a semiolon as the
	 * column separator.
	 */
	public CsvReader() {
		this(new CsvParser(System.lineSeparator(), "\"", ";"));
	}

	/** Creates a new instance with the given CSV parser.
	 * @param pParser The {@link CsvParser}, which is being used
	 *   internally.
	 */
	public CsvReader(CsvParser pParser) {
		Objects.requireNonNull(pParser, "Parser");
		parser = pParser;
	}

	/**
	 * Parses a CSV file from the given {@link InputStream},
	 * reporting the rows by invoking the given {@code consumer}.
	 * @param pIn The input stream, which is being parsed.
	 * @param pConsumer The consumer, which is being notified.
	 */
	public void parse(InputStream pIn, Consumer<CsvRow> pConsumer) {
		try (ReadableCharacterStream rcs = ReadableCharacterStream.newInstance(pIn, false)) {
			parse(rcs, pConsumer);
		} catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}

	/**
	 * Parses a CSV file from the given {@link ReadableCharacterStream character stream},
	 * reporting the rows by invoking the given {@code consumer}.
	 * @param pRcs The character stream, which is being parsed.
	 * @param pConsumer The consumer, which is being notified.
	 */
	public void parse(ReadableCharacterStream pRcs, Consumer<CsvRow> pConsumer) {
		Throwable th = null;
		try {
			final String headerLine = pRcs.readLine();
			if (headerLine == null) {
				throw new CsvParseException("Expected header line, but no line was found.");
			}
			final String[] headers = parser.asArray(headerLine, 0);
			int lineNum = 0;
			for (;;) {
				final String line = pRcs.readLine();
				if (line == null) {
					break;
				}
				final String[] row = parser.asArray(line, ++lineNum);
				final int num = lineNum;
				final CsvRow csvRow = new CsvRow() {
					Map<String,String> map;

					@Override
					public String[] getHeaderArray() {
						return headers;
					}

					@Override
					public String[] getRowAsArray() {
						return row;
					}

					@Override
					public Map<String, String> getRowAsMap() {
						if (map == null) {
							if (headers.length < row.length) {
								throw new CsvParseException("Row " + num + " contains " + row.length
										+ " columns, but header row has only " + headers.length + " columns");
							}
							map = new HashMap<>();
							for (int i = 0;  i < headers.length;  i++) {
								map.put(headers[i], row[i]);
							}
						}
						return map;
					}
				};
				pConsumer.accept(csvRow);
			}
		} catch (Throwable t) {
			th = t;
		}
		if (pConsumer instanceof FinalizableConsumer) {
			FinalizableConsumer<CsvRow> fc = (FinalizableConsumer<CsvRow>) pConsumer;
			if (th == null) {
				fc.finished();
			} else {
				fc.error(th);
			}
		}
	}
}
