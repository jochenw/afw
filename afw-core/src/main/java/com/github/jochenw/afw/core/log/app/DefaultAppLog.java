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

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.util.Exceptions;

/** Default implementation of {@link IAppLog}, writing to an {@link OutputStream}.
 */
public class DefaultAppLog extends AbstractAppLog implements AutoCloseable {
	private final @NonNull BufferedOutputStream out;
	private final @NonNull BufferedWriter writer;
	private final @NonNull String lineSeparator;

	/** Creates a new instance.
	 * @param pLevel The initial logging level. Can be changed later on using
	 *   {@link #setLevel(Level)}.
	 * @param pLineSeparator The line terminator to use.
	 * @param pCharset The character set for converting strings into bytes.
	 * @param pOut The output stream to write to.
	 */
	public DefaultAppLog(@NonNull Level pLevel, @NonNull Charset pCharset, @NonNull String pLineSeparator,
			             @NonNull OutputStream pOut) {
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

	/** Writes the given message, followed by a line terminator.
	 * @param pMsg The message, that is being written to the log file.
	 * @throws IOException Writing to the log file has failed.
	 */
	protected void writeLine(String pMsg) throws IOException {
		writer.write(pMsg);
		writer.newLine();
	}

	@Override
	public void log(Level pLevel, String pMsg) {
		runWriteLocked(() -> {
			if (isEnabledLocked(pLevel)) {
				writeLine(pMsg);
			}
		});
	}

	@Override
	public void log(Level pLevel, String pMsg, FailableConsumer<OutputStream, IOException> pStreamConsumer) {
		runWriteLocked(() -> {
			if (isEnabledLocked(pLevel)) {
				writeLine(pMsg);
				pStreamConsumer.accept(out);
				writer.newLine();
			}
		});
	}

	@Override
	public void close() throws IOException {
		runWriteLocked(() -> {
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
		});
	}
}
