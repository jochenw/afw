package com.github.jochenw.afw.core.log.log4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.jochenw.afw.core.log.AbstractLog;


public class Log4j2Log extends AbstractLog {
    private final Logger logger;

    public Log4j2Log(Log4j2LogFactory pFactory, String pId) {
        super(pFactory, pId);
        logger = LogManager.getLogger(pId);
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
        return logger.isWarnEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public boolean isFatalEnabled() {
        return logger.isFatalEnabled();
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
            return logger.isWarnEnabled();
        case ERROR:
            return logger.isErrorEnabled();
        case FATAL:
            return logger.isFatalEnabled();
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
