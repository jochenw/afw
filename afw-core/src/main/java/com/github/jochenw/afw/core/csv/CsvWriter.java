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


public class CsvWriter implements AutoCloseable {
	private final String[] header;
	private final String lineSeparator;
	private BufferedOutputStream bos;
	private Writer w;
	private BufferedWriter bw;

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
