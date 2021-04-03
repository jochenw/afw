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
package com.github.jochenw.afw.core.log.slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.github.jochenw.afw.core.log.AbstractLog;
import com.github.jochenw.afw.core.log.ILog;


/** Implementation of {@link ILog}, which is based on SLF4J.
 */
public class Slf4jLog extends AbstractLog {
    private static final Marker fatal = MarkerFactory.getMarker("FATAL");
    private final Logger logger;

    /**
     * Creates a new instance with the given factory, and the given logger id.
     * @param pFactory The logger factory, that creates this instance.
     * @param pId The logger id.
     */
    public Slf4jLog(Slf4jLogFactory pFactory, String pId) {
        super(pFactory, pId);
        logger = LoggerFactory.getLogger(pId);
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
        return logger.isErrorEnabled(fatal);
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
            return logger.isErrorEnabled(fatal);
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
            logger.error(fatal, pMessage);
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
            logger.error(fatal, pMessage, pTh);
            break;
        default:
            throw new IllegalStateException("Invalid logging level: " + pLevel);
        }
    }

}
