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
package com.github.jochenw.afw.core.log.simple;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.jochenw.afw.core.log.AbstractLog;
import com.github.jochenw.afw.core.log.AbstractLogFactory;
import com.github.jochenw.afw.core.log.ILog.Level;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.FileUtils;
import com.github.jochenw.afw.core.util.Objects;

/** Very simple implementation of {@link ILogFactory}, which is not based on any
 * external logging framework.
 */
public class SimpleLogFactory extends AbstractLogFactory {
    private final Writer writer;
    private final PrintStream ps;
    private final String eol = System.getProperty("line.separator");
    private Level level = Level.DEBUG;
    private long startTime = System.currentTimeMillis();
    private Supplier<String> nowStringSupplier;

    /**
     * Creates a new instance, which uses the given {@link Writer}
     * for writing messages.
     * @param pWriter The {@link Writer}, to which log messages are
     *   actually written.
     */
    public SimpleLogFactory(Writer pWriter) {
        this(pWriter, null);
    }

    private SimpleLogFactory(Writer pWriter, PrintStream pStream) {
        writer = pWriter;
        ps = pStream;
    }

    /**
     * Creates a new instance, which uses the given {@link PrintStream}
     * for writing messages.
     * @param pStream The {@link PrintStream}, to which log messages are
     *   actually written.
     */
    public SimpleLogFactory(PrintStream pStream) {
        this(null, pStream);
    }

    /**
     * Creates a new instance, which uses {@link System#err}
     * for writing messages.
     */
    public SimpleLogFactory() {
        this(null, System.err);
    }
    
    @Override
    protected AbstractLog newLog(String pId) {
        return new SimpleLog(this, pId);
    }

    /** Returns the log level.
     * @return The log level.
     */
    public Level getLevel() {
        return level;
    }

    /** Sets the log level.
     * @param pLevel The log level.
     */
    public void setLevel(Level pLevel) {
        level = pLevel;
    }

    /**
     * Writes a log message with the given logger id, and the given log level.
     * @param pId Id of the logger, that has been called to log the message.
     * @param pLevel The log level.
     * @param pMessage The log message.
     */
    public void write(String pId, Level pLevel, String pMessage) {
        final String msg = getNowAsString() + " " + pLevel + " " + pId + " " + pMessage;
        if (writer == null) {
            ps.println(msg);
        } else {
            try {
                writer.write(msg);
                writer.write(eol);
            } catch (IOException e) {
                throw Exceptions.newUncheckedIOException(e);
            }
        }
    }

    /** Returns the supplier for a string with the current time.
     * May be null, in which case the number of milliseconds since
     * the log factories creation (startTime) is returned.
     * @return The supplier for a string with the current time,
     *   null for the default supplier.
     */
    public Supplier<String> getNowStringSupplier() {
    	return nowStringSupplier;
    }

    /** Sets the supplier for a string with the current time.
     * May be null, in which case the number of milliseconds since
     * the log factories creation (startTime) is returned.
     * @param pNowStringSupplier The supplier for a string with the current time,
     *   or null for the default supplier.
     */
    public void setNowStringSupplier(Supplier<String> pNowStringSupplier) {
    	nowStringSupplier = pNowStringSupplier;
    }

    /** Returns the current time, as a string. If available (the
     * {@link #getNowStringSupplier() now string supplier} is non-null),
     * then that supplier is invoked to obtain the result string.
     * Otherwise, a default value is computed by subtracting the
     * {@link System#currentTimeMillis() current time in milliseconds}
     * from the time, when this log factory was created.
     * @return The current time, as a string.
     */
	protected String getNowAsString() {
		if (nowStringSupplier == null) {
			return String.valueOf(System.currentTimeMillis()-startTime);
		}
		return nowStringSupplier.get();
	}

