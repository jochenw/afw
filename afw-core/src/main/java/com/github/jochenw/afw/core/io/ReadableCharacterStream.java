package com.github.jochenw.afw.core.io;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.github.jochenw.afw.core.util.Streams;

public class ReadableCharacterStream implements AutoCloseable {
	private Charset charSet;
	private BufferedReader br;

	public ReadableCharacterStream(BufferedReader pReader) {
		br = pReader;
		charSet = null;
	}

	public ReadableCharacterStream(Reader pReader) {
		this(new BufferedReader(pReader));
	}

	public ReadableCharacterStream(BufferedInputStream pIn, Charset pCharSet) {
		this(new InputStreamReader(pIn, pCharSet));
		charSet = pCharSet;
	}

	public ReadableCharacterStream(InputStream pIn, Charset pCharSet) {
		this(new BufferedInputStream(pIn), pCharSet);
		charSet = pCharSet;
	}

	@Override
	public void close() throws IOException {
		br.close();
	}

	public Charset getCharSet() {
		return charSet;
	}

	public int read(char[] pBuffer) throws IOException {
		return br.read(pBuffer);
	}

	public int read() throws IOException {
		return br.read();
	}

	public int read(char[] pBuffer, int pOff, int pLen) throws IOException {
		return br.read(pBuffer, pOff, pLen);
	}

	public String readLine() throws IOException {
		return br.readLine();
	}

	public static ReadableCharacterStream newInstance(InputStream pIn, boolean pMayCloseStream) {
		return newInstance(pIn, StandardCharsets.UTF_8, pMayCloseStream);
	}

	public static ReadableCharacterStream newInstance(InputStream pIn, Charset pCharSet, boolean pMayCloseStream) {
		final InputStream in;
		if (pMayCloseStream) {
			in = pIn;
		} else {
			in = Streams.uncloseableStream(pIn);
		}
		return new ReadableCharacterStream(in, pCharSet);
	}

	public static ReadableCharacterStream newInstance(BufferedInputStream pIn, boolean pMayCloseStream) {
		return newInstance(pIn, StandardCharsets.UTF_8, pMayCloseStream);
	}

	public static ReadableCharacterStream newInstance(BufferedInputStream pIn, Charset pCharSet, boolean pClose) {
		if (pClose) {
			return new ReadableCharacterStream(pIn, pCharSet);
		} else {
			return new ReadableCharacterStream(Streams.uncloseableStream(pIn), pCharSet);
		}
	}

	public static ReadableCharacterStream newInstance(Reader pReader, boolean pMayClose) {
		final Reader r;
		if (pMayClose) {
			r = pReader;
		} else {
			r = Streams.uncloseableReader(pReader);
		}
		return new ReadableCharacterStream(r);
	}

	public static ReadableCharacterStream newInstance(BufferedReader pReader, boolean pMayClose) {
		final Reader r;
		if (pMayClose) {
			r = pReader;
		} else {
			r = Streams.uncloseableReader(pReader);
		}
		return new ReadableCharacterStream(r);
	}
}
