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

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.io.Writer;

import com.github.jochenw.afw.core.log.AbstractLog;
import com.github.jochenw.afw.core.log.AbstractLogFactory;
import com.github.jochenw.afw.core.log.ILog.Level;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.util.Exceptions;

/** Very simple implementation of {@link ILogFactory}, which is not based on any
 * external logging framework.
 */
public class SimpleLogFactory extends AbstractLogFactory {
    private final Writer writer;
    private final PrintStream ps;
    private final String eol = System.getProperty("line.separator");
    private Level level = Level.DEBUG;

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
        final String msg = System.currentTimeMillis() + " " + pLevel + " " + pId + " " + pMessage;
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

    /**
     * Writes a log message, and an exception, with the given logger id,
     * and the given log level.
     * @param pId Id of the logger, that has been called to log the message.
     * @param pLevel The log level.
     * @param pMessage The log message.
     * @param pTh The logged exception.
     */
    public void write(String pId, Level pLevel, String pMessage, Throwable pTh) {
        final String msg = System.currentTimeMillis() + " " + pLevel + " " + pId + " " + pMessage;
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
}
