package com.github.jochenw.afw.core.log;

public class DefaultLog extends AbstractLog implements LogManager.Listener {
    private AbstractLog log;

    public DefaultLog(DefaultLogFactory pFactory, String pId) {
        super(pFactory, pId);
        
    }

    @Override
    public synchronized void logFactoryChanged(ILogFactory pFactory) {
        log = (AbstractLog) pFactory.getLog(getId());
    }
    
    DefaultLogFactory getFactory() {
        return (DefaultLogFactory) logFactory;
    }
    
    @Override
    public synchronized boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    @Override
    public synchronized boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public synchronized boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    @Override
    public synchronized boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    @Override
    public synchronized boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    @Override
    public synchronized boolean isFatalEnabled() {
        return log.isFatalEnabled();
    }

    @Override
    public synchronized boolean isEnabledFor(Level pLevel) {
        return log.isEnabledFor(pLevel);
    }

    @Override
    protected synchronized void log(Level pLevel, String pMessage) {
        log.log(pLevel, pMessage);
    }

    @Override
    protected synchronized void log(Level pLevel, String pMessage, Throwable pTh) {
        log.log(pLevel, pMessage, pTh);
    }

}