    /**
     * Writes a log message, and an exception, with the given logger id,
     * and the given log level.
     * @param pId Id of the logger, that has been called to log the message.
     * @param pLevel The log level.
     * @param pMessage The log message.
     * @param pTh The logged exception.
     */
    public void write(String pId, Level pLevel, String pMessage, Throwable pTh) {
        final String msg = getNowAsString() + " " + pLevel + " " + pId + " " + pMessage;
        if (writer == null) {
            ps.println(msg);
            pTh.printStackTrace(ps);
        } else {
            try {
                writer.write(msg);
                writer.write(eol);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

	@Override
	protected void init() {
		// Does nothing.
	}

	/**
	 * Creates a new instance, which is logging to the given log file,
	 * using the given log level.
	 * @param pLogFile The log file to use. May be null, or empty, in
	 *   which case {@link System#out} will be used for logging.
	 * @param pLogLevel The log level to use. May be null, or empty, in
	 *   which case {@link Level#INFO} will be used as the log level.
	 * @return The created instance of {@link SimpleLogFactory}.
	 */
	public static @NonNull ILogFactory of(@Nullable String pLogFile, @Nullable String pLogLevel) {
		final Path logPath;
		if (pLogFile == null  ||  pLogFile.length() == 0) {
			logPath = null;
		} else {
			logPath = Paths.get(pLogFile);
		}
		return of(logPath, pLogLevel);
	}

	/**
	 * Creates a new instance, which is logging to the given log file,
	 * using the given log level.
	 * @param pLogFile The log file to use. May be null, or empty, in
	 *   which case {@link System#out} will be used for logging.
	 * @param pLogLevel The log level to use. May be null, or empty, in
	 *   which case {@link Level#INFO} will be used as the log level.
	 * @return The created instance of {@link SimpleLogFactory}.
	 */
	public static @NonNull ILogFactory of(@Nullable Path pLogFile, @Nullable String pLogLevel) {
		Level level = null;
		if (pLogLevel != null  &&  pLogLevel.length() > 0) {
			try {
				level = Level.valueOf(pLogLevel.toUpperCase());
			} catch (IllegalArgumentException e) {
				level = null;
			}
		}
		return of(pLogFile, level);
	}

	/**
	 * Creates a new instance, which is logging to the given log file,
	 * using the given log level.
	 * @param pLogFile The log file to use. May be null, or empty, in
	 *   which case {@link System#out} will be used for logging.
	 * @param pLogLevel The log level to use. May be null, or empty, in
	 *   which case {@link Level#INFO} will be used as the log level.
	 * @return The created instance of {@link SimpleLogFactory}.
	 */
	public static @NonNull ILogFactory of(@Nullable Path pLogFile, @Nullable Level pLogLevel) {
		final SimpleLogFactory slf;
		if (pLogFile == null) {
			slf = new SimpleLogFactory(System.out);
		} else {
			try {
				FileUtils.createDirectoryFor(pLogFile);
				final OutputStream out = new FileOutputStream(pLogFile.toFile(), true);
				final BufferedOutputStream bos = new BufferedOutputStream(out);
				slf = new SimpleLogFactory(new PrintStream(bos));
			} catch (IOException e) {
				throw Exceptions.show(e);
			}
		}
		slf.setLevel(Objects.notNull(pLogLevel, Level.INFO));
		return slf;
	}

	/**
	 * Creates a new instance, which is logging to the given {@link PrintStream}, using the given log level.
	 * @param pOut The {@link PrintStream} to use. May be null, or empty, in
	 *   which case {@link System#out} will be used for logging.
	 * @param pLogLevel The log level to use. May be null, or empty, in
	 *   which case {@link Level#INFO} will be used as the log level.
	 * @return The created instance of {@link SimpleLogFactory}.
	 */
	public static @NonNull SimpleLogFactory of(@Nullable PrintStream pOut, @Nullable Level pLogLevel) {
		final SimpleLogFactory slf = new SimpleLogFactory(Objects.notNull(pOut, Objects.requireNonNull(System.out)));
		slf.setLevel(Objects.notNull(pLogLevel, Level.INFO));
		return slf;
	}

	/**
	 * Creates a new instance, which is logging to the given {@link PrintStream},
	 * using the log level {@link Level#INFO}.
	 * @param pOut The {@link PrintStream} to use. May be null, or empty, in
	 *   which case {@link System#out} will be used for logging.
	 * @return The created instance of {@link SimpleLogFactory}.
	 */
	public static @NonNull SimpleLogFactory of(@Nullable PrintStream pOut) {
		return of(pOut, Level.INFO);
	}

	/**
	 * Creates a new instance, which is logging to the given {@link System#out},
	 * using the log level {@link Level#INFO}.
	 * @return The created instance of {@link SimpleLogFactory}.
	 */
	public static @NonNull SimpleLogFactory ofSystemOut() {
		return ofSystemOut(null);
	}

	/**
	 * Creates a new instance, which is logging to the given {@link System#out},
	 * using the given log level.
	 * @param pLevel The created log factories log level. May be null, in
	 *   which case the default ({@link Level#INFO}) is used.
	 * @return The created instance of {@link SimpleLogFactory}.
	 */
	public static @NonNull SimpleLogFactory ofSystemOut(@Nullable Level pLevel) {
		return of(System.out, Objects.notNull(pLevel, Level.INFO));
	}
}
