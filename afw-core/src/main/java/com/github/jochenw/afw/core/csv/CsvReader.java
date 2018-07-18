package com.github.jochenw.afw.core.csv;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import com.github.jochenw.afw.core.io.ReadableCharacterStream;
import com.github.jochenw.afw.core.util.FinalizableConsumer;


/**
 * The CsvReader is a light-weight, thread safe object for parsing CSV files.
 * Internally, it depends on an instance of {@link CsvParser}, which is used
 * to do the actual parsing. In other words: Details of the CSV format are
 * left to the parser. To use the parser, you've got to implement a
 * {@link Consumer} for instances of {@link CsvRow}. The consumer will be
 * invoked for each row in the CSV file.
 */
public class CsvReader {
	public interface CsvRow {
		String[] getHeaderArray();
		String[] getRowAsArray();
		Map<String,String> getRowAsMap();
	}
	private final CsvParser parser;
	
	public CsvReader() {
		this(new CsvParser(System.lineSeparator(), "\"", ";"));
	}

	public CsvReader(CsvParser pParser) {
		Objects.requireNonNull(pParser, "Parser");
		parser = pParser;
	}

	public void parse(InputStream pIn, Consumer<CsvRow> pConsumer) {
		if (pIn instanceof BufferedInputStream) {
			parse((BufferedInputStream) pIn, pConsumer);
		} else {
			try (ReadableCharacterStream rcs = ReadableCharacterStream.newInstance(pIn, false)) {
				parse(rcs, pConsumer);
			} catch (IOException ioe) {
				throw new UncheckedIOException(ioe);
			}
		}
	}

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
