package com.github.jochenw.afw.bootstrap.log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class AbstractLogger implements Logger {
	private final OutputStream out;
	private final Charset charset;
	private Level level = Level.info;
	private final long startTime = System.currentTimeMillis();

	protected AbstractLogger(OutputStream pOut, Charset pCharset) {
		out = pOut;
		charset = pCharset;
	}

	protected AbstractLogger(OutputStream pOut) {
		this(pOut, StandardCharsets.UTF_8);
	}

	/**
	 * @return the level
	 */
	public Level getLevel() {
		return level;
	}

	/**
	 * @param pLevel the level to set
	 */
	public void setLevel(Level pLevel) {
		level = pLevel;
	}

	@Override
	public Charset getCharset() {
		return charset;
	}

	@Override
	public boolean isEnabled(Level pLevel) {
		return level.ordinal() >= pLevel.ordinal();
	}

	@Override
	public void log(Level pLevel, OsConsumer pConsumer) {
		final OutputStream os = out;
		final StringBuilder sb = new StringBuilder();
		sb.append(System.currentTimeMillis()-startTime);
		sb.append(" ");
		sb.append(pLevel.name());
		sb.append(": ");
		final byte[] bytes = sb.toString().getBytes(getCharset());
		try {
			os.write(bytes, 0, bytes.length);
			pConsumer.accept(os);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
