package com.github.jochenw.afw.core.log.simple;

import com.github.jochenw.afw.core.log.AbstractLog;

public class SimpleLog extends AbstractLog {
    SimpleLog(SimpleLogFactory pFactory, String pId) {
        super(pFactory, pId);
    }

    protected SimpleLogFactory getFactory() {
        return (SimpleLogFactory) super.logFactory;
    }

    @Override
    public boolean isTraceEnabled() {
        return getFactory().getLevel().ordinal() <= Level.TRACE.ordinal();
    }

    @Override
    public boolean isDebugEnabled() {
        return getFactory().getLevel().ordinal() <= Level.DEBUG.ordinal();
    }

    @Override
    public boolean isInfoEnabled() {
        return getFactory().getLevel().ordinal() <= Level.INFO.ordinal();
    }

    @Override
    public boolean isWarnEnabled() {
        return getFactory().getLevel().ordinal() <= Level.WARN.ordinal();
    }

    @Override
    public boolean isErrorEnabled() {
        return getFactory().getLevel().ordinal() <= Level.ERROR.ordinal();
    }

    @Override
    public boolean isFatalEnabled() {
        return getFactory().getLevel().ordinal() <= Level.FATAL.ordinal();
    }

    @Override
    public boolean isEnabledFor(Level pLevel) {
        return getFactory().getLevel().ordinal() <= pLevel.ordinal();
    }

    @Override
    protected void log(Level pLevel, String pMessage) {
        getFactory().write(getId(), pLevel, pMessage);
    }

    @Override
    protected void log(Level pLevel, String pMessage, Throwable pTh) {
        getFactory().write(getId(), pLevel, pMessage, pTh);
    }

}
