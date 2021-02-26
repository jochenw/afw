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

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Streams;


/** A creator for CSV files.
 */
public class CsvWriter implements AutoCloseable {
	private final String[] header;
	private final String lineSeparator;
	private BufferedOutputStream bos;
	private Writer w;
	private BufferedWriter bw;

	/**
	 * Creates a new instance, which writes to the given {@link OutputStream},
	 * using the given array of header names.
	 * @param pOut The output stream, to which the CSV file is being written.
	 * @param pHeader The array of header names, which is being written as the
	 *   first line.
	 */
	public CsvWriter(OutputStream pOut, String[] pHeader) {
		try {
			header = pHeader;
			lineSeparator = System.lineSeparator();
			final BufferedOutputStream myBos;
			if (pOut instanceof BufferedOutputStream) {
				bos = null;
				myBos = (BufferedOutputStream) pOut;
			} else {
				myBos = bos = new BufferedOutputStream(Streams.uncloseableStream(pOut));
			}
			w = new OutputStreamWriter(myBos, StandardCharsets.UTF_8);
			bw = new BufferedWriter(w);
			write(header);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	protected void write(String[] pRow) throws IOException {
		for (int i = 0;  i < pRow.length;  i++) {
			if (i > 0) {
				bw.write(";");
			}
			bw.write(pRow[i]);
		}
		bw.write(lineSeparator);
	}

	@Override
	public void close() throws IOException {
		Throwable th = null;
		if (bw != null) {
			BufferedWriter bw = this.bw;
			this.bw = null;
			try {
				bw.close();
			} catch (Throwable t) {
				th = t;
			}
		}
		if (w != null) {
			Writer w = this.w;
			this.w = null;
			try {
				w.close();
			} catch (Throwable t) {
				if (th == null) {  // In case of multiple exceptions, throw the first.
					th = t;
				}
			}
		}
		if (w != null) {
			Writer w = this.w;
			this.w = null;
			try {
				w.close();
			} catch (Throwable t) {
				if (th == null) {  // In case of multiple exceptions, throw the first.
					th = t;
				}
			}
		}
		if (bos != null) {
			OutputStream os = bos;
			bos = null;
			try {
				os.close();
			} catch (Throwable t) {
				if (th == null) {  // In case of multiple exceptions, throw the first.
					th = t;
				}
			}
		}
		if (th != null) {
			if (th instanceof IOException) {
				throw (IOException) th;
			}
			throw Exceptions.show(th);
		}
	}

	/**
	 * Writes a CSV row with the given values, using the map keys to calculate
	 * the column indices.
	 * @param pMap The CSV row, as a map of key/value pairs.
	 */
	public void write(Map<String,String> pMap) {
		final String[] row = new String[header.length];
		for (int i = 0;  i < header.length;  i++) {
			String v = pMap.get(header[i]);
			if (v == null) {
				v = "";
			}
			row[i] = v;
		}
		try {
			write(row);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}
}
