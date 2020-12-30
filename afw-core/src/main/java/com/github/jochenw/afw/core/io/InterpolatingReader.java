package com.github.jochenw.afw.core.io;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.jochenw.afw.core.props.Interpolator;
import com.github.jochenw.afw.core.util.Streams;

public class InterpolatingReader extends Reader {
	private final Interpolator interpolator;
	private final String text;
	private final StringReader sr;

	public InterpolatingReader(Interpolator pInterpolator, Reader pParent) {
		super();
		interpolator = pInterpolator;
		final String t = Streams.read(pParent);
		text = interpolator.interpolate(t);
		sr = new StringReader(text);
	}

	@Override
	public int read(char[] pBuffer, int pOffset, int pLength) throws IOException {
		return sr.read(pBuffer, pOffset, pLength);
	}

	@Override
	public int read(char[] pBuffer) throws IOException {
		return sr.read(pBuffer);
	}

	@Override
	public int read() throws IOException {
		return sr.read();
	}

	@Override
	public void close() throws IOException {
		sr.close();
	}

}
