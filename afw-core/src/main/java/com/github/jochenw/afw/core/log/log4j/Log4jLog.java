package com.github.jochenw.afw.core.log.log4j;

import org.apache.log4j.Logger;

import com.github.jochenw.afw.core.log.AbstractLog;


public class Log4jLog extends AbstractLog {
    private final Logger logger;

    public Log4jLog(Log4jLogFactory pFactory, String pId) {
        super(pFactory, pId);
        logger = Logger.getLogger(pId);
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isEnabledFor(org.apache.log4j.Level.WARN);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isEnabledFor(org.apache.log4j.Level.ERROR);
    }

    @Override
    public boolean isFatalEnabled() {
        return logger.isEnabledFor(org.apache.log4j.Level.FATAL);
    }

    @Override
    public boolean isEnabledFor(Level pLevel) {
        switch (pLevel) {
        case TRACE:
            return logger.isTraceEnabled();
        case DEBUG:
            return logger.isDebugEnabled();
        case INFO:
            return logger.isInfoEnabled();
        case WARN:
            return logger.isEnabledFor(org.apache.log4j.Level.WARN);
        case ERROR:
            return logger.isEnabledFor(org.apache.log4j.Level.ERROR);
        case FATAL:
            return logger.isEnabledFor(org.apache.log4j.Level.FATAL);
        default:
            throw new IllegalStateException("Invalid logging level: " + pLevel);
        }
    }

    @Override
    protected void log(Level pLevel, String pMessage) {
        switch (pLevel) {
        case TRACE:
            logger.trace(pMessage);
            break;
        case DEBUG:
            logger.debug(pMessage);
            break;
        case INFO:
            logger.info(pMessage);
            break;
        case WARN:
            logger.warn(pMessage);
            break;
        case ERROR:
            logger.error(pMessage);
            break;
        case FATAL:
            logger.fatal(pMessage);
            break;
        default:
            throw new IllegalStateException("Invalid logging level: " + pLevel);
        }
    }

    @Override
    protected void log(Level pLevel, String pMessage, Throwable pTh) {
        switch (pLevel) {
        case INFO:
            logger.info(pMessage, pTh);
            break;
        case WARN:
            logger.warn(pMessage, pTh);
            break;
        case ERROR:
            logger.error(pMessage, pTh);
            break;
        case FATAL:
            logger.fatal(pMessage, pTh);
            break;
        default:
            throw new IllegalStateException("Invalid logging level: " + pLevel);
        }
    }

}
