package com.github.jochenw.afw.core.log.simple;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.io.Writer;

import com.github.jochenw.afw.core.log.AbstractLog;
import com.github.jochenw.afw.core.log.AbstractLogFactory;
import com.github.jochenw.afw.core.log.ILog.Level;
import com.github.jochenw.afw.core.util.Exceptions;

public class SimpleLogFactory extends AbstractLogFactory {
    private final Writer writer;
    private final PrintStream ps;
    private final String eol = System.getProperty("line.separator");
    private Level level = Level.DEBUG;

    public SimpleLogFactory(Writer pWriter) {
        this(pWriter, null);
    }

    private SimpleLogFactory(Writer pWriter, PrintStream pStream) {
        writer = pWriter;
        ps = pStream;
    }

    public SimpleLogFactory(PrintStream pStream) {
        this(null, pStream);
    }

    public SimpleLogFactory() {
        this(null, System.err);
    }
    
    @Override
    protected AbstractLog newLog(String pId) {
        return new SimpleLog(this, pId);
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level pLevel) {
        level = pLevel;
    }

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
