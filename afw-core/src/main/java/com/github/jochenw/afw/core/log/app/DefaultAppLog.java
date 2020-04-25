/**
 * 
 */
package com.github.jochenw.afw.core.log.app;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnull;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Functions.FailableConsumer;

/** Default implementation of {@link IAppLog}, writing to an {@link OutputStream}.
 */
public class DefaultAppLog extends AbstractAppLog implements AutoCloseable {
	private final @Nonnull BufferedOutputStream out;
	private final @Nonnull BufferedWriter writer;
	private final @Nonnull String lineSeparator;

	/** Creates a new instance.
	 * @param pLevel The initial logging level. Can be changed later on using
	 *   {@link #setLevel(Level)}.
	 * @param pLineSeparator The line terminator to use.
	 * @param pCharset The character set for converting strings into bytes.
	 * @param pOut The output stream to write to.
	 */
	public DefaultAppLog(@Nonnull Level pLevel, @Nonnull Charset pCharset, @Nonnull String pLineSeparator,
			             @Nonnull OutputStream pOut) {
		super(pLevel);
		out = new BufferedOutputStream(pOut);
		lineSeparator = pLineSeparator;
		writer = new BufferedWriter(new OutputStreamWriter(out, pCharset)) {
			@Override
			public void newLine() throws IOException {
				writer.write(pLineSeparator);
				writer.flush();
			}
		};
	}

	/** Creates a new instance with the initial logging level
	 * {@link IAppLog.Level#INFO},
	 * the character set {@link StandardCharsets#UTF_8},
	 * and the line separator {@link System#lineSeparator()}.
	 * @param pOut The output stream to write to.
	 */
	public DefaultAppLog(OutputStream pOut) {
		this(Level.INFO, StandardCharsets.UTF_8, System.lineSeparator(), pOut);
	}

	protected void writeLine(String pMsg) throws IOException {
		writer.write(pMsg);
		writer.newLine();
	}

	@Override
	public void log(Level pLevel, String pMsg) {
		runReadLocked(() -> {
			if (isEnabled(pLevel)) {
				writeLine(pMsg);
			}
		});
	}

	@Override
	public void log(Level pLevel, String pMsg, FailableConsumer<OutputStream, IOException> pStreamConsumer) {
		runReadLocked(() -> {
			if (isEnabled(pLevel)) {
				writeLine(pMsg);
				pStreamConsumer.accept(out);
				writer.newLine();
			}
		});
	}

	@Override
	public void close() throws IOException {
		Throwable th = null;
		try {
			writer.close();
		} catch (Throwable t) {
			th = t;
		}
		try {
			out.close();
		} catch (Throwable t) {
			if (th == null) {
				th = t;
			}
		}
		if (th != null) {
			throw Exceptions.show(th, IOException.class);
		}
	}
}
